package afterparty

import javax.xml.parsers.SAXParserFactory
import org.xml.sax.InputSource

class ContigSetController {

    def statisticsService
    def searchService
    def springSecurityService
    def blastService

    def miraService

    def index = { }


    def standaloneComparison = {}

    /*
    * TODO
    *
    * squash all individual contig info in scatterplot tooltip
    * add n50 contig length to datapoint label
    * histograms on the side of the scatterplot
    * trend line
    * cumulative curves without N's
    * low coverage filter
    *
    * */

    def uploadContigsStandalone = {

        def contigSetRawResult = []
        def contigSetStatsResult = []

        def assemblies = []

        params.entrySet().findAll({it.key.startsWith('fasta_')}).each { field ->
            println "processing parameter $field.key"

            def f = request.getFile(field.key)
            if (!f.empty) {
                println "got file $f.name"
                assemblies.push(f)
            }

        }

        // find all fasta file parameters
        assemblies.eachWithIndex { f, i ->

            // get contigs
            def contigs = miraService.parseFasta(f.inputStream)
            println "got ${contigs.size()} contigs"


            def cs = [
                    id: [],
                    length: [],
                    quality: [],
                    coverage: [],
                    topBlast: [],
                    gc: []
            ]

            contigs.each { contig ->
//                println "looking at contig $contig"
                def (id, quality, coverage) = contig.key.split(/_/)
                def sequence = contig.value
                cs.id.push(id)
                cs.length.push(sequence.length().toFloat())
                cs.quality.push(quality.toFloat())
                cs.coverage.push(coverage.toFloat())
                cs.topBlast.push(id)
                cs.gc.push(sequence.toLowerCase().findAll({it == 'g' || it == 'c'}).size() / sequence.toLowerCase().findAll({it != 'n'}).size())
            }

            cs.colour = StatisticsService.boldAssemblyColours[i % StatisticsService.boldAssemblyColours.size()]

            cs.label = f.originalFilename
            cs.size = contigs.size()

            int cumulativeLength = 0
            int n50Target = cs.length.sum() / 2
            int n90Target = (cs.length.sum() / 100) * 90
            int numberContigsSeen = 0

            for (contigLength in cs.length.sort().reverse()) {

                cumulativeLength += contigLength

                if (cumulativeLength >= n50Target && !cs.n50Contig) {
                    cs.n50Contig = numberContigsSeen
                    cs.n50Total = cumulativeLength
                    cs.n50length = contigLength
                }

                if (cumulativeLength >= n90Target && !cs.n90Contig) {
                    cs.n90Length = contigLength
                    cs.n90Contig = numberContigsSeen
                    cs.n90Total = cumulativeLength
                }

                if (contigLength <= 2000 && !cs.smallContig) {
                    cs.smallContig = numberContigsSeen
                    cs.smallTotal = cumulativeLength
                }
                numberContigsSeen++
            }
            println "calculated n50"


            contigSetRawResult.add(cs)
        }

        Integer overallMaxLength = contigSetRawResult.collect({it.length.max()}).max()     // nicely functional
        Integer overallMaxQuality = contigSetRawResult.collect({it.quality.max()}).max()
        Integer overallMaxCoverage = contigSetRawResult.collect({it.coverage.max()}).max()

        contigSetRawResult.each { assembly ->

            def statsResult = [:]
            statsResult.id = assembly.label
            statsResult.colour = assembly.colour

            // build a histogram of length and a scaled histogram of length
            statsResult.lengthvalues = []
            statsResult.scaledlengthvalues = []
            Integer logMaxLength = Math.log10(overallMaxLength)
            Integer stepSizeLength = 10 ** (logMaxLength - 2)
            println "length stepSize is $stepSizeLength"
            Integer numberOfStepsLength = (overallMaxLength / stepSizeLength) + 1
            (0..numberOfStepsLength).each {
                def floor = it * stepSizeLength
                def ceiling = (it * stepSizeLength) + stepSizeLength
                def count = assembly.length.findAll({it >= floor && it < ceiling}).size()
                statsResult.lengthvalues.add([floor, count])
                statsResult.scaledlengthvalues.add([floor, (1000 * (count / assembly.length.size())).toInteger()])
                println "counted length for $floor (max is $overallMaxLength)"
            }

            // build a histogram of quality and a scaled histogram of length
            statsResult.qualityvalues = []
            statsResult.scaledqualityvalues = []
            Integer logMaxQuality = Math.log10(overallMaxQuality)
            Integer stepSizeQuality = [10 ** (logMaxQuality - 2), 1].max()
            println "quality stepSize is $stepSizeQuality"
            Integer numberOfStepsQuality = (overallMaxQuality / stepSizeQuality) + 1

            (0..numberOfStepsQuality).each {
                def floor = it * stepSizeQuality
                def ceiling = (it * stepSizeQuality) + stepSizeQuality
                def count = assembly.quality.findAll({it >= floor && it < ceiling}).size()
                statsResult.qualityvalues.add([floor, count])
                statsResult.scaledqualityvalues.add([floor, (1000 * (count / assembly.quality.size())).toInteger()])
                println "counted quality for $floor (max is $overallMaxQuality)"

            }

            // build a histogram of coverage and a scaled histogram of length
            statsResult.coveragevalues = []
            statsResult.scaledcoveragevalues = []
            Integer logMaxCoverage = Math.log10(overallMaxCoverage)
            Integer stepSizeCoverage = [10 ** (logMaxCoverage - 2), 1].max()
            println "coverage stepSize is $stepSizeCoverage"
            Integer numberOfStepsCoverage = (overallMaxCoverage / stepSizeCoverage) + 1
            (0..numberOfStepsCoverage).each {
                def floor = it * stepSizeCoverage
                def ceiling = (it * stepSizeCoverage) + stepSizeCoverage
                def count = assembly.coverage.findAll({it >= floor && it < ceiling}).size()
                statsResult.coveragevalues.add([floor, count])
                statsResult.scaledcoveragevalues.add([floor, (1000 * (count / assembly.coverage.size())).toInteger()])
                println "counted coverage for $floor (max is $overallMaxCoverage)"

            }

            contigSetStatsResult.push(statsResult)
        }




        [
                contigSets: contigSetRawResult,
                contigSetRawDataJSON: contigSetRawResult.encodeAsJSON(),
                contigSetDataJSON: contigSetStatsResult.encodeAsJSON()
        ]
    }

    //todo - use multiple contig sets in a blast search (actually multiple blast searches) - use backgroundjob to track progress

    def blastAgainstSingleContigSet(Long id, String query) {

        println "blasting against single contig set"
        ContigSet cs = ContigSet.get(id)

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
        writer.println(">mySearch\n${query}")
        writer.close()

        def blastResults = []

        def handler = new BlastXmlResultHandler(blastResults: blastResults)
        def reader = SAXParserFactory.newInstance().newSAXParser().XMLReader
        reader.setContentHandler(handler)
        reader.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false)

//        blastProcess.in.eachLine {println it}
        reader.parse(new InputSource(blastProcess.in))

        blastResults.each {
            Contig c = Contig.get(it.contigId)
            it.contigName = c.name
            it.assemblyName = c.assembly.name
            it.compoundSampleName = c.assembly.compoundSample.name
        }

        return blastResults
    }

    def blastAgainstContigSet = {
        println "blasting against contig set ${params.id}"
        println "sequence is ${params.inputSequence}"

        def blastResults = blastAgainstSingleContigSet(params.id.toLong(), params.inputSequence)

        def fullResult = [
                hits: blastResults,
                query: params.inputSequence
        ]

        [
                resultsJSON: fullResult.encodeAsJSON(),
                results: blastResults,
                studyId: ContigSet.get(params.id).study.id
        ]

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

                cumulativeLength += contigLength

                if (cumulativeLength >= n50Target && !cs.n50Contig) {
                    cs.n50Contig = numberContigsSeen
                    cs.n50Total = cumulativeLength
                    cs.n50length = contigLength
                }

                if (cumulativeLength >= n90Target && !cs.n90Contig) {
                    cs.n90Length = contigLength
                    cs.n90Contig = numberContigsSeen
                    cs.n90Total = cumulativeLength
                }

                if (contigLength <= 500 && !cs.smallContig) {
                    cs.smallContig = numberContigsSeen
                    cs.smallTotal = cumulativeLength
                }
                numberContigsSeen++
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
        ContigSet c = new ContigSet(name: params.setName, description: "automatically generated contig set", study: Study.get(params.studyId), type: ContigSetType.USER)
        ids.each {
            c.addToContigs(Contig.get(it.toLong()))
        }
        blastService.attachBlastDatabaseToContigSet(c)
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
