package afterparty

import org.xml.sax.Attributes
import org.xml.sax.helpers.DefaultHandler

class RecordsHandler extends DefaultHandler {

    def jobId

    String currentElement
    Contig currentContig
    def currentProperties = [:]
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
                currentContig = Contig.get(currentProperties.get('Iteration_query-def').toLong())
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
            currentContig.addTags(b.description.tokenize().unique().findAll({it.size() > 5}))


        }

        if (qName == 'Iteration') {
            count++
            currentContig.save(flush: true)
            println "added hits for $currentContig.name"

            currentContig.index()
            currentContig = null

            if ((count % 1) == 0) {
                println "updating job to $count"
                BackgroundJob job = BackgroundJob.get(jobId)
                job.progress = "added $count contigs"
                job.save(flush: true)
            }
        }
    }
}