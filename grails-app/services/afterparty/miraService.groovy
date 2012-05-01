package afterparty

class miraService {

    static transactional = false

    def sessionFactory

    def statisticsService
    def executorService

    def grailsLinkGenerator

    def attachContigsFromMiraInfo(InputStream aceFile, Assembly a) {

        def added = 0
        def startTime = System.currentTimeMillis()

        def currentContigName
        def id2Start = [:]

        boolean inReadString = false
        boolean inContigString = false
        boolean inQualityString = false

        ArrayList currentReadString = []
        ArrayList currentContigString = []
        ArrayList currentQualityString = []

        def currentReadId

        def currentContig


        a.save(flush: true)

        aceFile.eachLine { line ->

            // for quick testing
            if (added > 100000) {
                return
            }

            if (line.startsWith(/CO /)) {
                // if we already have a contig then update it
                if (currentContig) {
//                    println "updating contig with ${currentContig.reads.size()} reads"
                    currentContig.averageCoverage = currentContig.calculateAverageCoverage()
//                    def start = System.currentTimeMillis()
                    currentContig.save()
//                    println System.currentTimeMillis() - start
                    if (++added % 100 == 0) {
                        println added
                    }
//

                }
                currentContigString = []
                currentContigName = line.split(/ /)[1]
//                println currentContigName
                inContigString = true

                currentContig = new Contig()
                currentContig.name = currentContigName
                currentContig.quality = 'qqq'
                currentContig.sequence = 'sss'
                currentContig.averageCoverage = 0
                currentContig.averageQuality = 0

                a.addToContigs(currentContig)
                currentContig.save()
            }

            else if (line.startsWith(/BQ/)) {
                inQualityString = true
            }

            else if (line.startsWith(/AF /)) {
                id2Start.put(line.split(/ /)[1], line.split(/ /)[3].toInteger())
            }

            else if (line.startsWith(/RD /)) {
                currentReadId = line.split(/ /)[1]
                inReadString = true
            }

            else if (line.equals('') && inReadString) {    // we have reached the end of the read sequence string
                Integer start = id2Start.get(currentReadId).toInteger()
                StringBuilder outputString = new StringBuilder()
                ArrayList alignedReadString = []

                Integer deletedBases = 0

                if (start > 0) {
                    alignedReadString = ['-'].multiply(start - 1) + currentReadString
                }
                else {
                    ((1 - start)..currentReadString.size()).each {
                        alignedReadString.add(currentReadString[it])
                    }
                }
                alignedReadString.eachWithIndex {base, i ->
                    def contigBase = currentContigString[i]
                    if (currentContigString.size() > i && contigBase == '*') {
                        outputString.append("")
                        deletedBases++
                    }
                    else {
                        outputString.append(base)
                    }
                }

                def r = new Read()
                r.name = currentReadId
                r.start = start
                r.sequence = outputString.toString()
                r.stop = start + currentReadString.size() - deletedBases
                String sourceString = a.name
                r.source = sourceString
                r.contig = currentContig
                currentContig.addToReads(r)
                r.save()
                currentReadString = []
                inReadString = false;
            }

            else if (line.equals('') && inContigString) {    // we have reached the end of the contig sequence string
                currentContig.sequence = currentContigString.join('').replaceAll(/\*/, '')
                inContigString = false;
            }

            else if (line.equals('') && inQualityString) {    // we have reached the end of the contig sequence string
                //        currentFile.append(currentQualityString.join('') + "\n")
                currentContig.quality = currentQualityString.join(' ')


                List qualities = currentContig.quality.split(/ /).collect({it.toInteger()})
                Integer sum = qualities.sum()
                Integer size = qualities.size()
                currentContig.averageQuality = [sum / size, 0.1].max()


                currentQualityString = []
                inQualityString = false;
            }

            else if (inReadString) {
                currentReadString.addAll(line.split('').findAll({it != ''}))
            }

            else if (inContigString) {
                currentContigString.addAll(line.split('').findAll({it != ''}))
            }
            else if (inQualityString) {
                currentQualityString.addAll(line.split(' ').findAll({it != ''}))
            }

        }
        // take care of the final contig in the file
        if (currentContig) {
            currentContig.averageCoverage = currentContig.calculateAverageCoverage()
            currentContig.save()

        }

        a = a.merge()
        a.save()
//
        statisticsService.createContigSetForAssembly(a.id)
        return a

    }



    def runMira(def readsFileIds, def jobId, def compoundSampleId) {

        println "i am running mira!"
        println " ${new Date()} running mira on reads file with id $readsFileIds"

//        update the job to show that it 's running
        BackgroundJob job = BackgroundJob.get(jobId)
        job.progress = "running mira"
        job.status = BackgroundJobStatus.RUNNING
        job.save(flush: true)

        // generate a uuid for the project and create an input file
        String projectName = UUID.randomUUID().toString()
        File procInput = new File("/tmp/${projectName}_in.454.fastq")
        procInput.delete()
        assert procInput.createNewFile()

        // write the reads to the input file
        readsFileIds.each { readsFileId ->
            ReadsFile readsFile = ReadsFile.get(readsFileId)
            byte[] myData = readsFile.data.fileData
            println "reads file is $readsFile"
            println "run of reads file is " + readsFile.run
            println "process input is ${procInput.absolutePath}"
//            println "process data is $myData"
            def readData = readsFile.data.fileData
            procInput.append(readData)
        }

        // construct the mira command line, set the working directory to /tmp, and start the process
        println "starting process - path is ${grailsApplication.config.miraPath}"
        println "${grailsApplication.config.miraPath} --job=denovo,est,draft,454 --project=${projectName} -DI:lrt=/tmp -GE:not=4 454_SETTINGS -LR:lsd=yes:ft=fastq -notraceinfo"
        def p = new ProcessBuilder("${grailsApplication.config.miraPath} --job=denovo,est,draft,454 --project=${projectName} -DI:lrt=/tmp -GE:not=4 454_SETTINGS -LR:lsd=yes:ft=fastq -notraceinfo".split(" "))
        job.commandLine = p.command().join('')
        p.directory(new File("/tmp"))
        p.redirectErrorStream(true)
        p = p.start()

        // monitor stdout of the mira process and update the job to show which pass we are on
        p.in.eachLine({
//            println it
            if (it.contains('Pass')) {
                println it
                job.progress = it
                job.save(flush: true)
            }
        })

        println "done!!"
        File aceFile = new File("/tmp/${projectName}_assembly/${projectName}_d_results/${projectName}_out.ace")

        CompoundSample s = CompoundSample.get(compoundSampleId)

        Assembly a = new Assembly(
                name: 'automatic assembly using mira',
                description: (new File("/tmp/${projectName}_assembly/${projectName}_d_info/${projectName}_info_assembly.txt")).text,
                compoundSample: s
        )
        a.save()
        s.addToAssemblies(a)
        String destination = grailsLinkGenerator.link(controller: 'assembly', action: 'show', id: a.id)

        println "attaching contigs to new assembly, destination is $destination"
        attachContigsFromMiraInfo(new FileInputStream(aceFile), a)

//        update the job to show that we 're finished and set the sink and source ids
        job.progress = 'finished'
        job.status = BackgroundJobStatus.FINISHED
        println "destination is $destination"
        job.destinationUrl = destination

        job.label = 'mira'
        job.save(flush: true)
        println "done with runMira"


    }

    Map parseFasta(InputStream contigsFile) {
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
