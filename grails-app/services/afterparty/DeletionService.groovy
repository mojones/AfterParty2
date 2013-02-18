package afterparty

import groovy.sql.Sql

class DeletionService {

    javax.sql.DataSource dataSource

    def deleteAssembly(Long id){
        def sql = new Sql(dataSource)
        def deleteAnnotationStatement = "delete from annotation using contig where annotation.contig_id = contig.id and contig.assembly_id = ${id}"
        println(deleteAnnotationStatement)
        println("deleting annotations")
        def deleteContigSetsStatement = "delete from contig_set_contig using contig where contig_set_contig.contig_id = contig.id and contig.assembly_id = ${id}"
        println(deleteContigSetsStatement)
        println("deleting contig sets")
        def deleteContigsStatement = "delete from contig where assembly_id=${id}"
        println(deleteContigsStatement)
        println("deleting contigs")
        def deleteAssemblyStatement = "delete from assembly where id = ${id};"
        println(deleteAssemblyStatement)
        println("deleting assembly")
        return "done"
    }

}
