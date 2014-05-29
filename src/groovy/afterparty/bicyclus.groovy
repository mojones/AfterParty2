import afterparty.*
import groovy.sql.Sql
//import org.codehaus.groovy.grails.plugins.domainclassgrailsplugin.property_instance_map
def query2contig = [:]
def n = 0

javax.sql.DataSource dataSource
//def propertyinstancemap = property_instance_map
def cleanUpGorm() {
    def session = ctx.sessionFactory.currentSession
    session.flush()
    session.clear()
    //propertyInstanceMap.get().clear()
}
def sqlAfterparty = new Sql(ctx.sessionFactory.currentSession.connection())
println sqlAfterparty
new File('/home/afterparty/non_filt_blast.out').eachLine { line ->
    n++
    if (n > 1000000){
        System.exit(0)
    }
    def query = line.split('\t')[0]
    def contig = line.split('\t')[1]
    String evalue = line.split('\t')[10].trim()
    //print "~~" + evalue + "~~\n"
    def coreContig, assemblyContig
    BigDecimal evalueDecimal = new java.math.BigDecimal(evalue)   
 

    //link from core contig to assembly contig 
    def core2assemblyStatement = """
insert into annotation 
values (
    nextval('hibernate_sequence'), 
    0, 
    (select id from contig where name='${contig}') ,
    NULL, 
    (select id from contig where name='${query}' and assembly_id=4676757) ,
    'match to contig ${contig}', 
    ${evalueDecimal}, 
    0, 
    0, 
    'OTHERCONTIG', 
    'BLAST vs assemblies');

""".toString()
    //println core2assemblyStatement
    //println query + ' ' + contig
    sqlAfterparty.execute(core2assemblyStatement)



    //link from assembly contig to core contig 
    def assembly2coreStatement = """
insert into annotation 
values (
    nextval('hibernate_sequence'), 
    0, 
    (select id from contig where name='${query}' and assembly_id=4676757) ,
    NULL, 
    (select id from contig where name='${contig}' ) ,
    'match to core contig ${query}', 
    ${evalueDecimal}, 
    0, 
    0, 
    'OTHERCONTIG', 
    'BLAST vs assemblies');

""".toString()
    //println core2assemblyStatement
    sqlAfterparty.execute(assembly2coreStatement)





    if (n % 100 == 0){
        //cleanUpGorm()
        println n
    }
}
