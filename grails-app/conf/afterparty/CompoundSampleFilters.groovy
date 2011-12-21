package afterparty

class CompoundSampleFilters {

    def springSecurityService


    def filters = {
        compoundSampleExists(controller: 'compoundSample', action: '(show|createSample)') {
            before = {
                println "checking if compound sample exists"
                CompoundSample s = CompoundSample.get(params.id)
                if (!s) {
                    flash.error = "Compound sample doesn't exist"
                    redirect(controller: 'study', action: 'listPublished')
                    return false
                }
            }
        }

        compoundSampleIsPublicOrOwnedByUser(controller: 'compoundSample', action: '(show)') {
            before = {
                println "checking if compound sample is either public or owned"
                CompoundSample s = CompoundSample.get(params.id)
                def user = springSecurityService.isLoggedIn() ? springSecurityService?.principal : null

                if (!s.study.published && s.study.user.id != user?.id) {
                    flash.error = "Compound sample is not published and you are not the owner"
                    redirect(controller: 'study', action: 'listPublished')
                    return false
                }
            }
        }

        studyIsOwnedByUser(controller: 'compoundSample', action: '(createSample)') {
            before = {
                println "checking if compound sample is owned by user"
                CompoundSample s = CompoundSample.get(params.id)
                if (s.study.user.id != springSecurityService.principal.id) {
                    flash.error = "Compound sample doesn't belong to you"
                    redirect(controller: 'compoundSample', action: 'show', id: s.id)
                    return false
                }
            }
        }

    }

}
