package afterparty

class ContigSetController {

    def statisticsService

    def index = { }

    def compareContigSets = {
        // do nothing, just render the page - we will do all the work via AJAX

    }

    def showContigSetsJSON = {


        def contigSetListResult = []
        params.idList.split(/,/).each {
            contigSetListResult.add(ContigSet.get(it.toLong()))
        }

        def contigSets = statisticsService.getStatsForContigSets(contigSetListResult)


        render(contentType: "text/json") {
            contigSetList = contigSets
        }
    }

}
