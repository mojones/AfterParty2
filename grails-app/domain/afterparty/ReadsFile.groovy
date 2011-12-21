package afterparty

class ReadsFile {

    def statisticsService


    String name
    String description
    ReadsFileData data
    ReadsFileStatus status



    static constraints = {
        name(maxSize: 1000)
        description(maxSize: 10000, nullable: true)

        }

    static belongsTo = [run : Run]


    def getReadCount() {
        return statisticsService.getReadFileDataStats(data.id.toLong()).readCount
    }

    def getMeanReadLength() {
        return statisticsService.getReadFileDataStats(data.id.toLong()).meanLength
    }

    def getMaxReadLength() {
        return statisticsService.getReadFileDataStats(data.id.toLong()).maxLength
    }

    def getMinReadLength() {
        return statisticsService.getReadFileDataStats(data.id.toLong()).minLength
    }

    def getBaseCount() {
        return statisticsService.getReadFileDataStats(data.id.toLong()).baseCount
    }


    def isPublished(){
        return this.run.isPublished()
    }

    def isOwnedBy(def user){
         return this.run.isOwnedBy(user)
    }


}
