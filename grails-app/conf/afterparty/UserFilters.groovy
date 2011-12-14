package afterparty

class UserFilters {

    def springSecurityService

    def filters = {

        uploadedFileExists(controller: 'assembly', action: '(uploadBlastAnnotation|uploadContigs)') {
            before = {
                println "running uploaded file filter"
                def f = request.getFile('myFile')

                if (f.empty) {
                    flash.error = "File cannot be empty"
                    redirect(controller: 'assembly', action: 'show', id: params.id)
                    return false
                }
            }

        }

        assemblyExists(controller: 'assembly', action: '(uploadBlastAnnotation|uploadContigs|download|runBlast|scatterplotAjax|histogramAjax)') {
            before = {
                println "checking if assembly exists"
                Assembly a = Assembly.get(params.id)
                if (!a) {
                    flash.error = "Assembly doesn't exist"
                    redirect(controller: 'study', action:'listPublished')
                    return false
                }
            }
        }

        assemblyIsPublicOrOwnedByUser(controller: 'assembly', action: '(download|scatterplotAjax|histogramAjax)') {
            before = {
                println "checking if study is either public or owned"
                Assembly a = Assembly.get(params.id)
                def user = springSecurityService.isLoggedIn() ? springSecurityService?.principal : null

                if (!a.study.published && a.study.user.id != user?.id) {
                    flash.error = "Assembly is not published and you are not the owner"
                    redirect(controller: 'study', action:'listPublished')
                    return false
                }
            }
        }


        assemblyIsOwnedByUser(controller: 'assembly', action: '(uploadBlastAnnotation|uploadContigs|runBlast)') {
            before = {
                println "checking if assembly is owned by user"
                Assembly a = Assembly.get(params.id)
                if (a?.study?.user?.id != springSecurityService.principal.id) {
                    flash.error = "Assembly doesn't belong to you"
                    redirect(controller: 'assembly', action: 'show', id: params.id)
                    return false
                }
            }
        }


    }

}
