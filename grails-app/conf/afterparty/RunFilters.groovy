package afterparty

class RunFilters {

    def springSecurityService

    def filters = {

        uploadedFileExists(controller: 'run', action: '(attachRawReads|attachTrimmedReads)') {
            before = {
                println "running uploaded file filter"
                def f = request.getFile('myFile')

                if (f.empty) {
                    flash.error = "File cannot be empty"
                    redirect(controller: 'run', action: 'show', id: params.id)
                    return false
                }
            }

        }

        runExists(controller: 'run', action: '(attachRawReads|attachTrimmedReads|show)') {
            before = {
                println "checking if run exists"
                Run a = Run.get(params.id)
                if (!a) {
                    flash.error = "Run doesn't exist"
                    redirect(controller: 'study', action:'listPublished')
                    return false
                }
            }
        }

        runIsPublicOrOwnedByUser(controller: 'run', action: '(show)') {
            before = {
                println "checking if run is either public or owned"
                Run r = Run.get(params.id)
                def user = springSecurityService.isLoggedIn() ? springSecurityService?.principal : null

                if (!r.experiment.sample.compoundSample.study.published && r.experiment.sample.compoundSample.study.user.id != user?.id) {
                    flash.error = "Run is not published and you are not the owner"
                    redirect(controller: 'study', action:'listPublished')
                    return false
                }
            }
        }


        runIsOwnedByUser(controller: 'run', action: '(attachTrimmedReads|attachRawReads)') {
            before = {
                println "checking if run is owned by user"
                Run r = Run.get(params.id)
                if (r.experiment.sample.study.user.id != springSecurityService.principal.id) {
                    flash.error = "Run doesn't belong to you"
                    redirect(controller: 'run', action: 'show', id: params.id)
                    return false
                }
            }
        }




    }

}
