package afterparty

class CompoundSampleController {

    def springSecurityService


   def show = {
       def c = CompoundSample.get(params.id)
       def userId = springSecurityService.isLoggedIn() ? springSecurityService?.principal?.id : 'none'
       [compoundSample : c, isOwner : c.study.user.id == userId]
   }
}
