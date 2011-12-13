package afterparty

import org.xml.sax.Attributes
import org.xml.sax.helpers.DefaultHandler

class RecordsHandler extends DefaultHandler {

    def jobId

    // to make sure we only add annotation to the right contigs
    def assembly

    String currentElement
    Contig currentContig
    def currentProperties = [:]
    Set currentTags = []
    def count = 0

    void startElement(String ns, String localName, String qName, Attributes atts) {
        if (qName in ['Iteration_query-def', 'Hit_def', 'Hit_accession', 'Hsp_bit-score', 'Hsp_query-from', 'Hsp_query-to']) {
            currentElement = qName
        }
    }

    void characters(char[] chars, int offset, int length) {
        if (currentElement) {
            currentProperties.put(currentElement, new String(chars, offset, length))

            if (currentElement == 'Iteration_query-def') {
                currentContig = Contig.findByAssemblyAndName(this.assembly, currentProperties.get('Iteration_query-def'))
            }
        }
    }



    void endElement(String ns, String localName, String qName) {
        currentElement = null
        if (qName == 'Hsp') {
            BlastHit b = new BlastHit(
                    description: currentProperties.get('Hit_def'),
                    accession: currentProperties.get('Hit_accession'),
                    bitscore: currentProperties.get('Hsp_bit-score').toFloat(),
                    start: currentProperties.get('Hsp_query-from').toInteger(),
                    stop: currentProperties.get('Hsp_query-to').toInteger()
            )
            currentContig.addToBlastHits(b)
            b.description.tokenize().unique().findAll({it.size() > 5}).each {
                currentTags.add(it.toString())
            }

        }

        if (qName == 'Iteration') {
            count++

            println "added hits for $currentContig.name"
//            currentContig.addTags(currentTags)
            currentContig.save(flush: true)
            currentContig.index()
            currentContig = null
            currentTags.clear()

            if ((count % 10) == 0) {
                println "updating job to $count"
                BackgroundJob job = BackgroundJob.get(jobId)
                job.progress = "added $count contigs"
                job.save(flush: true)
            }
        }
    }
}