package afterparty

class ReadsFileData {

    byte[] fileData

    static constraints = {
    }

    static belongsTo = ReadsFile
    static hasMany = [readsFiles : ReadsFile]
}
