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
def getStudyCounts(){
    def sql = new Sql(dataSource)
    def studyStatement = """
select 
    compound_sample.study_id as study_id, 
    count(distinct contig.id) as contig_count, 
    count(distinct assembly.id) as assembly_count, 
    annotation.type,
    count(distinct annotation.id) 
from 
    annotation, contig, assembly, compound_sample 
where 
    annotation.contig_id = contig.id and 
    contig.assembly_id = assembly.id and 
    assembly.compound_sample_id = compound_sample.id 
group by 
    compound_sample.study_id,annotation.type;
                    """    
    def result = [:]
    sql.rows(studyStatement).each{ row ->
        def currentMap = result.get(row.study_id, [:])
        currentMap.put('contigCount',row.contig_count)
        currentMap.put('assembly_count', row.assembly_count)
        def currentTypeMap = currentMap.get(row.type, [:])
        currentTypeMap.put(row.type, row.count)
        currentMap.put('types', currentTypeMap)
        result.put(row.study_id, currentMap)
    }
    return result
}


@Cacheable("myCache")
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



@Cacheable("myCache")
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
            contig_set_contig.contig_set_contigs_id=${id} and 
            contig_set_contig.contig_id = contig.id            
            """    
    sql.rows(contigStatsStatement).each{ row ->
        result.span = row.sum
        result.min = row.min
        result.max = row.max
        result.mean = row.avg?.toInteger()
    }

    println result
    return result

}

@Cacheable("myCache")
def getReadFileDataStats(Long id) {

    println "getting reads file data for $id"
    ReadsFileData f = ReadsFileData.get(id)

    //        get the data
    String fileString = new String(f.fileData)
    println "length is ${fileString.size()}"
    println "data : ${fileString[0..1000]}"

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

    println "total bases : $totalBases"
    println "read count : $readCount"

    int meanLength = totalBases / readCount
    def result = [
    'readCount': readCount,
    'meanLength': meanLength,
    'baseCount': totalBases,
    'maxLength': maxReadLength,
    'minLength': minReadLength,
    ]
    println result
    return result
}





@Cacheable("contigSetCache")
Map getStatsForContigSet(Long contigSetId) {
    println "getting stats with sql for contig set with id $contigSetId"
    Map cs = [
    id: [],
    length: [],
    lengthwithoutn: [],
    quality: [],
    coverage: [],
    gc: []
    ]

    def sql = new Sql(dataSource)

        def sqlString = """
select 
    id, 
    name, 
    average_coverage,
    average_quality,
    length(sequence) as length,
    length(replace(sequence, 'N', '')) as lengthwithoutn,
    (array_upper(string_to_array(sequence,'G'),1) +array_upper(string_to_array(sequence,'C'),1)) as GC
from 
    contig
where
    id in (
        select contig_id from contig_set_contig where contig_set_contigs_id = ${contigSetId} 
    )
        """

        println sqlString
        sql.rows(sqlString).each({ row ->
          //  println row
            cs.id.push(row.id)
            cs.length.push(row.length)
            cs.lengthwithoutn.push(row.lengthwithoutn)
            cs.quality.push(row.average_quality)
            cs.coverage.push(row.average_coverage)
            cs.gc.push(row.gc / row.length)
          })
    return cs
}


def createContigSetForCompoundSample(Long id) {
    println "creating contig set for compound sample $id"
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
    cs.data = new ContigSetData(blastHeaderFile: 'a', blastIndexFile : 'b', blastSequenceFile : 'c')
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
            assembly?.defaultContigSet?.contigs.each { contig ->
                cs.addToContigs(contig)
            }
        }
    }


    
    def oldDefaultContigSet = s.defaultContigSet
    
    s.defaultContigSet = cs
    cs.data = new ContigSetData(blastHeaderFile: 'a', blastIndexFile : 'b', blastSequenceFile : 'c')

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
        cs.addToContigs(it)
        //println it.id
    }

    if (a.defaultContigSet) {
        def currentDefaultContigSet = a.defaultContigSet
        println "deleting old contig set for assembly with id ${currentDefaultContigSet.id}"
        a.defaultContigSet = null
        a.save(flush:true)
        a.compoundSample.study.removeFromContigSets(currentDefaultContigSet)
        //a.defaultContigSet = null
        currentDefaultContigSet.delete(flush:true)
    }
    cs.data = new ContigSetData(blastHeaderFile: 'a', blastIndexFile : 'b', blastSequenceFile : 'c')
    a.defaultContigSet = cs
    cs.save(flush:true)
    a.save(flush:true)
    cs.save(flush:true)

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







def getFilteredContigCount(Long contigSetId, String query){
    def sql = new Sql(dataSource)
    //TODO this should probably be unique otherwise we get exaggerated counts
    def idStatement = """
        select 
            count(distinct annotation.contig_id) 
        from 
            contig_set_contig, annotation 
        where 
            contig_set_contigs_id=${contigSetId} and 
            contig_set_contig.contig_id=annotation.contig_id and 
            to_tsvector('english', replace(description, '.', ' ') || ' ' || description) @@ to_tsquery('english', ${query})
    """
    print('getFilteredContigCount : ' + idStatement.toString())
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
        to_tsvector('english', replace(description, '.', ' ') || ' ' || description) @@ to_tsquery('english', '${query}') and 
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
            to_tsvector('english', replace(description, '.', ' ') || ' ' || description) @@ to_tsquery('english', ${query}) 
        order by 
            ${Sql.expand(orderBy)} ${Sql.expand(sortDirection)} 
        offset 
            ${offset} 
        limit 
            ${limit}

            """    
    println idStatement.toString()
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
    def assembly_id

    // first get the contig stats
    def contigStatement = "select * from contig where id=${id}"
    sql.rows(contigStatement).each{ row ->
        result.name = row.name
        result.coverage = row.average_coverage.toInteger()
        result.quality = row.average_quality.toInteger()
        result.length = row.sequence.size()
        result.id = id
        result.gc = (row.sequence.toUpperCase().findAll({it == 'G' || it == 'C'}).size() * 100 / result.length).toInteger()
        assembly_id = row.assembly_id
    }    

    // now get the name of the compoundSample
    def speciesNameStatement = "select compound_sample.name from compound_sample, assembly where assembly.id=${assembly_id} and assembly.compound_sample_id = compound_sample.id"
    sql.rows(speciesNameStatement).each{ row ->
        result.species = row.name
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
            to_tsvector('english', replace(description, '.', ' ') || ' ' || description) @@ to_tsquery('english', ${query}) 
        order by 
            evalue desc"""
        }
    
    sql.rows(annotationStatement).each{ row ->
        result.put(row.type + '_desc', row.description + ' (' + row.source + ')')
        result.put(row.type + '_score', row.evalue)
    }

    return result

}

def getInfoForSingleContig(Long id){
    def result = [:]
    def sql = new Sql(dataSource)

    def assembly_id
    // first get the contig stats
    def contigStatement = "select * from contig where id=${id}"
    sql.rows(contigStatement).each{ row ->
        result.name = row.name
        result.coverage = row.average_coverage.toInteger()
        result.quality = row.average_quality.toInteger()
        result.length = row.sequence.size()
        result.id = id
        result.gc = (row.sequence.toUpperCase().findAll({it == 'G' || it == 'C'}).size() * 100 / result.length).toInteger()
        assembly_id = row.assembly_id
    }    

    // now get the name of the compoundSample
    def speciesNameStatement = "select compound_sample.name from compound_sample, assembly where assembly.id=${assembly_id} and assembly.compound_sample_id = compound_sample.id"
    sql.rows(speciesNameStatement).each{ row ->
        result.species = row.name
    }    
    // now the annotation
    def annotationStatement = "select * from annotation where contig_id=${id} order by evalue desc"
    sql.rows(annotationStatement).each{ row ->
        result.put(row.type + '_desc', row.description + ' (' + row.source + ')')
        result.put(row.type + '_score', row.evalue)
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
