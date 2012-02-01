package afterparty

import grails.plugin.springcache.annotations.Cacheable

class StatisticsService {

    static transactional = true

    def grailsApplication

    static paleAssemblyColours = ['LightCyan', 'LightPink', 'LightSkyBlue']
    static boldAssemblyColours = ['Cyan', 'Pink', 'SkyBlue']


    @Cacheable("myCache")
    def getAssemblyStats(Long id) {

        println "getting assembly stats for $id"
        def start = System.currentTimeMillis()
        def criteria = Assembly.createCriteria()
        def a = criteria.get({
            eq('id', id)
            fetchMode 'contigs', org.hibernate.FetchMode.JOIN
//            fetchMode 'contigs.blastHits', org.hibernate.FetchMode.JOIN
            //            fetchMode 'contigs.reads', org.hibernate.FetchMode.JOIN
        })
        println "got raw assembly object : " + (System.currentTimeMillis() - start)

        def cs = new ContigSet(name: 'a', description: 'b', study: a.compoundSample.study)
        a.contigs.each {cs.addToContigs(it)}
        println "created contig set : " + (System.currentTimeMillis() - start)

        cs.save(flush: true)
        println "saved contig set : " + (System.currentTimeMillis() - start)

        def assemblyStats = grailsApplication.mainContext.statisticsService.getContigStatsForContigSet(cs.id)



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

//        Assembly a = Assembly.findById(id, [fetch: [contigs: 'eager']])
        def criteria = ContigSet.createCriteria()
        def cs = criteria.get({
            eq('id', id)
            fetchMode 'contigs', org.hibernate.FetchMode.JOIN
//            fetchMode 'contigs.blastHits', org.hibernate.FetchMode.JOIN
            fetchMode 'contigs.reads', org.hibernate.FetchMode.JOIN
        })
        println "got $cs"
        println "got contigs : " + (System.currentTimeMillis() - start)

//        ArrayList topBlasts = cs.contigs.collect({ contig ->
        //            def hits = contig.blastHits.toArray().sort({-it.bitscore})
        //            if (hits.size() > 0) {
        //                return hits[0]
        //            }
        //            else {
        //                return null
        //            }
        //        })
        //        println "assembled blast hits : " + (System.currentTimeMillis() - start)

        def contigs = cs.contigs
        def result = [
                id: contigs*.id,
                length: contigs*.length(),
                quality: contigs*.averageQuality(),
                coverage: contigs*.averageCoverage(),
                gc: contigs*.gc()
//                topBlast: topBlasts
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

    def getStatsForContigSets(List<ContigSet> contigSetList) {

        def result = []

        // figure out the buckets sizes for histograms
        def overallMaxLength = contigSetList.collect({grailsApplication.mainContext.statisticsService.getContigStatsForContigSet(it.id).length.max()}).max()     // nicely functional
        def overallMaxQuality = contigSetList.collect({grailsApplication.mainContext.statisticsService.getContigStatsForContigSet(it.id).quality.max()}).max()
        def overallMaxCoverage = contigSetList.collect({grailsApplication.mainContext.statisticsService.getContigStatsForContigSet(it.id).coverage.max()}).max()

        contigSetList.eachWithIndex {  ContigSet contigSet, index ->

            def contigSetJSON = [:]

            contigSetJSON.id = contigSet.name
            contigSetJSON.colour = StatisticsService.boldAssemblyColours[index]
            def contigStats = grailsApplication.mainContext.statisticsService.getContigStatsForContigSet(contigSet.id)

            // build a histogram of length
            def lengthX = []
            def lengthY = []
            (0..overallMaxLength / 10).each {
                def floor = it * 10
                def ceiling = (it * 10) + 10
                def count = contigStats.length.findAll({it >= floor && it < ceiling}).size()
                lengthX.add(floor)
                lengthY.add(count)
            }
            contigSetJSON.lengthXvalues = lengthX
            contigSetJSON.lengthYvalues = lengthY
            contigSetJSON.lengthYmax = lengthY.max()

            // build a histogram of quality
            def qualityX = []
            def qualityY = []
            (0..overallMaxQuality).each {
                def floor = it
                def ceiling = it + 1
                def count = contigStats.quality.findAll({it >= floor && it < ceiling}).size()
                qualityX.add(floor)
                qualityY.add(count)
            }
            contigSetJSON.qualityXvalues = qualityX
            contigSetJSON.qualityYvalues = qualityY
            contigSetJSON.qualityYmax = qualityY.max()

            // build a histogram of coverage
            def coverageX = []
            def coverageY = []
            (0..overallMaxCoverage).each {
                def floor = it
                def ceiling = it + 1
                def count = contigStats.coverage.findAll({it >= floor && it < ceiling}).size() + 1
                coverageX.add(floor)
                Float logCount = Math.log10(count)
                coverageY.add(logCount)
            }
            contigSetJSON.coverageXvalues = coverageX
            contigSetJSON.coverageYvalues = coverageY
            contigSetJSON.coverageYmax = 10000

            result.add(contigSetJSON)
        }
        return result

    }

    def createContigSetForAssembly(Long id) {
        def criteria = Assembly.createCriteria()
        def a = criteria.get({
            eq('id', id)
            fetchMode 'contigs', org.hibernate.FetchMode.JOIN
        })

        def cs = new ContigSet(
                name: "$a.name",
                description: "automatically generated contig set for $a.name",
                study: a.compoundSample.study
        )
        a.contigs.each {
            cs.addToContigs(it)
        }
        a.defaultContigSet = cs
        cs.save(flush: true)

    }
}
