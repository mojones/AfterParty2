package afterparty

/**
 * Created by IntelliJ IDEA.
 * User: martin
 * Date: 24/04/12
 * Time: 12:04
 * To change this template use File | Settings | File Templates.
 */
class Timer {

    def start

    Timer() {
        this.start = System.currentTimeMillis()
    }

    def log(String label) {
        println "\t$label : ${System.currentTimeMillis() - this.start}"
    }
}
