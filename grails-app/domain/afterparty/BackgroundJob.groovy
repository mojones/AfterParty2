package afterparty

import groovy.time.TimeCategory
import groovy.time.TimeDuration

class BackgroundJob {
    String name

    String label  //for drawing workflows

    String progress
    String commandLine

    BackgroundJobStatus status

    BackgroundJobType type

    // for calculating ETA
    Integer startedTime = System.currentTimeMillis()
    Integer totalUnits = 0
    Integer unitsDone = 0

    def ETA() {
        if (totalUnits > 0 && unitsDone > 0) {
            Integer millisPassed = System.currentTimeMillis() - startedTime
            Float millisPerUnit = millisPassed / unitsDone
            Integer unitsRemaining = totalUnits - unitsDone
            Integer millisRemaining = millisPerUnit * unitsRemaining
            Date finish = new Date(System.currentTimeMillis() + millisRemaining)
            Date now = new Date(System.currentTimeMillis())
            TimeDuration td = TimeCategory.minus(finish, now)

            return "$td remaining"
        }
        else {
            return ''
        }
    }

    static belongsTo = [study: Study]

    static hasMany = [sources: Long, sinks: Long]

    static constraints = {
        label(nullable: true) // we will add the label after the job is done, so it needs to be null at the start
        commandLine(nullable: true)
        startedTime()
    }

    static mapping = {
        commandLine(type: 'text')
    }

}
