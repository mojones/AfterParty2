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
            return [assemblies : study.assemblies]
        }
        try {
            String completeQuery = "${params.q} AND searchAssemblyId:${params.assemblyId}"
            params.max = 50
            return [searchResult: Contig.search(completeQuery), assemblies : study.assemblies]
//            return [searchResult: Contig.search('assemblyId:42', params)]
        } catch (SearchEngineQueryParseException ex) {
            return [parseException: true]
        }
    }

    def list = {
        params.max = Math.min(params.max ? params.int('max') : 10, 100)
        [contigInstanceList: Contig.list(params), contigInstanceTotal: Contig.count()]
    }

    def create = {
        def contigInstance = new Contig()
        contigInstance.properties = params
        return [contigInstance: contigInstance]
    }

    def save = {
        def contigInstance = new Contig(params)
        if (contigInstance.save(flush: true)) {
            flash.message = "${message(code: 'default.created.message', args: [message(code: 'contig.label', default: 'Contig'), contigInstance.id])}"
            redirect(action: "show", id: contigInstance.id)
        }
        else {
            render(view: "create", model: [contigInstance: contigInstance])
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

    def edit = {
        def contigInstance = Contig.get(params.id)
        if (!contigInstance) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'contig.label', default: 'Contig'), params.id])}"
            redirect(action: "list")
        }
        else {
            return [contigInstance: contigInstance]
        }
    }

    def update = {
        def contigInstance = Contig.get(params.id)
        if (contigInstance) {
            if (params.version) {
                def version = params.version.toLong()
                if (contigInstance.version > version) {

                    contigInstance.errors.rejectValue("version", "default.optimistic.locking.failure", [message(code: 'contig.label', default: 'Contig')] as Object[], "Another user has updated this Contig while you were editing")
                    render(view: "edit", model: [contigInstance: contigInstance])
                    return
                }
            }
            contigInstance.properties = params
            if (!contigInstance.hasErrors() && contigInstance.save(flush: true)) {
                flash.message = "${message(code: 'default.updated.message', args: [message(code: 'contig.label', default: 'Contig'), contigInstance.id])}"
                redirect(action: "show", id: contigInstance.id)
            }
            else {
                render(view: "edit", model: [contigInstance: contigInstance])
            }
        }
        else {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'contig.label', default: 'Contig'), params.id])}"
            redirect(action: "list")
        }
    }

    def delete = {
        def contigInstance = Contig.get(params.id)
        if (contigInstance) {
            try {
                contigInstance.delete(flush: true)
                flash.message = "${message(code: 'default.deleted.message', args: [message(code: 'contig.label', default: 'Contig'), params.id])}"
                redirect(action: "list")
            }
            catch (org.springframework.dao.DataIntegrityViolationException e) {
                flash.message = "${message(code: 'default.not.deleted.message', args: [message(code: 'contig.label', default: 'Contig'), params.id])}"
                redirect(action: "show", id: params.id)
            }
        }
        else {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'contig.label', default: 'Contig'), params.id])}"
            redirect(action: "list")
        }
    }
}
