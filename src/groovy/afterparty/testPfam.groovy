package afterparty

import groovy.sql.Sql

def pfamService = ctx.getBean("pfamService")
pfamService.addPfamFromInput(new FileInputStream(new File("/home/martin/Downloads/pfam.out")))

