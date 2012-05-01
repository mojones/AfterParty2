package afterparty

import groovy.sql.Sql

class PfamService {

    // transactional must be false in order to make sure that backgroundjob gets updated correctly
    static transactional = false

    javax.sql.DataSource dataSource

    def grailsApplication

    def addPfamFromInput(InputStream input) {
        input.eachLine { line ->
            def startTime = System.currentTimeMillis()
            def (contigName, type, acc, start, stop, evalue, description) = line.split("\t")[0, 3, 4, 6, 7, 8, 12]

            // strip off the last part of the hit hame to get the name of the input sequence, which is also the name of the original contig
            String realContigName = contigName.replaceAll(/_\d+_ORF\d+/, '')

            // for some reason this is much quicker than simply calling findByName()
            def sql = new Sql(dataSource)
            Long contigId = sql.firstRow("select id from contig where name=$realContigName")?.id
            println "id is $contigId"

            def theContig = Contig.get(contigId)

            println "evalue : $evalue - type $type $acc from $start to $stop with $description to $contigName ($theContig) (${System.currentTimeMillis() - startTime})"

            Annotation a = new Annotation(accession: acc, evalue: null, bitscore: null, description: description, start: start, stop: stop)

            if (evalue.isNumber()) {
                a.evalue = new Float(evalue)
            }

            def type2type = [
                    'HMMPfam': AnnotationType.PFAM,
                    'FPrintScan': AnnotationType.FPRINTSCAN,
                    'Seg': AnnotationType.SEG,
                    'HMMPanther': AnnotationType.HMMPANTHER,
                    'superfamily': AnnotationType.SUPERFAMILY,
                    'Gene3D': AnnotationType.GENE3D,
                    'HMMSmart': AnnotationType.HMMSMART,
                    'Coil': AnnotationType.COIL,
                    'BlastProDom': AnnotationType.BLASTPRODOM,
                    'HMMTigr': AnnotationType.HMMTIGR
            ]

            a.type = type2type.get(type)

            if (a.type == null) {
                println "new annotation type : $type"
            }

            if (a.type == AnnotationType.SEG) {
                a.description = 'low complexity region'
            }

            // Seg hits don't have evalue
            if (evalue.toString() == 'NA') {
                a.evalue = null
            }

            if (theContig != null) {
                theContig.addToAnnotations(a)
                println "about to save : ${System.currentTimeMillis() - startTime}"
//                a.save(flush: true)

                println "saved ${System.currentTimeMillis() - startTime}"

            }

        }

    }

    def runPfam(def assemblyId, def backgroundJobId) {
        println "running pfam for $assemblyId"
        Assembly assembly = Assembly.get(assemblyId)
        BackgroundJob job = BackgroundJob.get(backgroundJobId)
        job.progress = "starting PFAM"
        def contigCount = assembly.contigs.size()
        job.totalUnits = contigCount
        job.status = BackgroundJobStatus.RUNNING
        job.save(flush: true)

        def n = 0


        assembly.contigs.each { contig ->



            File contigFastaFile = File.createTempFile('contig', '.fasta')
            println "temporary file is ${contigFastaFile.absolutePath}"

            contigFastaFile.append(">${contig.name}\n${contig.sequence}\n")
            println "${grailsApplication.config.interproscanPath} -cli -i ${contigFastaFile.absolutePath} -o ${contigFastaFile.absolutePath}.out -trlen 20 -verbose -format raw -iprlookup"
            def pfamProcess = new ProcessBuilder("${grailsApplication.config.interproscanPath} -cli -i ${contigFastaFile.absolutePath} -o ${contigFastaFile.absolutePath}.out -trlen 20 -verbose -format raw -iprlookup".split(" "))
            pfamProcess.redirectErrorStream(true)
            pfamProcess = pfamProcess.start()
            pfamProcess.in.eachLine({
                println "pfam : $it"
            })



            addPfamFromInput(new FileInputStream(new File(contigFastaFile.absolutePath + '.out')))


            n++
            job.progress = "Annotated $n / $contigCount"
            job.unitsDone = n
            job.save(flush: true)
        }

        job.progress = "finished"
        job.status = BackgroundJobStatus.FINISHED
        job.save(flush: true)


    }
}
