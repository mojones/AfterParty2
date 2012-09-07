package afterparty

class ReadsFileData {

    String fileData

    static constraints = {
    }

    static mapping = {
       fileData type: 'text'
    }

    static belongsTo = ReadsFile
}
