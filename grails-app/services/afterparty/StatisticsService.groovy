package afterparty

import grails.plugin.springcache.annotations.Cacheable
import groovy.sql.Sql


class StatisticsService {

    static transactional = false

    javax.sql.DataSource dataSource


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
            //            fetchMode 'contigs.annotations', org.hibernate.FetchMode.JOIN
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
            //            fetchMode 'contigs.annotations', org.hibernate.FetchMode.JOIN
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
        it.annotations.size() > 0 ? it.annotations.toArray()[0].description : 'no blast hit'
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
        fetchMode 'contigs.annotations', org.hibernate.FetchMode.JOIN
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

    getContigInfoForContigSet(contigSetId).each{
        cs.id.push(it.id)
        cs.length.push(it.length)
        cs.lengthwithoutn.push(it.lengthWithoutN)
        cs.quality.push(it.quality)
        cs.coverage.push(it.coverage)
        cs.topBlast.push('replaceme')
        cs.gc.push(it.gc)

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
        println "adding contigs from assembly $assembly"
        if (assembly.defaultContigSet != null) {
            assembly.defaultContigSet.contigs.each { contig ->
                println "adding contig $contig"
                cs.addToContigs(contig)
            }
        }
    }

    if (c.defaultContigSet) {
        println "deleting old contig set for compound sample"
        def currentDefaultContigSet = c.defaultContigSet

        def existingStudyContigsSets = []
        existingStudyContigsSets += c.study.contigSets
        existingStudyContigsSets.each{
            if (it.id == currentDefaultContigSet.id){
                c.study.removeFromContigSets(it)
                println "\t\tdeleting it!"
            }
        }

        c.defaultContigSet = null
        currentDefaultContigSet.delete(flush:true)
    }
    c.defaultContigSet = cs
    cs.data = new ContigSetData()
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


    
    def oldDefaultContigSet = s.defaultContigSet
    
    s.defaultContigSet = cs
    cs.data = new ContigSetData()

    blastService.attachBlastDatabaseToContigSet(cs)
    cs.save(flush: true)

    if (oldDefaultContigSet){

        println "deleting old contig set for study with id ${oldDefaultContigSet.id}"
        def existingStudyContigsSets = []
        existingStudyContigsSets += s.contigSets
        
        existingStudyContigsSets.each{
            println "\tone contig set is ${it.id}"
            if (it.id == oldDefaultContigSet.id){
                s.removeFromContigSets(it)
                println "\t\tdeleting it!"
            }
        }

    // this does not work!!!! why??
    s.removeFromContigSets(oldDefaultContigSet)

    oldDefaultContigSet.delete(flush:true)
    }
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
//            println "adding ${count++} / ${a.contigs.size()}"
cs.addToContigs(it)
}

if (a.defaultContigSet) {
    def currentDefaultContigSet = a.defaultContigSet
    println "deleting old contig set for assembly with id ${currentDefaultContigSet.id}"
    a.compoundSample.study.removeFromContigSets(currentDefaultContigSet)
    currentDefaultContigSet.delete(flush:true)
}
cs.data = new ContigSetData()
blastService.attachBlastDatabaseToContigSet(cs)
a.defaultContigSet = cs
cs.save(flush:true)
a.save(flush:true)


cs.save(flush:true)

        // now update the compound sample that owns this assembly
        createContigSetForCompoundSample(a.compoundSample.id)

    }

    @Cacheable('contigSetContigCountCache')
    def countContigsForContigSet(Long id){
      return ContigSet.get(id).contigs.size()
  }

  @Cacheable('contigSetCache')
  ContigSet getContigSet(Long id){
    return ContigSet.get(id)
}

@Cacheable('contigInfoCache')
def getContigInfoForContigSet(Long id){
    println "id is $id"
    def result = []

    def sql = new Sql(dataSource)

        // scary sql ahead
        // we need to pull out the annotation for a given contig, then transpose some rows to columns

        def sqlString = """
            select 
            id, 
            name,
            MAX(CASE WHEN type = 'BLAST' THEN description ELSE NULL END) AS top_blast, 
            MAX(CASE WHEN type = 'BLAST' THEN bitscore ELSE NULL END) AS blast_bitscore,
            MAX(CASE WHEN type = 'PFAM' THEN description ELSE NULL END) AS top_pfam,
            MAX(CASE WHEN type = 'PFAM' THEN bitscore ELSE NULL END) AS pfam_bitscore,
            average_coverage,
            average_quality,
            sequence 
        from (
            select 
                distinct on (annotation.type, contig.id) 
                contig.id, contig.name, contig.average_coverage, contig.average_quality, contig.sequence, annotation.type, annotation.description, annotation.bitscore
            from 
                contig, annotation, contig_set_contig 
            where annotation.contig_id=contig.id and (annotation.type='BLAST' or annotation.type='PFAM') and contig_set_contig.contig_id=contig.id and contig_set_contig.contig_set_contigs_id = ${id}
            order by annotation.type, contig.id, bitscore desc
        ) as bar 
        group by id, name, average_coverage, average_quality, sequence;
   

        """

        println sqlString
        sql.rows(sqlString).each({ row ->
          //  println row
          result.add(
            [
            'id' : row.id, 
            'name': row.name,
            'coverage' : row.average_coverage,
            'quality':row.average_quality,
            'length' : row.sequence.length(),
            'lengthWithoutN' : row.sequence.toUpperCase().replaceAll('n', '').length(),
            'gc' : row.sequence.toUpperCase().findAll({it == 'G' || it == 'C'}).size() / row.sequence.length(),
            'topBlast' : row.top_blast,
            'blastBitscore' : row.blast_bitscore,
            'topPfam' : row.top_pfam,
            'pfamBitscore' : row.pfam_bitscore
            ]
                   )
          })
        
    

    return result


}
@Cacheable('contigInfoCache')
def getContigInfoForContigList(def ids){

    def sql = new Sql(dataSource)
    println "sql is $sql"
    def result = []
    // TODO make this one sql call rather than an each{}
    ids.each{ id ->

        // scary sql ahead
        // we need to pull out the annotation for a given contig, then transpose some rows to columns

        def sqlString = """
            select 
            id, 
            name,
            MAX(CASE WHEN type = 'BLAST' THEN description ELSE NULL END) AS top_blast, 
            MAX(CASE WHEN type = 'BLAST' THEN bitscore ELSE NULL END) AS blast_bitscore,
            MAX(CASE WHEN type = 'PFAM' THEN description ELSE NULL END) AS top_pfam,
            MAX(CASE WHEN type = 'PFAM' THEN bitscore ELSE NULL END) AS pfam_bitscore,
            average_coverage,
            average_quality,
            sequence 
        from (
            select 
                distinct on (annotation.type, contig.id) 
                contig.id, contig.name, contig.average_coverage, contig.average_quality, contig.sequence, annotation.type, annotation.description, annotation.bitscore
            from 
                contig, annotation 
            where annotation.contig_id=contig.id and (annotation.type='BLAST' or annotation.type='PFAM') and contig.id = ${id}
            order by annotation.type, contig.id, bitscore desc
        ) as bar 
        group by id, name, average_coverage, average_quality, sequence;

        """

        println sqlString
        sql.rows(sqlString).each({ row ->
          //  println row
          result.add(
            [
            'id' : row.id, 
            'name': row.name,
            'coverage' : row.average_coverage,
            'quality':row.average_quality,
            'length' : row.sequence.length(),
            'lengthWithoutN' : row.sequence.toUpperCase().replaceAll('n', '').length(),
            'gc' : row.sequence.toUpperCase().findAll({it == 'G' || it == 'C'}).size() / row.sequence.length(),
            'topBlast' : row.top_blast,
            'blastBitscore' : row.blast_bitscore,
            'topPfam' : row.top_pfam,
            'pfamBitscore' : row.pfam_bitscore
            ]
                   )
          })
        
    }
    return result


}

def getFilteredContigInfoForContigList(def ids, def query){

    def sql = new Sql(dataSource)
    println "sql is $sql"
    def result = []
    // TODO make this one sql call rather than an each{}
    ids.each{ id ->

        // scary sql ahead
        // we need to pull out the annotation for a given contig, then transpose some rows to columns

        def sqlString = """
            select 
            id, 
            name,
            MAX(CASE WHEN type = 'BLAST' THEN description ELSE NULL END) AS top_blast, 
            MAX(CASE WHEN type = 'BLAST' THEN bitscore ELSE NULL END) AS blast_bitscore,
            MAX(CASE WHEN type = 'PFAM' THEN description ELSE NULL END) AS top_pfam,
            MAX(CASE WHEN type = 'PFAM' THEN bitscore ELSE NULL END) AS pfam_bitscore,
            average_coverage,
            average_quality,
            sequence 
        from (
            select 
                distinct on (annotation.type, contig.id) 
                contig.id, contig.name, contig.average_coverage, contig.average_quality, contig.sequence, annotation.type, annotation.description, annotation.bitscore
            from 
                contig, annotation 
            where annotation.contig_id=contig.id and (annotation.type='BLAST' or annotation.type='PFAM') and contig.id = ${id} and to_tsvector('english', annotation.description) @@ to_tsquery('english', ${query})
            order by annotation.type, contig.id, bitscore desc
        ) as bar 
        group by id, name, average_coverage, average_quality, sequence;

        """

        println sqlString
        sql.rows(sqlString).each({ row ->
          //  println row
          result.add(
            [
            'id' : row.id, 
            'name': row.name,
            'coverage' : row.average_coverage,
            'quality':row.average_quality,
            'length' : row.sequence.length(),
            'lengthWithoutN' : row.sequence.toUpperCase().replaceAll('n', '').length(),
            'gc' : row.sequence.toUpperCase().findAll({it == 'G' || it == 'C'}).size() / row.sequence.length(),
            'topBlast' : row.top_blast,
            'blastBitscore' : row.blast_bitscore,
            'topPfam' : row.top_pfam,
            'pfamBitscore' : row.pfam_bitscore
            ]
                   )
          })
        
    }
    return result


}

}
