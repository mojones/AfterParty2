package afterparty



class ContigAnnotationService {

    static transactional = true


    def random = new Random()

    def drawAnnotation(def contigId) {
        Contig contig = Contig.get(contigId)
        return AnnotationDrawer.drawAnnotation({
            title(text: 'sequence scale')
            sequence(length: contig.length)

            title(text: 'quality')
            quality(qualityString: contig.quality)

            title(text: 'BLAST hits (hover for description)')
            contig.blastHits.sort({-it.bitscore}).each { hit ->
                blastHit(hit)
            }

        })
    }
}
