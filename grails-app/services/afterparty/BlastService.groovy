package afterparty

class BlastService {

    static transactional = false

    def addBlastHitsFromInput(InputStream input) {
        def spf = javax.xml.parsers.SAXParserFactory.newInstance()
        spf.validating = false
        spf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false)
        def nodes = new XmlSlurper(spf.newSAXParser()).parse(input)

        int count = 0


        nodes.BlastOutput_iterations.Iteration.each { iteration ->

            Long contigId = iteration.'Iteration_query-def'.toLong()
            println "query id is $contigId"
            Contig contig = Contig.get(contigId)


            iteration.Iteration_hits.children().each {


                count++

                BlastHit b = new BlastHit(
                        description: it.Hit_def.toString(),
                        accession: it.Hit_accession.toString(),
                        bitscore: it.Hit_hsps.Hsp[0].'Hsp_bit-score'.toFloat(),
                        start: it.Hit_hsps.Hsp[0].'Hsp_query-from'.toInteger(),
                        stop: it.Hit_hsps.Hsp[0].'Hsp_query-to'.toInteger()
                )

                contig.addToBlastHits(b)

                contig.addTags(b.description.tokenize().unique().findAll({it.size() > 5}))
                contig.save(flush: true)


            }
        }
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
