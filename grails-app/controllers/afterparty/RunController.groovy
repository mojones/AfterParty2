package afterparty

import grails.plugins.springsecurity.Secured

class RunController {

    def miraService
    def springSecurityService
    def executorService

    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

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
                study: Run.get(runId).experiment.sample.compoundSample.study
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
            r.run = run
            run.trimmedReadsFile = r
            run.save()
            job.status = BackgroundJobStatus.FINISHED

            job.save(flush: true)
        }

        redirect(controller: 'backgroundJob', action: 'list')

    }

    @Secured(['ROLE_USER'])
    def runMira = {

        def run = Run.get(params.id)

        def study = run.experiment.sample.compoundSample.study
        println "run is $run"

        BackgroundJob job = new BackgroundJob(
                name: "Running MIRA on ${run.name}",
                progress: 'queued',
                status: BackgroundJobStatus.QUEUED,
                type: BackgroundJobType.ASSEMBLE,
                user: AfterpartyUser.get(springSecurityService.principal.id)
        )
        job.save(flush: true)

        miraService.runMira([run.trimmedReadsFile.id], job.id, run.experiment.sample.compoundSample.id)

        redirect(controller: 'backgroundJob', action: 'list')

    }


    def show = {
        def runInstance = Run.get(params.id)
        [runInstance: runInstance]

    }


}
