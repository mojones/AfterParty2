package afterparty

import javax.xml.parsers.SAXParserFactory
import org.codehaus.groovy.grails.commons.ApplicationHolder
import org.xml.sax.InputSource
import groovy.sql.Sql



class ContigSetController {

    def statisticsService
    def searchService
    def springSecurityService
    def blastService

    def miraService

        def g = new org.codehaus.groovy.grails.plugins.web.taglib.ApplicationTagLib()


    def index = { }

    def makeNameForContig(contig){
        // start with eye icon
        def result = '<i class="icon-eye-open"></i>&nbsp;'
        def linkUrl = createLink(controller: 'contig', action:'show', params:[id: contig.id])
        // the contig name links to the contig page
        result += "<a href=\"${linkUrl}\">${contig.name}</a>"
       
        return result
    }

    def makeAnnotationForContig(contig){
        def annotationLines = []
        if (contig.BLAST_desc){
            annotationLines.add("<span class=\"label label-important\">blast</span>&nbsp;${contig.BLAST_desc} (${String.format('%10.3G', contig.BLAST_score)})")
        }
        if (contig.PFAM_desc){
            annotationLines.add("<span class=\"label label-success\">pfam</span>&nbsp;${contig.PFAM_desc} (${String.format('%10.3G', contig.PFAM_score)})")
        }
        if (contig.HMMPANTHER_desc){
            annotationLines.add("<span class=\"label label-info\">hmmpanther</span>&nbsp;${contig.HMMPANTHER_desc} (${String.format('%10.3G', contig.HMMPANTHER_score)})")
        }
        if (contig.GENE3D_desc){
            annotationLines.add("<span class=\"label\">gene3d</span>&nbsp;${contig.GENE3D_desc} (${String.format('%10.3G', contig.GENE3D_score)})")
        }

        return annotationLines.join('<br/>')
    }

    def getContigsJSON = {

        def contigSetId = params.contigSetId.toLong()
        def start = params.iDisplayStart.toLong()
        def limit = params.iDisplayLength.toLong()

        def sortIndex = params.iSortCol_0.toInteger()
        def sortDirection = params.sSortDir_0
        def sortColumnNames = ['contig.name', 'length(contig.sequence)', 'contig.average_quality', 'contig.average_coverage']

        def searchString = params.sSearch
        println "search is ${searchString}"

        def totalContigCount = statisticsService.getContigCount(contigSetId)
        def filteredContigCount
        def idList
        if (!searchString){
            filteredContigCount = totalContigCount
            idList = statisticsService.getContigIds(contigSetId, start, limit, sortColumnNames[sortIndex], sortDirection)
        }
        else{
            filteredContigCount = statisticsService.getFilteredContigCount(contigSetId, searchString)
            idList = statisticsService.getFilteredContigIds(contigSetId, start, limit, sortColumnNames[sortIndex], sortDirection, searchString)
        }

        // get the data
        //def idList = [341873]
        def dataArray = []
        idList.each{
            def oneContig
            if (!searchString){
                oneContig = statisticsService.getInfoForSingleContig(it.toLong())
            }
            else{
                oneContig = statisticsService.getFilteredInfoForSingleContig(it.toLong(), searchString)
            }
            dataArray.add([
                            makeNameForContig(oneContig),
                            oneContig.length,
                            oneContig.quality,
                            oneContig.coverage,
                            oneContig.gc,
                            makeAnnotationForContig(oneContig)
                        ])
        }

        def result = [
            sEcho : params.sEcho,
            iTotalRecords : totalContigCount,
            iTotalDisplayRecords : filteredContigCount,
            aaData: dataArray
            ]
        render result.encodeAsJSON()
    }


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

        def templateString = g.render(
            template: 'uploadContigsStandalone', model: [
                contigSets: contigSetRawResult,
                contigSetRawDataJSON: contigSetRawResult.encodeAsJSON(),
                fileName: output.name
            ]
        )

        def withLayout = g.applyLayout(name:'standalone', templateString)

        output.append(withLayout)
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
        (new File(temporaryBlastDirectory, 'blast.nhr')).append(cs.data.blastHeaderFile)
        (new File(temporaryBlastDirectory, 'blast.nin')).append(cs.data.blastIndexFile)
        (new File(temporaryBlastDirectory, 'blast.nsq')).append(cs.data.blastSequenceFile)

        println "temp blast directory is ${temporaryBlastDirectory.absolutePath}"

        def blastProcess = new ProcessBuilder("${grailsApplication.config.blastnPath} -outfmt 5 -db ${temporaryBlastDirectory.absolutePath}/blast".split(" "))
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

    def blastAgainstContigSets = {

        println request.getHeader('referer') 

        if (params.blastQuery.toString() == ''){
            println "error, no blast query"
            flash.error = 'Error, need a sequence to BLAST against'
            redirect(url: request.getHeader('referer') )
            return false
        }

        def idList
        if (!params.idList) {
            idList = getIdsFromCheckbox(params)
        }
        else {
            idList = params.idList.split(',')
        }
        println "idlist is $idList"
        def allResults = []
        def studyId = 0
        idList.each {
            studyId = ContigSet.get(it).study.id
            println "\tblasting against contig set ${ContigSet.get(it).name}"
            def blastResults = blastAgainstSingleContigSet(it.toLong(), params.blastQuery)
            println "\tgot ${blastResults.size()} results"
            allResults.addAll(blastResults)
        }

        def fullResult = [
                hits: allResults,
                query: params.blastQuery
        ]

        [
                resultsJSON: fullResult.encodeAsJSON(),
                results: allResults,
                studyId: studyId
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
        def idList
        if (!params.idList) {
            idList = getIdsFromCheckbox(params)
        }
        else {
            idList = params.idList.split(',')
        }
        println "idlist is $idList"
        def contigSetListResult = []
        def readSourcesResult = ['any']
        idList.sort().each {
            println "getting a contig set with id $it"
            contigSetListResult.add(ContigSet.get(it.toLong()))
            readSourcesResult.addAll(statisticsService.getReadSourcesForContigSetId(it.toLong()))
        }

        def userId = springSecurityService.isLoggedIn() ? springSecurityService?.principal?.id : 'none'

        // comment
        [
        contigSets: contigSetListResult, 
        isOwner: contigSetListResult[0].study.user.id == userId,
        readSources : readSourcesResult
        ]
    }

    def createFromContigList = {

        ContigSet cs = new ContigSet(name: params.q, description: "automatically generated contig set from query ${params.q}", study: Study.get(params.studyId), type: ContigSetType.USER)
        params.contigList.split(',').each {
            println "adding contig $it"
            cs.addToContigs(Contig.get(it))
        }
        cs.data = new ContigSetData()
        blastService.attachBlastDatabaseToContigSet(cs)
        cs.save()
        redirect(action: 'compareContigSets', 'params': ['idList': [cs.id]])

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
        c.data = new ContigSetData()
        blastService.attachBlastDatabaseToContigSet(c)
        c.save(flush: true)
        statisticsService.getStatsForContigSet(c)
        println "rendering $c.id"
        render(c.id)
    }

    def download = {

        response.setHeader("Content-disposition", "attachment; filename=contigs.fasta");
        response.flushBuffer()

        def criteria = ContigSet.createCriteria()
        def a = criteria.get({
            eq('id', params.id.toLong())
            fetchMode 'contigs', org.hibernate.FetchMode.JOIN
        })

        println "got ${a.contigs.size()} contigs for download";

        a.contigs.each {
            response.outputStream << ">${it.name}\n${it.sequence}\n"
        }
    }

    def searchContigSets = {

        if (params.searchQuery.toString() == ''){
            println "error, no blast query"
            flash.error = 'Error, need a query to search against'
            redirect(url: request.getHeader('referer') )
            return false
        }

        def idList
        if (!params.idList) {
            idList = getIdsFromCheckbox(params)
        }
        else {
            idList = params.idList.split(',')
        }
        println "idlist is $idList"
        Integer max = params.numberOfResults.toInteger()
        println "max is $max"
        def allContigs = []
        def studyId = 0


        idList.each {
            println "searching in contig set $it"
            ContigSet set = ContigSet.get(it)
            studyId = set.study.id
            def t = new Timer()
            def contigs
            if ('any' in params.readSource || params.readSource == null){
                println "searching all read sources"
                contigs = searchService.searchInContigSet(set, params.searchQuery, max)
            }
            else{
                println "searching only read sources : ${params.readSource}"

                contigs = searchService.searchInContigSetAndLibrary(set, params.searchQuery, max, params.list("readSource"))
            }
            t.log("called search service")
            println "got ${contigs.size()} results for ${params.searchQuery}"
            allContigs.addAll(contigs*.id)
        }

        def contigInfo = statisticsService.getFilteredContigInfoForContigList(allContigs, params.searchQuery)


        println "rendering view...."
        [
                contigs: contigInfo,
                query: params.searchQuery,
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
