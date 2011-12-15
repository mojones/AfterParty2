package afterparty

class ExperimentFilters {

    def springSecurityService

    def filters = {

        uploadedFileExists(controller: 'experiment', action: '(attachAdapterSequences)') {
            before = {
                println "running uploaded file filter"
                def f = request.getFile('myFile')

                if (f.empty) {
                    flash.error = "Adapters file cannot be empty"
                    redirect(controller: 'experiment', action: 'show', id: params.id)
                    return false
                }
            }

        }

        experimentExists(controller: 'experiment', action: '(trimAllReadFiles|attachAdapterSequences|show|create|save)') {
            before = {
                println "checking if experiment exists"
                Experiment e = Experiment.get(params.id)
                if (!e) {
                    flash.error = "Experiment doesn't exist"
                    redirect(controller: 'study', action:'listPublished')
                    return false
                }
            }
        }

        experimentIsPublicOrOwnedByUser(controller: 'experiment', action: '(show)') {
            before = {
                println "checking if experiment is either public or owned"
                Experiment a = Experiment.get(params.id)
                def user = springSecurityService.isLoggedIn() ? springSecurityService?.principal : null

                if (!a.sample.study.published && a.sample.study.user.id != user?.id) {
                    flash.error = "Experiment is not published and you are not the owner"
                    redirect(controller: 'study', action:'listPublished')
                    return false
                }
            }
        }


        experimentIsOwnedByUser(controller: 'experiment', action: '(trimAllReadFiles|attachAdapterSequences|save)') {
            before = {
                println "checking if experiment is owned by user"
                Experiment e = Experiment.get(params.id)
                if (e.sample.study.user.id != springSecurityService.principal.id) {
                    flash.error = "Experiment doesn't belong to you"
                    redirect(controller: 'sample', action: 'show', id: e.sample.id)
                    return false
                }
            }
        }




    }

}
