dataSource {
    pooled = true
    driverClassName = "org.postgresql.Driver"
    username = "mysuperuser"
    password = "jukur6ai"
    dbCreate = "create"
    url = "jdbc:postgresql://localhost:5432/afterparty"
}
hibernate {
    cache.use_second_level_cache = true
    cache.use_query_cache = true
    cache.provider_class = 'net.sf.ehcache.hibernate.EhCacheProvider'
}
// environment specific settings
environments {
    development {
        dataSource {
            dbCreate = "update" // one of 'create', 'create-drop','update'
                        loggingSql = false
        }
    }

    development_rebuild {
        dataSource {
            dbCreate = "create" // one of 'create', 'create-drop','update'
//            loggingSql = true
        }
    }

    big_test {
        dataSource {
            dbCreate = "create" // one of 'create', 'create-drop','update'
//            loggingSql = true
        }
    }

    test {
        dataSource {
            dbCreate = "update"
        }
    }
    production {
        dataSource {
            dbCreate = "update"
        }
    }
}
