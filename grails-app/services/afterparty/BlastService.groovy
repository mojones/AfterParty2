package afterparty

import javax.xml.parsers.SAXParserFactory
import org.xml.sax.InputSource
import groovy.sql.Sql

class BlastService {
    def sessionFactory
    def grailsApplication

    javax.sql.DataSource dataSource
    def propertyInstanceMap = org.codehaus.groovy.grails.plugins.DomainClassGrailsPlugin.PROPERTY_INSTANCE_MAP

    static transactional = false

    
     def cleanUpGorm() {
        def session = sessionFactory.currentSession
        session.flush()
        session.clear()
        propertyInstanceMap.get().clear()
    }

    def addBlastHitsFromInput(InputStream input, def backgroundJobId, def assemblyId) {

        def sqlAfterparty = new Sql(dataSource)
        BackgroundJob job = BackgroundJob.get(backgroundJobId)




        def interestingElements = ['Hit_def', 'Hit_accession', 'Hsp_bit-score', 'Hsp_query-from', 'Hsp_query-to', 'Hsp_evalue']

        def currentProperties = [:]
        def currentContigId
        def currentContig

        def processed = 0
        def hitsForContig = 0

        input.eachLine { line ->
            if (hitsForContig < 10){


                def elementMatcher = line =~ /<(Hit_def|Hit_accession|Hsp_bit-score|Hsp_query-from|Hsp_query-to|Hsp_evalue|Iteration_query-def)>(.+)<\/.+>/
                if (hitsForContig < 10 && elementMatcher){
                    def tagName = elementMatcher[0][1]
                    def tagContents = elementMatcher[0][2]

                    if (tagName in interestingElements){
                        currentProperties.put(tagName, tagContents)
                    }

                    if (tagName == 'Iteration_query-def'){
                        sqlAfterparty.rows("""
                            select 
                                contig.id 
                            from 
                                contig
                            where 
                                name=${tagContents} and 
                                assembly_id=${assemblyId.toLong()}
                            """).each{ row ->
                            currentContigId = row.id
                            currentContig = Contig.get(currentContigId)
                        }
                    }


                }
            }

            def endTagMatcher = line =~ /<\/(Hsp|Iteration)>/
            if (endTagMatcher){
                def endTag = endTagMatcher[0][1]
                if (endTag == 'Hsp' && hitsForContig < 11){
                    hitsForContig++
                    def description = currentProperties.'Hit_def'
                    def accession = currentProperties.'Hit_accession'
                    def bitscore = currentProperties.'Hsp_bit-score'.toFloat()
                    def evalue = currentProperties.'Hsp_evalue'.toFloat()
                    def start = currentProperties.'Hsp_query-from'.toInteger()
                    def stop = currentProperties.'Hsp_query-to'.toInteger()

                    Annotation b = new Annotation()
                    b.description = description
                    b.accession = accession
                    b.bitscore = bitscore
                    b.start = start
                    b.stop = stop
                    b.evalue = evalue
                    b.type = AnnotationType.BLAST

                    currentContig.addToAnnotations(b)

                } 

                if (endTag == 'Iteration'){
                    processed++
                    currentContig.save(flush:true)
                    hitsForContig = 0
                    print "."

                    if (processed % 100 == 0){
                        println "added ${hitsForContig} hits for ${currentContig} (${processed}) "
                        job.progress = "added hits for ${processed} contigs"
                        job.save(flush:true)

                        cleanUpGorm()
                    }
                }
            }

        }

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
