package afterparty

import groovy.sql.Sql

class PfamService {

    // transactional must be false in order to make sure that backgroundjob gets updated correctly
    static transactional = false

    javax.sql.DataSource dataSource
    def sessionFactory

    def grailsApplication
    def propertyInstanceMap = org.codehaus.groovy.grails.plugins.DomainClassGrailsPlugin.PROPERTY_INSTANCE_MAP

    def cleanUpGorm() {
        def session = sessionFactory.currentSession
        session.flush()
        session.clear()
        propertyInstanceMap.get().clear()
    }

    def addPfamFromInput(InputStream input, Long jobId, Long assemblyId) {

        def added = 0
        BackgroundJob job = BackgroundJob.get(jobId)
        job.status = BackgroundJobStatus.RUNNING
        job.save(flush: true)

        input.eachLine { line ->
            if (line.split('\t').size() == 9){
                //println line
                def (contig_name, source, type, start, stop, evalue, strand, phase, attributes_string) = line.split('\t')
                def attrs=[:]
                attributes_string.split(';').each{
                    if (it.split('=').size() == 2){
                        def (key, value) = it.split('=')
                        attrs.put(key, value)
                    }
                }
                def acc = attrs.containsKey('Name') ? attrs.get('Name') : 'none'
                def description = attrs.containsKey('signature_desc') ? attrs.get('signature_desc') : 'none'
                if (['Pfam', 'Phobius', 'Gene3D', 'Coils', 'SMART'].contains(source)){
                    added = added + 1
                    if (added % 100 == 0){
                        println("updating job with $added")
                        job.progress = "added $added"
                        job.save(flush:true)
                        cleanUpGorm()
                    }


                    def startTime = System.currentTimeMillis()

                    def sql = new Sql(dataSource)
                    Long contigId = sql.firstRow("select id from contig where name=$contig_name and assembly_id = $assemblyId")?.id
                    //println "id is $contigId"

                    def theContig = Contig.get(contigId)
                    //println "evalue : $evalue - type $type $acc from $start to $stop with $description to $contigId ($theContig) (${System.currentTimeMillis() - startTime})"

                    Annotation a = new Annotation(accession: acc, evalue: null, bitscore: null, description: description, start: start.toInteger()*3, stop: stop.toInteger()*3)

                    if (evalue.isNumber()) {
                        a.evalue = new Float(evalue)
                    }

                    def type2type = [
                            'Pfam': AnnotationType.PFAM,
                            'Gene3D': AnnotationType.GENE3D,
                            'SMART': AnnotationType.HMMSMART,
                            'Coils': AnnotationType.COIL,
                            'Phobius': AnnotationType.PHOBIUS
                    ]

                    a.type = type2type.get(source)

                    //println("type is $type")

                    if (a.type == null) {
                        println "new annotation type : $type"
                    }

                    // Seg hits don't have evalue
                    if (evalue.toString() == 'NA') {
                        a.evalue = null
                    }

                    if (theContig != null) {
                        theContig.addToAnnotations(a)
                        //println "about to save : ${System.currentTimeMillis() - startTime}"
                        a.save()

                        //println "saved ${System.currentTimeMillis() - startTime}"

                    }
                }
            }

        }
        job.progress = "finished"
        job.status = BackgroundJobStatus.FINISHED
        job.save(flush: true)
    }

    def runPfam(def assemblyId, def backgroundJobId) {
        println "running pfam for $assemblyId"
        Assembly assembly = Assembly.get(assemblyId)
        BackgroundJob job = BackgroundJob.get(backgroundJobId)
        job.progress = "starting interproscan"
        def contigCount = assembly.contigs.size()
        job.totalUnits = contigCount
        job.status = BackgroundJobStatus.RUNNING
        job.save(flush: true)

        def n = 0

        println "started interproscan : ${System.currentTimeMillis()}"

        assembly.contigs.each { contig ->



            File contigFastaFile = File.createTempFile('contig', '.fasta')
            println "temporary file is ${contigFastaFile.absolutePath}"

            contigFastaFile.append(">${contig.name}\n${contig.sequence.toLowerCase().replaceAll(/[^atgc]/, 'n')}\n")
            def pfamString =  "${grailsApplication.config.interproscanPath} -f GFF3 -appl ProDom-2006.1,PfamA-26.0,TIGRFAM-12.0,SMART-6.2,Gene3d-3.3.0,Coils-2.2,Phobius-1.01 -i ${contigFastaFile.absolutePath} -t n -dp -o ${contigFastaFile.absolutePath}.gff3 "
            pfamString =  "${grailsApplication.config.interproscanPath} -f GFF3 -appl Coils-2.2 -i ${contigFastaFile.absolutePath} -t n -dp -o ${contigFastaFile.absolutePath}.gff3 "
            println pfamString
            

            def pfamProcess = new ProcessBuilder(pfamString.split(" "))
            pfamProcess.redirectErrorStream(true)
            pfamProcess = pfamProcess.start()
            pfamProcess.in.eachLine({
                println "pfam : $it"
            })


            def pfamOutput = new File(contigFastaFile.absolutePath + '.gff3')
            if (pfamOutput.exists()){
                addPfamFromInput(new FileInputStream(pfamOutput), backgroundJobId, assemblyId.toLong())
            }


            n++
            job.progress = "Annotated $n / $contigCount"
            job.unitsDone = n
            job.status = BackgroundJobStatus.RUNNING
            job.save(flush: true)
        }

        println "finished interproscan : ${System.currentTimeMillis()}"
        job.progress = "finished"
        job.status = BackgroundJobStatus.FINISHED
        job.save(flush: true)


    }
}
