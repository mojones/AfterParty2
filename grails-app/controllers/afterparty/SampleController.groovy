package afterparty

import grails.plugins.springsecurity.Secured

class SampleController {

    def springSecurityService

    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]





    @Secured(['ROLE_USER'])
    def createExperiment = {
        def sampleInstance = Sample.get(params.id)
        def newExperiment = new Experiment(name: 'experiment name')
        sampleInstance.addToExperiments(newExperiment)
        sampleInstance.save()
        flash.success = "added a new experiment"
        redirect(action: show, id: sampleInstance.id)
    }


    def show = {
        def sampleInstance = Sample.get(params.id)
        [sampleInstance: sampleInstance, isOwner : sampleInstance.isOwnedBy(springSecurityService.principal)]

    }


}
