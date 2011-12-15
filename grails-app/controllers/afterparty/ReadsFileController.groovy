package afterparty
import grails.plugins.springsecurity.Secured

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

    @Secured(['ROLE_USER'])
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
    @Secured(['ROLE_USER'])
    def runMira = {

        def id = params.id
        def studyId = session.studyId
        println "id is $id"

        BackgroundJob job = new BackgroundJob(
                name: "Running MIRA on ${ReadsFile.get(id).name}",
                progress: 'queued',
                status: BackgroundJobStatus.QUEUED,
                type: BackgroundJobType.ASSEMBLE,
                study: Study.get(session.studyId)
        )
        job.save(flush: true)

        runAsync {
            miraService.runMira([id], job.id, studyId)
        }

        redirect(controller: 'backgroundJob', action: list)

    }

    def download = {
        def read = ReadsFile.get(params.id)
        response.setHeader("Content-disposition", "attachment; filename=${read.name}");
        response.outputStream << read.data.fileData
    }


}
