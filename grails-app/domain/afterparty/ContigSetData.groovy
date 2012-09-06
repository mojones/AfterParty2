package afterparty

class ContigSetData{
	
    byte[] blastHeaderFile
    byte[] blastIndexFile
    byte[] blastSequenceFile


    static belongsTo = [contigSet : ContigSet]
    
}