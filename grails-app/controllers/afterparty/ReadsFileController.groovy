package afterparty

class ReadsFileController {

    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

    def trimReadsService
    def miraService
    def overviewService


    def graph = {
        ReadsFile f = ReadsFile.get(params.id)
        def image = overviewService.getReadsFileOverview(f)
        response.setHeader('Content-length', image.length.toString())
        response.contentType = 'image/svg+xml' // or the appropriate image content type
        response.outputStream << image
        response.outputStream.flush()
    }

    def trim = {

        def id = params.id
        println "id is $id"

        BackgroundJob job = new BackgroundJob(
                name: "trimming FASTQ file ${ReadsFile.get(id).name}",
                progress: 'queued',
                status: BackgroundJobStatus.QUEUED,
                type: BackgroundJobType.TRIM,
                study: Run.get(id).experiment.sample.study
        )
        job.save(flush: true)


        runAsync {
            trimReadsService.trimReads(id, job.id)
        }

        redirect(controller: 'backgroundJob', action: list)


    }

    def runMira = {

        def id = params.id
        println "id is $id"

        BackgroundJob job = new BackgroundJob(
                name: "Running MIRA on ${ReadsFile.get(id).name}",
                progress: 'queued',
                status: BackgroundJobStatus.QUEUED,
                type: BackgroundJobType.ASSEMBLE,
                study: ReadsFile.get(id).run.experiment.sample.study
        )
        job.save(flush: true)

        runAsync {
            miraService.runMira([id], job.id)
        }

        redirect(controller: 'backgroundJob', action: list)

    }

    def download = {
        def read = ReadsFile.get(params.id)
        response.setHeader("Content-disposition", "attachment; filename=${read.name}");
        response.outputStream << read.data.fileData
    }

    def index = {
        redirect(action: "list", params: params)
    }

    def list = {
        params.max = Math.min(params.max ? params.int('max') : 10, 100)
        [readsFileInstanceList: ReadsFile.list(params), readsFileInstanceTotal: ReadsFile.count()]
    }

    def create = {
        def readsFileInstance = new ReadsFile()
        readsFileInstance.properties = params
        return [readsFileInstance: readsFileInstance]
    }

    def save = {
        def readsFileInstance = new ReadsFile(params)
        if (readsFileInstance.save(flush: true)) {
            flash.message = "${message(code: 'default.created.message', args: [message(code: 'readsFile.label', default: 'ReadsFile'), readsFileInstance.id])}"
            redirect(action: "show", id: readsFileInstance.id)
        }
        else {
            render(view: "create", model: [readsFileInstance: readsFileInstance])
        }
    }

    def show = {
        def readsFileInstance = ReadsFile.get(params.id)
        if (!readsFileInstance) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'readsFile.label', default: 'ReadsFile'), params.id])}"
            redirect(action: "list")
        }
        else {
            [readsFileInstance: readsFileInstance]
        }
    }

    def edit = {
        def readsFileInstance = ReadsFile.get(params.id)
        if (!readsFileInstance) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'readsFile.label', default: 'ReadsFile'), params.id])}"
            redirect(action: "list")
        }
        else {
            return [readsFileInstance: readsFileInstance]
        }
    }

    def update = {
        def readsFileInstance = ReadsFile.get(params.id)
        if (readsFileInstance) {
            if (params.version) {
                def version = params.version.toLong()
                if (readsFileInstance.version > version) {

                    readsFileInstance.errors.rejectValue("version", "default.optimistic.locking.failure", [message(code: 'readsFile.label', default: 'ReadsFile')] as Object[], "Another user has updated this ReadsFile while you were editing")
                    render(view: "edit", model: [readsFileInstance: readsFileInstance])
                    return
                }
            }
            readsFileInstance.properties = params
            if (!readsFileInstance.hasErrors() && readsFileInstance.save(flush: true)) {
                flash.message = "${message(code: 'default.updated.message', args: [message(code: 'readsFile.label', default: 'ReadsFile'), readsFileInstance.id])}"
                redirect(action: "show", id: readsFileInstance.id)
            }
            else {
                render(view: "edit", model: [readsFileInstance: readsFileInstance])
            }
        }
        else {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'readsFile.label', default: 'ReadsFile'), params.id])}"
            redirect(action: "list")
        }
    }

    def delete = {
        def readsFileInstance = ReadsFile.get(params.id)
        if (readsFileInstance) {
            try {
                readsFileInstance.delete(flush: true)
                flash.message = "${message(code: 'default.deleted.message', args: [message(code: 'readsFile.label', default: 'ReadsFile'), params.id])}"
                redirect(action: "list")
            }
            catch (org.springframework.dao.DataIntegrityViolationException e) {
                flash.message = "${message(code: 'default.not.deleted.message', args: [message(code: 'readsFile.label', default: 'ReadsFile'), params.id])}"
                redirect(action: "show", id: params.id)
            }
        }
        else {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'readsFile.label', default: 'ReadsFile'), params.id])}"
            redirect(action: "list")
        }
    }
}
