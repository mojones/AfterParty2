package afterparty

class NavController {

    def show = {
        def study = Study.get(session.studyId)
        [study: study ]
    }

    def showStudies = {
        def studies = Study.findAllByPublished(true)
        [studies: studies]
    }
}
