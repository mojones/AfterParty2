package afterparty


class Contig {

    String name
    String sequence
    String quality

    // this allows us to search contigs from specific assemblies
    Integer searchAssemblyId

    // tell the searchable plugin that the blast hits are to be treated as a componenet of the contig for searching purposes
    static searchable = {
        except = ['id', 'reads']
        blastHits component: true
        searchAssemblyId: accessor: 'property'
    }

    static constraints = {
        name(maxSize: 500)
    }

    static mapping = {
        sequence type: 'text'
        quality type: 'text'
    }




    static hasMany = [blastHits: BlastHit, reads: Read]

    // TODO change this
    def topBlastHitMatching(String query) {
        return this?.blastHits.sort({-it.bitscore})[0] ?: new BlastHit(description: 'none', bitscore: 0)
    }

    def averageQuality() {
        List qualities = quality.split(/ /).collect({it.toInteger()})
        Integer sum = qualities.sum()
        Integer size = qualities.size()
        return sum / size
    }

    def coverage() {
        def result = []
        this.sequence.eachWithIndex { base, i ->
            Integer coverage = this.reads.findAll({it.start <= i+1 && it.stop > i+1}).size()    // use i+1 because read alignment positions start at 1 not 0
            result.add(coverage)
        }
        return result
    }

    def gc() {
        return sequence.findAll({it == 'g' || it == 'c'}).size() / sequence.length()
    }

    def length() {
        return this.sequence.length()
    }

    static belongsTo = [assembly: Assembly]


    def isPublished() {
        return this.assembly.isPublished()
    }

    def isOwnedBy(def user) {
        return this.assembly.isOwnedBy(user)
    }
}
