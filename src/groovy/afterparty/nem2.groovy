package afterparty

import groovy.sql.Sql

def statisticsService = ctx.getBean("statisticsService")


println "running nem2"
def sqlSpecies = Sql.newInstance("jdbc:postgresql://localhost:5432/species", 'mysuperuser', 'jukur6ai', 'org.postgresql.Driver')
def sqlData = Sql.newInstance("jdbc:postgresql://localhost:5432/nembase4", 'mysuperuser', 'jukur6ai', 'org.postgresql.Driver')
def sqlAfterparty = Sql.newInstance("jdbc:postgresql://localhost:5432/afterparty", 'mysuperuser', 'jukur6ai', 'org.postgresql.Driver')

println "deleting old study"
println "deleting blast hits"
sqlAfterparty.execute("delete from annotation using contig, assembly, compound_sample, study where annotation.contig_id = contig.id and contig.assembly_id = assembly.id and assembly.compound_sample_id = compound_sample.id and compound_sample.study_id = study.id and study.name='Nembase'")
println "deleting reads"
sqlAfterparty.execute("delete from read using contig, assembly, compound_sample, study where read.contig_id = contig.id and contig.assembly_id = assembly.id and assembly.compound_sample_id = compound_sample.id and compound_sample.study_id = study.id and study.name='Nembase'")
println "deleting contigsets"
sqlAfterparty.execute("delete from contig_set_contig using contig, assembly, compound_sample, study where contig_set_contig.contig_id = contig.id and contig.assembly_id = assembly.id and assembly.compound_sample_id = compound_sample.id and compound_sample.study_id = study.id and study.name='Nembase'")
println "deleting contigs"
sqlAfterparty.execute("delete from contig using assembly, compound_sample, study where contig.assembly_id = assembly.id and assembly.compound_sample_id = compound_sample.id and compound_sample.study_id = study.id and study.name='Nembase'")
println "deleting everything else"
Study.findByName('Nembase')?.delete(flush: true)




println "deleted old study"

Study nembaseStudy = new Study(
        name: 'Nembase',
        description: 'many species imported from nembase',
        published: true,
        user: AfterpartyUser.findByUsername('martin')
).save()

sqlSpecies.rows('select * from species').eachWithIndex {speciesRow, i ->
    if (true) {
//    if ( i < 10) {
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

        def libraryId2sample = [:]

        sqlData.rows("select * from lib where organism=$speciesName").eachWithIndex {libraryRow, j ->
            if (true) {
                def sampleName = libraryRow.name
                def sampleDescription = libraryRow.description
                def libraryId = libraryRow.lib_id
                def s = new Sample()
                s.name = sampleName
                s.description = sampleDescription
                cs.addToSamples(s)
                libraryId2sample.put(libraryId, s)
                println "\tfound a library $sampleDescription"
            }
        }


        println "getting contigs for $speciesName"
        Integer contigsAdded = 0

        sqlData.rows("select cluster.clus_id, cluster.contig, cluster.consensus from cluster where substr(clus_id, 0, 4) = $speciesId").eachWithIndex {clusterRow, i2 ->
            if (true) {

                def clusterId = clusterRow.clus_id
                def contigId = clusterRow.contig
                def consensus = clusterRow.consensus
//                println "\tfound a cluster $clusterId with $consensus"

                def c = new Contig()
                c.name = clusterId + '_' + contigId
                c.sequence = consensus
                c.quality = '0 ' * consensus.length()
                c.averageQuality = 20

                a.addToContigs(c)

                sqlData.rows("select * from est, est_seq, lib where est.clus_id=$clusterId and est.contig=$contigId and est.est_id = est_seq.est_id and est.library=lib.lib_id").eachWithIndex {estRow, i3 ->
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
                    r.source = libraryId2sample.get(estLibrary)?.name ?: 'no name'
                    c.addToReads(r)
                }

                c.averageCoverage = c.calculateAverageCoverage()

                sqlData.rows("select * from blast where clus_id=$clusterId and contig=$contigId").eachWithIndex {blastRow, i4 ->
                    def blastAcc = blastRow.id
                    String blastDescription = blastRow.description
                    def blastBitscore = blastRow.score
                    def blastStart = blastRow.b_start
                    def blastStop = blastRow.b_end
                    def evalue = blastRow.eval
//                    println "\t\tfound a blast hit from $blastStart to $blastStop with $blastDescription"

                    def b = new Annotation()

                    b.accession = blastAcc
                    b.description = blastDescription.length() > 999 ? blastDescription[0..980] : blastDescription
                    b.bitscore = blastBitscore
                    b.start = blastStart
                    b.stop = blastStop
                    b.type = AnnotationType.BLAST
                    b.evalue = evalue
                    c.addToAnnotations(b)
                }

                println "cluster id is $clusterId and contig id is $contigId"
                sqlData.rows("select * from p4e_ind where clus_id=$clusterId and contig=$contigId").eachWithIndex {p4eRow, i5 ->
                    def peptideId = p4eRow.pept_id
                    println "peptide id is $peptideId"
                }

                if (++contigsAdded % 100 == 0) {
                    println "\tadded $contigsAdded"
                }
            }

        }
        a.save(flush:true)
        statisticsService.createContigSetForAssembly(a.id)
        nembaseStudy.save(flush: true)

    }

}



