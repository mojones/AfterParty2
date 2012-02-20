package afterparty

import javax.xml.parsers.SAXParserFactory
import org.xml.sax.InputSource

class ContigSetController {

    def statisticsService
    def searchService
    def springSecurityService

    def index = { }

    def blastAgainstContigSet = {
        println "blasting against contig set ${params.id}"
        println "sequence is ${params.inputSequence}"

        ContigSet cs = ContigSet.get(params.id)

        //write out blast files
        File temporaryBlastDirectory = File.createTempFile('blastDir', '')
        temporaryBlastDirectory.delete()
        temporaryBlastDirectory.mkdir()
        (new File(temporaryBlastDirectory, 'blast.nhr')).append(cs.blastHeaderFile)
        (new File(temporaryBlastDirectory, 'blast.nin')).append(cs.blastIndexFile)
        (new File(temporaryBlastDirectory, 'blast.nsq')).append(cs.blastSequenceFile)

        println "temp blast directory is ${temporaryBlastDirectory.absolutePath}"

        def blastProcess = new ProcessBuilder("/home/martin/Dropbox/downloads/ncbi-blast-2.2.25+/bin/blastn -outfmt 5 -db ${temporaryBlastDirectory.absolutePath}/blast".split(" "))
        blastProcess.redirectErrorStream(true)
        blastProcess = blastProcess.start()


        def writer = new PrintWriter(new BufferedOutputStream(blastProcess.out))
        writer.println(">mySearch\n${params.inputSequence}")
        writer.close()

        def blastResults = []

        def handler = new BlastXmlResultHandler(blastResults: blastResults)
        def reader = SAXParserFactory.newInstance().newSAXParser().XMLReader
        reader.setContentHandler(handler)
        reader.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false)

//        blastProcess.in.eachLine {println it}
        reader.parse(new InputSource(blastProcess.in))

        blastResults.each{
            Contig c = Contig.get(it.contigId)
            it.contigName = c.name
            it.assemblyName = c.assembly.name
            it.compoundSampleName = c.assembly.compoundSample.name
        }

        def fullResult = [
                hits: blastResults,
                query: params.inputSequence
        ]

        [
                resultsJSON: fullResult.encodeAsJSON(),
                results: blastResults
        ]

    }

    def buildBlastDatabase = {
        def contigSetId = params.id
        println "building blast database for $contigSetId"
        ContigSet cs = ContigSet.get(contigSetId)
        File contigsFastaFile = File.createTempFile('contigs', '.fasta')
        println "temporary file is ${contigsFastaFile.absolutePath} ${contigsFastaFile.name}"
        cs.contigs.each {
            contigsFastaFile.append(">" + it.id + "\n" + it.sequence + "\n")
        }
        println "running formatblasdb"

        println("/home/martin/Dropbox/downloads/ncbi-blast-2.2.25+/bin/makeblastdb -in ${contigsFastaFile.absolutePath} -input_type 'fasta' -dbtype 'nucl'".split(" "))
        def blastProcess = new ProcessBuilder("/home/martin/Dropbox/downloads/ncbi-blast-2.2.25+/bin/makeblastdb -in ${contigsFastaFile.absolutePath} -input_type fasta -dbtype nucl".split(" "))
        blastProcess.redirectErrorStream(true)
        blastProcess = blastProcess.start()
        blastProcess.in.eachLine({
            println "blast : $it"
        })

        cs.blastHeaderFile = (new File(contigsFastaFile.absolutePath + '.nhr')).getBytes()
        cs.blastIndexFile = (new File(contigsFastaFile.absolutePath + '.nin')).getBytes()
        cs.blastSequenceFile = (new File(contigsFastaFile.absolutePath + '.nsq')).getBytes()

        cs.save()
        redirect(action: compareContigSets, params : [idList: contigSetId])

    }


    def compareContigSetsFromCheckbox = {

        //which contigsets are we looking at?
        def ids = []
        params.entrySet().findAll({it.key.startsWith('check_')}).each {
            Integer contigSetId = it.key.split(/_/)[1].toInteger()
            ids.add(contigSetId)
        }
        //render(view: 'compareContigSets', model: [contigSets: contigSetListResult])
        redirect(action: compareContigSets, params: ['idList': ids.join(',')])
        return false
    }

    def compareContigSets = {
        def contigSetListResult = []
        params.idList.split(/,/).sort().each {
            contigSetListResult.add(ContigSet.get(it.toLong()))
        }
        def userId = springSecurityService.isLoggedIn() ? springSecurityService?.principal?.id : 'none'

        [contigSets: contigSetListResult, isOwner: contigSetListResult[0].study.user.id == userId]
    }

    def createFromSearch = {
        List assemblies = []

        //which assemblies are we looking at?
        println "query is ${params.q}"

        def contigs = searchService.getContigsForSearch(params.q, 0, 100000000).contigs

        ContigSet cs = new ContigSet(name: params.q, description: "automatically generated contig set from query ${params.q}", study: Study.get(params.studyId))
        contigs.each {

            println "adding contig $it"
            cs.addToContigs(it)
        }
        cs.save()
        redirect(action: compareContigSets, params: [idList: [cs.id]])

    }

    def showContigSetsJSON = {
        def contigSetListResult = []
        params.idList.split(/,/).sort().eachWithIndex { id, i ->
            def cs = statisticsService.getContigStatsForContigSet(id.toLong())
            cs.colour = statisticsService.boldAssemblyColours[i]

            cs.contigSetId = id
            cs.label = ContigSet.get(id).name

            int cumulativeLength = 0
            int n50Target = cs.length.sum() / 2
            int n90Target = (cs.length.sum() / 100) * 90
            int numberContigsSeen = 0

            for (contigLength in cs.length.sort().reverse()) {
                numberContigsSeen++
                cumulativeLength += contigLength

                if (cumulativeLength > n50Target && !cs.n50Contig) {
                    cs.n50Contig = numberContigsSeen
                    cs.n50Total = cumulativeLength
                    cs.n50length = contigLength
                }

                if (cumulativeLength > n90Target && !cs.n90Contig) {
                    cs.n90Length = contigLength
                    cs.n90Contig = numberContigsSeen
                    cs.n90Total = cumulativeLength
                }

                if (contigLength < 500 && !cs.smallContig) {
                    cs.smallContig = numberContigsSeen
                    cs.smallTotal = cumulativeLength
                }
            }


            contigSetListResult.add(cs)
        }

        render(contentType: "text/json") {
            contigSetList = contigSetListResult
        }

    }

    def createContigSetAJAX = {
        println "creating contigset with $params.setName"
        Set ids = []
        params.idList.split(/,/).sort().each {
            ids.add(it)
        }
        ContigSet c = new ContigSet(name: params.setName, description: "automatically generated contig set from chart", study: Study.get(params.studyId))
        ids.each {c.addToContigs(Contig.get(it.toLong()))}
        c.save()
        println "rendering $c.id"
        render(c.id)
    }


    def showContigSetsStatsJSON = {


        def contigSetListResult = []
        params.idList.split(/,/).each {
            contigSetListResult.add(ContigSet.get(it.toLong()))
        }

        println "contigSets are " + contigSetListResult

        def contigSets = statisticsService.getStatsForContigSets(contigSetListResult)




        def drawQualityBoolean = false
        if (contigSets[0].qualityvalues.size() > 1) {
            drawQualityBoolean = true
        }
        def drawCoverageBoolean = false
        if (contigSets[0].coveragevalues.size() > 1) {
            drawCoverageBoolean = true
        }

        render(contentType: "text/json") {
            contigSetList = contigSets
            lengthYmax = contigSets*.lengthYmax.max()
            scaledLengthYmax = contigSets*.scaledLengthYmax.max()
            qualityYmax = contigSets*.qualityYmax.max()
            scaledQualityYmax = contigSets*.scaledQualityYmax.max()
            coverageYmax = contigSets*.coverageYmax.max()
            scaledCoverageYmax = contigSets*.scaledCoverageYmax.max()
            drawQuality = drawQualityBoolean
            drawCoverage = drawCoverageBoolean
        }
    }

}
