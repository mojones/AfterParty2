package afterparty

class SampleFilters {

    def springSecurityService

    def filters = {


        sampleExists(controller: 'sample', action: '(show)') {
            before = {
                println "checking if sample exists"
                Sample s = Sample.get(params.id)
                if (!s) {
                    flash.error = "Sample doesn't exist"
                    redirect(controller: 'study', action:'listPublished')
                    return false
                }
            }
        }

        sampleIsPublicOrOwnedByUser(controller: 'sample', action: '(show)') {
            before = {
                println "checking if sample is either public or owned"
                Sample s = Sample.get(params.id)
                def user = springSecurityService.isLoggedIn() ? springSecurityService?.principal : null

                if (!s.compoundSample.study.published && s.compoundSample.study.user.id != user?.id) {
                    flash.error = "Sample is not published and you are not the owner"
                    redirect(controller: 'study', action:'listPublished')
                    return false
                }
            }
        }
    }
}
