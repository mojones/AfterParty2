package afterparty

import groovy.sql.Sql

def sqlAfterparty = Sql.newInstance("jdbc:postgresql://localhost:5432/afterparty", 'mysuperuser', 'jukur6ai', 'org.postgresql.Driver')


Study n = Study.findByName('Nembase')
contigs = n.compoundSamples*.assemblies*.contigs.flatten()
contigs.eachWithIndex{ contig, i ->
    def averageCoverage = contig.calculateAverageCoverage()
    println "new value is $averageCoverage"
    sqlAfterparty.execute("update contig set average_coverage=$averageCoverage where id = $contig.id")
    println("$i / ${contigs.size()}")
}
println contigs.size()
