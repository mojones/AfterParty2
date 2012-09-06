package afterparty

import grails.plugins.springsecurity.Secured

class StudyController {

    def overviewService
    def springSecurityService
    def searchService
    def statisticsService

    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]



    def index = {
        redirect(action: "listPublished", params: params)
    }


    @Secured(['ROLE_USER'])
    def createCompoundSample = {
        def studyInstance = Study.get(params.id)
        def newCompoundSample = new CompoundSample(name: 'compound sample name')
        studyInstance.addToCompoundSamples(newCompoundSample)
        studyInstance.save(flush:true)
        newCompoundSample.save(flush: true)
        flash.success = "added a new compound sample"
        redirect(controller: 'compoundSample', action: 'show', id: newCompoundSample.id)
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
        flash.success = "created a new study"
        redirect(action: 'show', id: studyInstance.id)
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
        def criteria = Study.createCriteria()
        def studyInstance = criteria.get({
            eq('id', params.id.toLong())
            fetchMode 'compoundSamples', org.hibernate.FetchMode.JOIN
            fetchMode 'compoundSamples.samples', org.hibernate.FetchMode.JOIN
            fetchMode 'compoundSamples.samples.experiments', org.hibernate.FetchMode.JOIN
            fetchMode 'compoundSamples.samples.assemblies', org.hibernate.FetchMode.JOIN
        })
        session.studyId = params.id
        def userId = springSecurityService.isLoggedIn() ? springSecurityService?.principal?.id : 'none'
        [studyInstance: studyInstance, isOwner: studyInstance.user.id == userId]

    }


}
