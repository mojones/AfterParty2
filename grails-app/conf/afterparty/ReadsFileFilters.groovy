package afterparty

class ReadsFileFilters {

    def springSecurityService

    def filters = {

        uploadedFileExists(controller: 'readsFile', action: '(attachAdapterSequences)') {
            before = {
                println "running uploaded file filter"
                def f = request.getFile('myFile')

                if (f.empty) {
                    flash.error = "Adapters file cannot be empty"
                    redirect(controller: 'readsFile', action: 'show', id: params.id)
                    return false
                }
            }

        }

        readsFileExists(controller: 'readsFile', action: '(trim|runMira|graph|download)') {
            before = {
                println "checking if readsFile exists"
                ReadsFile r = ReadsFile.get(params.id)
                if (!r) {
                    flash.error = "ReadsFile doesn't exist"
                    redirect(controller: 'study', action: 'listPublished')
                    return false
                }
            }
        }

        readsFileIsPublicOrOwnedByUser(controller: 'readsFile', action: '(graph|download)') {
            before = {
                println "checking if readsFile is either public or owned"
                ReadsFile r = ReadsFile.get(params.id)
                def user = springSecurityService.isLoggedIn() ? springSecurityService?.principal : null

                if (!r.run.experiment.sample.compoundSample.study.published && r.run.experiment.sample.compoundSample.study.user.id != user?.id) {
                    flash.error = "ReadsFile is not published and you are not the owner"
                    redirect(controller: 'study', action: 'listPublished')
                    return false
                }
            }
        }


        readsFileIsOwnedByUser(controller: 'readsFile', action: '(trim|runMira)') {
            before = {
                println "checking if readsFile is owned by user"
                ReadsFile r = ReadsFile.get(params.id)
                println "run is $r.run"
                if (r.run.experiment.sample.compoundSample.study.user.id != springSecurityService.principal.id) {
                    flash.error = "ReadsFile doesn't belong to you"
                    redirect(controller: 'sample', action: 'show', id: r.run.experiment.sample.id)
                    return false
                }
            }
        }


    }

}
