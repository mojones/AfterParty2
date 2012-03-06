package afterparty

class Read {

    String name
    String sequence
    Integer start
    Integer stop

    String source

    static mapping = {
        sequence type: 'text'
        sort "start"
        contig(index: 'read_contig')

    }

    static belongsTo = [contig: Contig]

}
