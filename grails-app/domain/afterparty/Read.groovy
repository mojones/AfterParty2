package afterparty

class Read {

    String name
    String sequence
    Integer start
    Integer stop

    Sample sampleSource
    Assembly assemblySource

    static constraints = {
        sampleSource(nullable: true)
        assemblySource(nullable: true)
    }

    static mapping = {
        sequence type: 'text'
        sort "start"
        contig (index:'read_contig')

    }

    static belongsTo = [contig : Contig]

}
