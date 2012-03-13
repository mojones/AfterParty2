import afterparty.*

class BootStrap {

    def ExecutorService
    def miraService
    def trimReadsService
    def statisticsService
    def blastService
    def taxonomyService

    def sessionFactory
    def dataSource

    def springSecurityService

    def init = { servletContext ->


        executorService.executor.setMaximumPoolSize(6)
        executorService.executor.setCorePoolSize(6)

        // remove stale background jobs
        BackgroundJob.findAllByStatus(BackgroundJobStatus.RUNNING).each {it.delete(flush: true)}
        BackgroundJob.findAllByStatus(BackgroundJobStatus.QUEUED).each {it.delete(flush: true)}


        environments {
            development_rebuild {

                // add roles and user
                def userRole = afterparty.AfterPartyRole.findByAuthority('ROLE_USER') ?: new AfterPartyRole(authority: 'ROLE_USER').save(failOnError: true)
                def adminRole = AfterPartyRole.findByAuthority('ROLE_ADMIN') ?: new AfterPartyRole(authority: 'ROLE_ADMIN').save(failOnError: true)

                def adminUser = AfterpartyUser.findByUsername('admin') ?: new AfterpartyUser(
                        username: 'admin',
                        password: springSecurityService.encodePassword('admin'),
                        enabled: true).save(failOnError: true)

                if (!adminUser.authorities.contains(adminRole)) {
                    AfterpartyUserAfterPartyRole.create adminUser, adminRole
                }

                def normalUser = AfterpartyUser.findByUsername('martin') ?: new AfterpartyUser(
                        username: 'martin',
                        password: springSecurityService.encodePassword('martin'),
                        enabled: true).save(failOnError: true)

                if (!normalUser.authorities.contains(userRole)) {
                    AfterpartyUserAfterPartyRole.create normalUser, userRole
                }



                Study litoStudy = new Study(
                        name: ' 454 Sequencing of Litomosoides sigmodontis transcriptome from 3 lifestages',
                        description: 'cDNA from three life-stages of the filarial nematode Litomosoides sigmodontis were obtained and sequenced using 454 FLX and Titanium chemistries. These reads are being used to generate a protein set that will be used for annotating the litomsoides sigmodontis genome, and to act as a reference transcriptome for further transcriptome studies using short read RNA-seq.',
                        published: true,
                        user: normalUser
                )
                litoStudy.save()

                CompoundSample lSig = new CompoundSample(
                        name: 'Litomosoides sigmodontis'
                )
                litoStudy.addToCompoundSamples(lSig)

                Sample femaleSample = new Sample(name: 'Adult female transcriptome')
                Sample maleSample = new Sample(name: 'Adult mail transcriptome')
                Sample microfilariaSample = new Sample(name: 'Microfilaria transcriptome')
                lSig.addToSamples(femaleSample)
                lSig.addToSamples(maleSample)
                lSig.addToSamples(microfilariaSample)


                Experiment femaleExperiment = new Experiment(name: '454 GS FLX sequencing', description: 'Total RNA from Litomosoides sigmodontis adult female was converted to double stranded cDNA using Evrogen?s MINT cDNA synthesis kit. First strand cDNA was synthesised using reverse transcriptase (RT) from a 3?-primer with oligo(dT) sequence that annealed to the poly-A stretch of RNA and synthesised cDNA until the 5? end of the mRNA. Finally, double stranded cDNA synthesis was performed using PCR amplification, and the final product contained the same MINT adapter sequence at both 3? and 5? ends. The cDNA was fragmented, size-selected, library-prepped, and sequenced according to standard Roche-454 FLX and Titanium protocols.')
                femaleSample.addToExperiments(femaleExperiment)
                Experiment maleExperiment = new Experiment(name: '454 GS FLX sequencing', description: 'Total RNA from Litomosoides sigmodontis adult male was converted to double stranded cDNA using Evrogen?s MINT cDNA synthesis kit. First strand cDNA was synthesised using reverse transcriptase (RT) from a 3?-primer with oligo(dT) sequence that annealed to the poly-A stretch of RNA and synthesised cDNA until the 5? end of the mRNA. Finally, double stranded cDNA synthesis was performed using PCR amplification, and the final product contained the same MINT adapter sequence at both 3? and 5? ends. The cDNA was fragmented, size-selected, library-prepped, and sequenced according to standard Roche-454 FLX and Titanium protocols.')
                maleSample.addToExperiments(maleExperiment)
                Experiment microfilariaExperiment = new Experiment(name: '454 GS FLX sequencing', description: 'Microfilaria transcriptome')
                microfilariaSample.addToExperiments(microfilariaExperiment)

                //small dataset for testing
                ReadsFileData smallRawData = new ReadsFileData(fileData: (new File('/home/martin/Downloads/afterPartydata/mothData/moth_in.454.fastq')).getBytes())



                Run femaleRun = new Run(name: '454 GS FLX sequencing (16-APR-2009)')
                femaleExperiment.addToRuns(femaleRun)
                ReadsFileData femaleRawData = new ReadsFileData(fileData: (new File('/home/martin/Downloads/afterPartydata/litoData/female.fastq')).getBytes())
                ReadsFile femaleReads = new ReadsFile(name: 'female run 1', data: smallRawData, status: ReadsFileStatus.RAW)
//                ReadsFile femaleReads = new ReadsFile(name: 'female run 1', data: femaleRawData, status: ReadsFileStatus.RAW)
                femaleRun.rawReadsFile = femaleReads

                Run maleRun = new Run(name: '454 GS FLX sequencing (21-APR-2009)')
                maleExperiment.addToRuns(maleRun)
                ReadsFileData maleRawData = new ReadsFileData(fileData: (new File('/home/martin/Downloads/afterPartydata/litoData/male.fastq')).getBytes())
                ReadsFile maleReads = new ReadsFile(name: 'male run 1', data: smallRawData, status: ReadsFileStatus.RAW)
//                ReadsFile maleReads = new ReadsFile(name: 'male run 1', data: maleRawData, status: ReadsFileStatus.RAW)
                maleRun.rawReadsFile = maleReads

                Run microfilariaRunOne = new Run(name: '454 GS FLX Titanium sequencing (08-DEC-2009)')
                microfilariaExperiment.addToRuns(microfilariaRunOne)
                ReadsFileData microfilariaRawDataOne = new ReadsFileData(fileData: (new File('/home/martin/Downloads/afterPartydata/litoData/mf1.fastq')).getBytes())
                ReadsFile microfilariaReadsOne = new ReadsFile(name: 'microfilaria run 1', data: smallRawData, status: ReadsFileStatus.RAW)
//                ReadsFile microfilariaReadsOne = new ReadsFile(name: 'microfilaria run 1', data: microfilariaRawDataOne, status: ReadsFileStatus.RAW)
                microfilariaRunOne.rawReadsFile = microfilariaReadsOne

                Run microfilariaRunTwo = new Run(name: '454 GS FLX Titanium sequencing (21-DEC-2009)')
                microfilariaExperiment.addToRuns(microfilariaRunTwo)
                ReadsFileData microfilariaRawDataTwo = new ReadsFileData(fileData: (new File('/home/martin/Downloads/afterPartydata/litoData/mf2.fastq')).getBytes())
                ReadsFile microfilariaReadsTwo = new ReadsFile(name: 'microfilaria run 2', data: smallRawData, status: ReadsFileStatus.RAW)
//                ReadsFile microfilariaReadsTwo = new ReadsFile(name: 'microfilaria run 2', data: microfilariaRawDataTwo, status: ReadsFileStatus.RAW)
                microfilariaRunTwo.rawReadsFile = microfilariaReadsTwo

                Run microfilariaRunThree = new Run(name: '454 GS FLX Titanium sequencing (17-DEC-2009)')
                microfilariaExperiment.addToRuns(microfilariaRunThree)
                ReadsFileData microfilariaRawDataThree = new ReadsFileData(fileData: (new File('/home/martin/Downloads/afterPartydata/litoData/mf3.fastq')).getBytes())
                ReadsFile microfilariaReadsThree = new ReadsFile(name: 'microfilaria run 3', data: smallRawData, status: ReadsFileStatus.RAW)
//                ReadsFile microfilariaReadsThree = new ReadsFile(name: 'microfilaria run 3', data: microfilariaRawDataThree, status: ReadsFileStatus.RAW)
                microfilariaRunThree.rawReadsFile = microfilariaReadsThree

                AdaptersFile maleLitoAdapters = new AdaptersFile(name: 'lito MINT adapters', data: (new File('/home/martin/Downloads/afterPartydata/adapters.fasta')).getBytes())
                AdaptersFile femaleLitoAdapters = new AdaptersFile(name: 'lito MINT adapters', data: (new File('/home/martin/Downloads/afterPartydata/adapters.fasta')).getBytes())
                AdaptersFile microfilariaLitoAdapters = new AdaptersFile(name: 'lito MINT adapters', data: (new File('/home/martin/Downloads/afterPartydata/adapters.fasta')).getBytes())
                maleExperiment.adapters = maleLitoAdapters
                femaleExperiment.adapters = femaleLitoAdapters
                microfilariaExperiment.adapters = microfilariaLitoAdapters

                [maleExperiment, femaleExperiment, microfilariaExperiment].each { experiment ->
                    experiment.runs.each { run ->
                        println "\ttrimming reads file for run $run.name"
                        def job = new BackgroundJob(
                                name: "trimming FASTQ file for ${run.name}",
                                progress: 'queued',
                                status: afterparty.BackgroundJobStatus.QUEUED,
                                type: afterparty.BackgroundJobType.TRIM,
                                user: normalUser
                        )
                        job.save(flush: true)
                        sessionFactory.getCurrentSession().flush()
                        trimReadsService.trimReads(run.id, job.id)
                    }
                }

                println "uploading assembly"

                def a = new Assembly(
                        name: 'assembly generated from mira ace file',
                        description: (new File('/home/martin/Downloads/afterPartydata/smallData/smallAssembly_assembly/smallAssembly_d_info/smallAssembly_info_assembly.txt')).text
                )
                lSig.addToAssemblies(a)

                miraService.attachContigsFromMiraInfo(
                        new FileInputStream(new File('/home/martin/Downloads/afterPartydata/litoData/lito_assembly/lito_d_results/lito_out.ace')),
//                        new FileInputStream(new File('/home/martin/Downloads/afterPartydata/smallData/smallAssembly_assembly/smallAssembly_d_results/smallAssembly_out.ace')),
                        a
                )

                statisticsService.createContigSetForAssembly(a.id)

                println "adding blast annotation"

                File blastFile = new File('/home/martin/Downloads/afterPartydata/litoData/lito_assembly/lito_d_results/lito_out_blast.xml')
                InputStream blastInput = new FileInputStream(blastFile)
                def job = new BackgroundJob(
                        name: "bootstrapping blast data",
                        progress: 'queued',
                        status: afterparty.BackgroundJobStatus.QUEUED,
                        type: afterparty.BackgroundJobType.UPLOAD_BLAST_ANNOTATION,
                        user: normalUser
                )
                job.save(flush: true)
                sessionFactory.getCurrentSession().flush()
                blastService.addBlastHitsFromInput(blastInput, job.id, a.id)

                //                BackgroundJob j = new BackgroundJob(
                //                        progress: '',
                //                        status: BackgroundJobStatus.RUNNING,
                //                        type: BackgroundJobType.ASSEMBLE,
                //                        name: 'bootstrapping male read assembly',
                //                        study: litoStudy
                //                )
                //                j.save()
                //                sessionFactory.getCurrentSession().flush()
                //                def severalReadsFiles = [
                //                        ReadsFile.findByName('trimmed version of male run 1').id,
                //                                                ReadsFile.findByName('trimmed version of female run 1').id,
                //                                                ReadsFile.findByName('trimmed version of microfilaria run 1').id,
                //                                                ReadsFile.findByName('trimmed version of microfilaria run 2').id,
                //                        ReadsFile.findByName('trimmed version of microfilaria run 3').id
                //                ]
                //                def assemblyId = miraService.runMira(severalReadsFiles, j.id)

                //                BackgroundJob b = new BackgroundJob(
                //                        progress: '',
                //                        status: BackgroundJobStatus.RUNNING,
                //                        type: BackgroundJobType.BLAST,
                //                        name: 'BLASTing assembly',
                //                        study: litoStudy
                //                )
                //                b.save()
                //                sessionFactory.getCurrentSession().flush()
                //                blastService.runBlast(assemblyId, b.id)


                //                new Study(name: 'study number two').save()
                //                new Study(name: 'study code gamma').save()


            }

            big_test {
                Study s = new Study(name: 'test study', description: 'testing')
                s.save()
                println "creating assembly"
                Assembly a = new Assembly(description: 'test assembly desc', name: "assembly from test")
                s.addToAssemblies(a)
                println "creating contigs"
                def start = System.currentTimeMillis()
                (0..100).each {
                    Contig c = new Contig(name: "contig $it", sequence: 'atgc' * 10)
                    a.addToContigs(c)

                }
                sessionFactory.getCurrentSession().flush()
                println System.currentTimeMillis() - start
            }
        }


    }
    def destroy = {
    }

}
