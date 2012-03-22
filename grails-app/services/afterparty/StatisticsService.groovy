package afterparty

import grails.plugin.springcache.annotations.Cacheable

class StatisticsService {

    static transactional = true

    def grailsApplication
    def blastService

    static paleAssemblyColours = ['LightCyan', 'LightPink', 'LightSkyBlue']
//    public static boldAssemblyColours = ['#00FFFF', '#FFC0CB', '#87CEEB', '#8A2BE2', '#DC143C']
    public static boldAssemblyColours = ['blue', 'red', 'green', 'purple', 'fuchsia', 'grey', 'lime', 'maroon', 'navy', 'olive', 'teal', 'yellow', 'aqua']


    @Cacheable("myCache")
    def getAssemblyStats(Long id) {

        println "getting assembly stats for $id"
        def start = System.currentTimeMillis()

        if (!Assembly.get(id).defaultContigSet) {
            createContigSetForAssembly(id)
        }

        def criteria = Assembly.createCriteria()
        def a = criteria.get({
            eq('id', id)
//            fetchMode 'contigs', org.hibernate.FetchMode.JOIN
            //            fetchMode 'contigs.blastHits', org.hibernate.FetchMode.JOIN
            //            fetchMode 'contigs.reads', org.hibernate.FetchMode.JOIN
        })
        println "got raw assembly object : " + (System.currentTimeMillis() - start)


        println "saved contig set : " + (System.currentTimeMillis() - start)

        def assemblyStats = grailsApplication.mainContext.statisticsService.getContigStatsForContigSet(a.defaultContigSet.id)



        int n50Total = 0
        int n50 = 0
        int n50Target = assemblyStats.length.sum() / 2
        for (contigLength in assemblyStats.length.sort().reverse()) {
            n50Total += contigLength
            if (n50Total > n50Target) {
                println "got n50 for $contigLength with $n50Total"
                n50 = contigLength
                break
            }
        }

        println "calculated n50 : " + (System.currentTimeMillis() - start)


        def result = [
                'readCount': assemblyStats.length.size(),
                'meanLength': assemblyStats.length.sum() / assemblyStats.length.size(),
                'baseCount': assemblyStats.length.sum(),
                'maxLength': assemblyStats.length.max(),
                'minLength': assemblyStats.length.min(),
                'n50': n50
        ]
        println "built return : " + (System.currentTimeMillis() - start)

        return result
    }

//    Method to generate statistics about a file of reads. We will calculate all the
    //    stats at once and return them as a map, ensuring that the results get cached
    @Cacheable("myCache")
    def getReadFileDataStats(Long id) {

        ReadsFileData f = ReadsFileData.get(id)

//        get the data
        String fileString = new String(f.fileData)
        def lines = fileString.split("\n")
        int totalBases = 0
        int readCount = 0
        int maxReadLength = 0
        int minReadLength = 10000000
        for (int i = 1; i < lines.size(); i += 4) {
            readCount++
            int readLength = lines[i].length()
            totalBases += readLength

            if (readLength > maxReadLength) {
                maxReadLength = readLength
            }
            if (readLength < minReadLength) {
                minReadLength = readLength
            }
        }

        int meanLength = totalBases / readCount
        return [
                'readCount': readCount,
                'meanLength': meanLength,
                'baseCount': totalBases,
                'maxLength': maxReadLength,
                'minLength': minReadLength,
        ]
    }



    @Cacheable("myCache")
    def getContigStatsForContigSet(Long id) {
        println "starting getContigStatsForContig"
        def start = System.currentTimeMillis()

        def criteria = ContigSet.createCriteria()
        def cs = criteria.get({
            eq('id', id)
//            fetchMode 'contigs', org.hibernate.FetchMode.JOIN
            //            fetchMode 'contigs.blastHits', org.hibernate.FetchMode.JOIN
        })
        println "got $cs"
        println "got contigs : " + (System.currentTimeMillis() - start)

        def contigs = cs.contigs
        def result = [
                id: contigs*.id,
                length: contigs*.length(),
                quality: contigs*.averageQuality,
                coverage: contigs*.averageCoverage,
                gc: contigs*.gc(),
                topBlast: contigs.collect({
                    it.blastHits.size() > 0 ? it.blastHits.toArray()[0].description : 'no blast hit'
                })
        ]
        println "built return : " + (System.currentTimeMillis() - start)
        return result

    }

    @Cacheable("contigCache")
    def getTagCloudForAssembly(Long id) {
        def criteria = Assembly.createCriteria()
        def a = criteria.get({
            eq('id', id)
            fetchMode 'contigs', org.hibernate.FetchMode.JOIN
            fetchMode 'contigs.blastHits', org.hibernate.FetchMode.JOIN
        })

        def lines = afterparty.Contig.list().blastHits*.description.flatten()

        def word2count = [:]
        lines.collect({it.tokenize()}).flatten().findAll({it.size() > 5}).each {
            word2count.put(it, word2count.containsKey(it) ? word2count.get(it) + 1 : 1)
        }

        return word2count.sort({-it.value})
    }

    @Cacheable("contigSetCache")
    Map getStatsForContigSet(Long contigSetId) {
        Map cs = [
                id: [],
                length: [],
                lengthwithoutn: [],
                quality: [],
                coverage: [],
                topBlast: [],
                gc: []
        ]

        ContigSet.get(contigSetId).contigs.each { contig ->

            def sequence = contig.sequence.toLowerCase()
            cs.id.push(contig.id)
            cs.length.push(sequence.length())
            def lengthWithoutN = sequence.replaceAll('n', '').length()
            cs.lengthwithoutn.push(lengthWithoutN)
            cs.quality.push(contig.averageQuality)
            cs.coverage.push(contig.averageCoverage)
            cs.topBlast.push(contig.topBlastHit)
            cs.gc.push(100 * (sequence.count('g') + sequence.count('c')) / lengthWithoutN)

        }
        return cs

    }

    def createContigSetForCompoundSample(Long id) {

        CompoundSample c = CompoundSample.get(id)

        def cs = new ContigSet(
                name: "all contigs in assemblies for $c.name",
                description: "all contigs in assemblies for $c.name",
                study: c.study,
                type: ContigSetType.COMPOUND_SAMPLE
        )

        c.assemblies.each { assembly ->
            assembly.defaultContigSet.contigs.each { contig ->
                cs.addToContigs(contig)
            }
        }

        if (c.defaultContigSet) {
            println "deleting old contig set"
            def currentDefaultContigSet = c.defaultContigSet
            currentDefaultContigSet.delete()
        }
        c.defaultContigSet = cs
        blastService.attachBlastDatabaseToContigSet(cs)
        cs.save(flush: true)

        createContigSetForStudy(c.study.id)
    }

    def createContigSetForStudy(Long id) {

        Study s = Study.get(id)

        def cs = new ContigSet(
                name: "all contigs in assemblies for study $s.name",
                description: "all contigs in assemblies for $s.name",
                study: s,
                type: ContigSetType.STUDY
        )

        s.compoundSamples.each { compoundSample ->
            compoundSample.assemblies.each { assembly ->
                assembly.defaultContigSet.contigs.each { contig ->
                    cs.addToContigs(contig)
                }
            }
        }


        if (s.defaultContigSet) {
            println "deleting old contig set"
            def currentDefaultContigSet = s.defaultContigSet
            currentDefaultContigSet.delete()
        }
        s.defaultContigSet = cs
        blastService.attachBlastDatabaseToContigSet(cs)
        cs.save(flush: true)
    }

    def createContigSetForAssembly(Long id) {
        println "creating contig set for assembly $id"
        def criteria = Assembly.createCriteria()
        def a = criteria.get({
            eq('id', id)
            fetchMode 'contigs', org.hibernate.FetchMode.JOIN
        })

        def cs = new ContigSet(
                name: "$a.name",
                description: "automatically generated contig set for $a.name",
                study: a.compoundSample.study,
                type: ContigSetType.ASSEMBLY
        )

        Integer count = 0

        a.contigs.each {
            println "adding ${count++} / ${a.contigs.size()}"
            cs.addToContigs(it)
        }

        if (a.defaultContigSet) {
            println "deleting old contig set"
            def currentDefaultContigSet = a.defaultContigSet
            currentDefaultContigSet.delete()
        }
        a.defaultContigSet = cs

        blastService.attachBlastDatabaseToContigSet(cs)
        cs.save()

        // now update the compound sample that owns this assembly
        createContigSetForCompoundSample(a.compoundSample.id)

    }


}
