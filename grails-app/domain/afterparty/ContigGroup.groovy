package afterparty

class ContigGroup {

    String description

    static hasMany = [contigs : Contig]

    static constraints = {
        description(maxSize: 1000)
    }

    static belongsTo = [study : Study]


    def isPublished(){
        return this.study.isPublished()
    }

    def isOwnedBy(def user){
         return this.study.isOwnedBy(user)
    }
}
