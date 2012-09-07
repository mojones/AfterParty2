package afterparty

class AdaptersFile {

    String name
    String description
    String data

    static constraints = {
        name(maxSize: 1000)
        description(maxSize: 10000, nullable: true)
    }


    static mapping = {
        data type: 'text'
    }

    static belongsTo = [experiment: Experiment]
}
