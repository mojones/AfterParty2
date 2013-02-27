package afterparty

import grails.plugins.springsecurity.Secured

class RunController {


    def miraService
    def springSecurityService
    def trimReadsService
    def grailsLinkGenerator
    def statisticsService

    def g = new org.codehaus.groovy.grails.plugins.web.taglib.ApplicationTagLib()
    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]



    @Secured(['ROLE_USER'])
    def attachReads = {

        // we must get all the data from the request before we enter the async block
        def f = request.getFile('myFile')
        def realData = f.getFileItem().getString()
        def realFilename = f.originalFilename

        def type = params.type

        def runId = params.id
        BackgroundJob job = new BackgroundJob(
                name: 'adding FASTQ file',
                status: BackgroundJobStatus.QUEUED,
                progress: 'queued',
                type: BackgroundJobType.UPLOAD_READS,
                user: AfterpartyUser.get(springSecurityService.principal.id),
                destinationUrl: g.createLink(controller: 'run', action: 'show', params: [id: runId])
        )
        job.save(flush: true)



        //runAsync({
            Run run = Run.get(runId)

            // we must get() these domain objects inside the runAsync block
            BackgroundJob realJob = BackgroundJob.get(job.id)

            println "attaching FASTQ file to run ${run}"
            realJob.status = BackgroundJobStatus.RUNNING
            realJob.progress = "attaching FASTQ file to run ${run}"
            realJob.save(flush: true)
            println "creating rfd"
            ReadsFileData d = new ReadsFileData(fileData: realData)
            d.save(flush:true)
            println "creating..."
            ReadsFile r = new ReadsFile(name: "uploaded FASTQ ${realFilename} for ${run.name}", data: d, status: ReadsFileStatus.RAW)
            println "saving...."
            r.save(flush: true, failOnError: true)

            println "done saving"
            println "before: raw is ${run.rawReadsFile},trimmed is ${run.trimmedReadsFile}"
            if (type == 'raw') {
                println "type is raw, attaching to raw"
                run.rawReadsFile = r
            }
            if (type == 'trimmed') {
                            println "type is trimmed, attaching to trimmed"

                run.trimmedReadsFile = r
            }
            run.save()
            println "after: raw is ${run.rawReadsFile},trimmed is ${run.trimmedReadsFile}"

            realJob.status = BackgroundJobStatus.FINISHED
            realJob.destinationUrl = grailsLinkGenerator.link(controller: 'run', action: 'show', id: run.id)
            realJob.save(flush: true)
            println "done everything"
        //})

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

        runAsync {
            miraService.runMira([run.trimmedReadsFile.id], job.id, run.experiment.sample.compoundSample.id)
        }
        redirect(controller: 'backgroundJob', action: 'list')

    }


    def show = {
        def runInstance = Run.get(params.id)
        def userId = springSecurityService.isLoggedIn() ? springSecurityService?.principal?.id : 'none'
        def rawDataStats
        if (runInstance.rawReadsFile){
            rawDataStats = statisticsService.getReadFileDataStats(runInstance.rawReadsFile.data.id)
        }
        def trimmedDataStats
        if (runInstance.trimmedReadsFile){
            trimmedDataStats = statisticsService.getReadFileDataStats(runInstance.trimmedReadsFile.data.id)
        }
        [
        runInstance: runInstance, 
        isOwner: runInstance.experiment.sample.compoundSample.study.user.id == userId,
        rawReadStats : rawDataStats,
        trimmedReadStats : trimmedDataStats
        ]

    }


    @Secured(['ROLE_USER'])
    def trim = {

        def id = params.id
        println "id is $id"

        BackgroundJob job = new BackgroundJob(
                name: "trimming FASTQ file ${Run.get(id).rawReadsFile.name}",
                progress: 'queued',
                status: BackgroundJobStatus.QUEUED,
                type: BackgroundJobType.TRIM,
                user: AfterpartyUser.get(springSecurityService.principal.id)

        )
        job.save(flush: true)


        trimReadsService.trimReads(id, job.id)

        redirect(controller: 'backgroundJob', action: 'list')


    }


}
