package afterparty

class Run {


    String name
    String description
    ReadsFile rawReadsFile
    ReadsFile trimmedReadsFile

    static constraints = {
        name(maxSize: 1000)
        description(maxSize: 10000, nullable: true)
        rawReadsFile(nullable: true)
        trimmedReadsFile(nullable: true)
    }

    static belongsTo = [experiment: Experiment]


}
