package afterparty

class ContigSet {

    def statisticsService

    String name
    String description

    ContigSetType type


    static hasMany = [contigs: Contig]

    static belongsTo = [study: Study]

    ContigSetData data

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
