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
    def dataSource
    def pfamService

    def makeHybridAssembly = {
        def ids = []
        params.idList.split(/,/).sort().each {
            ids.add(it.toLong())
        }
        render "merging assemblies " + ids
    }

    @Secured(['ROLE_USER'])
    def uploadBlastAnnotation = {
        def f = request.getFile('myFile')

        def assemblyId = params.id

        BackgroundJob job = new BackgroundJob(
                name: 'uploading BLAST annotation',
                progress: 'running',
                study: Assembly.get(assemblyId).compoundSample.study,
                status: BackgroundJobStatus.QUEUED,
                type: BackgroundJobType.UPLOAD_BLAST_ANNOTATION)
        job.save(flush: true)

        runAsync {
            BackgroundJob job2 = BackgroundJob.get(job.id)
            job2.status = BackgroundJobStatus.RUNNING
            job2.save(flush: true)
            blastService.addBlastHitsFromInput(f.inputStream, job.id, assemblyId)
            println "back in controller, indexing"

            BackgroundJob.withNewSession {
                job2 = BackgroundJob.get(job.id)
                job2.progress = 'finished'
                job2.status = BackgroundJobStatus.FINISHED
                job2.save()
            }

        }

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
                destinationUrl: g.createLink(controllerName: 'assembly', actionName: 'show', params: [id: assemblyId])
        )
        job.save(flush: true)


        runAsync {
            BackgroundJob job2 = BackgroundJob.get(job.id)
            job2.status = BackgroundJobStatus.RUNNING
            job2.save(flush: true)

            job2.progress = "deleting old contigs"
            job2.save(flush: true)

            Assembly assembly = Assembly.get(assemblyId)
            assembly.defaultContigSet = null
            assembly.save(flush: true)


            println "deleting individual contigs"

            def sqlAfterparty = Sql.newInstance("jdbc:postgresql://localhost:5432/afterparty", 'mysuperuser', 'jukur6ai', 'org.postgresql.Driver')
            sqlAfterparty.execute("delete from blast_hit using contig where blast_hit.contig_id = contig.id and contig.assembly_id = $assemblyId")
            sqlAfterparty.execute("delete from read using contig where read.contig_id = contig.id and contig.assembly_id = $assemblyId")
            sqlAfterparty.execute("delete from contig_set_contig using contig where contig_set_contig.contig_id = contig.id and contig.assembly_id = $assemblyId")
            sqlAfterparty.execute("delete from contig where contig.assembly_id = $assemblyId")

            println "done deleting individual contigs"

            miraService.attachContigsFromMiraInfo(f.inputStream, assembly)

            assembly.save(flush: true)
            job2.progress = "creating contig set"
            job2.save(flush: true)

            statisticsService.createContigSetForAssembly(assembly.id)



            job2.progress = 'finished'
            job2.status = BackgroundJobStatus.FINISHED
        }

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
                type: BackgroundJobType.UPLOAD_CONTIGS)
        job.save(flush: true)


        runAsync {
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
//                    println "\t checking contigset $set"
                    if (set.contigs.contains(contig)) {
//                        println "\t\tremoving $contig from $set"
                        set.removeFromContigs(contig)
                        set.save()
                    }
                }
                contig.delete()
            }
            println "done deleting individual contigs"
            assembly.save(flush: true)
            println "re-saved assembly"


            def contigs = miraService.parseFasta(f.inputStream)
            println "got some contigs: ${contigs.size()}"

            def created = 0
            contigs.each { name, seq ->

                def contig = new Contig(name: name, sequence: seq)
                contig.quality = '0 ' * seq.length()
                contig.searchAssemblyId = assemblyId
                assembly.addToContigs(contig)
                created++
                if (created % 10 == 0) {

                    println "uploaded $created of ${contigs.size()}"
                    job2.progress = "uploaded $created of ${contigs.size()}"
                    job2.save(flush: true)

                }


            }
            assembly.save(flush: true)
            job2.progress = "creating contig set"
            job2.save(flush: true)

            statisticsService.createContigSetForAssembly(assembly.id)



            job2.progress = 'finished'
            job2.status = BackgroundJobStatus.FINISHED
        }

        redirect(controller: 'backgroundJob', action: 'list')

    }




    @Secured(['ROLE_USER'])
    def runBlast = {
        def assemblyId = params.id
        println "id is $assemblyId"

        BackgroundJob job = new BackgroundJob(
                name: "Running BLAST on ${assemblyId}",
                progress: 'queued',
                status: BackgroundJobStatus.QUEUED,
                type: BackgroundJobType.BLAST,
                user: AfterpartyUser.get(springSecurityService.principal.id)
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
                name: "Running Pfam on ${assemblyId}",
                progress: 'queued',
                status: BackgroundJobStatus.QUEUED,
                type: BackgroundJobType.BLAST,
                user: AfterpartyUser.get(springSecurityService.principal.id)
        )
        job.save(flush: true)

        runAsync {
            pfamService.runPfam(assemblyId, job.id)
        }

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
        def assemblyInstance = new Assembly(name: 'Assembly name', description: 'Assembly description')
        Study.get(params.studyId.toLong()).addToAssemblies(assemblyInstance)
        assemblyInstance.save()
        redirect(action: show, id: assemblyInstance.id)

    }

    def show = {
//        def assemblyInstance = Assembly.get(params.id)
        def criteria = Assembly.createCriteria()
        def start = System.currentTimeMillis()

        // todo should we get all contigs here and dump them in a data table?
        def a = criteria.get({
            eq('id', params.id.toLong())
            fetchMode 'contigs', org.hibernate.FetchMode.JOIN
//            fetchMode 'contigs.reads', org.hibernate.FetchMode.JOIN
        })
        def contigs = Contig.findAllByAssembly(a, [max: 20])
        println "fetched : ${System.currentTimeMillis() - start}"
        println "sorted : ${System.currentTimeMillis() - start}"
        [assemblyInstance: a, contigs: contigs]
    }


}
