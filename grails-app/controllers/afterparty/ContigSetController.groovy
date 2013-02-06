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
            annotationLines.add("<span class=\"label label-important\">blast</span>&nbsp;${contig.BLAST_desc} <span class=\"badge badge-warning\">Evalue : ${String.format('%10.3G', contig.BLAST_score)}</span>")
        }
        if (contig.PFAM_desc){
            annotationLines.add("<span class=\"label label-success\">pfam</span>&nbsp;${contig.PFAM_desc} <span class=\"badge badge-warning\">Evalue : ${String.format('%10.3G', contig.PFAM_score)}</span>")
        }
        if (contig.HMMPANTHER_desc){
            annotationLines.add("<span class=\"label label-info\">hmmpanther</span>&nbsp;${contig.HMMPANTHER_desc} <span class=\"badge badge-warning\">Evalue : ${String.format('%10.3G', contig.HMMPANTHER_score)}</span>")
        }
        if (contig.GENE3D_desc){
            annotationLines.add("<span class=\"label\">gene3d</span>&nbsp;${contig.GENE3D_desc} <span class=\"badge badge-warning\">Evalue : ${String.format('%10.3G', contig.GENE3D_score)}</span>")
        }

        return annotationLines.join('<br/>')
    }

    def makeTableRowArrayForContig(contigId, searchString){
        def oneContig
        if (searchString == ''){
            oneContig = statisticsService.getInfoForSingleContig(contigId.toLong())
        }
        else{
            oneContig = statisticsService.getFilteredInfoForSingleContig(contigId.toLong(), searchString)
        }
        return [
                    makeNameForContig(oneContig),
                    oneContig.length,
                    oneContig.quality,
                    oneContig.coverage,
                    oneContig.gc,
                    makeAnnotationForContig(oneContig)
                ]
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
            
            dataArray.add(makeTableRowArrayForContig(it, searchString))
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



    def blastAgainstSingleContigSet(Long id, String query, String program, String expect) {

        println "blasting against single contig set with expect ${expect}"
        ContigSet cs = ContigSet.get(id)

        File blastDatabaseIndexFile = new File(grailsApplication.config.contigSetDatabasePath + '/' + id + '.nin')
        println "looking for contig set blast database in " + blastDatabaseIndexFile.absolutePath
        if (blastDatabaseIndexFile.exists()){
            // we will assume that if the .nin file is there, then so are the others
            println "file already exists"
        }
        else{
            print "file does not exist, about to create it"
            File contigSetFastaFile = new File(grailsApplication.config.contigSetDatabasePath + '/' + id)

            //write out files
            contigSetFastaFile.append(statisticsService.getFastaForContigSet(id))
            println "wrote ${contigSetFastaFile.absolutePath}"
            println "running formatblasdb"

            def makeBlastDbCommand = "${grailsApplication.config.makeblastdbPath} -in ${contigSetFastaFile.absolutePath} -input_type fasta -dbtype nucl"
            println makeBlastDbCommand
            def makeBlastDbProcess = new ProcessBuilder(makeBlastDbCommand.split(" "))
            makeBlastDbProcess.redirectErrorStream(true)
            makeBlastDbProcess = makeBlastDbProcess.start()
            makeBlastDbProcess.in.eachLine({
                println "makeblastdb : $it"
            })
            makeBlastDbProcess.waitFor()
        }

        def blastCommand
        def databasePath = grailsApplication.config.contigSetDatabasePath + '/' + id
        if (program == 'blastn'){
            blastCommand = "${grailsApplication.config.blastnPath} -outfmt 5 -db ${databasePath} -evalue ${expect}"
        }
        if (program == 'tblastn'){
             blastCommand = "${grailsApplication.config.tblastnPath} -outfmt 5 -db ${databasePath} -evalue${expect}"
        }
        if (program == 'tblastx'){
             blastCommand = "${grailsApplication.config.tblastxPath} -outfmt 5 -db ${databasePath} -evalue ${expect}"

        }

        println blastCommand
        def blastProcess = new ProcessBuilder(blastCommand.split(" "))
        blastProcess.redirectErrorStream(true)
        blastProcess = blastProcess.start()


        def writer = new PrintWriter(new BufferedOutputStream(blastProcess.out))
        writer.println(query)
        writer.close()

       //blastProcess.in.eachLine{
       //     println it
       // }

        def blastResults = []

        def handler = new BlastXmlResultHandler(blastResults: blastResults)
        def reader = SAXParserFactory.newInstance().newSAXParser().XMLReader
        reader.setContentHandler(handler)
        reader.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false)

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
        String expect = '1e-20'
        if (params.expect){
            expect = params.expect
        }
        println "idlist is $idList"
        def allResults = []
        def studyId = 0
        idList.each {
            studyId = ContigSet.get(it).study.id
            println "\tblasting against contig set ${ContigSet.get(it).name}"
            def blastResults = blastAgainstSingleContigSet(it.toLong(), params.blastQuery, params.program, expect)
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
        cs.data = new ContigSetData(blastHeaderFile: 'a', blastIndexFile : 'b', blastSequenceFile : 'c')
        
        cs.save()
        redirect(action: 'compareContigSets', 'params': ['idList': [cs.id]])

    }

    def showContigSetsJSON = {

        def contigSetListResult = []
        params.idList.split(/,/).sort().eachWithIndex { id, i ->
            ContigSet set = ContigSet.get(id.toLong())

            def start = System.currentTimeMillis()

            Map cs = statisticsService.getStatsForContigSet(set.id.toLong())

            println "built map of ${cs.size()} in ${System.currentTimeMillis() - start}"

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
        c.data = new ContigSetData(blastHeaderFile: 'a', blastIndexFile : 'b', blastSequenceFile : 'c')
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

        def studyId = 0
        def contigIdList

        idList.each { id ->
            println "searching in contig set $id"
            Long contigSetId = id.toLong()
            studyId = ContigSet.get(contigSetId).study.id
            if ('any' in params.readSource || params.readSource == null){
                println "searching all read sources"
                contigIdList = statisticsService.getFilteredContigIds(contigSetId, 0, max, 'contig.average_coverage', 'desc', params.searchQuery)
            }
            else{
                println "searching only read sources : ${params.readSource}"
                contigIdList = statisticsService.getFilteredContigIdsByLibrary(contigSetId, max, params.searchQuery, params.list('readSource'))
            }
        }

        def allContigs = []
        contigIdList.each{id ->
            println "getting a table row for $id"
            allContigs.add(makeTableRowArrayForContig(id, params.searchQuery))
        }

        println "rendering view...."
        [
                contigs: allContigs,
                contigIdList : contigIdList,
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
