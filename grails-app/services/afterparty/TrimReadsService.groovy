package afterparty

class TrimReadsService {

    static transactional = false

    def executorService
    def grailsLinkGenerator


    def trimReads(def runId, def jobId) {

        runAsync {
            def run = Run.get(runId)
            def readsFileId = run.rawReadsFile.id
            BackgroundJob job = BackgroundJob.get(jobId)
            job.progress = "parsing reads file"
            job.status = BackgroundJobStatus.RUNNING
            job.save(flush: true)

            ReadsFile rawReadsFile = ReadsFile.get(readsFileId)
            println "reads file is $rawReadsFile"
            def readData = rawReadsFile.data.fileData
            def adaptersData = run.experiment.adapters.data


            File procInput = new File("/tmp/${UUID.randomUUID().toString()}.fastq")
            procInput.delete()
            procInput.append(readData)

            File procAdapters = new File("/tmp/${UUID.randomUUID().toString()}adapters.fasta")
            procAdapters.delete()
            procAdapters.append(adaptersData)

            String command = "fastq-mcf ${procAdapters.absolutePath} ${procInput.absolutePath}"
            job.commandLine = command


            Process p = command.execute()
            println command
            println "Process: ${p}"

            int lines = 0
            StringBuffer file = new StringBuffer()
            p.in.newReader().eachLine { line ->
                lines++
                if (lines % 1000 == 0) {
                    int readsProcessed = lines / 4
//                println "read : $lines"
                    job.progress = "$readsProcessed reads processed"
                    job.save(flush: true)
//                    sleep(100*1000)
                }
                file.append(line + "\n")
            }
            job.progress = 'saving trimmed reads to db'
            job.save()

            ReadsFileData d = new ReadsFileData(fileData: file.toString().getBytes())
            ReadsFile trimmedReadsFile = new ReadsFile(name: "trimmed version of $rawReadsFile.name", data: d, description: p.err.text, status: ReadsFileStatus.TRIMMED, run: run)
            run.trimmedReadsFile = trimmedReadsFile

            run.save(flush: true)
            trimmedReadsFile.save(flush: true)


            job.progress = 'finished'
            job.status = BackgroundJobStatus.FINISHED
            String destination = grailsLinkGenerator.link(controller: 'run', action: 'show', id: runId)
            job.destinationUrl = destination
            job.label = 'fastq-mcf'
            job.save(flush: true)





            return trimmedReadsFile.id
        }
    }
}
