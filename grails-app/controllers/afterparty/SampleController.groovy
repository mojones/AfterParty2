package afterparty

class SampleController {

    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]


    def create = {
        def sampleInstance = new Sample(name: 'Sample name', description: 'Sample description')
        Study.get(params.studyId.toLong()).addToSamples(sampleInstance)
        sampleInstance.save()
        redirect(action: show, id: sampleInstance.id)
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


}
