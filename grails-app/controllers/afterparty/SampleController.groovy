package afterparty

class SampleController {

    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

    def index = {
        redirect(action: "list", params: params)
    }

    def list = {
        params.max = Math.min(params.max ? params.int('max') : 10, 100)
        [sampleInstanceList: Sample.list(params), sampleInstanceTotal: Sample.count()]
    }

    def create = {
        def sampleInstance = new Sample()
        sampleInstance.properties = params
        return [sampleInstance: sampleInstance, studyId : params.studyId]
    }

    def save = {
        def sampleInstance = new Sample(params)
        sampleInstance.study = Study.get(params.studyId)
        if (sampleInstance.save(flush: true)) {
            flash.message = "${message(code: 'default.created.message', args: [message(code: 'sample.label', default: 'Sample'), sampleInstance.id])}"
            redirect(action: "show", id: sampleInstance.id)
        }
        else {
            render(view: "create", model: [sampleInstance: sampleInstance])
        }
    }

    def show = {
        def sampleInstance = Sample.get(params.id)
        if (!sampleInstance) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'sample.label', default: 'Sample'), params.id])}"
            redirect(action: "list")
        }
        else {
            [sampleInstance: sampleInstance]
        }
    }

    def edit = {
        def sampleInstance = Sample.get(params.id)
        if (!sampleInstance) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'sample.label', default: 'Sample'), params.id])}"
            redirect(action: "list")
        }
        else {
            return [sampleInstance: sampleInstance]
        }
    }

    def update = {
        def sampleInstance = Sample.get(params.id)
        if (sampleInstance) {
            if (params.version) {
                def version = params.version.toLong()
                if (sampleInstance.version > version) {

                    sampleInstance.errors.rejectValue("version", "default.optimistic.locking.failure", [message(code: 'sample.label', default: 'Sample')] as Object[], "Another user has updated this Sample while you were editing")
                    render(view: "edit", model: [sampleInstance: sampleInstance])
                    return
                }
            }
            sampleInstance.properties = params
            if (!sampleInstance.hasErrors() && sampleInstance.save(flush: true)) {
                flash.message = "${message(code: 'default.updated.message', args: [message(code: 'sample.label', default: 'Sample'), sampleInstance.id])}"
                redirect(action: "show", id: sampleInstance.id)
            }
            else {
                render(view: "edit", model: [sampleInstance: sampleInstance])
            }
        }
        else {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'sample.label', default: 'Sample'), params.id])}"
            redirect(action: "list")
        }
    }

    def delete = {
        def sampleInstance = Sample.get(params.id)
        if (sampleInstance) {
            try {
                sampleInstance.delete(flush: true)
                flash.message = "${message(code: 'default.deleted.message', args: [message(code: 'sample.label', default: 'Sample'), params.id])}"
                redirect(action: "list")
            }
            catch (org.springframework.dao.DataIntegrityViolationException e) {
                flash.message = "${message(code: 'default.not.deleted.message', args: [message(code: 'sample.label', default: 'Sample'), params.id])}"
                redirect(action: "show", id: params.id)
            }
        }
        else {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'sample.label', default: 'Sample'), params.id])}"
            redirect(action: "list")
        }
    }
}
