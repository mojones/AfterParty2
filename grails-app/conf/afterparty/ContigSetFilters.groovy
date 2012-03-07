package afterparty

class ContigSetFilters {

    def springSecurityService

    def filters = {

//        allContigSetsExist(controller: 'contigSet', action: '(compareContigSets|showContigSetsStatsJSON)') {
//            before = {
//                println "checking if contig set exists"
//                params.idList.split(/,/).each {
//                    def cs = ContigSet.get(it.toLong())
//                    if (!cs) {
//                        flash.error = "ContigSet $it doesn't exist"
//                        redirect(controller: 'study', action: 'listPublished')
//                        return false
//                    }
//                    else{
//                        session.studyId = cs.study.id
//                    }
//                }
//
//            }
//        }

//        assemblyIsPublicOrOwnedByUser(controller: 'assembly', action: '(download|scatterplotAjax|histogramAjax|show)') {
//            before = {
//                println "checking if study is either public or owned"
//                Assembly a = Assembly.get(params.id)
//                def user = springSecurityService.isLoggedIn() ? springSecurityService?.principal : null
//
//                if (!a.compoundSample.study.published && a.compoundSample.study.user.id != user?.id) {
//                    flash.error = "Assembly is not published and you are not the owner"
//                    redirect(controller: 'study', action: 'listPublished')
//                    return false
//                }
//            }
//        }
//
//
//        assemblyIsOwnedByUser(controller: 'assembly', action: '(uploadBlastAnnotation|uploadContigs|runBlast)') {
//            before = {
//                println "checking if assembly is owned by user"
//                Assembly a = Assembly.get(params.id)
//                if (a.compoundSample.study.user.id != springSecurityService.principal.id) {
//                    flash.error = "Assembly doesn't belong to you"
//                    redirect(controller: 'assembly', action: 'show', id: params.id)
//                    return false
//                }
//            }
//        }


    }

}
