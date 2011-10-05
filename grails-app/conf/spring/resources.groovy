import grails.plugin.executor.SessionBoundExecutorService
import java.util.concurrent.Executors

// Place your Spring DSL code here
beans = {
    executorService(SessionBoundExecutorService) { bean ->
        bean.destroyMethod = 'destroy'
        sessionFactory = ref("sessionFactory")
        executor = Executors.newFixedThreadPool(2)
    }

}
