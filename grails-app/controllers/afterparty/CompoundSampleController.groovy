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

        // this is what we will eventually render as JSON
        def assemblies = []

        def compoundSample = CompoundSample.get(params.id)

        // figure out the buckets for length histogram
        def overallMaxLength = compoundSample.assemblies.collect({statisticsService.getContigStatsForAssembly(it.id).length.max()}).max()     // nicely functional
        def overallMaxQuality = compoundSample.assemblies.collect({statisticsService.getContigStatsForAssembly(it.id).quality.max()}).max()
        def overallMaxCoverage = compoundSample.assemblies.collect({statisticsService.getContigStatsForAssembly(it.id).coverage.max()}).max()

        println "overall max length is $overallMaxLength"
        println "overall max quality is $overallMaxQuality"

        compoundSample.assemblies.sort().eachWithIndex{  assembly, index ->

            def assemblyJSON = [:]

            assemblyJSON.id = it
            assemblyJSON.colour = StatisticsService.boldAssemblyColours[index]
            def contigStats = statisticsService.getContigStatsForAssembly(assembly.id)

            // build a histogram of length
            def lengthX = []
            def lengthY = []
            (0..overallMaxLength / 10).each {
                def floor = it * 10
                def ceiling = (it * 10) + 10
                def count = contigStats.length.findAll({it >= floor && it < ceiling}).size()
                lengthX.add(floor)
                lengthY.add(count)
            }
            assemblyJSON.lengthXvalues = lengthX
            assemblyJSON.lengthYvalues = lengthY
            assemblyJSON.lengthYmax = lengthY.max()

            // build a histogram of quality
            def qualityX = []
            def qualityY = []
            (0..overallMaxQuality).each {
                def floor = it
                def ceiling = it  + 1
                def count = contigStats.quality.findAll({it >= floor && it < ceiling}).size()
                qualityX.add(floor)
                qualityY.add(count)
            }
            assemblyJSON.qualityXvalues = qualityX
            assemblyJSON.qualityYvalues = qualityY
            assemblyJSON.qualityYmax = qualityY.max()


            // build a histogram of coverage
            def coverageX = []
            def coverageY = []
            (0..overallMaxCoverage).each {
                def floor = it
                def ceiling = it  + 1
                def count = contigStats.coverage.findAll({it >= floor && it < ceiling}).size() + 1
                coverageX.add(floor)
                Float logCount = Math.log10(count)
                println logCount
                coverageY.add(logCount)
            }
            assemblyJSON.coverageXvalues = coverageX
            assemblyJSON.coverageYvalues = coverageY
            assemblyJSON.coverageYmax = 10000

            assemblies.add(assemblyJSON)
        }
        render(contentType: "text/json") {
            assemblyList = assemblies
        }
    }


}
