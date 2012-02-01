package afterparty

class ContigSet {

    String name
    String description

    static hasMany = [contigs: Contig]

    static belongsTo = [study: Study]

    static constraints = {
    }

    static mapping = {
        name type: 'text'
        description type: 'text'
    }

        def isPublished(){
        return this.study.isPublished()
    }

    def isOwnedBy(def user){
         return this.study.isOwnedBy(user)
    }
}
