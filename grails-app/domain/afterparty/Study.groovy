package afterparty

class Study {

    String name
    String description
    Boolean published

    ContigSet defaultContigSet

    static constraints = {
        name(maxSize: 1000)
        description(maxSize: 10000, nullable: true)
        defaultContigSet(nullable: true)
    }

    static hasMany = [compoundSamples: CompoundSample, contigSets : ContigSet]

    static belongsTo = [user: AfterpartyUser]

    def getRawReadsCount() {
        int result = 0
        samples.each {
            result += it?.rawReadsCount
        }
        return result
    }

    def isPublished(){
        return this.published
    }

    def isOwnedBy(def user) {
        return user.hasProperty('id') && user?.id == this.user.id
    }


}
