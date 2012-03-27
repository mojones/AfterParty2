package afterparty

import grails.plugins.springsecurity.Secured

class ReadsFileController {

    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

    def trimReadsService
    def miraService
    def overviewService
    def springSecurityService


    def graph = {
        ReadsFile f = ReadsFile.get(params.id)
        def image = overviewService.getReadsFileOverview(f)
        response.setHeader('Content-length', image.length.toString())
        response.contentType = 'image/svg+xml' // or the appropriate image content type
        response.outputStream << image
        response.outputStream.flush()
    }


    def download = {
        def read = ReadsFile.get(params.id)
        response.setHeader("Content-disposition", "attachment; filename=${read.name}");
        response.outputStream << read.data.fileData
    }


}
