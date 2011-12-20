package afterparty

import grails.plugins.springsecurity.Secured

class StudyController {

    def overviewService
    def springSecurityService

    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]



    def index = {
        redirect(action: "listPublished", params: params)
    }

    def createCompoundSample = {
        def studyInstance = Study.get(params.id)
        def newCompoundSample = new CompoundSample(name : 'compound sample name')
        studyInstance.addToCompoundSamples(newCompoundSample)
        studyInstance.save()
        flash.success = "added a new compound sample"
        redirect(action: show, id:studyInstance.id)
    }


    def overview = {
        def studyInstance = Study.get(params.id)

        if (studyInstance && (studyInstance.published || studyInstance.user.id == springSecurityService.principal.id)) {
            def image = overviewService.getDatasetOverview(params.id)
            response.setHeader('Content-length', image.length.toString())
            response.contentType = 'image/svg+xml' // or the appropriate image content type
            response.outputStream << image
            response.outputStream.flush()
        }
        else {
            render "no such study"
        }
    }

    def listPublished = {
        [studyInstanceList: Study.findAllByPublished(true)]
    }

    @Secured(['ROLE_USER'])
    def create = {
        def studyInstance = new Study(name: 'Study name', description: 'Study description', published: false)
        def user = AfterpartyUser.get(springSecurityService.principal.id)
        user.addToStudies(studyInstance)
        studyInstance.save()
        redirect(action: show, id: studyInstance.id)
    }

    @Secured(['ROLE_USER'])
    def makePublished = {
        def study = Study.get(params.id)
        if (study.user.id == springSecurityService.principal.id) {
            study.published = true
            flash.success = "published study ${study.name}"
        }
        redirect(action: 'listPublished')
    }


    def show = {
        def studyInstance = Study.get(params.id)
        session.studyId = params.id
        def userId = springSecurityService.isLoggedIn() ? springSecurityService?.principal?.id : 'none'
        [studyInstance: studyInstance, isOwner: studyInstance.user.id == userId]

    }


}
