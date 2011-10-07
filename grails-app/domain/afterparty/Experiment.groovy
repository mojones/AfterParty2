package afterparty

class Experiment {

    String name
    String description

    AdaptersFile adapters

    static constraints = {
        name(maxSize: 1000)
        description(maxSize: 10000, nullable: true)
        adapters(nullable: true)
    }

    static belongsTo = [sample: Sample]

    static hasMany = [runs: Run]

    def getRawReadsCount() {
        int result = 0
        runs.each {
            result += it.rawReadsFile ? it.rawReadsFile.readCount : 0
        }
        return result
    }

    def getBaseCount() {
        int result = 0
        runs.each {
            result += it.rawReadsFile ? it.rawReadsFile.baseCount : 0
        }
        return result
    }


}
