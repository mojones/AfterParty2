package afterparty

import org.xml.sax.Attributes
import org.xml.sax.helpers.DefaultHandler
import java.math.MathContext

class BlastXmlResultHandler extends DefaultHandler {

    def blastResults

    String currentElement
    def currentProperties = [:]

    void startElement(String ns, String localName, String qName, Attributes atts) {
        if (qName in ['Iteration_query-def', 'Hit_def', 'Hit_accession', 'Hsp_bit-score', 'Hsp_evalue', 'Hsp_query-from', 'Hsp_query-to', 'Hsp_hit-from', 'Hsp_hit-to']) {
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

            def description = currentProperties.get('Hit_def')
            def accession = currentProperties.get('Hit_accession')
            def bitscore = currentProperties.get('Hsp_bit-score').toFloat()
            BigDecimal evalue = new BigDecimal(currentProperties.get('Hsp_evalue').toString())
            def queryStart = currentProperties.get('Hsp_query-from').toInteger()
            def queryStop = currentProperties.get('Hsp_query-to').toInteger()
            def hitStart = currentProperties.get('Hsp_hit-from').toInteger()
            def hitStop = currentProperties.get('Hsp_hit-to').toInteger()

            println "found a hit with $description from $queryStart to $queryStop on the query"
            println "from $hitStart to $hitStop on the hit"
            println "evalue is " + evalue.round(new MathContext(5))

            blastResults.push([
                    contigId : description,
                    bitscore : bitscore,
                    start : queryStart,
                    stop : queryStop,
                    evalue: evalue
            ])


        }


    }
}