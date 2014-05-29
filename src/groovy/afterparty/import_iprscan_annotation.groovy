package afterparty

import groovy.sql.Sql
import groovy.time.*
import java.util.logging.* 
import java.sql.SQLException

//
// generate BLAST report with 
// blastx -num_fasta 50 -max_target_seqs 10 -db /exports/work/blast/uniref90.fasta -i urt.fixed2.fasta -outfmt "6 qseqid sacc qseqid stitle evalue bitscore qstart qend"
//
//
//



def logger = Logger.getLogger('groovy.sql') 
logger.level = Level.FINEST 
logger.addHandler(new ConsoleHandler(level: Level.FINEST)) 

sqlAfterparty = Sql.newInstance("jdbc:postgresql://localhost:5432/afterparty", 'afterparty', 'afterparty', 'org.postgresql.Driver')
assembly_id = args[0].toInteger()
iprscanFile = args[1]
source_name = args[2]
def timeStart = new Date()
def added_annotation_count = 0
println("adding InterProScan records from " + iprscanFile )
try{

    sqlAfterparty.withBatch(200, "insert into annotation values (nextval('hibernate_sequence'), 1, ?, ?, (select id from contig where name=? and assembly_id=? limit 1), ?, ?, ?, ?, ?, ? )") { prepared_statement ->

        new File(iprscanFile).eachLine { line ->
            added_annotation_count++;
            cols = line.split("\t")
            (contig_name, md5, length, analysis, accession, description, start, stop, evalue, status, date) = cols
            def annotation_type
            if (analysis == 'Pfam'){
                annotation_type = 'PFAM'
            }
            prepared_statement.addBatch(accession, 0, contig_name, assembly_id,  description, evalue.toFloat(), start.toInteger(), stop.toInteger(), annotation_type, source_name) 
        }

    }

    def timeStop = new Date()
    TimeDuration duration = TimeCategory.minus(timeStop, timeStart)
    println("added $added_annotation_count annotations in " + duration)
} catch (SQLException e) {
    println('letting you know: ' + e)
    println('next : ' + e.getNextException())
}
