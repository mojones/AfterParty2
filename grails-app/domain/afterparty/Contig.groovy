package afterparty


class Contig {

    String name
    String sequence
    String quality

    Float averageQuality
    Float averageCoverage

    Assembly assembly


    static constraints = {
        name(maxSize: 500)
    }

    static mapping = {
        sequence type: 'text'
        quality type: 'text'
        name(index: 'findbyname')
        assembly(index: 'findbyassembly')
    }

    static belongsTo = [Assembly]

    static transients = ['topBlastHit', 'topBlastBitscore']


    static hasMany = [annotations: Annotation, reads: Read]

    String getTopBlastHit() {
        if (this.annotations && this.annotations.findAll({it.type == AnnotationType.BLAST}).size() > 0) {
            return this.annotations.toArray().findAll({it.type == AnnotationType.BLAST}).sort({-it.bitscore})[0].description
        }
        else {
            return null
        }
    }

    Float getTopBlastBitscore() {
        if (this.annotations && this.annotations.findAll({it.type == AnnotationType.BLAST}).size() > 0) {
            return this.annotations.findAll({it.type == AnnotationType.BLAST}).toArray().sort({-it.bitscore})[0].bitscore
        }
        else {
            return null
        }
    }

    // TODO change this
    def topBlastHitMatching(String query) {
        return this?.annotations.findAll({it.type == AnnotationType.BLAST}).sort({-it.bitscore})[0] ?: new Annotation(description: 'none', bitscore: 0)
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

    def calculateAverageCoverage() {
        def coverage = this.coverage()
        return (Float) [coverage.sum() / coverage.size(), 0.1].max()
    }

    def gc() {
        return sequence.toUpperCase().findAll({it == 'G' || it == 'C'}).size() / sequence.length()
    }

    def length() {
        return this.sequence.length()
    }




    def isPublished() {
        return this.assembly.isPublished()
    }

    def isOwnedBy(def user) {
        return this.assembly.isOwnedBy(user)
    }

}
