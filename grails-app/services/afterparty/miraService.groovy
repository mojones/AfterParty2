package afterparty

import java.util.regex.Matcher

class miraService {

    static transactional = false

    def sessionFactory

    Assembly createAssemblyAndContigsFromMiraInfo(File miraInfoFile, File contigsFile, File contigsQualityFile, File contigsStatsFile, Study s) {

        // open up the assembly info file and create an Assembly to hold it
        Assembly a = new Assembly(description: miraInfoFile.text, name: "assembly from ${miraInfoFile.name} ")
        // attach the assembly to the appropriate study
        s.addToAssemblies(a)
        a.save(flush:true)
        println "created assembly"

        // open up the contigs FASTA file and parse it, creating contigs as we go
        // keep a map of name->contig so that we can add stats to the contig later
        Map name2contig = [:]
        def contigs = parseFasta(contigsFile)
        contigs.each { name, seq ->
            Matcher m = (name =~ /.+_(c\d+$)/)
            def contigId = m[0][1]
            def contig = new Contig(name: contigId, sequence: seq)
            name2contig.put(name, contig)
        }

        // open up the contigs quality FASTA file and parse it
        def contigsQuality = parseFasta(contigsQualityFile)
        contigsQuality.each { name, seq ->
            Matcher m = (name =~ /.+_(c\d+$)/)
            name2contig.get(name).quality = seq
        }




        contigsStatsFile.eachLine { line ->
            if (!line.startsWith('#')) {
                def cols = line.split(/\s+/)
                Contig theContig = name2contig.get(cols[0])
                theContig.length = cols[1].toInteger()
                theContig.averageQuality = cols[2].toFloat()
                theContig.readCount = cols[3].toFloat()
                theContig.maximumCoverage = cols[4].toFloat()
                theContig.averageCoverage = cols[5].toFloat()
                theContig.gc = cols[6].toFloat()
                theContig.searchAssemblyId = a.id
                a.addToContigs(theContig)
            }

        }

        println "saving contigs"
        a.save(flush:true)
        println "saved all contigs"
        println "indexing...."
        Contig.index(name2contig.values())
        println "finished indexing"

        return a

    }



    def runMira(def readsFileIds, def jobId) {

        println " ${new Date()} running mira on reads file with id $readsFileIds"

        // update the job to show that it's running
        BackgroundJob job = BackgroundJob.get(jobId)
        job.progress = "running mira"
        job.status = BackgroundJobStatus.RUNNING
        job.save(flush: true)

        // generate a uuid for the project and create an input file
        String projectName = UUID.randomUUID().toString()
        File procInput = new File("/tmp/${projectName}_in.454.fastq")
        procInput.delete()

        // write the reads to the input file
        readsFileIds.each { readsFileId ->
            ReadsFile readsFile = ReadsFile.get(readsFileId)
            println "reads file is $readsFile"
            println "run of reads file is " + readsFile.run
            def readData = readsFile.data.fileData
            procInput.append(readData)
        }

        // construct the mira command line, set the working directory to /tmp, and start the process
        println "starting process"
        def p = new ProcessBuilder("/home/martin/Downloads/mira_3.2.1_prod_linux-gnu_x86_64_static/bin/mira --job=denovo,est,draft,454 --project=${projectName} -DI:lrt=/tmp -GE:not=4 454_SETTINGS -LR:lsd=yes:ft=fastq -notraceinfo".split(" "))
        job.commandLine = p.command().join('')
        p.directory(new File("/tmp"))
        p.redirectErrorStream(true)
        p = p.start()

        // monitor stdout of the mira process and update the job to show which pass we are on
        p.in.eachLine({
            if (it.contains('Pass')) {
                job.progress = it
                job.save(flush: true)
            }
        })

        File assemblyInfoFile = new File("/tmp/${projectName}_assembly/${projectName}_d_info/${projectName}_info_assembly.txt")
        File contigsFile = new File("/tmp/${projectName}_assembly/${projectName}_d_results/${projectName}_out.padded.fasta")
        File contigsQualityFile = new File("/tmp/${projectName}_assembly/${projectName}_d_results/${projectName}_out.padded.fasta.qual")
        File contigsStatsFile = new File("/tmp/${projectName}_assembly/${projectName}_d_info/${projectName}_info_contigstats.txt")

        Study s = ReadsFile.get(readsFileIds[0]).run.experiment.sample.study

        Assembly a = createAssemblyAndContigsFromMiraInfo(assemblyInfoFile, contigsFile, contigsQualityFile, contigsStatsFile, s)

        // update the job to show that we're finished and set the sink and source ids
        job.progress = 'finished'
        job.status = BackgroundJobStatus.FINISHED
        readsFileIds.each { readsFileId ->
            job.addToSources(readsFileId.toLong())
        }
        job.addToSinks(a.id)
        job.label = 'mira'
        job.save(flush: true)


    }

    private Map parseFasta(File contigsFile) {
        Map name2seq = [:]
        StringBuffer seq = new StringBuffer('')
        String name = ""
        contigsFile.eachLine { line ->
            if (line.startsWith('>')) {
                if (name) {
                    name2seq.put(name, seq.toString())
                }
                name = line.substring(1)
                seq = new StringBuffer()
            }
            else {
                seq.append(line)
            }
        }
        name2seq.put(name, seq.toString())
        return name2seq
    }
}