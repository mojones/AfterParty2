package afterparty

class ContigSetController {

    def statisticsService
    def searchService

    def index = { }


    def compareContigSetsFromCheckbox = {

        //which contigsets are we looking at?
        def ids = []
        params.entrySet().findAll({it.key.startsWith('check_')}).each {
            Integer contigSetId = it.key.split(/_/)[1].toInteger()
            ids.add(contigSetId)
        }
        //render(view: 'compareContigSets', model: [contigSets: contigSetListResult])
        redirect(action: compareContigSets, params:['idList': ids.join(',')])
        return false
    }

    def compareContigSets = {
        def contigSetListResult = []
        params.idList.split(/,/).sort().each {
            contigSetListResult.add(ContigSet.get(it.toLong()))
        }
        [contigSets: contigSetListResult]
    }

    def createFromSearch = {
        List assemblies = []

        //which assemblies are we looking at?
        println "query is ${params.q}"

        def contigs = searchService.getContigsForSearch(params.q, 0, 100000000).contigs

        ContigSet cs = new ContigSet(name: params.q, description: "automatically generated contig set from query ${params.q}", study: Study.get(params.studyId))
        contigs.each {

            println "adding contig $it"
            cs.addToContigs(it)
        }
        cs.save()
        redirect(action: compareContigSets, params: [idList: [cs.id]])

    }

    def showContigSetsJSON = {
        def contigSetListResult = []
        params.idList.split(/,/).sort().eachWithIndex { id, i ->
            def cs = statisticsService.getContigStatsForContigSet(id.toLong())
            cs.colour = statisticsService.boldAssemblyColours[i]

            cs.contigSetId = id
            cs.label = ContigSet.get(id).name
            contigSetListResult.add(cs)
        }

        render(contentType: "text/json") {
            contigSetList = contigSetListResult
        }

    }

    def createContigSetAJAX = {

        Set ids = []
        params.idList.split(/,/).sort().each{
            ids.add(it)
        }
        ContigSet c = new ContigSet(name: "${ids.size()} contigs", description: "automatically generated contig set from chart", study: Study.get(params.studyId))
        ids.each {c.addToContigs(Contig.get(it.toLong()))}
        c.save()
        render(c.id)
    }


    def showContigSetsStatsJSON = {


        def contigSetListResult = []
        params.idList.split(/,/).each {
            contigSetListResult.add(ContigSet.get(it.toLong()))
        }

        println "contigSets are " + contigSetListResult

        def contigSets = statisticsService.getStatsForContigSets(contigSetListResult)




        def drawQualityBoolean = false
        if (contigSets[0].qualityvalues.size() > 1) {
            drawQualityBoolean = true
        }
        def drawCoverageBoolean = false
        if (contigSets[0].coveragevalues.size() > 1) {
            drawCoverageBoolean = true
        }

        render(contentType: "text/json") {
            contigSetList = contigSets
            lengthYmax = contigSets*.lengthYmax.max()
            scaledLengthYmax = contigSets*.scaledLengthYmax.max()
            qualityYmax = contigSets*.qualityYmax.max()
            scaledQualityYmax = contigSets*.scaledQualityYmax.max()
            coverageYmax = contigSets*.coverageYmax.max()
            scaledCoverageYmax = contigSets*.scaledCoverageYmax.max()
            drawQuality = drawQualityBoolean
            drawCoverage = drawCoverageBoolean
        }
    }

}
