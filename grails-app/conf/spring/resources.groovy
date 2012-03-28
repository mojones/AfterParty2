import java.util.concurrent.Executors

//import grails.plugin.executor.PersistenceContextExecutorWrapper

// Place your Spring DSL code here
beans = {
//    executorService(SessionBoundExecutorService) { bean ->
    //        bean.destroyMethod = 'destroy'
    //        sessionFactory = ref("sessionFactory")
    //        executor = Executors.newFixedThreadPool(2)
    //    }

    executorService(grails.plugin.executor.PersistenceContextExecutorWrapper) { bean ->
        bean.destroyMethod = 'destroy'
        persistenceInterceptor = ref("persistenceInterceptor")
        executor = Executors.newFixedThreadPool(2)
    }


}
