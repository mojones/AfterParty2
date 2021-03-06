package afterparty

class ContigFilters {

    def springSecurityService

    def filters = {


        contigExists(controller: 'contig', action: '(show|showJSON)') {
            before = {
                println "User agent: ${request.getHeader("User-Agent")}"
                println "checking if contig exists"
                Contig c = Contig.get(params.id)
                if (!c) {
                    flash.error = "Contig doesn't exist"
                    redirect(controller: 'study', action: 'listPublished')
                    return false
                }
            }
        }

        contigIsPublicOrOwnedByUser(controller: 'contig', action: '(show|showJSON)') {
            before = {
                println "checking if contig is either public or owned"
                Contig c = Contig.get(params.id)
                def user = springSecurityService.isLoggedIn() ? springSecurityService?.principal : null

                if (!c.assembly.compoundSample.study.published && c.assembly.compoundSample.study.user.id != user?.id) {
                    flash.error = "Contig is not published and you are not the owner"
                    redirect(controller: 'study', action: 'listPublished')
                    return false
                }
            }
        }


    }

}
