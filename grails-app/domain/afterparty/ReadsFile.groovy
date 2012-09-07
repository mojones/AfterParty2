package afterparty

class ReadsFile {

    def statisticsService


    String name
    String description
    ReadsFileData data
    ReadsFileStatus status



    static constraints = {
        name(maxSize: 1000)
        description(maxSize: 10000, nullable: true)

        }



    def isPublished(){
        return this.run.isPublished()
    }

    def isOwnedBy(def user){
         return this.run.isOwnedBy(user)
    }


}
