package afterparty

class BackgroundJobController {

    def overviewService

    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

    def index = {
        redirect(action: "list", params: params)
    }

    def overview = {
        def image = overviewService.getWorkflowOverview(params.id.toLong())
        response.setHeader('Content-length', image.length.toString())
        response.contentType = 'image/svg+xml' // or the appropriate image content type
        response.outputStream << image
        response.outputStream.flush()

    }

    def graph = {
        BackgroundJob job = BackgroundJob.get(params.id)
        def image = overviewService.getBackgroundJobGraph(job)
        response.setHeader('Content-length', image.length.toString())
        response.contentType = 'image/svg+xml' // or the appropriate image content type
        response.outputStream << image
        response.outputStream.flush()

    }

    def list = {
//  don't do anything here - just render the list view - all the work is done by the listAjax method
        [studyId : params.studyId]
    }

    def listAjax = {
        params.max = Math.min(params.max ? params.int('max') : 10, 100)


        render(
                template: 'jobsTemplate',
                model: [
                        runningJobList: BackgroundJob.findAllByStatus(afterparty.BackgroundJobStatus.RUNNING, [sort: 'id']),
                        completedJobList: BackgroundJob.findAllByStatus(afterparty.BackgroundJobStatus.FINISHED, [sort: 'id']),
                        queuedJobList: BackgroundJob.findAllByStatus(afterparty.BackgroundJobStatus.QUEUED, [sort: 'id'])
                ]
        )
    }



    def show = {
        def backgroundJobInstance = BackgroundJob.get(params.id)
        if (!backgroundJobInstance) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'backgroundJob.label', default: 'BackgroundJob'), params.id])}"
            redirect(action: "list")
        }
        else {
            [backgroundJobInstance: backgroundJobInstance]
        }
    }



}
