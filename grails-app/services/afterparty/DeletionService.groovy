package afterparty

import groovy.sql.Sql

class DeletionService {

    javax.sql.DataSource dataSource

    def deleteAssembly(Long id){
        def sqlConnection = new Sql(dataSource)
        def deleteAnnotationStatement = "delete from annotation using contig where annotation.contig_id = contig.id and contig.assembly_id = ${id}"
        println(deleteAnnotationStatement)
        println("deleting annotations")
        sqlConnection.execute(deleteAnnotationStatement)
        
        def deleteReadsStatement = "delete from read using contig where read.contig_id = contig.id and contig.assembly_id = ${id}"
        println(deleteReadsStatement)
        println("deleting reads")
        sqlConnection.execute(deleteReadsStatement)

        def deleteContigSetsStatement = "delete from contig_set_contig using contig where contig_set_contig.contig_id = contig.id and contig.assembly_id = ${id}"
        println(deleteContigSetsStatement)
        println("deleting contig sets")
        sqlConnection.execute(deleteContigSetsStatement)

        def deleteContigsStatement = "delete from contig where assembly_id=${id}"
        println(deleteContigsStatement)
        println("deleting contigs")
        sqlConnection.execute(deleteContigsStatement)

        def deleteAssemblyStatement = "delete from assembly where id = ${id};"
        println(deleteAssemblyStatement)
        println("deleting assembly")
        sqlConnection.execute(deleteAssemblyStatement)
        return "done"
    }

}
