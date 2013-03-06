package afterparty

import grails.converters.JSON

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
                    source: it.source
            ]
        })

        // assemble a source -> colour map
        Set readSources = readCollection.collect({it.source}).unique()
        def source2colour = [:]
        def description2colour = []
        readSources.eachWithIndex { source, i ->
            source2colour.put(source, StatisticsService.boldAssemblyColours[i % StatisticsService.boldAssemblyColours.size()])
            description2colour.add(
                    [
                            'source': source,
                            'colour': StatisticsService.boldAssemblyColours[i % StatisticsService.boldAssemblyColours.size()]
                    ]
            )

        }

        // attach the correct colour to each read
        readCollection.each {
            it.colour = source2colour.get(it.source)
        }


        Map annotationsMap = [:]
        AnnotationType.each { type ->
            if (type != AnnotationType.BLAST) {
                annotationsMap.put(type, contigInstance.annotations.findAll({it.type == type}).sort({-it.start}).sort({it.evalue}).reverse())
            }
        }

        def result = [
                length: contigInstance.length(),
                quality: contigInstance.quality.split(' ').collect({it.toInteger()}),
                coverage: contigInstance.coverage(),
                blastHits: contigInstance.annotations.findAll({it.type == AnnotationType.BLAST}).sort({-it.bitscore}),
                annotations: annotationsMap,
                reads: readCollection,   // sort the reads by start position so they pile up nicely
                readColours: description2colour
        ]
        render result as JSON


    }

}


