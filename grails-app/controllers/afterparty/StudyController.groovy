package afterparty

class StudyController {

    def overviewService

    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

    def index = {
        redirect(action: "listPublished", params: params)
    }

    def overview = {
        def image = overviewService.getDatasetOverview(params.id)
        response.setHeader('Content-length', image.length.toString())
        response.contentType = 'image/svg+xml' // or the appropriate image content type
        response.outputStream << image
        response.outputStream.flush()

    }




    def listPublished = {
        [studyInstanceList: Study.findAllByPublished(true)]
    }

    def create = {
        def studyInstance = new Study()
        studyInstance.properties = params
        return [studyInstance: studyInstance]
    }

    def save = {
        def studyInstance = new Study(params)
        if (studyInstance.save(flush: true)) {
            flash.message = "${message(code: 'default.created.message', args: [message(code: 'study.label', default: 'Study'), studyInstance.id])}"
            redirect(action: "show", id: studyInstance.id)
        }
        else {
            render(view: "create", model: [studyInstance: studyInstance])
        }
    }

    def show = {
        def studyInstance = Study.get(params.id)

        if (!studyInstance) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'study.label', default: 'Study'), params.id])}"
            redirect(action: "list")
        }
        else {

            session.studyId = params.id
            [studyInstance: studyInstance]
        }
    }


    def delete = {
        def studyInstance = Study.get(params.id)
        if (studyInstance) {
            try {
                studyInstance.delete(flush: true)
                flash.message = "${message(code: 'default.deleted.message', args: [message(code: 'study.label', default: 'Study'), params.id])}"
                redirect(action: "list")
            }
            catch (org.springframework.dao.DataIntegrityViolationException e) {
                flash.message = "${message(code: 'default.not.deleted.message', args: [message(code: 'study.label', default: 'Study'), params.id])}"
                redirect(action: "show", id: params.id)
            }
        }
        else {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'study.label', default: 'Study'), params.id])}"
            redirect(action: "list")
        }
    }
}
