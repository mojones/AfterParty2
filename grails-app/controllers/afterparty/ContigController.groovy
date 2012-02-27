package afterparty

class ContigController {

    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

    def searchableService
    def contigAnnotationService
    def statisticsService

    def index = {
        redirect(action: "list", params: params)
    }



    def show = {
        def contigInstance = Contig.get(params.id)
        if (!contigInstance) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'contig.label', default: 'Contig'), params.id])}"
            redirect(action: "list")
        }
        else {
            [contigInstance: contigInstance]
        }
    }


    def showJSON = {
        def contigInstance = Contig.get(params.id)
        def readCollection = contigInstance.reads.findAll({it.start > 0}).sort({it.start}).collect({
            [
                    name: it.name,
                    start: it.start,
                    stop: it.stop,
                    sampleSource: it.sampleSource,
                    assemblySource : it.assemblySource
            ]
        })

        // assemble a source -> colour map
        Set readSources = readCollection.collect({it.sampleSource}).findAll({it != null}).unique()
        readSources.addAll(readCollection.collect({it.assemblySource}).findAll({it != null}).unique())
        def source2colour = [:]
        def description2colour = []
        readSources.eachWithIndex { source, i ->
            source2colour.put(source, StatisticsService.boldAssemblyColours[i % StatisticsService.boldAssemblyColours.size()])
            description2colour.add(
                    [
                            'source': source.name,
                            'colour': StatisticsService.boldAssemblyColours[i % StatisticsService.boldAssemblyColours.size()]
                    ]
            )

        }

        // attach the correct colour to each read
        readCollection.each {
            if (source2colour.containsKey(it.sampleSource)){
                it.colour = source2colour.get(it.sampleSource)
            }
            if (source2colour.containsKey(it.assemblySource)){
                it.colour = source2colour.get(it.assemblySource)
            }
            else{
                it.colour = 'black'
            }
        }





        render(contentType: "text/json") {
            length = contigInstance.length()
            quality = contigInstance.quality.split(' ')
            coverage = contigInstance.coverage()
            blastHits = contigInstance.blastHits.sort({-it.bitscore})
            reads = readCollection   // sort the reads by start position so they pile up nicely
            readColours = description2colour
        }
    }

}


