package afterparty

import grails.plugins.springsecurity.Secured

class AssemblyController {


    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

    def blastService
    def statisticsService
    def chartService
    def miraService
    def springSecurityService
    def dataSource

    @Secured(['ROLE_USER'])
    def uploadBlastAnnotation = {
        def f = request.getFile('myFile')

        def assemblyId = params.id

        BackgroundJob job = new BackgroundJob(
                name: 'uploading BLAST annotation',
                progress: 'running',
                study: Assembly.get(assemblyId).compoundSample.study,
                status: BackgroundJobStatus.QUEUED,
                type: BackgroundJobType.UPLOAD_BLAST_ANNOTATION)
        job.save(flush: true)

        runAsync {
            BackgroundJob job2 = BackgroundJob.get(job.id)
            job2.status = BackgroundJobStatus.RUNNING
            job2.save(flush: true)
            blastService.addBlastHitsFromInput(f.inputStream, job.id, assemblyId)
            println "back in controller, indexing"

            BackgroundJob.withNewSession {
                job2 = BackgroundJob.get(job.id)
                job2.progress = 'finished'
                job2.status = BackgroundJobStatus.FINISHED
                job2.save()
            }

        }

        redirect(controller: 'backgroundJob', action: 'list')
    }

    @Secured(['ROLE_USER'])
    def uploadContigs = {
        def f = request.getFile('myFile')


        println "uploading file of contigs called ${f.name}"
        def assemblyId = params.id.toInteger()



        BackgroundJob job = new BackgroundJob(
                name: 'uploading contigs',
                progress: 'queued',
                study: Assembly.get(assemblyId).compoundSample.study,
                status: BackgroundJobStatus.QUEUED,
                type: BackgroundJobType.UPLOAD_CONTIGS)
        job.save(flush: true)


        runAsync {
            BackgroundJob job2 = BackgroundJob.get(job.id)
            job2.status = BackgroundJobStatus.RUNNING
            job2.save(flush: true)

//            def sql = new Sql(dataSource)
            //            def contigSetsToDelete = []
            //
            //            // get list of contig sets to delete
            //            sql.eachRow("select distinct contig_set_contigs_id as cid from contig_set_contig where contig_id in (select id from contig where assembly_id = $assemblyId)") {
            //                contigSetsToDelete.add(it.cid)
            //            }
            //            println contigSetsToDelete
            //
            //            // first delete the contigset->contig mappings
            //            sql.execute("delete from contig_set_contig where contig_id in (select id from contig where assembly_id = $assemblyId)")
            //
            //            // now delete the contig sets themselves
            //            contigSetsToDelete.each {csid ->
            //                sql.execute("delete from contig_set where id=$csid")
            //            }
            //
            //            // now we can go ahead and delete the contigs
            job2.progress = "deleting old contigs"
            job2.save(flush: true)
            //
            //
            //            Contig.executeUpdate("delete Contig where assembly_id = $assemblyId")

            Assembly assembly = Assembly.get(assemblyId)
            assembly.defaultContigSet = null
            assembly.save(flush: true)

            assembly.contigs.each {
                it.delete()
            }

            def contigs = miraService.parseFasta(f.inputStream)
            println "got some contigs: ${contigs.size()}"

            def created = 0
            contigs.each { name, seq ->

                def contig = new Contig(name: name, sequence: seq)
                contig.quality = '0 ' * seq.length()
                contig.searchAssemblyId = assemblyId
                assembly.addToContigs(contig)
                created++
                if (created % 1000 == 0) {
                    job2.progress = "uploaded $created of ${contigs.size()}"
                    job2.save(flush: true)

                }


            }
            assembly.save(flush: true)
            job2.progress = "creating contig set"
            job2.save(flush: true)

            statisticsService.createContigSetForAssembly(assembly.id)



            job2.progress = 'finished'
            job2.status = BackgroundJobStatus.FINISHED
        }

        redirect(controller: 'backgroundJob', action: 'list')

    }


    def download = {

        response.setHeader("Content-disposition", "attachment; filename=contigs.fasta");
        response.flushBuffer()

        def criteria = Assembly.createCriteria()
        def a = criteria.get({
            eq('id', params.id.toLong())
            fetchMode 'contigs', org.hibernate.FetchMode.JOIN
        })

        println "got ${a.contigs.size()} contigs for download";

        a.contigs.each {
            response.outputStream << ">${it.id}\n${it.sequence}\n"
        }
    }

    @Secured(['ROLE_USER'])
    def runBlast = {
        def assemblyId = params.id
        println "id is $assemblyId"

        BackgroundJob job = new BackgroundJob(
                name: "Running BLAST on ${assemblyId}",
                progress: 'queued',
                status: BackgroundJobStatus.QUEUED,
                type: BackgroundJobType.BLAST,
                study: Assembly.get(assemblyId).compoundSample.study
        )
        job.save(flush: true)

        runAsync {
            blastService.runBlast(assemblyId, job.id)
        }

        redirect(controller: 'backgroundJob', action: 'list')

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

    @Secured(['ROLE_USER'])
    def create = {
        def assemblyInstance = new Assembly(name: 'Assembly name', description: 'Assembly description')
        Study.get(params.studyId.toLong()).addToAssemblies(assemblyInstance)
        assemblyInstance.save()
        redirect(action: show, id: assemblyInstance.id)

    }

    def show = {
//        def assemblyInstance = Assembly.get(params.id)
        def criteria = Assembly.createCriteria()
        def start = System.currentTimeMillis()

        def a = criteria.get({
            eq('id', params.id.toLong())
            fetchMode 'contigs', org.hibernate.FetchMode.JOIN
//            fetchMode 'contigs.reads', org.hibernate.FetchMode.JOIN
        })
        def contigs = Contig.findAllByAssembly(a, [max: 20])
        println "fetched : ${System.currentTimeMillis() - start}"
        println "sorted : ${System.currentTimeMillis() - start}"
        [assemblyInstance: a, contigs: contigs]
    }


}
