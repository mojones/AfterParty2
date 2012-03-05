package afterparty


class Contig {

    String name
    String sequence
    String quality

    // this allows us to search contigs from specific assemblies
    Integer searchAssemblyId

    // tell the searchable plugin that the blast hits are to be treated as a componenet of the contig for searching purposes
    static searchable = {
        except = ['id', 'reads', 'quality']
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


    static transients = ['topBlastHit', 'topBlastBitscore']


    static hasMany = [blastHits: BlastHit, reads: Read]

    String getTopBlastHit(){
        if (this.blastHits && this.blastHits.size() > 0){
            return this.blastHits.toArray().sort({-it.bitscore})[0].description
        }
        else{
            return null
        }
    }

    String getTopBlastBitscore(){
        if (this.blastHits && this.blastHits.size() > 0){
            return this.blastHits.toArray().sort({-it.bitscore})[0].bitscore
        }
        else{
            return null
        }
    }

    // TODO change this
    def topBlastHitMatching(String query) {
        return this?.blastHits.sort({-it.bitscore})[0] ?: new BlastHit(description: 'none', bitscore: 0)
    }

    def averageQuality() {
        List qualities = quality.split(/ /).collect({it.toInteger()})
        Integer sum = qualities.sum()
        Integer size = qualities.size()
        return [sum / size, 0.1].max()
    }

    def coverage() {

//        println "calculating coverage for ${this.id}, sequence length is ${sequence.length()}"
        def result = new Integer[sequence.length()]
        (0..sequence.length() - 1).each {
            result[it] = 0
        }

        this.reads.each {
//            println "start is $it.start and stop is $it.stop"

            def firstPosition = [[it.start, it.stop, sequence.length() - 1].min(), 0].max()

            def lastPosition = [[it.stop, it.start, 0].max(), this.sequence.length() - 1].min()
//            println "incrementing from $firstPosition to $lastPosition (length is ${result.size()})"

            (firstPosition..lastPosition).each {
                result[it]++
            }

        }
        return result

    }

    def averageCoverage() {
        def coverage = this.coverage()
        return (Float) [coverage.sum() / coverage.size(), 0.1].max()
    }

    def gc() {
        return sequence.findAll({it == 'G' || it == 'C'}).size() / sequence.length()
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
