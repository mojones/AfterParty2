package afterparty

class StudyFilters {

    def springSecurityService


    def filters = {
        studyExists(controller: 'study', action: '(overview|show)') {
            before = {
                println "checking if study exists"
                Study s = Study.get(params.id)
                if (!s) {
                    flash.error = "Study doesn't exist"
                    redirect(controller: 'study', action:'listPublished')
                    return false
                }
            }
        }

        studyIsPublicOrOwnedByUser(controller: 'study', action: '(overview|show)') {
            before = {
                println "checking if study is either public or owned"
                Study s = Study.get(params.id)
                def user = springSecurityService.isLoggedIn() ? springSecurityService?.principal : null

                if (!s.published && s.user.id != user?.id) {
                    flash.error = "Study is not published and you are not the owner"
                    redirect(controller: 'study', action:'listPublished')
                    return false
                }
            }
        }
    }

}
