package afterparty

import org.xml.sax.Attributes
import org.xml.sax.helpers.DefaultHandler

class RecordsHandler extends DefaultHandler {
    String currentElement
    def currentProperties = [:]

    void startElement(String ns, String localName, String qName, Attributes atts) {
        if (qName in ['Iteration_query-def', 'Hit_def', 'Hit_accession', 'Hsp_bit-score', 'Hsp_query-from', 'Hsp_query-to']) {
            currentElement = qName
        }
    }

    void characters(char[] chars, int offset, int length) {
        if (currentElement) {
            currentProperties.put(currentElement, new String(chars, offset, length))
        }
    }

    void endElement(String ns, String localName, String qName) {
        currentElement = null
        if (qName == 'Hsp') {


            Long contigId = currentProperties.get('Iteration_query-def').toLong()
            println "query id is $contigId"
            Contig contig = Contig.get(contigId)
            BlastHit b = new BlastHit(
                    description: currentProperties.get('Hit_def'),
                    accession: currentProperties.get('Hit_accession'),
                    bitscore: currentProperties.get('Hsp_bit-score').toFloat(),
                    start: currentProperties.get('Hsp_query-from').toInteger(),
                    stop: currentProperties.get('Hsp_query-to').toInteger()
            )
            contig.addToBlastHits(b)
            contig.addTags(b.description.tokenize().unique().findAll({it.size() > 5}))

            contig.save(flush: true)
            println "added hits for $contig.name"

        }
    }
}