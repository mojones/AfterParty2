package afterparty

class ContigSet {

    def statisticsService

    String name
    String description

    //TODO move these big fields into a separate domain object so that they can be loaded lazily
    byte[] blastHeaderFile
    byte[] blastIndexFile
    byte[] blastSequenceFile

    ContigSetType type


    static hasMany = [contigs: Contig]

    static belongsTo = [study: Study]



    static constraints = {
    }

    static mapping = {
        name type: 'text'
        description type: 'text'
        contigs(index: 'martin9')

    }

    def isPublished() {
        return this.study.isPublished()
    }

    def isOwnedBy(def user) {
        return this.study.isOwnedBy(user)
    }

    def numberOfContigs(){
        return statisticsService.countContigsForContigSet(this.id)
    }

}
