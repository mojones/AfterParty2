package afterparty

import org.grails.taggable.Taggable

class Contig implements Taggable{

    String name
    String sequence
    String quality

    // this allows us to search contigs from specific assemblies
    Integer searchAssemblyId

    // tell the searchable plugin that the blast hits are to be treated as a componenet of the contig for searching purposes
    static searchable = {
        except = ['id', 'reads']
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




    static hasMany = [blastHits: BlastHit, reads : Read]

    // TODO change this
    def topBlastHitMatching(String query){
        return this?.blastHits.sort({-it.bitscore})[0] ?: new BlastHit(description: 'none', bitscore : 0)
    }

    static belongsTo = [assembly: Assembly]


    def isPublished(){
        return this.assembly.isPublished()
    }

    def isOwnedBy(def user){
         return this.assembly.isOwnedBy(user)
    }
}
