package afterparty

import javax.xml.parsers.SAXParserFactory
import org.xml.sax.InputSource

class BlastService {
    def sessionFactory
    def grailsApplication

    javax.sql.DataSource dataSource

    static transactional = false

    def attachBlastDatabaseToContigSet(ContigSet cs) {
        File contigsFastaFile = File.createTempFile('contigs', '.fasta')
        println "temporary file is ${contigsFastaFile.absolutePath} ${contigsFastaFile.name}"
        cs.contigs.each {
            contigsFastaFile.append(">" + it.id + "\n" + it.sequence + "\n")
        }
        println "running formatblasdb"

        println("${grailsApplication.config.makeblastdbPath} -in ${contigsFastaFile.absolutePath} -input_type 'fasta' -dbtype 'nucl'".split(" "))
        def blastProcess = new ProcessBuilder("${grailsApplication.config.makeblastdbPath} -in ${contigsFastaFile.absolutePath} -input_type fasta -dbtype nucl".split(" "))
        blastProcess.redirectErrorStream(true)
        blastProcess = blastProcess.start()
        blastProcess.in.eachLine({
            println "blast : $it"
        })
        blastProcess.waitFor()
        def headerBytes = (new File(contigsFastaFile.absolutePath + '.nhr')).getBytes()
        def indexBytes = (new File(contigsFastaFile.absolutePath + '.nin')).getBytes()
        def sequenceBytes = (new File(contigsFastaFile.absolutePath + '.nsq')).getBytes()
       
        println('saved some new blast data')
        return cs
    }


    def addBlastHitsFromInput(InputStream input, def backgroundJobId, def assemblyId) {

//        input.eachLine {
        //            println "BLAST: $it"
        //        }
        //
        def session = sessionFactory.openStatelessSession()

        def handler = new BlastXmlResultAnnotationHandler(jobId: backgroundJobId, assembly: Assembly.get(assemblyId.toLong()), statelessSession: session)
        def reader = SAXParserFactory.newInstance().newSAXParser().XMLReader
        reader.setContentHandler(handler)
        reader.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false)

        reader.parse(new InputSource(input))
        session.finalize()
        println "returning from service"
    }

    def runBlast(def assemblyId, def backgroundJobId) {

        println "running blast for $assemblyId"
        Assembly assembly = Assembly.get(assemblyId)
        BackgroundJob job = BackgroundJob.get(backgroundJobId)
        job.progress = "starting BLAST"
//        job.status = BackgroundJob.RUNNING
        def contigCount = assembly.contigs.size()
        job.totalUnits = contigCount
        job.status = BackgroundJobStatus.RUNNING
        job.save(flush: true)



        def n = 0


        assembly.contigs.each { contig ->

            if (n < 10 * 1000) {

                println "blast path is ${grailsApplication.config.blastxPath}"

                println "running blast on contig $n"




                File contigFastaFile = File.createTempFile('contig', '.fasta')
                println "temporary file is ${contigFastaFile.absolutePath}"

                contigFastaFile.append(">${contig.name}\n${contig.sequence}\n")

                def blastProcess = new ProcessBuilder("${grailsApplication.config.blastxPath} -db ${grailsApplication.config.sprotPath} -outfmt 5 -window_size 0 -num_threads 4 -max_target_seqs 10".split(" "))
                blastProcess.redirectErrorStream(true)
                blastProcess = blastProcess.start()


                def writer = new PrintWriter(new BufferedOutputStream(blastProcess.out))
                writer.println(">${contig.name}\n${contig.sequence}")
                writer.close()

                addBlastHitsFromInput(blastProcess.in, job.id, assembly.id)
//                blastProcess.in.eachLine({
                //                    println "blast : $it"
                //                })

            }
            n++
            job.progress = "BLASTED $n / $contigCount"
            job.unitsDone = n
            job.save(flush: true)
        }

        job.progress = "finished"
        job.status = BackgroundJobStatus.FINISHED
        job.save(flush: true)


    }
}
