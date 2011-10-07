package afterparty

class RunController {

    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

    def index = {
        redirect(action: "list", params: params)
    }

    def list = {
        params.max = Math.min(params.max ? params.int('max') : 10, 100)
        [runInstanceList: Run.list(params), runInstanceTotal: Run.count()]
    }

    def create = {
        def runInstance = new Run(name : 'Run name', description: 'Run description')
        Experiment.get(params.experimentId.toLong()).addToRuns(runInstance)
        runInstance.save()
        redirect(action: show, id : runInstance.id)
    }

    def save = {
        def runInstance = new Run(params)
        runInstance.experiment = Experiment.get(params.experimentId)
        if (runInstance.save(flush: true)) {
            flash.message = "${message(code: 'default.created.message', args: [message(code: 'run.label', default: 'Run'), runInstance.id])}"
            redirect(action: "show", id: runInstance.id)
        }
        else {
            render(view: "create", model: [runInstance: runInstance])
        }
    }

    def attachRawReads = {
        def f = request.getFile('myFile')
        if (!f.empty) {
            def runId = params.runId
            BackgroundJob job = new BackgroundJob(
                    name: 'adding FASTQ file',
                    status: BackgroundJobStatus.QUEUED,
                    progress: 'queued',
                    type: BackgroundJobType.UPLOAD_READS,
                    study: Run.get(runId).experiment.sample.study
            )
            job.save(flush: true)
            runAsync {

                Run run = Run.get(runId)


                println "attaching FASTQ file to run ${run}"
                job.status = BackgroundJobStatus.RUNNING
                job.progress = "attaching FASTQ file to run ${run}"
                job.save(flush: true)
                ReadsFileData d = new ReadsFileData(fileData: f)
                ReadsFile r = new ReadsFile(name: "uploaded FASTQ ${f.originalFilename} for ${run.name}", data: d, status: ReadsFileStatus.RAW)
                run.addToReadsFiles(r)
                run.save()
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

    def show = {
        def runInstance = Run.get(params.id)
        if (!runInstance) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'run.label', default: 'Run'), params.id])}"
            redirect(action: "list")
        }
        else {
            [runInstance: runInstance]
        }
    }

    def edit = {
        def runInstance = Run.get(params.id)
        if (!runInstance) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'run.label', default: 'Run'), params.id])}"
            redirect(action: "list")
        }
        else {
            return [runInstance: runInstance]
        }
    }

    def update = {
        def runInstance = Run.get(params.id)
        if (runInstance) {
            if (params.version) {
                def version = params.version.toLong()
                if (runInstance.version > version) {

                    runInstance.errors.rejectValue("version", "default.optimistic.locking.failure", [message(code: 'run.label', default: 'Run')] as Object[], "Another user has updated this Run while you were editing")
                    render(view: "edit", model: [runInstance: runInstance])
                    return
                }
            }
            runInstance.properties = params
            if (!runInstance.hasErrors() && runInstance.save(flush: true)) {
                flash.message = "${message(code: 'default.updated.message', args: [message(code: 'run.label', default: 'Run'), runInstance.id])}"
                redirect(action: "show", id: runInstance.id)
            }
            else {
                render(view: "edit", model: [runInstance: runInstance])
            }
        }
        else {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'run.label', default: 'Run'), params.id])}"
            redirect(action: "list")
        }
    }

    def delete = {
        def runInstance = Run.get(params.id)
        if (runInstance) {
            try {
                runInstance.delete(flush: true)
                flash.message = "${message(code: 'default.deleted.message', args: [message(code: 'run.label', default: 'Run'), params.id])}"
                redirect(action: "list")
            }
            catch (org.springframework.dao.DataIntegrityViolationException e) {
                flash.message = "${message(code: 'default.not.deleted.message', args: [message(code: 'run.label', default: 'Run'), params.id])}"
                redirect(action: "show", id: params.id)
            }
        }
        else {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'run.label', default: 'Run'), params.id])}"
            redirect(action: "list")
        }
    }
}
