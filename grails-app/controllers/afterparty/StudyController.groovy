package afterparty

import grails.plugins.springsecurity.Secured

class StudyController {

    def overviewService
    def springSecurityService

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

    @Secured(['ROLE_USER'])
    def create = {

        def studyInstance = new Study(name: 'Study name', description: 'Study description', published: false)
//        AfterpartyUser.findByUsername('martin').addToStudies(studyInstance)
        def user = AfterpartyUser.get(springSecurityService.principal.id)
        user.addToStudies(studyInstance)
        studyInstance.save()
        redirect(action: show, id: studyInstance.id)
    }

    @Secured(['ROLE_USER'])
    def makePublished = {
        def study = Study.get(params.id)
        def user = AfterpartyUser.get(springSecurityService.principal.id)
        if (study.user == user) {
            study.published = true
            flash.success = "published study ${study.name}"
        }
        redirect(action: 'listPublished')
    }


    def show = {
        def studyInstance = Study.get(params.id)

        if (!studyInstance) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'study.label', default: 'Study'), params.id])}"
            redirect(action: "list")
        }
        else {
            session.studyId = params.id
            def user = springSecurityService.isLoggedIn() ? AfterpartyUser.get(springSecurityService?.principal?.id) : 'none'

            [studyInstance : studyInstance, isOwner : studyInstance.user == user]
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
