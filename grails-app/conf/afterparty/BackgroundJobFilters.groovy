package afterparty

class BackgroundJobFilters {

    def springSecurityService


    def filters = {
        studyExists(controller: 'backgroundJob', action: '(overview)') {
            before = {
                println "checking if study exists for backgroundJob"
                Study s = Study.get(params.id)
                if (!s) {
                    flash.error = "Study doesn't exist"
                    redirect(controller: 'study', action:'listPublished')
                    return false
                }
            }
        }

        studyIsPublicOrOwnedByUser(controller: 'backgroundJob', action: '(overview)') {
            before = {
                println "checking if study is either public or owned for background job"
                Study s = Study.get(params.id)
                def user = springSecurityService.isLoggedIn() ? springSecurityService?.principal : null

                if (!s.published && s.user.id != user?.id) {
                    flash.error = "Study is not published and you are not the owner"
                    redirect(controller: 'study', action:'listPublished')
                    return false
                }
            }
        }

        backgroundJobExists(controller: 'backgroundJob', action: '(graph,show)'){
            before = {
                println "checking if background job exists"
                BackgroundJob j = BackgroundJob.get(params.id)
                if (!j){
                    flash.error = "No such background job"
                    redirect(controller: 'backgroundJob', action: 'list')
                    return false
                }
            }
        }


    }

}
