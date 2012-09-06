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


def getFastaForContigSet(Long id){
    def sql = new Sql(dataSource)
    def allContigsStatement = """
        select id, sequence from contig, contig_set_contig  where contig_set_contig.contig_set_contigs_id=${id} and contig_set_contig.contig_id = contig.id
            """    
    def result = new StringBuilder()
    sql.rows(allContigsStatement).each{ row ->
        result.append(">${row.id}\n${row.sequence}\n")
    }
    return result.toString()
}


def getContigSetStats(Long id){
    def result = [:]
    def sql = new Sql(dataSource)
    
    // get contig count
    def contigCountStatement = """
        select count(*) from contig, contig_set_contig  where contig_set_contig.contig_set_contigs_id=${id} and contig_set_contig.contig_id = contig.id
            """    
    sql.rows(contigCountStatement).each{ row ->
        result.count = row.count
    }

    // get contig stats
    def contigStatsStatement = """
        select 
            sum(length(contig.sequence)), 
            min(length(contig.sequence)), 
            max(length(contig.sequence)), 
            avg(length(contig.sequence)) 
        from 
            contig, contig_set_contig  
        where 
            contig_set_contig.contig_set_contigs_id=1016695 and 
            contig_set_contig.contig_id = contig.id            
            """    
    sql.rows(contigStatsStatement).each{ row ->
        result.span = row.sum
        result.min = row.min
        result.max = row.max
        result.mean = row.avg.toInteger()
    }

    println result
    return result

}

@Cacheable("myCache")
def getAssemblyStats(Long id) {

   // println "getting assembly stats for $id"
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
   // println "got raw assembly object : " + (System.currentTimeMillis() - start)


   // println "saved contig set : " + (System.currentTimeMillis() - start)

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
    a.defaultContigSet = null
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

        // TODO we can probably simplify this a lot as we don't strictly need all the complicated annotation when drawing scatter plots
        def sqlString = """
            select 
            id, 
            name,
            MAX(CASE WHEN type = 'BLAST' THEN description ELSE NULL END) AS top_blast, 
            MAX(CASE WHEN type = 'BLAST' THEN bitscore ELSE NULL END) AS blast_bitscore,
            MAX(CASE WHEN type = 'PFAM' THEN description ELSE NULL END) AS top_pfam,
            MAX(CASE WHEN type = 'PFAM' THEN bitscore ELSE NULL END) AS pfam_bitscore,
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
            where annotation.contig_id=contig.id and contig_set_contig.contig_id=contig.id and contig_set_contig.contig_set_contigs_id = ${id}
            order by annotation.type, contig.id, bitscore desc
        ) as bar 
        group by id, name, average_coverage, average_quality, sequence;
   

        """

        //println sqlString
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




def getFilteredContigCount(Long contigSetId, String query){
    def sql = new Sql(dataSource)
    def idStatement = "select count(contig_id) from contig_set_contig where contig_set_contigs_id=${contigSetId}"
    def result    
    sql.rows(idStatement).each{ row ->
        result = row.count
    }
    return result
}
def getContigCount(Long contigSetId){
    def sql = new Sql(dataSource)
    def idStatement = "select count(contig_id) from contig_set_contig where contig_set_contigs_id=${contigSetId}"
    def result    
    sql.rows(idStatement).each{ row ->
        result = row.count
    }
    return result
}

def getFilteredContigIdsByLibrary(Long contigSetId, Integer limit, String query, def readSourcesList){
    def sql = new Sql(dataSource)
    
    String listString

    if (readSourcesList.size() > 1){
        println "turning list into quoted"
        listString = "'" + readSourcesList.join("','") + "'"
    }
    else{
        listString = "'" + readSourcesList[0] + "'"
    }

    def idStatement = """
    select 
        distinct annotation.contig_id 
    from 
        annotation, contig_set_contig, read 
    where
        annotation.contig_id=contig_set_contig.contig_id and 
        contig_set_contig.contig_set_contigs_id=${contigSetId} and 
        to_tsvector('english', annotation.description) @@ to_tsquery('english', '${query}') and 
        read.contig_id=annotation.contig_id and 
        read.source in (${listString})
    limit
        ${limit}
    """.toString()
    println idStatement
    def result = []
    sql.rows(idStatement).each{ row ->
        println row.contig_id
        result.add(row.contig_id)
    }
    return result
}



def getFilteredContigIds(Long contigSetId, Long offset, Long limit, String orderBy, String sortDirection, String query){
    def sql = new Sql(dataSource)
    def idStatement = """
        select distinct on (contig.id, ${Sql.expand(orderBy)}) 
            contig.id 
        from 
            contig_set_contig, contig, annotation 
        where 
            contig.id = contig_set_contig.contig_id and 
            contig_set_contig.contig_set_contigs_id=${contigSetId} and 
            annotation.contig_id = contig.id and 
            to_tsvector('english', annotation.description) @@ to_tsquery('english', ${query}) 
        order by 
            ${Sql.expand(orderBy)} ${Sql.expand(sortDirection)} 
        offset 
            ${offset} 
        limit 
            ${limit}

            """    
    println idStatement
    def result = []
    sql.rows(idStatement).each{ row ->
        result.add(row.id)
    }

    def exactNameMatchStatement = """
        select
            contig.id 
        from 
            contig_set_contig, contig 
        where 
            contig.id = contig_set_contig.contig_id and 
            contig_set_contig.contig_set_contigs_id=${contigSetId} and
            contig.name=${query}
        order by 
            ${Sql.expand(orderBy)} ${Sql.expand(sortDirection)} 
        offset 
            ${offset} 
        limit 
            ${limit}

            """    
    println exactNameMatchStatement
    sql.rows(exactNameMatchStatement).each{ row ->
        result.add(row.id)
    }
    return result
}

def getContigIds(Long contigSetId, Long offset, Long limit, String orderBy, String sortDirection){
    def sql = new Sql(dataSource)
    def idStatement = """
        select 
            contig_id 
        from 
            contig_set_contig, contig 
        where 
            contig.id = contig_set_contig.contig_id and 
            contig_set_contig.contig_set_contigs_id=${contigSetId} 
        order by 
             ${Sql.expand(orderBy)} ${Sql.expand(sortDirection)} 
        offset 
            ${offset} 
        limit 
            ${limit}
            """    
    println idStatement
    def result = []
    sql.rows(idStatement).each{ row ->
        result.add(row.contig_id)
    }
    return result
}

def getFilteredInfoForSingleContig(Long id, String query){
    def result = [:]
    def sql = new Sql(dataSource)

    // first get the contig stats
    def contigStatement = "select * from contig where id=${id}"
    sql.rows(contigStatement).each{ row ->
        result.name = row.name
        result.coverage = row.average_coverage.toInteger()
        result.quality = row.average_quality.toInteger()
        result.length = row.sequence.size()
        result.id = id
        result.gc = (row.sequence.toUpperCase().findAll({it == 'G' || it == 'C'}).size() * 100 / result.length).toInteger()
    }    

    // now the annotation
    // if the contig name is an exact match for the query, then just grab all annotation, otherwise grab the items that match the query

    def annotationStatement
    if (result.name == query){
        annotationStatement = """
        select 
            * 
        from 
            annotation 
        where 
            contig_id=${id}
        order by 
            evalue desc"""
    }
    else{
        annotationStatement = """
        select 
            * 
        from 
            annotation 
        where 
            contig_id=${id} and 
            to_tsvector('english', annotation.description) @@ to_tsquery('english', ${query}) 
        order by 
            evalue desc"""
        }
    
    sql.rows(annotationStatement).each{ row ->
        result.put(row.type + '_desc', row.description)
        result.put(row.type + '_score', row.evalue)
    }

    return result

}

def getInfoForSingleContig(Long id){
    def result = [:]
    def sql = new Sql(dataSource)

    // first get the contig stats
    def contigStatement = "select * from contig where id=${id}"
    sql.rows(contigStatement).each{ row ->
        result.name = row.name
        result.coverage = row.average_coverage.toInteger()
        result.quality = row.average_quality.toInteger()
        result.length = row.sequence.size()
        result.id = id
        result.gc = (row.sequence.toUpperCase().findAll({it == 'G' || it == 'C'}).size() * 100 / result.length).toInteger()
    }    

    // now the annotation
    def annotationStatement = "select * from annotation where contig_id=${id} order by evalue desc"
    sql.rows(annotationStatement).each{ row ->
        result.put(row.type + '_desc', row.description)
        result.put(row.type + '_score', row.evalue)
    }

    return result

}


@Cacheable('contigInfoCache')
def getContigInfoForContigList(def ids){

    def sql = new Sql(dataSource)
    //println "sql is $sql"
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
            where annotation.contig_id=contig.id and (annotation.type='BLAST' or annotation.type='PFAM' or annotation.type='CONTIG') and contig.id = ${id}
            order by annotation.type, contig.id, bitscore desc
        ) as bar 
        group by id, name, average_coverage, average_quality, sequence;

        """

        //println sqlString
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
    //println "sql is $sql"
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
            where annotation.contig_id=contig.id and (annotation.type='BLAST' or annotation.type='PFAM' or annotation.type='CONTIG') and contig.id = ${id} and to_tsvector('english', annotation.description) @@ to_tsquery('english', ${query})
            order by annotation.type, contig.id, bitscore desc
        ) as bar 
        group by id, name, average_coverage, average_quality, sequence;

        """

      //  println sqlString
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

def getReadSourcesForContigSetId(def id){

    def sql = new Sql(dataSource)
   // println "sql is $sql"
    def result = []
   
        def sqlString = """
    select distinct source from read, contig, contig_set_contig where contig_set_contig.contig_set_contigs_id=${id} and contig.id = contig_set_contig.contig_id and read.contig_id = contig.id
        """

       // println sqlString
        sql.rows(sqlString).each({ row ->
          //  println row
          result.add(row.source)
          })
    
    return result
}




}
