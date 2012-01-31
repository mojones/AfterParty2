package afterparty

import grails.plugins.springsecurity.Secured

class CompoundSampleController {

    def springSecurityService
    def statisticsService


    def show = {
        def c = CompoundSample.get(params.id)
        def userId = springSecurityService.isLoggedIn() ? springSecurityService?.principal?.id : 'none'
        [compoundSample: c, isOwner: c.study.user.id == userId]
    }

    @Secured(['ROLE_USER'])
    def createSample = {
        def compoundSampleInstance = CompoundSample.get(params.id)
        def newSample = new Sample(name: 'sample name', description: 'sample description')
        compoundSampleInstance.addToSamples(newSample)
        compoundSampleInstance.save()
        flash.success = "added a new sample"
        redirect(action: show, id: compoundSampleInstance.id)
    }

    @Secured(['ROLE_USER'])
    def createAssembly = {
        def compoundSampleInstance = CompoundSample.get(params.id)
        def newAssembly = new Assembly(name: 'assembly name', description: 'assembly description')
        compoundSampleInstance.addToAssemblies(newAssembly)
        compoundSampleInstance.save()
        flash.success = "added a new assembly"
        redirect(action: show, id: compoundSampleInstance.id)
    }

    def showAssembliesJSON = {


        def compoundSample = CompoundSample.get(params.id)
        def contigSetList = []
        compoundSample.assemblies.each { assembly ->
            def cs = new ContigSet(
                    name: "$assembly.name",
                    description: "automatically generated contig set for $assembly.name",
                    study: compoundSample.study
            )
            assembly.contigs.each {
                cs.addToContigs(it)
            }
            cs.save(flush: true)
            contigSetList.add(cs)
        }

        def assemblies = statisticsService.getStatsForContigSets(contigSetList)


        render(contentType: "text/json") {
            assemblyList = assemblies
        }
    }


}
