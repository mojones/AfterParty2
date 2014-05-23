package afterparty

import groovy.sql.Sql
import groovy.time.*
import java.util.logging.* 

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
assembly_id = args[0]
contigsFile = args[1]
blastFile = args[2]

if (contigsFile != 'none'){
    println("processing contigs from " + contigsFile)
    count = 0
    def all_seqs = []
    def timeStart = new Date()
    def current_header = ''
    def current_sequence = ''
    new File(contigsFile).eachLine { line ->
        if (line.startsWith('>')){
                if (current_header != ''){
                    all_seqs.add([current_header.take(400), current_sequence])
                    count++
                }
            current_header = line[1..-1]
            current_sequence = ''
        }
        else{
            current_sequence = current_sequence + line.toUpperCase()
        }
        
    }
    all_seqs.add([current_header, current_sequence])

    sqlAfterparty.withBatch(200,"insert into contig values (nextval('hibernate_sequence'),1,?,1,1,?,?,?)") { ps ->
        all_seqs.each{ pair ->
            name = pair[0]
            seq = pair[1]
            ps.addBatch(assembly_id.toInteger(), name, '0 ' * seq.length(), seq)
        }
    }

    def timeStop = new Date()
    TimeDuration duration = TimeCategory.minus(timeStop, timeStart)
    println("added $count contigs in " + duration)

}

if (blastFile != 'none'){
    def timeStart = new Date()
    def added_annotation_count = 0
    println("adding BLAST records from " + blastFile )
    try{

        sqlAfterparty.withBatch(200, "insert into annotation values (nextval('hibernate_sequence'), 1, ?, ?, (select id from contig where name=? limit 1), ?, ?, ?, ?, ?, ? )") { prepared_statement ->

            new File(blastFile).eachLine { line ->
                added_annotation_count++;
                cols = line.split("\t")
                (contig_name, accession, description, evalue, bitscore, start, stop) = cols
                prepared_statement.addBatch(accession, bitscore.toFloat(), contig_name, description, evalue.toFloat(), start.toInteger(), stop.toInteger(), 'BLAST', blastFile) 
            }

        }

        def timeStop = new Date()
        TimeDuration duration = TimeCategory.minus(timeStop, timeStart)
        println("added $added_annotation_count annotations in " + duration)
    } catch (Exception e) {
        println('letting you know: ' + e)
        println('next : ' + e.getNextException())
    }
}
