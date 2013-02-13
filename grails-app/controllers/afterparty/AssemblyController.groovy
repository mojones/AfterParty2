package afterparty

import grails.plugin.springcache.annotations.CacheFlush
import grails.plugins.springsecurity.Secured
import groovy.sql.Sql

class AssemblyController {


    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

    def blastService
    def statisticsService
    def chartService
    def miraService
    def springSecurityService
    javax.sql.DataSource dataSource
    def pfamService
    def deletionService

    def g = new org.codehaus.groovy.grails.plugins.web.taglib.ApplicationTagLib()

    def sessionFactory
    def propertyInstanceMap = org.codehaus.groovy.grails.plugins.DomainClassGrailsPlugin.PROPERTY_INSTANCE_MAP
    def cleanUpGorm() {
        def session = sessionFactory.currentSession
        session.flush()
        session.clear()
        propertyInstanceMap.get().clear()
    }


    def makeHybridAssembly = {
        def ids = []
        params.idList.split(/,/).sort().each {
            ids.add(it.toLong())
        }
        render "merging assemblies " + ids
    }

    
    @Secured(['ROLE_USER'])
    def deleteAssembly = {
        deletionService.deleteAssembly(params.assemblyId.toLong())
    }

    @Secured(['ROLE_USER'])
    def uploadBlastAnnotation = {
        def f = request.getFile('myFile')

        def assemblyId = params.id

        BackgroundJob job = new BackgroundJob(
                name: "uploading BLAST annotation from ${request.getFile('myFile').getOriginalFilename()}",
                progress: 'running',
                study: Assembly.get(assemblyId).compoundSample.study,
                status: BackgroundJobStatus.QUEUED,
                type: BackgroundJobType.UPLOAD_BLAST_ANNOTATION,
                user: AfterpartyUser.get(springSecurityService.principal.id),
                destinationUrl: g.createLink(controller: 'assembly', action: 'show', params: [id: assemblyId])
                )

        job.save(flush: true)

        //runAsync {
            BackgroundJob job2 = BackgroundJob.get(job.id)
            job2.status = BackgroundJobStatus.RUNNING
            job2.save(flush: true)
            def gzipInputStream = new java.util.zip.GZIPInputStream(f.inputStream) 
            
            blastService.addBlastHitsFromInput(gzipInputStream, job.id, assemblyId)
            println "back in controller, indexing"

            BackgroundJob.withNewSession {
                job2 = BackgroundJob.get(job.id)
                job2.progress = 'finished'
                job2.status = BackgroundJobStatus.FINISHED
                job2.save()
            }

        //}

        redirect(controller: 'backgroundJob', action: 'list')
    } 

    @Secured(['ROLE_USER'])
    def uploadInterproscanAnnotation = {
        def f = request.getFile('myFile')

        def assemblyId = params.id.toLong()

        BackgroundJob job = new BackgroundJob(
                name: "uploading InterProScan annotation from ${request.getFile('myFile').getOriginalFilename()}",
                progress: 'running',
                study: Assembly.get(assemblyId).compoundSample.study,
                status: BackgroundJobStatus.QUEUED,
                type: BackgroundJobType.UPLOAD_BLAST_ANNOTATION,
                user: AfterpartyUser.get(springSecurityService.principal.id),
                destinationUrl: g.createLink(controller: 'assembly', action: 'show', params: [id: assemblyId])
                )

        job.save(flush: true)

        //runAsync {
            BackgroundJob job2 = BackgroundJob.get(job.id)
            job2.status = BackgroundJobStatus.RUNNING
            job2.save(flush: true)
            
            pfamService.addPfamFromInput(f.inputStream, job.id, assemblyId)
            println "back in controller, indexing"

            BackgroundJob.withNewSession {
                job2 = BackgroundJob.get(job.id)
                job2.progress = 'finished'
                job2.status = BackgroundJobStatus.FINISHED
                job2.save()
            }

       // }

        redirect(controller: 'backgroundJob', action: 'list')
    }

    @CacheFlush("myCache")
    @Secured(['ROLE_USER'])
    def uploadAce = {
        def f = request.getFile('aceFile')


        println "uploading ACE file called ${f.name}"
        def assemblyId = params.id.toInteger()


        println "logged-in user is ${AfterpartyUser.get(springSecurityService.principal.id)}"

        BackgroundJob job = new BackgroundJob(
                name: 'uploading ACE file',
                progress: 'queued',
                user: AfterpartyUser.get(springSecurityService.principal.id),
                status: BackgroundJobStatus.QUEUED,
                type: BackgroundJobType.UPLOAD_CONTIGS,
                destinationUrl: g.createLink(controller: 'assembly', action: 'show', params: [id: assemblyId])
        )
        job.save(flush: true)


        //runAsync {
            BackgroundJob job2 = BackgroundJob.get(job.id)
            job2.status = BackgroundJobStatus.RUNNING
            job2.save(flush: true)

            job2.progress = "deleting old contigs"
            job2.save(flush: true)

            Assembly assembly = Assembly.get(assemblyId)
            def oldDefaultContigSetId = assembly.defaultContigSet?.id
            assembly.defaultContigSet = null
            assembly.save(flush: true)


            println "deleting individual contigs"

            def sqlAfterparty = new Sql(dataSource)

            sqlAfterparty.execute("delete from annotation using contig where annotation.contig_id = contig.id and contig.assembly_id = $assemblyId")
            sqlAfterparty.execute("delete from read using contig where read.contig_id = contig.id and contig.assembly_id = $assemblyId")
            sqlAfterparty.execute("delete from contig_set_contig using contig where contig_set_contig.contig_id = contig.id and contig.assembly_id = $assemblyId")
            sqlAfterparty.execute("delete from contig where contig.assembly_id = $assemblyId")
            if (oldDefaultContigSetId){
                println "nuking old contig set"
                sqlAfterparty.execute("delete from contig_set where id = $oldDefaultContigSetId")
            }

            println "done deleting individual contigs"

            miraService.attachContigsFromMiraInfo(f.inputStream, assembly, job2.id)

            assembly.save(flush: true)
            job2.progress = "creating contig set"
            job2.save(flush: true)

            statisticsService.createContigSetForAssembly(assembly.id)



            job2.progress = 'finished'
            job2.status = BackgroundJobStatus.FINISHED
        //}

        redirect(controller: 'backgroundJob', action: 'list')

    }

    @CacheFlush("myCache")
    @Secured(['ROLE_USER'])
    def uploadContigs = {
        def f = request.getFile('myFile')


        println "uploading file of contigs called ${f.name}"
        def assemblyId = params.id.toInteger()



        BackgroundJob job = new BackgroundJob(
                name: 'uploading contigs',
                progress: 'queued',
                study: Assembly.get(assemblyId).compoundSample.study,
                status: BackgroundJobStatus.QUEUED,
                type: BackgroundJobType.UPLOAD_CONTIGS,
                user: AfterpartyUser.get(springSecurityService.principal.id),
                destinationUrl: g.createLink(controller: 'assembly', action: 'show', params: [id: assemblyId])

                )

        job.save(flush: true)

        def contigs = miraService.parseFasta(f.inputStream)

        //runAsync {
            BackgroundJob job2 = BackgroundJob.get(job.id)
            job2.status = BackgroundJobStatus.RUNNING
            job2.save(flush: true)

            job2.progress = "deleting old contigs"
            job2.save(flush: true)


            Assembly assembly = Assembly.get(assemblyId)
            assembly.defaultContigSet?.delete()
            assembly.defaultContigSet = null
            assembly.save(flush: true)

            println "deleting individual contigs"

            def contigSets = assembly.compoundSample.study.contigSets

            def assemblyContigs = assembly.contigs.toArray()

            assemblyContigs.each { contig ->
                println "deleting $contig"
                assembly.removeFromContigs(contig)
                contigSets.each { set ->
                    if (set.contigs.contains(contig)) {
                        set.removeFromContigs(contig)
                        set.save()
                    }
                }
                contig.delete()
            }
            println "done deleting individual contigs"
            assembly.save(flush: true)
            println "re-saved assembly"


            println "got some contigs: ${contigs.size()}"

            def created = 0
            contigs.each { name, seq ->

                def contig = new Contig(name: name, sequence: seq)
                contig.quality = '0 ' * seq.length()
                contig.averageCoverage = 1
                contig.averageQuality = 1
                assembly.addToContigs(contig)
                created++
                if (created % 1000 == 0) {

                    println "uploaded $created of ${contigs.size()}"
                    job2.progress = "uploaded $created of ${contigs.size()}"
                    job2.save(flush: true)
                    cleanUpGorm()
                }


            }
            assembly.save(flush: true)
            job2.progress = "creating contig set"
            job2.save(flush: true)

            statisticsService.createContigSetForAssembly(assembly.id)



            job2.progress = 'finished'
            job2.status = BackgroundJobStatus.FINISHED
        //}

        redirect(controller: 'backgroundJob', action: 'list')

    }




    @Secured(['ROLE_USER'])
    def runBlast = {
        def assemblyId = params.id
        println "id is $assemblyId"
        println "url is ${g.createLink(controller: 'assembly', action: 'show', params: [id: assemblyId])}"

        BackgroundJob job = new BackgroundJob(
                name: "Running BLAST on ${assemblyId}",
                progress: 'queued',
                status: BackgroundJobStatus.QUEUED,
                type: BackgroundJobType.BLAST,
                user: AfterpartyUser.get(springSecurityService.principal.id),
                destinationUrl: g.createLink(controller: 'assembly', action: 'show', params: [id: assemblyId])

        )
        job.save(flush: true)

        runAsync {
            blastService.runBlast(assemblyId, job.id)
        }

        redirect(controller: 'backgroundJob', action: 'list')

    }

    @Secured(['ROLE_USER'])
    def runPfam = {
        def assemblyId = params.id
        println "id is $assemblyId"

        BackgroundJob job = new BackgroundJob(
                name: "Running InterProScan on ${assemblyId}",
                progress: 'queued',
                status: BackgroundJobStatus.QUEUED,
                type: BackgroundJobType.BLAST,
                user: AfterpartyUser.get(springSecurityService.principal.id),
                destinationUrl: g.createLink(controller: 'assembly', action: 'show', params: [id: assemblyId])
                
        )
        job.save(flush: true)

        //runAsync {
            pfamService.runPfam(assemblyId, job.id)
        //}

        redirect(controller: 'backgroundJob', action: 'list')

    }



    def scatterplotAjax = {

        def start = System.currentTimeMillis()
        println "getting graph in controller for ${params.assemblyId} ${params.x} vs ${params.y} "
        def image = chartService.getScatterplot(params.assemblyId.toLong(), params.x, params.y, params.cutoff.toInteger(), params.colour)
        println "generated chart in " + (System.currentTimeMillis() - start)

        response.setHeader('Content-length', image.length.toString())
        response.contentType = 'image/png' // or the appropriate image content type
        response.outputStream << image
        response.outputStream.flush()
    }

    def histogramAjax = {

        def image = chartService.getHistogram(params.assemblyId.toLong(), params.x, params.scale)

        response.setHeader('Content-length', image.length.toString())
        response.contentType = 'image/png' // or the appropriate image content type
        response.outputStream << image
        response.outputStream.flush()
    }

    @Secured(['ROLE_USER'])
    def create = {
        println("creating new assembly")
        def assemblyInstance = new Assembly(name: 'Assembly name', description: 'Assembly description')
        Study.get(params.studyId.toLong()).addToAssemblies(assemblyInstance)
        assemblyInstance.save(flush:true)
        println("creating default contigset for ${assemblyInstance.id}")
        redirect(action: show, id: assemblyInstance.id)

    }

    def show = {
        def criteria = Assembly.createCriteria()
        def start = System.currentTimeMillis()

        def a = criteria.get({
            eq('id', params.id.toLong())
        })
        println("contig set is ${a.defaultContigSet}")
        def readSources = ['any']
        def stats
        if (a.defaultContigSet){
            readSources.addAll(statisticsService.getReadSourcesForContigSetId(a.defaultContigSet.id))
            stats = statisticsService.getContigSetStats(a.defaultContigSet.id)
        }

        

        println "fetched : ${System.currentTimeMillis() - start}"
        println "sorted : ${System.currentTimeMillis() - start}"
        def userId = springSecurityService.isLoggedIn() ? springSecurityService?.principal?.id : 'none'
        [assemblyInstance: a, readSources:readSources, stats: stats, isOwner: a.compoundSample.study.user.id == userId]
    }


}
