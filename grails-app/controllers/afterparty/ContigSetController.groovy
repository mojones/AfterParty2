package afterparty

import javax.xml.parsers.SAXParserFactory
import org.codehaus.groovy.grails.commons.ApplicationHolder
import org.xml.sax.InputSource

class ContigSetController {

    def statisticsService
    def searchService
    def springSecurityService
    def blastService

    def miraService

    def index = { }


    def standaloneComparison = {}

    def uploadContigsStandalone = {

        def contigSetRawResult = []

        def assemblies = []

        params.entrySet().findAll({it.key.startsWith('fasta_')}).each { field ->
            println "processing parameter $field.key"

            def f = request.getFile(field.key)
            if (!f.empty) {
                println "got file $f.name"
                assemblies.push(f)
            }

        }

        def start = System.currentTimeMillis()
        // find all fasta file parameters
        assemblies.eachWithIndex { f, i ->
            // get contigs
            def contigs = miraService.parseFasta(f.inputStream)
            println "got ${contigs.size()} contigs in ${System.currentTimeMillis() - start}"


            def cs = [
                    id: [],
                    length: [],
                    lengthwithoutn: [],
                    quality: [],
                    coverage: [],
                    topBlast: [],
                    gc: []
            ]


            contigs.each { contig ->
//                println "looking at contig $contig"
                def (id, quality, coverage) = contig.key.split('_')
                if (coverage.toFloat() > 0) {
                    def sequence = contig.value.toLowerCase()
                    cs.id.push(id)
                    cs.length.push(sequence.length())
                    def lengthWithoutN = sequence.replaceAll('n', '').length()
                    cs.lengthwithoutn.push(lengthWithoutN)
                    cs.quality.push(quality.toFloat())
                    cs.coverage.push(coverage.toFloat())
                    cs.topBlast.push(id)
                    cs.gc.push(100 * (sequence.count('g') + sequence.count('c')) / lengthWithoutN)
                }
            }

            println "built map in ${System.currentTimeMillis() - start}"

            cs.colour = StatisticsService.boldAssemblyColours[i % StatisticsService.boldAssemblyColours.size()]

            cs.label = f.originalFilename
            cs.size = contigs.size()
            contigSetRawResult.add(cs)
        }



        File tempFileDirectory = ApplicationHolder.application.parentContext.getResource("standalonePages").file
//        File tempFileDirectory = new File((String) g.resource(dir: 'standalonePages', absolute: 'true'))
        println "temp directory is $tempFileDirectory.absolutePath"
        File output = File.createTempFile('standalone', '.html', tempFileDirectory)
        println "output file is $output.absolutePath"

        def templateString = g.render(template: 'uploadContigsStandalone', model: [
                contigSets: contigSetRawResult,
                contigSetRawDataJSON: contigSetRawResult.encodeAsJSON(),
                fileName: output.name
        ])


        output.append(templateString)
        redirect(uri: "/standalonePages/$output.name")
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

        ContigSet cs = new ContigSet(name: params.q, description: "automatically generated contig set from query ${params.q}", study: Study.get(params.studyId), type: ContigSetType.USER)
        params.contigList.split(',').each {
            println "adding contig $it"
            cs.addToContigs(Contig.get(it))
        }
        blastService.attachBlastDatabaseToContigSet(cs)
        cs.save()
        redirect(action: compareContigSets, params: [idList: [cs.id]])

    }

    def showContigSetsJSON = {

        def contigSetListResult = []
        params.idList.split(/,/).sort().eachWithIndex { id, i ->
            ContigSet set = ContigSet.get(id.toLong())

            def start = System.currentTimeMillis()

            Map cs = statisticsService.getStatsForContigSet(set.id.toLong())

            println "built map in ${System.currentTimeMillis() - start}"

            cs.colour = StatisticsService.boldAssemblyColours[i % StatisticsService.boldAssemblyColours.size()]

            cs.label = set.name
            cs.size = set.contigs.size()
            contigSetListResult.add(cs)
        }




        render(contentType: "text/json") {
            contigSetListResult
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
        c.save(flush: true)
        statisticsService.getStatsForContigSet(c)
        println "rendering $c.id"
        render(c.id)
    }



    def searchContigSets = {
        def idList = getIdsFromCheckbox(params)
        Integer offset = params.offset.toInteger() ?: 0
        def allContigs = []
        def studyId = 0
        idList.each {
            println "searching in contig set $it"
            ContigSet set = ContigSet.get(it)
            studyId = set.study.id
            def contigs = searchService.searchInContigSet(set, params.searchQuery)
            println "got ${contigs.size()} results for ${params.searchQuery}"
            allContigs.addAll(contigs)
        }
        [
                contigs: allContigs,
                query: params.searchQuery,
                offset: offset,
                max: [allContigs.size(), offset + 100.toInteger()].min(),
                studyId: studyId
        ]
    }


    def getIdsFromCheckbox(params) {
        //which contigsets are we looking at?
        def ids = []
        params.entrySet().findAll({it.key.startsWith('check_')}).each {
            Integer contigSetId = it.key.split(/_/)[1].toInteger()
            ids.add(contigSetId)
        }
        return ids
    }
}
