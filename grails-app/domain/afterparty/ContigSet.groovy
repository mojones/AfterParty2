package afterparty

class ContigSet {

    String name
    String description
    byte[] blastHeaderFile
    byte[] blastIndexFile
    byte[] blastSequenceFile

    ContigSetType type


    static hasMany = [contigs: Contig]

    static belongsTo = [study: Study]



    static constraints = {
        blastHeaderFile(nullable: true)
        blastIndexFile(nullable: true)
        blastSequenceFile(nullable: true)
    }

    static mapping = {
        name type: 'text'
        description type: 'text'
    }

    def isPublished() {
        return this.study.isPublished()
    }

    def isOwnedBy(def user) {
        return this.study.isOwnedBy(user)
    }
}
