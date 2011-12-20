package afterparty

class Sample {

      String name
    String description

    static constraints = {
        name(maxSize: 1000)
        description(maxSize: 10000, nullable: true)
    }

    static belongsTo = [compoundSample: CompoundSample]

    static hasMany = [experiments: Experiment]

    def getRawReadsCount(){
        int result = 0
        experiments.each{
            result += it?.rawReadsCount
        }
        return result
    }

}
