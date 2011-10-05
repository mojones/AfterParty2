package afterparty

class BackgroundJob {
    String name

    String label  //for drawing workflows

    String progress
    String commandLine

    BackgroundJobStatus status

    BackgroundJobType type

    static belongsTo = [study: Study]

    static hasMany = [sources: Long, sinks: Long]

    static constraints = {
        label(nullable: true) // we will add the label after the job is done, so it needs to be null at the start
        commandLine(nullable: true)
    }

    static mapping = {
        commandLine(type: 'text')
    }

}
