package afterparty

class Run {


    String name
    String description
    ReadsFile rawReadsFile
    ReadsFile trimmedReadsFile

    static constraints = {
        name(maxSize: 1000)
        description(maxSize: 10000, nullable: true)
        rawReadsFile(nullable: true)
        trimmedReadsFile(nullable: true)
    }

    static belongsTo = [experiment: Experiment]

    def getRawReadsCount(){  return rawReadsFile ? rawReadsFile.readCount : 0    }
    def getRawReadsMinLength(){  return rawReadsFile ? rawReadsFile.minReadLength : 0    }
    def getRawReadsMeanLength(){  return rawReadsFile ? rawReadsFile.meanReadLength : 0    }
    def getRawReadsMaxLength(){  return rawReadsFile ? rawReadsFile.maxReadLength : 0    }
    def getRawReadsBaseCount(){  return rawReadsFile ? rawReadsFile.baseCount : 0    }
    
    def getTrimmedReadsCount(){  return trimmedReadsFile ? trimmedReadsFile.readCount : 0    }
    def getTrimmedReadsMinLength(){  return trimmedReadsFile ? trimmedReadsFile.minReadLength : 0    }
    def getTrimmedReadsMeanLength(){  return trimmedReadsFile ? trimmedReadsFile.meanReadLength : 0    }
    def getTrimmedReadsMaxLength(){  return trimmedReadsFile ? trimmedReadsFile.maxReadLength : 0    }
    def getTrimmedReadsBaseCount(){  return trimmedReadsFile ? trimmedReadsFile.baseCount : 0    }


}
