package afterparty

import grails.plugins.springsecurity.Secured

class ExperimentController {

    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

    def trimReadsService
    def springSecurityService


    @Secured(['ROLE_USER'])
    def trimAllReadFiles = {
        Experiment e = Experiment.get(params.id)
        for (Run run in e.runs) {
            println "trimming run $run.name"
            // only trim raw files - do not retrim files that have already been trimmed!
            println "\ttrimming reads file $run.rawReadsFile.name"
            def job = new BackgroundJob(
                    name: "trimming FASTQ file ${run.rawReadsFile.name}",
                    progress: 'queued',
                    status: BackgroundJobStatus.QUEUED,
                    type: BackgroundJobType.TRIM,
                    study: e.sample.study)
            job.save(flush: true)

            runAsync {
                trimReadsService.trimReads(run.id, job.id)
            }
        }
        redirect(controller: 'backgroundJob', action: list)

    }

    @Secured(['ROLE_USER'])
    def attachAdapterSequences = {
        def f = request.getFile('myFile')

        def experimentId = params.experimentId

        BackgroundJob job = new BackgroundJob(
                name: 'uploading adapters file',
                progress: 'running',
                study: Experiment.get(experimentId).sample.study,
                status: BackgroundJobStatus.QUEUED,
                type: BackgroundJobType.UPLOAD_ADAPTERS)
        job.save(flush: true)


        runAsync {
            Experiment e = Experiment.get(experimentId)

            println "attaching adapters file to experiment ${e.name}"
            AdaptersFile af = new AdaptersFile(name: "uploaded adapters file : ${f.originalFilename}", data: f)
            e.adapters = af
            e.save()
            job.progress = 'finished'
            job.status = BackgroundJobStatus.FINISHED
            job.save(flush: true)
        }

        redirect(controller: 'backgroundJob', action: list)

    }

    @Secured(['ROLE_USER'])
    def createRun = {
        def experimentInstance = Experiment.get(params.id)
        def newRun = new Run(name: 'run name')
        experimentInstance.addToRuns(newRun)
        experimentInstance.save()
        flash.success = "added a new run"
        redirect(action: show, id: experimentInstance.id)
    }



    def show = {
        def experimentInstance = Experiment.get(params.id)

        [experimentInstance: experimentInstance, isOwner: experimentInstance.isOwnedBy(springSecurityService.principal)]

    }


}
