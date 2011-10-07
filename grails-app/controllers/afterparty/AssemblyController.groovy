package afterparty

class AssemblyController {


    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

    def blastService
    def statisticsService
    def chartService

    def uploadBlastAnnotation = {
        def f = request.getFile('myFile')
        if (!f.empty) {
            def assemblyId = params.id

            BackgroundJob job = new BackgroundJob(
                    name: 'uploading BLAST annotation',
                    progress: 'running',
                    study: Assembly.get(assemblyId).study,
                    status: BackgroundJobStatus.QUEUED,
                    type: BackgroundJobType.UPLOAD_BLAST_ANNOTATION)
            job.save(flush: true)


            runAsync {
                job.status = BackgroundJobStatus.RUNNING
                job.save(flush: true)
                blastService.addBlastHitsFromInput(f.inputStream)
                println "back in controller, indexing"
                job.progress = 'indexing BLAST hits for search'
                job.save(flush: true)
                Contig.index(Assembly.get(assemblyId).contigs)
                println "done indexing"
                job.progress = 'finished'
                job.status = BackgroundJobStatus.FINISHED
                job.save(flush: true)
            }

            redirect(controller: 'backgroundJob', action: list)
        }
        else {
            flash.message = 'file cannot be empty'
            render(view: 'uploadForm')
        }

    }

    def download = {
        response.setHeader("Content-disposition", "attachment; filename=contigs.fasta");
        response.flushBuffer()

        def criteria = Assembly.createCriteria()
        def a = criteria.get({
            eq('id', params.id.toLong())
            fetchMode 'contigs', org.hibernate.FetchMode.JOIN
        })

        a.contigs.each {
            response.outputStream << ">${it.id}\n${it.sequence}\n"
        }
    }


    def runBlast = {
        def assemblyId = params.id
        println "id is $assemblyId"

        BackgroundJob job = new BackgroundJob(
                name: "Running BLAST on ${assemblyId}",
                progress: 'queued',
                status: BackgroundJobStatus.QUEUED,
                type: BackgroundJobType.BLAST,
                study: Assembly.get(assemblyId).study
        )
        job.save(flush: true)

        runAsync {
            blastService.runBlast(assemblyId, job.id)
        }

        redirect(controller: 'backgroundJob', action: list)

    }

    def scatterplotAjax = {

        def start = System.currentTimeMillis()
        println "getting graph in controller for ${params.assemblyId} ${params.x} vs ${params.y} "
        def image = chartService.getScatterplot(params.assemblyId.toLong(), params.x, params.y, params.cutoff.toInteger(), params.colour)
        println "generated chart in " + (System.currentTimeMillis() - start)

        response.setHeader('Content-length', image.length.toString())
        response.contentType = 'image/png' // or the appropriate image content type
        response.outputStream << image
        response.outputStream.flush()
    }

    def histogramAjax = {

        def image = chartService.getHistogram(params.assemblyId.toLong(), params.x, params.scale)

        response.setHeader('Content-length', image.length.toString())
        response.contentType = 'image/png' // or the appropriate image content type
        response.outputStream << image
        response.outputStream.flush()
    }

    def create = {
        def assemblyInstance = new Assembly()
        assemblyInstance.properties = params
        return [assemblyInstance: assemblyInstance]
    }

    def show = {
        def assemblyInstance = Assembly.get(params.id)
        if (!assemblyInstance) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'assembly.label', default: 'Assembly'), params.id])}"
            redirect(action: "list")
        }
        else {
            [
                    assemblyInstance: assemblyInstance,
            ]
        }
    }

    def delete = {
        def assemblyInstance = Assembly.get(params.id)
        if (assemblyInstance) {
            try {
                assemblyInstance.delete(flush: true)
                flash.message = "${message(code: 'default.deleted.message', args: [message(code: 'assembly.label', default: 'Assembly'), params.id])}"
                redirect(action: "list")
            }
            catch (org.springframework.dao.DataIntegrityViolationException e) {
                flash.message = "${message(code: 'default.not.deleted.message', args: [message(code: 'assembly.label', default: 'Assembly'), params.id])}"
                redirect(action: "show", id: params.id)
            }
        }
        else {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'assembly.label', default: 'Assembly'), params.id])}"
            redirect(action: "list")
        }
    }
}
