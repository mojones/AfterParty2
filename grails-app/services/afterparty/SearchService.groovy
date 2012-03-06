package afterparty

import grails.plugin.springcache.annotations.Cacheable

class SearchService {

    static transactional = true

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
    def searchInContigSet(ContigSet set, String query) {
        def idList = set.contigs*.id

        def c = Contig.createCriteria()
        def result = c.listDistinct() {
            blastHits {
                order('bitscore')
                ilike('description', "%$query%")
            }
            inList('id', idList)
        }
        println "got $result.size() results"
        return result
    }

}
