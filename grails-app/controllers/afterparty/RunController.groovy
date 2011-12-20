package afterparty

import grails.plugins.springsecurity.Secured

class RunController {

    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

    @Secured(['ROLE_USER'])
    def create = {
        def runInstance = new Run(name: 'Run name', description: 'Run description')
        Experiment.get(params.experimentId.toLong()).addToRuns(runInstance)
        runInstance.save()
        redirect(action: show, id: runInstance.id)
    }



    @Secured(['ROLE_USER'])
    def attachRawReads = {
        def f = request.getFile('myFile')

        def runId = params.id
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
            run.rawReadsFile = r
            run.save()
            job.status = BackgroundJobStatus.FINISHED

            job.save(flush: true)
        }

        redirect(controller: 'backgroundJob', action: 'list')

    }


    @Secured(['ROLE_USER'])
    def attachTrimmedReads = {
        def f = request.getFile('myFile')

        def runId = params.id
        BackgroundJob job = new BackgroundJob(
                name: 'adding trimmed reads file',
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
            ReadsFile r = new ReadsFile(name: "uploaded FASTQ ${f.originalFilename} for ${run.name}", data: d, status: ReadsFileStatus.TRIMMED)
            run.trimmedReadsFile = r
            run.save()
            job.status = BackgroundJobStatus.FINISHED

            job.save(flush: true)
        }

        redirect(controller: 'backgroundJob', action: 'list')

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


}
