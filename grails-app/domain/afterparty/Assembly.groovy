package afterparty

class Assembly {

    def statisticsService

    String description
    String name

    static hasMany = [contigs: Contig]

    static fetchMode = [contigs: "eager"]

    static constraints = {
    }

// mark this class as NOT a searchable root, so that it doesn't get returned in searches - instead, we want to always return the contig
    static searchable = {
        root false
    }



    static mapping = {
        description type: 'text'
        name type: 'text'

    }

    static belongsTo = [study: Study]

    def getContigCount() {
        return statisticsService.getAssemblyStats(this.id.toLong()).readCount
    }

    def getMeanContigLength() {
        return statisticsService.getAssemblyStats(this.id.toLong()).meanLength
    }

    def getMaxContigLength() {
        return statisticsService.getAssemblyStats(this.id.toLong()).maxLength
    }

    def getMinContigLength() {
        return statisticsService.getAssemblyStats(this.id.toLong()).minLength
    }

    def getBaseCount() {
        return statisticsService.getAssemblyStats(this.id.toLong()).baseCount
    }

    def getN50() {
        return statisticsService.getAssemblyStats(this.id.toLong()).n50
    }

}
