package afterparty

import grails.plugin.springcache.annotations.Cacheable
import groovy.sql.Sql

class SearchService {

    static transactional = true


    javax.sql.DataSource dataSource



    def buildQueryString(List assemblies, String query) {
        StringBuilder queryStringBuilder = new StringBuilder()
        queryStringBuilder.append("${query} AND (")
        queryStringBuilder.append(assemblies.collect({"searchAssemblyId:$it.id"}).join(' OR '))
        queryStringBuilder.append(')')
        println "final query string is " + queryStringBuilder.toString()
        return queryStringBuilder.toString()
    }

    def getContigsForSearch(String query, Integer offset, Integer max) {


        def searchResultContigs = []
        def rawSearchResult = Contig.search(query, [max: max, offset: offset])

        rawSearchResult.results.each {
            println "$it is a result"
            def c = Contig.get(it.id)
            if (c) {
                searchResultContigs.add(Contig.get(it.id))
            }
        }
        println searchResultContigs
        return [contigs: searchResultContigs, rawSearch: rawSearchResult]

    }

    @Cacheable('annotationSearchCache')




    def searchInContigSetAndLibrary(ContigSet set, String query, Integer max, def readSourcesList) {

//        enter raw sql territory
        def sql = new Sql(dataSource)
        def t = new Timer()

        println "size of read source list is ${readSourcesList.size()}"

        String listString
        if (readSourcesList.size() > 1){
            println "turning list into quoted"
            listString = "'" + readSourcesList.join("','") + "'"
        }
        else{
            listString = "'" + readSourcesList[0] + "'"
        }

        def sqlStatement = """
        select distinct annotation.contig_id 
            from annotation, contig_set_contig, read 
            where to_tsvector('english', replace(description, '.', ' ') || ' ' || description) @@ to_tsquery('english', '${query}') 
            and annotation.contig_id = contig_set_contig.contig_id 
            and contig_set_contig.contig_set_contigs_id=${set.id} 
            and read.contig_id = annotation.contig_id and read.source in (${listString})
        """.toString()

        println sqlStatement
        def result = []
        def count = 0
        sql.rows(sqlStatement).each {
            println "got one!!"
            if(count < max){
            result.add(Contig.get(it.contig_id))
            }
            count++
        }
        t.log("got list of ids")

        println "got $result.size results"
        t.log("made all results")
        return result
    }


    def searchInContigSet(ContigSet set, String query, Integer max) {

//        enter raw sql territory
        def sql = new Sql(dataSource)
        def t = new Timer()

        println """
        select distinct annotation.contig_id 
            from annotation, contig_set_contig 
            where to_tsvector('english', annotation.description) @@ to_tsquery('english', ${query}) 
            and annotation.contig_id = contig_set_contig.contig_id 
            and contig_set_contig.contig_set_contigs_id=${set.id}
        """

        def result = []
        def count = 0
        sql.rows("""select distinct annotation.contig_id 
            from annotation, contig_set_contig 
            where to_tsvector('english', annotation.description) @@ to_tsquery('english', ${query}) 
            and annotation.contig_id = contig_set_contig.contig_id 
            and contig_set_contig.contig_set_contigs_id=${set.id}""").each {
            
            if(count < max){
            result.add(Contig.get(it.contig_id))
            }
            count++
        }
        t.log("got list of ids")

        println "got $result.size results"
        t.log("made all results")
        return result
    }

}
