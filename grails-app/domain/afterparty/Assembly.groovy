package afterparty

class Assembly {

    def statisticsService

    String description
    String name

    ContigSet defaultContigSet

    static hasMany = [contigs: Contig]

//    static fetchMode = [contigs: "eager"]

    static constraints = {
        defaultContigSet(nullable: true)
    }


    static mapping = {
        description type: 'text'
        name type: 'text'
        contigs cascade: "all-delete-orphan"

    }

    static belongsTo = [compoundSample: CompoundSample]


    def isPublished(){
        return this.compoundSample.isPublished()
    }

    def isOwnedBy(def user){
         return this.compoundSample.isOwnedBy(user)
    }
}
