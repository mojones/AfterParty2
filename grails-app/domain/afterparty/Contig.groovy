package afterparty

import org.grails.taggable.Taggable

class Contig implements Taggable{

    String name
    String sequence
    String quality
    Integer readCount
    Integer length
    Integer averageQuality
    Float maximumCoverage
    Float averageCoverage
    Float gc

    // this allows us to search contigs from specific assemblies
    Integer searchAssemblyId

    // tell the searchable plugin that the blast hits are to be treated as a componenet of the contig for searching purposes
    static searchable = {
        blastHits component: true
        searchAssemblyId : accessor:'property'
    }

    static constraints = {
        name(maxSize: 500)
    }

    static mapping = {
        sequence type: 'text'
        quality type: 'text'
    }



    static hasMany = [blastHits: BlastHit]

    // TODO change this
    def topBlastHitMatching(String query){
        return this?.blastHits.sort({-it.bitscore})[0] ?: new BlastHit(description: 'none', bitscore : 0)
    }

    static belongsTo = [assembly: Assembly]
}
