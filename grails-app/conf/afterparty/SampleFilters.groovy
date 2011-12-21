package afterparty

class SampleFilters {

    def springSecurityService

    def filters = {


        sampleExists(controller: 'sample', action: '(show|createExperiment)') {
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

                if (!s.isPublished() && !s.isOwnedBy(user)) {
                    flash.error = "Sample is not published and you are not the owner"
                    redirect(controller: 'study', action:'listPublished')
                    return false
                }
            }
        }

        sampleIsOwnedByUser(controller: 'sample', action: '(createExperiment)') {
            before = {
                println "checking if sample is owned by user"
                Sample s = Sample.get(params.id)
                if (!s.isOwnedBy(springSecurityService.principal)) {
                    flash.error = "Sample doesn't belong to you"
                    redirect(controller: 'sample', action: 'show', id: s.id)
                    return false
                }
            }
        }
    }
}
