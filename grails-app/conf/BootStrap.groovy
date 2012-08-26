import groovy.sql.Sql
import afterparty.*

class BootStrap {

    def ExecutorService
    def miraService
    def trimReadsService
    def statisticsService
    def blastService
    def taxonomyService
    def pfamService

    def sessionFactory
    javax.sql.DataSource dataSource

    def springSecurityService

    def init = { servletContext ->

        //executorService.executor.setMaximumPoolSize(6)
        //executorService.executor.setCorePoolSize(6)

        // remove stale background jobs
        BackgroundJob.findAllByStatus(BackgroundJobStatus.RUNNING).each {it.delete(flush: true)}
        BackgroundJob.findAllByStatus(BackgroundJobStatus.QUEUED).each {it.delete(flush: true)}



        environments {
            development_rebuild {

                def sql = new Sql(dataSource)
                sql.execute("CREATE INDEX annotation_desc_idx ON annotation USING gin(to_tsvector('english', description));")
                sql.execute("CREATE INDEX annotation_idx ON annotation USING gin(to_tsvector('english', description));")

                // add roles and user
                def userRole = afterparty.AfterPartyRole.findByAuthority('ROLE_USER') ?: new AfterPartyRole(authority: 'ROLE_USER').save(failOnError: true)
                def adminRole = AfterPartyRole.findByAuthority('ROLE_ADMIN') ?: new AfterPartyRole(authority: 'ROLE_ADMIN').save(failOnError: true)

                def adminUser = AfterpartyUser.findByUsername('admin') ?: new AfterpartyUser(
                        username: 'admin',
                        password: springSecurityService.encodePassword('admin'),
                        enabled: true).save(failOnError: true)

                if (!adminUser.authorities.contains(adminRole)) {
                    AfterpartyUserAfterPartyRole.create adminUser, adminRole
                }

                def normalUser = AfterpartyUser.findByUsername('martin') ?: new AfterpartyUser(
                        username: 'martin',
                        password: springSecurityService.encodePassword('martin'),
                        enabled: true).save(failOnError: true)

                if (!normalUser.authorities.contains(userRole)) {
                    AfterpartyUserAfterPartyRole.create normalUser, userRole
                }


            }

            big_test {
                Study s = new Study(name: 'test study', description: 'testing')
                s.save()
                println "creating assembly"
                Assembly a = new Assembly(description: 'test assembly desc', name: "assembly from test")
                s.addToAssemblies(a)
                println "creating contigs"
                def start = System.currentTimeMillis()
                (0..100).each {
                    Contig c = new Contig(name: "contig $it", sequence: 'atgc' * 10)
                    a.addToContigs(c)

                }
                sessionFactory.getCurrentSession().flush()
                println System.currentTimeMillis() - start
            }

        }


    }
    def destroy = {
    }

}
