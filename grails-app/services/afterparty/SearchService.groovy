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
    def searchInContigSet(ContigSet set, String query, Integer max) {

//        enter raw sql territory
        def sql = new Sql(dataSource)
        def t = new Timer()
        def result = []
        sql.rows("""
        select distinct contig.id from contig
        inner join contig_set_contig on contig.id = contig_set_contig.contig_id
        inner join annotation on contig.id=annotation.contig_id
        where
        contig_set_contig.contig_set_contigs_id=$set.id and
        (to_tsvector('english', annotation.description) @@ to_tsquery('english', $query) or annotation.accession = $query)
        limit $max
        """).each {
            result.add(Contig.get(it.id))
        }
        t.log("got list of ids")

        println "got $result.size results"
        t.log("made all results")
        return result
    }

}
