package afterparty

// this will normally be a species. All assemblies belong to this class, so it should represent groupings that are never assembled together
class CompoundSample {

    String name

    ContigSet defaultContigSet

    static hasMany = [samples : Sample, assemblies : Assembly]

    static belongsTo = [study : Study]

    static constraints = {
        name(maxSize: 1000)
        defaultContigSet(nullable: true)
    }

    def isPublished(){
        return this.study.published
    }

    def isOwnedBy(def user){
         return this.study.isOwnedBy(user)
    }
}
