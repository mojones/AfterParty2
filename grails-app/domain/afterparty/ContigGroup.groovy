package afterparty

class ContigGroup {

    String description

    static hasMany = [contigs : Contig]

    static constraints = {
        description(maxSize: 1000)
    }
}
