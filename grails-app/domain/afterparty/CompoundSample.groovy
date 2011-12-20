package afterparty

// this will normally be a species. All assemblies belong to this class, so it should represent groupings that are never assembled together
class CompoundSample {

    String name

    static hasMany = [samples : Sample, assemblies : Assembly]


    static constraints = {
        name(maxSize: 1000)
    }
}
