package afterparty

class Study {

    String name
    String description
    Boolean published

    static constraints = {
        name(maxSize: 1000)
        description(maxSize: 10000, nullable: true)
    }

    static hasMany = [compoundSamples : CompoundSample]

    static belongsTo = [user : AfterpartyUser]

    def getRawReadsCount(){
        int result = 0
        samples.each{
            result += it?.rawReadsCount
        }
        return result
    }

}
