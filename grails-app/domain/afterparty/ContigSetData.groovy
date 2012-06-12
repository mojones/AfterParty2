package afterparty

class ContigSetData{
	
	    //TODO move these big fields into a separate domain object so that they can be loaded lazily
    byte[] blastHeaderFile
    byte[] blastIndexFile
    byte[] blastSequenceFile


    static belongsTo = [contigSet : ContigSet]
    
}