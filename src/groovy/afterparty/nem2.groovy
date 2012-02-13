package afterparty

import groovy.sql.Sql

def statisticsService = ctx.getBean("statisticsService")


println "running nem2"
def sqlSpecies = Sql.newInstance("jdbc:postgresql://localhost:5432/species", 'mysuperuser', 'jukur6ai', 'org.postgresql.Driver')
def sqlData = Sql.newInstance("jdbc:postgresql://localhost:5432/nembase4", 'mysuperuser', 'jukur6ai', 'org.postgresql.Driver')

Study.findAllByName('Nembase').each {
    it.delete()
}
Study nembaseStudy = new Study(
        name: 'Nembase',
        description: 'many species imported from nembase',
        published: true,
        user: AfterpartyUser.findByUsername('martin')
).save()

sqlSpecies.rows('select * from species').eachWithIndex {speciesRow, i ->
    if (true) {
        def speciesName = speciesRow.species
        def speciesId = speciesRow.spec_id

        def cs = new CompoundSample(
                name: speciesName
        )
        nembaseStudy.addToCompoundSamples(cs)
        cs.save()

        def a = new Assembly(
                description: "imported assembly from nembase",
                name: "imported assembly for $speciesName"
        )
        cs.addToAssemblies(a)
        a.save()



        println "getting contigs for $speciesName"
        Integer contigsAdded = 0

        sqlData.rows("select distinct(cluster.clus_id), cluster.consensus from cluster where substr(clus_id, 0, 4) = $speciesId").eachWithIndex {clusterRow, i2 ->
            if (true) {
                def clusterId = clusterRow.clus_id
                def consensus = clusterRow.consensus
//                println "\tfound a cluster $clusterId with $consensus"

                def c = new Contig()
                c.name = clusterId
                c.sequence = consensus
                c.quality = '0 ' * consensus.length()
                c.searchAssemblyId = a.id

                a.addToContigs(c)

                sqlData.rows("select * from est, est_seq, lib where est.clus_id=$clusterId and est.est_id = est_seq.est_id and est.library=lib.lib_id").eachWithIndex {estRow, i3 ->
                    def estStart = estRow.q_start
                    def estStop = estRow.q_end
                    def estSequence = estRow.sequence
                    def estLibrary = estRow.lib_id
                    def estName = estRow.est_id
//                    println "\t\tfound an est from $estStart to $estStop with $estSequence"

                    def r = new Read()
                    r.name = estName
                    r.sequence = estSequence
                    r.start = estStart
                    r.stop = estStop
                    c.addToReads(r)
                }

                sqlData.rows("select * from blast where clus_id=$clusterId").eachWithIndex {blastRow, i4 ->
                    def blastAcc = blastRow.id
                    String blastDescription = blastRow.description
                    def blastBitscore = blastRow.score
                    def blastStart = blastRow.b_start
                    def blastStop = blastRow.b_end
//                    println "\t\tfound a blast hit from $blastStart to $blastStop with $blastDescription"

                    def b = new BlastHit()

                    b.accession = blastAcc
                    b.description = blastDescription.length() > 999 ? blastDescription[0..980] : blastDescription
                    b.bitscore = blastBitscore
                    b.start = blastStart
                    b.stop = blastStop
                    c.addToBlastHits(b)
                }

                if (++contigsAdded % 100 == 0) {
                    println "\tadded $contigsAdded"
                }
            }

        }

        statisticsService.createContigSetForAssembly(a.id)
        nembaseStudy.save(flush: true)
//        sqlData.rows("select * from lib where organism=$speciesName").eachWithIndex {libraryRow, j ->
        //            if (j == 1) {
        //                def sampleName = libraryRow.name
        //                def sampleDescription = libraryRow.description
        //                def libraryId = libraryRow.lib_id
        //                println "\tfound a library: $sampleDescription"
        //
        //            }
        //        }
    }

}



