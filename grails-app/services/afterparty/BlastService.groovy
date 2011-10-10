package afterparty

import javax.xml.parsers.SAXParserFactory
import org.xml.sax.Attributes
import org.xml.sax.InputSource
import org.xml.sax.helpers.DefaultHandler




class BlastService {

    static transactional = true

    def addBlastHitsFromInput(InputStream input, def backgroundJobId) {


        def handler = new RecordsHandler(jobId : backgroundJobId)
        def reader = SAXParserFactory.newInstance().newSAXParser().XMLReader
        reader.setContentHandler(handler)
        reader.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false)

        reader.parse(new InputSource(input))

        println "returning from service"
    }

    def runBlast(def assemblyId, def backgroundJobId) {

        println "running blast for $assemblyId"
        BackgroundJob job = BackgroundJob.get(backgroundJobId)
        job.progress = "starting BLAST"
//        job.status = BackgroundJob.RUNNING
        job.status = BackgroundJobStatus.RUNNING
        job.save(flush: true)

        Assembly assembly = Assembly.get(assemblyId)

        def contigCount = assembly.contigs.size()

        def n = 0


        assembly.contigs.each { contig ->

            if (n < 10 * 1000) {

                def p = new ProcessBuilder("/home/martin/Dropbox/downloads/ncbi-blast-2.2.25+/bin/blastx -db /home/martin/Downloads/uniprot_sprot.fasta -outfmt 5 -window_size 0 -num_threads 6 -max_target_seqs 10".split(" "))
                p.redirectErrorStream(true)
                p = p.start()


                def writer = new PrintWriter(new BufferedOutputStream(p.out))
                writer.println(">${contig.id}\n${contig.sequence}")
                writer.close()

//                        println "saw $count hits"
                addBlastHitsFromInput(p.in)

            }
            n++
            job.progress = "BLASTED $n / $contigCount"
            job.save(flush: true)
        }
        job.progress = "creating search index"
        job.save(flush: true)

        Contig.index(assembly.contigs)

        job.progress = "finished"
        job.status = BackgroundJobStatus.FINISHED
        job.save(flush: true)


    }
}
