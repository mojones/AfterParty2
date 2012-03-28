package afterparty

import grails.util.Environment

class RunFilters {

    def springSecurityService

    def filters = {

        autoLogin(controller: '*', action: '*') {
            before = {
//                println "filter environment is ${Environment.current}"
                if (Environment.current == Environment.DEVELOPMENT) {
//                    println "auto-login power!"
//                    println "current user is ${springSecurityService.principal}"
                    springSecurityService.reauthenticate('martin')

                }
            }
        }

        uploadedFileExists(controller: 'run', action: '(attachReads|attachTrimmedReads)') {
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

        runExists(controller: 'run', action: '(attachReads|attachTrimmedReads|show)') {
            before = {
                println "checking if run exists"
                Run a = Run.get(params.id)
                if (!a) {
                    flash.error = "Run doesn't exist"
                    redirect(controller: 'study', action: 'listPublished')
                    return false
                }
            }
        }

        runIsPublicOrOwnedByUser(controller: 'run', action: '(show)') {
            before = {
                println "checking if run is either public or owned"
                Run r = Run.get(params.id)


                if (!r.isPublished() && !r.isOwnedBy(springSecurityService.principal)) {
                    flash.error = "Run is not published and you are not the owner"
                    redirect(controller: 'study', action: 'listPublished')
                    return false
                }
            }
        }


        runIsOwnedByUser(controller: 'run', action: '(attachTrimmedReads|attachReads)') {
            before = {
                println "checking if run is owned by user"
                Run r = Run.get(params.id)
                if (!r.isOwnedBy(springSecurityService.principal)) {
                    flash.error = "Run doesn't belong to you"
                    redirect(controller: 'run', action: 'show', id: params.id)
                    return false
                }
            }
        }


    }

}
