package afterparty

import org.xml.sax.Attributes
import org.xml.sax.helpers.DefaultHandler

class BlastXmlResultAnnotationHandler extends DefaultHandler {

    def jobId
    def statelessSession
    def sql

    // to make sure we only add annotation to the right contigs
    def assembly

    String currentElement
    Contig currentContig
    def currentProperties = [:]
    Set currentTags = []
    def count = 0

    void startElement(String ns, String localName, String qName, Attributes atts) {
        if (qName in ['Iteration_query-def', 'Hit_def', 'Hit_accession', 'Hsp_bit-score', 'Hsp_query-from', 'Hsp_query-to', 'Hsp_evalue']) {
            currentElement = qName
        }
        else{
            currentElement = null
        }
    }

    void characters(char[] chars, int offset, int length) {
        if (currentElement) {
            currentProperties.put(currentElement, new String(chars, offset, length))

            if (currentElement == 'Iteration_query-def') {
                //currentContig = Contig.findByAssemblyAndName(this.assembly, currentProperties.get('Iteration_query-def'))
            }
        }
    }



    void endElement(String ns, String localName, String qName) {
        currentElement = null
        if (qName == 'Hsp') {
            //Annotation b = new Annotation()
            //b.description = currentProperties.get('Hit_def')
            //b.accession = currentProperties.get('Hit_accession')
            //b.bitscore = currentProperties.get('Hsp_bit-score').toFloat()
            //b.start = currentProperties.get('Hsp_query-from').toInteger()
            //b.stop = currentProperties.get('Hsp_query-to').toInteger()
            //b.evalue = currentProperties.get('Hsp_evalue').toFloat()
            //b.type = AnnotationType.BLAST

            //currentContig.addToAnnotations(b)
            //b.save()
//            statelessSession.insert(b)
            //            b.description.tokenize().unique().findAll({it.size() > 5}).each {
            //                currentTags.add(it.toString())
            //            }

        }

        if (qName == 'Iteration') {
            count++

            println "added hits for ${currentProperties.get('Iteration_query-def')}"
            //            currentContig.addTags(currentTags)
            //            currentContig.save(flush: true)
            //            currentContig.index()
            currentContig = null
            currentTags.clear()

            if ((count % 100) == 0) {
                BackgroundJob.withNewSession {
                    println "updating job to $count"
                    BackgroundJob job = BackgroundJob.get(jobId)
                    job.progress = "added $count contigs"
                    job.save(flush: true)
                }
            }
        }
    }
}