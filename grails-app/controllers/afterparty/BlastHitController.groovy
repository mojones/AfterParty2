package afterparty

class BlastHitController {

    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

    def index = {
        redirect(action: "list", params: params)
    }

    def list = {
        params.max = Math.min(params.max ? params.int('max') : 10, 100)
        [blastHitInstanceList: BlastHit.list(params), blastHitInstanceTotal: BlastHit.count()]
    }

    def create = {
        def blastHitInstance = new BlastHit()
        blastHitInstance.properties = params
        return [blastHitInstance: blastHitInstance]
    }

    def save = {
        def blastHitInstance = new BlastHit(params)
        if (blastHitInstance.save(flush: true)) {
            flash.message = "${message(code: 'default.created.message', args: [message(code: 'blastHit.label', default: 'BlastHit'), blastHitInstance.id])}"
            redirect(action: "show", id: blastHitInstance.id)
        }
        else {
            render(view: "create", model: [blastHitInstance: blastHitInstance])
        }
    }

    def show = {
        def blastHitInstance = BlastHit.get(params.id)
        if (!blastHitInstance) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'blastHit.label', default: 'BlastHit'), params.id])}"
            redirect(action: "list")
        }
        else {
            [blastHitInstance: blastHitInstance]
        }
    }

    def edit = {
        def blastHitInstance = BlastHit.get(params.id)
        if (!blastHitInstance) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'blastHit.label', default: 'BlastHit'), params.id])}"
            redirect(action: "list")
        }
        else {
            return [blastHitInstance: blastHitInstance]
        }
    }

    def update = {
        def blastHitInstance = BlastHit.get(params.id)
        if (blastHitInstance) {
            if (params.version) {
                def version = params.version.toLong()
                if (blastHitInstance.version > version) {

                    blastHitInstance.errors.rejectValue("version", "default.optimistic.locking.failure", [message(code: 'blastHit.label', default: 'BlastHit')] as Object[], "Another user has updated this BlastHit while you were editing")
                    render(view: "edit", model: [blastHitInstance: blastHitInstance])
                    return
                }
            }
            blastHitInstance.properties = params
            if (!blastHitInstance.hasErrors() && blastHitInstance.save(flush: true)) {
                flash.message = "${message(code: 'default.updated.message', args: [message(code: 'blastHit.label', default: 'BlastHit'), blastHitInstance.id])}"
                redirect(action: "show", id: blastHitInstance.id)
            }
            else {
                render(view: "edit", model: [blastHitInstance: blastHitInstance])
            }
        }
        else {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'blastHit.label', default: 'BlastHit'), params.id])}"
            redirect(action: "list")
        }
    }

    def delete = {
        def blastHitInstance = BlastHit.get(params.id)
        if (blastHitInstance) {
            try {
                blastHitInstance.delete(flush: true)
                flash.message = "${message(code: 'default.deleted.message', args: [message(code: 'blastHit.label', default: 'BlastHit'), params.id])}"
                redirect(action: "list")
            }
            catch (org.springframework.dao.DataIntegrityViolationException e) {
                flash.message = "${message(code: 'default.not.deleted.message', args: [message(code: 'blastHit.label', default: 'BlastHit'), params.id])}"
                redirect(action: "show", id: params.id)
            }
        }
        else {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'blastHit.label', default: 'BlastHit'), params.id])}"
            redirect(action: "list")
        }
    }
}
