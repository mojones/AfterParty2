// locations to search for config files that get merged into the main config
// config files can either be Java properties files or ConfigSlurper scripts

grails.config.locations = ["file:${userHome}/afterparty.config.groovy"]

// if(System.properties["${appName}.config.location"]) {
//    grails.config.locations << "file:" + System.properties["${appName}.config.location"]
// }

grails.project.groupId = appName // change this to alter the default package name and Maven publishing destination
grails.mime.file.extensions = true // enables the parsing of file extensions from URLs into the request format
grails.mime.use.accept.header = false
grails.mime.types = [html: ['text/html', 'application/xhtml+xml'],
        xml: ['text/xml', 'application/xml'],
        text: 'text/plain',
        js: 'text/javascript',
        rss: 'application/rss+xml',
        atom: 'application/atom+xml',
        css: 'text/css',
        csv: 'text/csv',
        all: '*/*',
        json: ['application/json', 'text/json'],
        form: 'application/x-www-form-urlencoded',
        multipartForm: 'multipart/form-data'
]

// URL Mapping Cache Max Size, defaults to 5000
//grails.urlmapping.cache.maxsize = 1000

// The default codec used to encode data with ${}
grails.views.default.codec = "none" // none, html, base64
grails.views.gsp.encoding = "UTF-8"
grails.converters.encoding = "UTF-8"
// enable Sitemesh preprocessing of GSP pages
grails.views.gsp.sitemesh.preprocess = true
// scaffolding templates configuration
grails.scaffolding.templates.domainSuffix = 'Instance'

// Set to false to use the new Grails 1.2 JSONBuilder in the render method
grails.json.legacy.builder = false
// enabled native2ascii conversion of i18n properties files
grails.enable.native2ascii = true
// whether to install the java.util.logging bridge for sl4j. Disable for AppEngine!
grails.logging.jul.usebridge = true
// packages to include in Spring bean scanning
grails.spring.bean.packages = []

// request parameters to mask when logging exceptions
grails.exceptionresolver.params.exclude = ['password']

// run in root context
grails.app.context = "/"

// set per-environment serverURL stem for creating absolute links
//environments {
//    production {
//        grails.serverURL = "http://www.changeme.com"
//    }
//    development {
//        grails.serverURL = "http://localhost:8080"
//        grails.gorm.failOnError = true
//    }
//    development_rebuild {
//        grails.serverURL = "http://localhost:8080"
//        grails.gorm.failOnError = true
//    }
//    big_test {
//        grails.serverURL = "http://localhost:8080"
//        grails.gorm.failOnError = true
//    }
//    test {
//        grails.serverURL = "http://localhost:8080"
//    }
//
//}

// log4j configuration
log4j = {
    // Example of changing the log pattern for the default console
    // appender:
    //
    appenders {
        console name: 'stdout', layout: pattern(conversionPattern: '%c{2} %m%n')
    }

    error 'org.codehaus.groovy.grails.web.servlet',  //  controllers
            'org.codehaus.groovy.grails.web.pages', //  GSP
            'org.codehaus.groovy.grails.web.sitemesh', //  layouts
            'org.codehaus.groovy.grails.web.mapping.filter', // URL mapping
            'org.codehaus.groovy.grails.web.mapping', // URL mapping
            'org.codehaus.groovy.grails.commons', // core / classloading
            'org.codehaus.groovy.grails.plugins', // plugins
            'org.codehaus.groovy.grails.orm.hibernate', // hibernate integration
            'org.springframework',
            'org.hibernate',
            'net.sf.ehcache.hibernate'

//    debug 'org.codehaus.groovy.grails.plugins.searchable'
    //    debug 'org.compass'
    warn 'org.mortbay.log'

    warn 'grails.plugin.springcache'

    warn 'jdbc.sqltiming'
}

// use jquery for ajax purposes
grails.views.javascript.library = "jquery"


springcache {
    defaults {
        timeToLive = 1000000
        timeToIdle = 1000000
    }
}
// Added by the Spring Security Core plugin:
grails.plugins.springsecurity.userLookup.userDomainClassName = 'afterparty.AfterpartyUser'
grails.plugins.springsecurity.userLookup.authorityJoinClassName = 'afterparty.AfterpartyUserAfterPartyRole'
grails.plugins.springsecurity.authority.className = 'afterparty.AfterPartyRole'

// password options
grails.plugins.springsecurity.ui.password.minLength = 4
grails.plugins.springsecurity.ui.password.maxLength = 10
grails.plugins.springsecurity.ui.password.validationRegex = '^.*$'

conf.ui.password.minLength = 4
conf.ui.password.validationRegex = '.*'

grails.gorm.failOnError = true

// log slow sql queries
log4jdbc.sqltiming.warn.threshold = 100

// secure the spring security ui console
grails.plugins.springsecurity.controllerAnnotations.staticRules = [ 
   '/user/**': ['ROLE_ADMIN'], 
   '/role/**': ['ROLE_ADMIN'], 
   '/aclclass/**': ['ROLE_ADMIN']
] 