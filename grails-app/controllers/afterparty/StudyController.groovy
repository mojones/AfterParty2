package afterparty

import grails.plugins.springsecurity.Secured

class StudyController {

    def overviewService
    def springSecurityService
    def searchService
    def statisticsService
    def grailsApplication

    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]



    def index = {
        redirect(action: "listPublished", params: params)
    }

    def uploadContigSet(){
        def f = request.getFile('myFile')
        def fileName = request.getFile('myFile').getOriginalFilename()
        println "creating a contig set from an uploaded file : ${fileName}"
        ContigSet cs = new ContigSet()
        cs.name = "contig set create from an uploaded file : ${fileName}"
        cs.description =  "contig set create from an uploaded file : ${fileName}"
        cs.type = ContigSetType.USER
        cs.study = Study.get(params.id)
        cs.data = new ContigSetData(blastHeaderFile: 'a', blastIndexFile : 'b', blastSequenceFile : 'c')
        f.inputStream.eachLine { line -> 
            def criteria = Contig.createCriteria()
            def c = criteria{
                eq('name', line)
                assembly {
                    compoundSample{
                        study{
                            eq('id', params.id.toLong())
                        }
                    }
                }
            }                

          cs.addToContigs(c[0])
            println line
        }
        cs.save(flush:true)
        redirect(action:'show', id : params.id)
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
        println(statisticsService.getStudyCounts())
        println( Study.findAllByPublished(true))
        [
            studyInstanceList: Study.findAllByPublished(true), 
            config: grailsApplication.config,
            studyCounts: statisticsService.getStudyCounts() 
        ]
    }

    @Secured(['ROLE_USER'])
    def create = {
        def studyInstance = new Study(name: 'Study name', description: 'Study description', published: false, downloadable: false)
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
            if (params.setting == 'true'){
                study.published = true
                flash.success = "published study ${study.name}"
            }
            if (params.setting == 'false'){
                study.published = false
                flash.success = "unpublished study ${study.name}"
            }
        }
        redirect(action: 'listPublished')
    }

    @Secured(['ROLE_USER'])
    def makeDownloadable = {
        def study = Study.get(params.id)
        if (study.user.id == springSecurityService.principal.id) {
            if (params.setting == 'true'){
                study.downloadable = true
                flash.success = "allowed downloads for study ${study.name}"
            }
            if (params.setting == 'false'){
                study.downloadable = false
                flash.success = "disallowed downloads for study ${study.name}"
            }
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
