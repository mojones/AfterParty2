package afterparty

import org.compass.core.engine.SearchEngineQueryParseException

class ContigController {

    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

    def searchableService
    def contigAnnotationService

    def index = {
        redirect(action: "list", params: params)
    }

    def annotation = {
        render contigAnnotationService.drawAnnotation(params.id)
    }



    def search = {
        println "searching with assembly $params.assemblyId"

        println "query is " + params.q
        def study = Study.get(session.studyId)
        if (!params.q?.trim()) {
            return [assemblies: study.assemblies]
        }
        try {
            String completeQuery = "${params.q} AND searchAssemblyId:${params.assemblyId}"
            params.max = 50
            return [searchResult: Contig.search(completeQuery), assemblies: study.assemblies]
//            return [searchResult: Contig.search('assemblyId:42', params)]
        } catch (SearchEngineQueryParseException ex) {
            return [parseException: true]
        }
    }




    def show = {
        def contigInstance = Contig.get(params.id)
        if (!contigInstance) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'contig.label', default: 'Contig'), params.id])}"
            redirect(action: "list")
        }
        else {
            [contigInstance: contigInstance]
        }
    }


    def showJSON = {
        def contigInstance = Contig.get(params.id)
        render(contentType: "text/json") {
            length = contigInstance.length
            quality = contigInstance.quality.split(' ')
            blastHits = contigInstance.blastHits.sort({-it.bitscore})
        }
    }

}


