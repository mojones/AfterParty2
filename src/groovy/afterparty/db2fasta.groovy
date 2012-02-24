package afterparty

Assembly a = Assembly.get(42)
File f = new File('/home/martin/test2.fasta')
f.delete()
a.contigs.each{
    f.append ">${it.id}_${it.averageQuality()}_${it.averageCoverage()}\n"
    f.append it.sequence + "\n"
}
println a

