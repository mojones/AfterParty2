package afterparty

class Read {

    String name
    String sequence
    Integer start
    Integer stop

    // if the input sequences that make up the contig are reads then they have a sample source
    Sample sampleSource

    // if the input sequences that make up the contigs are other contigs - i.e. we are doing a hybrid assembly - they have an assembly source
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
