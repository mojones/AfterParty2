package afterparty

import grails.plugins.springsecurity.Secured
import org.compass.core.engine.SearchEngineQueryParseException

class StudyController {

    def overviewService
    def springSecurityService

    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]



    def index = {
        redirect(action: "listPublished", params: params)
    }

    def indexForSearching = {
        BackgroundJob job = new BackgroundJob(
                name: 'Indexing contigs for search',
                progress: 'running',
                study: Study.get(params.id),
                status: BackgroundJobStatus.QUEUED,
                type: BackgroundJobType.INDEX_CONTIGS)
        job.save(flush: true)

        def studyId = params.id.toLong()    // we need to grab the parameter here; once we're inside the runAsync thread the request is invisible

        runAsync {
            BackgroundJob job2 = BackgroundJob.get(job.id)
            job2.status = BackgroundJobStatus.RUNNING
            job2.save(flush: true)


            Study s = Study.get(studyId)

            // first calculate the total number of contigs we will be indexing
            Integer totalContigsToIndex = 0
            s.compoundSamples.each { compoundSample ->
                compoundSample.assemblies.each { ass ->
                    totalContigsToIndex += ass.contigs.size()
                }
            }
            job2.totalUnits = totalContigsToIndex
            job2.save(flush: true)

            // now do the indexing

            s.compoundSamples.each { compoundSample ->
                println "indexing compound sample $compoundSample.name"
                compoundSample.assemblies.each { ass ->
                    println "indexing assembly $ass.name"

                    Integer batchSize = 100
                    for (Integer offset = 0; offset < ass.contigs.size(); offset += batchSize) {
                        Integer remaining = ass.contigs.size() - offset

                        println "$remaining contigs remaining"
                        Integer thisBatch = [batchSize, remaining].min()
                        println "getting $thisBatch contigs starting at $offset"
                        def start = System.currentTimeMillis()
                        def result = afterparty.Contig.withCriteria {
                            eq('assembly', ass)
                            fetchMode 'contigs', org.hibernate.FetchMode.JOIN
                            fetchMode 'contigs.blastHits', org.hibernate.FetchMode.JOIN
                            fetchMode 'contigs.reads', org.hibernate.FetchMode.JOIN
                            firstResult(offset)
                            maxResults(thisBatch)

                        }
                        println "fetched in : " + (System.currentTimeMillis() - start)
                        Contig.index(result)
                        println "indexed in : " + (System.currentTimeMillis() - start)
                        job2.progress = "indexed $offset / ${ass.contigs.size()} (total $totalContigsToIndex)"
                        job2.unitsDone = job2.unitsDone + thisBatch
                        job2.save(flush: true)
                    }
                }
            }
            job2.status = BackgroundJobStatus.FINISHED
            job2.save(flush: true)
        }
        redirect(controller: 'backgroundJob', action: 'list')

    }

    def search = {


        println "query is " + params.q
        def study = Study.get(params.id)
        if (!params || !params.q?.trim()) {
            // if there is no query then we will just return the assemblies
            return [assemblies: study.compoundSamples.assemblies.flatten(), studyInstance: study, showResults: false]
        }
        try {

            List assemblies = []
            //which assemblies are we looking at?
            params.entrySet().findAll({it.key.startsWith('check_')}).each {
                Integer assemblyId = it.key.split(/_/)[1].toInteger()
                assemblies.add(Assembly.get(assemblyId))
            }

            // set up colours for assemblies
            def assemblyColours = ['LightCyan', 'LightPink', 'LightSkyBlue']
            def assemblyToColour = [:]
            assemblies.eachWithIndex { assembly, index ->
                assemblyToColour.put(assembly, assemblyColours.get(index))
            }




            StringBuilder queryStringBuilder = new StringBuilder()
            queryStringBuilder.append("${params.q} AND (")
            queryStringBuilder.append(assemblies.collect({"searchAssemblyId:$it.id"}).join(' OR '))
            queryStringBuilder.append(')')
            params.max = 50
            println "final query string is " + queryStringBuilder.toString()

            Integer offset = params.offset ? params.offset.toInteger() : 0

            def searchResultContigs = []
            def rawSearchResult = Contig.search(queryStringBuilder.toString(), [max: 50, offset: offset])
            rawSearchResult.results.each {
                searchResultContigs.add(Contig.get(it.id))
            }

            // we will return a lot of data to render the search results...
            return [
                    assemblyToColour: assemblyToColour,    // allows us to colour each contig to show which assembly it came from
                    searchedAssemblies: assemblies,         // the list of assemblies that was involved in the search
                    searchResultContigs: searchResultContigs,         // the list of results, as full Contig domain objects
                    searchResult: rawSearchResult,                    // the result object that contains the query, offset, etc
                    assemblies: study.compoundSamples.assemblies.flatten(),     // the list of available assemblies so that we can draw the checkbox for the next search
                    studyInstance: study, // the study that we are looking at
                    showResults: true                                           // tell the gsp to show the results
            ]


        } catch (SearchEngineQueryParseException ex) {
            return [parseException: true]
        }
    }


    @Secured(['ROLE_USER'])
    def createCompoundSample = {
        def studyInstance = Study.get(params.id)
        def newCompoundSample = new CompoundSample(name: 'compound sample name')
        studyInstance.addToCompoundSamples(newCompoundSample)
        studyInstance.save()
        flash.success = "added a new compound sample"
        redirect(action: show, id: studyInstance.id)
    }


    def overview = {
        def studyInstance = Study.get(params.id)

        if (studyInstance && (studyInstance.published || studyInstance.user.id == springSecurityService.principal.id)) {
            def image = overviewService.getDatasetOverview(params.id)
            response.setHeader('Content-length', image.length.toString())
            response.contentType = 'image/svg+xml' // or the appropriate image content type
            response.outputStream << image
            response.outputStream.flush()
        }
        else {
            render "no such study"
        }
    }

    def listPublished = {
        [studyInstanceList: Study.findAllByPublished(true)]
    }

    @Secured(['ROLE_USER'])
    def create = {
        def studyInstance = new Study(name: 'Study name', description: 'Study description', published: false)
        def user = AfterpartyUser.get(springSecurityService.principal.id)
        user.addToStudies(studyInstance)
        studyInstance.save()
        redirect(action: show, id: studyInstance.id)
    }

    @Secured(['ROLE_USER'])
    def makePublished = {
        def study = Study.get(params.id)
        if (study.user.id == springSecurityService.principal.id) {
            study.published = true
            flash.success = "published study ${study.name}"
        }
        redirect(action: 'listPublished')
    }


    def show = {
        def studyInstance = Study.get(params.id)
        session.studyId = params.id
        def userId = springSecurityService.isLoggedIn() ? springSecurityService?.principal?.id : 'none'
        [studyInstance: studyInstance, isOwner: studyInstance.user.id == userId]

    }


}
