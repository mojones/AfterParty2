package afterparty

class AsyncMethodFilters {

    def filters = {
        dontFinishBeforeAsyncThreadStarts(controller: '*', action: '(runMira|attachReads|runBlast|runPfam)') {
            before = {

            }
            after = { Map model ->
                println "sleeping to make sure the background thread has time to kick off"
                Thread.sleep(2000)
            }
            afterView = { Exception e ->

            }
        }
    }
}
