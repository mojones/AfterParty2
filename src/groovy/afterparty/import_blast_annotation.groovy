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
blastFile = args[1]
sourceName = args[2]

// set optional evalue cutoff
def evalue_cutoff = 100
if(args.size() == 4){
    evalue_cutoff = args[3].toFloat()
    println("evalue cutoff is ${evalue_cutoff}")
}
def timeStart = new Date()
def added_annotation_count = 0
println("adding BLAST records from " + blastFile )
try{

    sqlAfterparty.withBatch(200, "insert into annotation values (nextval('hibernate_sequence'), 1, ?, ?, (select id from contig where name=? and assembly_id=? limit 1), ?, ?, ?, ?, ?, ? )") { prepared_statement ->

        new File(blastFile).eachLine { line ->
            cols = line.split("\t")
            (contig_name, accession, description, evalue, bitscore, start, stop) = cols
            if (evalue.toFloat() < evalue_cutoff){
                print('.')
                added_annotation_count++;
                prepared_statement.addBatch(accession, bitscore.toFloat(), contig_name, assembly_id,  description, evalue.toFloat(), start.toInteger(), stop.toInteger(), 'BLAST', sourceName) 
            }
        }

    }

    def timeStop = new Date()
    TimeDuration duration = TimeCategory.minus(timeStop, timeStart)
    println("added $added_annotation_count annotations in " + duration)
} catch (SQLException e) {
    println('letting you know: ' + e)
    println('next : ' + e.getNextException())
}
