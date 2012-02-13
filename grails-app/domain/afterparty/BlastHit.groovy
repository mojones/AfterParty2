package afterparty

class BlastHit {

    String description
    Float bitscore
    int start
    int stop
    String accession

    // mark this class as NOT a searchable root, so that it doesn't get returned in searches - instead, we want to always return the contig
    static searchable = {
    except = ['start', 'stop']
    }

    static constraints = {
        description(maxSize: 1000)
    }

    static mapping = {
       // sort bitscore:'desc'
        contig (index:'blasthit_contig')
    }

    static belongsTo = [contig : Contig]

    def isPublished(){
        return this.contig.isPublished()
    }

    def isOwnedBy(def user){
         return this.contig.isOwnedBy(user)
    }
}
