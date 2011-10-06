package afterparty
/**
 * Created by IntelliJ IDEA.
 * User: martin
 * Date: 13/07/11
 * Time: 15:50
 * To change this template use File | Settings | File Templates.
 */


enum BackgroundJobStatus {
    QUEUED, RUNNING, FINISHED
}

enum BackgroundJobType {
    TRIM, ASSEMBLE, BLAST, UPLOAD_READS, UPLOAD_ADAPTERS, UPLOAD_BLAST_ANNOTATION
}

enum ReadsFileStatus {
    RAW, TRIMMED
}


