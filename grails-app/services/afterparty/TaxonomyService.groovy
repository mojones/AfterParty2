package afterparty

import grails.plugin.springcache.annotations.Cacheable
import java.util.regex.Pattern

class TaxonomyService {

    static transactional = true

    def grailsApplication


    @Cacheable('ncbi cache')
    def getParentForName(String name, String rank) {
        def taxid2node = grailsApplication.mainContext.taxonomyService.getTaxid2Node()
        def name2taxid = grailsApplication.mainContext.taxonomyService.getName2Taxid()
        def taxid2Name = grailsApplication.mainContext.taxonomyService.getTaxid2Name()

        def myTaxid = name2taxid.get(name)

        def currentTaxid = myTaxid
        if (currentTaxid) {
            while (currentTaxid != 1) {
//                println "getting node for taxid $currentTaxid"
                def currentNode = taxid2node.get(currentTaxid)
                def currentName = taxid2Name.get(currentTaxid)
//                println "\tname is ${currentName}, rank is ${currentNode.rank}"
                if (currentNode.rank == rank) {
//                    println "found an answer :" + currentName
                    return currentName
                }
                currentTaxid = currentNode.parent
            }
        }
        return currentTaxid
    }

    @Cacheable('ncbi cache')
    def getTaxid2Node() {
        def taxid2node = [:]
        def nodePattern = ~/^(\d+)\t\|\t(\d+)\t\|\t(.+?)\t\|/

        println ""
        def count = 0
        // open the NCBI taxonomy for structure
        new File("/home/martin/Downloads/nodes.dmp").eachLine {
            line ->
            count++

            def matcher = (line =~ nodePattern)
            if (matcher.matches()) {
                Integer myId = matcher[0][1].toInteger()
                Integer parentId = matcher[0][2].toInteger()
                String myRank = matcher[0][3]

                if ((count % 1000) == 0) {
                    print "processing node $count\n"
                }
                taxid2node.put(myId, ['taxid': myId, 'rank': myRank, 'parent': parentId])
            }
        }
        return taxid2node
    }

    @Cacheable('ncbi cache')
    def getName2Taxid() {

        def name2taxid = [:]

        // now process names file to add scientific names to nodes
        Pattern namePattern = ~/^(\d+)\t\|\t(.+)\t\|\t(.*)\t\|\t(.+)\t\|/
        new File("/home/martin/Downloads/names.dmp").eachLine {
            line ->
            def nameMatcher = (line =~ namePattern)


            if (
                nameMatcher.matches() && (
                nameMatcher[0][4].equals("scientific name") || nameMatcher[0][4].equals("synonym") || nameMatcher[0][4].equals("misspelling"))
            ) {
                Integer taxid = nameMatcher[0][1].toInteger()
                String name = nameMatcher[0][2]
                name2taxid.put(name, taxid)
            }
        }
        return name2taxid
    }

    @Cacheable('ncbi cache')
    def getTaxid2Name() {
        def result = [:]
        grailsApplication.mainContext.taxonomyService.getName2Taxid().each {
            result.put(it.value, it.key)
        }
        return result
    }
}