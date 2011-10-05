package afterparty

class AdaptersFile {

    String name
    String description
    byte[] data

    static constraints = {
        name(maxSize: 1000)
        description(maxSize: 10000, nullable: true)
    }

    static belongsTo = [experiment: Experiment]
}
