package afterparty

import groovy.xml.MarkupBuilder
import java.math.MathContext

class OverviewService {

    static transactional = true

    def grailsApplication
    def statisticsService


    def formatBaseCount(int count) {
        if (count < 1000) {
            return "$count b"
        }
        if (count < 1000000) {
            return (count / 1000).round(new MathContext(2)) + "Kb"
        }
        return (count / 1000000).round(new MathContext(2)) + "Mb"
    }


    def addStringFunctions() {
        String.metaClass.wrap = { def width, def indent = 0 ->
//            println "wrapping $delegate at $width"
            StringBuffer result = new StringBuffer()
            StringBuffer currentLine = new StringBuffer()
            delegate.split(" ").each { word ->
                if ((currentLine.length() + word.length()) > width) {
                    result.append(currentLine + '<BR/>')
                    currentLine = new StringBuffer()
                    currentLine.append(" " * indent)
                    currentLine.append(word + " ")
                }
                else {
                    currentLine.append(word + " ")
                }
            }
            result.append(currentLine)
            return result.toString()
        }
    }

    def buildDotfileLabel(def title, Map attributes, def backgroundColour, def url) {

        addStringFunctions()

        StringWriter writer = new StringWriter()
        def html = new MarkupBuilder(writer)
        html.table(['border': '0', 'cellborder': '1', 'cellspacing': '0', 'cellpadding': '5', 'bgcolor': backgroundColour]) {
            tr() {
                td(['colspan': 2, 'bgcolor': 'grey', href: url, target: '_top']) {
                    mkp.yieldUnescaped(title.toString().wrap(15))
                }
            }
            attributes.each { attribute ->
                tr {
                    td(attribute.key)
                    td(attribute.value)
                }
            }

        }
        return writer
    }

    def buildRunLine(Run run) {
        def g = new org.codehaus.groovy.grails.plugins.web.taglib.ApplicationTagLib()
        def label = buildDotfileLabel(
                run.name,
                [
                        'no raw reads': run.rawReadsFile.readCount,
                        'min read length': run.rawReadsFile.minReadLength,
                        'mean read length': run.rawReadsFile.meanReadLength,
                        'max read length': run.rawReadsFile.maxReadLength,
                        'span': formatBaseCount(run.rawReadsFile.baseCount)
                ],
                'palegreen',
                g.createLink(controller: 'run', action: 'show', id: run.id)
        )
        return ("${run.rawReadsFile.id} [shape=plaintext, label=<$label>]; ")
    }

    def buildReadsFileLine(ReadsFile readsFile) {
        def g = new org.codehaus.groovy.grails.plugins.web.taglib.ApplicationTagLib()
        def label = buildDotfileLabel(
                readsFile.name,
                [
                        'no raw reads': readsFile.readCount,
                        'min read length': readsFile.minReadLength,
                        'mean read length': readsFile.meanReadLength,
                        'max read length': readsFile.maxReadLength,
                        'span': formatBaseCount(readsFile.baseCount)
                ],
                'lightpink',
                g.createLink(controller: 'readsFile', action: 'show', id: readsFile.id)
        )

        return ("${readsFile.id} [shape=plaintext, label=<$label>]; ")
    }

    def buildAssemblyLine(Assembly assembly) {
        def g = new org.codehaus.groovy.grails.plugins.web.taglib.ApplicationTagLib()
        def label = buildDotfileLabel(
                assembly.name,
                [
                        'no contigs': assembly.contigCount,
                        'min contig length': assembly.minContigLength,
                        'mean contig length': assembly.meanContigLength,
                        'max contig length': assembly.maxContigLength,
                        'span': formatBaseCount(assembly.baseCount),
                        'n50': formatBaseCount(assembly.n50)
                ],
                'peachpuff',
                g.createLink(controller: 'assembly', action: 'show', id: assembly.id)
        )
        return ("${assembly.id} [shape=plaintext, label=<$label>]; ")
    }

    def buildStudyLine(Study study) {
        def g = new org.codehaus.groovy.grails.plugins.web.taglib.ApplicationTagLib()
        def studyLabel = buildDotfileLabel(
                study.name, [
                        'no. samples': study.samples.size(),
                        'no. raw reads': study.rawReadsCount
                ],
                'aliceblue',
                g.createLink(controller: 'study', action: 'show', id: study.id))
        return ("${study.id} [shape=plaintext, label=<$studyLabel>]; ")
    }

    def buildSampleLine(Sample sample) {
        def g = new org.codehaus.groovy.grails.plugins.web.taglib.ApplicationTagLib()
        def label = buildDotfileLabel(
                sample.name,
                [
                        'no raw reads': sample.rawReadsCount
                ],
                'lightpink',
                g.createLink(controller: 'sample', action: 'show', id: sample.id))
        return ("${sample.id} [shape=plaintext, label=<$label>]; ")
    }

    def buildExperimentLine(Experiment experiment) {
        def g = new org.codehaus.groovy.grails.plugins.web.taglib.ApplicationTagLib()
        def label = buildDotfileLabel(
                experiment.name,
                [
                        'no raw reads': experiment.rawReadsCount,
                        'span': formatBaseCount(experiment.baseCount)
                ],
                'peachpuff',
                g.createLink(controller: 'experiment', action: 'show', id: experiment.id)
        )
        return ("${experiment.id} [shape=plaintext, label=<$label>]; ")

    }

    def getBackgroundJobGraph(BackgroundJob job) {
        addStringFunctions()

// we will want to create links to the objects we are drawing
        def g = new org.codehaus.groovy.grails.plugins.web.taglib.ApplicationTagLib()
        GraphViz gv = new GraphViz();
        gv.addln(gv.start_graph());

//        gv.addln("ratio = 1;")
        gv.addln("layout=dot;")
        gv.addln("ranksep=1;")
        gv.addln("rankdir=LR;")
        // first draw nodes
        switch (job.type) {
            case BackgroundJobType.TRIM:
                job.sources.each { id ->
                    gv.addln(buildRunLine(Run.get(id)))
                }
                job.sinks.each { id ->
                    gv.addln(buildReadsFileLine(ReadsFile.get(id)))
                }
                break

            case BackgroundJobType.ASSEMBLE:
                job.sources.each { id ->
                    gv.addln(buildReadsFileLine(ReadsFile.get(id)))
                }
                job.sinks.each {id ->
                    gv.addln(buildAssemblyLine(Assembly.get(id)))
                }
                break
        }

        // now draw edges
        job.sources.each { source ->
            job.sinks.each { sink ->
                gv.addln("""${source}->${sink} [label="${job.label}"];""")

            }
        }

        gv.addln(gv.end_graph());
//        System.out.println(gv.getDotSource());

        10.times { gv.decreaseDpi()}
        return gv.getGraph(gv.getDotSource(), 'svg')

    }

    def getWorkflowOverview(def studyId) {
        println "drawing workflow for $studyId"
        addStringFunctions()

        Study study = Study.get(studyId)

        // we will want to create links to the objects we are drawing
        def g = new org.codehaus.groovy.grails.plugins.web.taglib.ApplicationTagLib()
        GraphViz gv = new GraphViz();
        gv.addln(gv.start_graph());

//        gv.addln("ratio = 1;")
        gv.addln("layout=dot;")
        gv.addln("ranksep=1;")

        gv.addln("""
            subgraph cluster_0 {
		    color=blue;
   		    label = < <font color="blue">raw reads</font> >;
		    labeljust = "l";
        """);

        // add all raw reads
        study.samples.each { sample ->
            sample.experiments.each { experiment ->
                experiment.runs.each {
                    gv.addln(buildRunLine(it))
                }
            }
        }

        gv.addln("}")

        gv.addln("""
            subgraph cluster_1 {
		    color=red;
		    label = < <font color="red">trimmed reads</font> >;
		    labeljust = "l";
        """);

        // add all trimmed reads
        study.samples.each { sample ->
            sample.experiments.each { experiment ->
                experiment.runs.each { run ->
                    if (run.trimmedReadsFile){
                        gv.addln(buildReadsFileLine(run.trimmedReadsFile))
                    }
                }
            }
        }


        gv.addln("}")


        gv.addln("""
            subgraph cluster_2 {
		    color=orange;
		    label = < <font color="orange">primary assembly</font> >;
		    labeljust = "l";
        """)

        // add assemblies
        study.assemblies.each {
            gv.addln(buildAssemblyLine(it))
        }

        gv.addln("}")

        // draw connections
        BackgroundJob.findAllByStudy(study).each { job ->
            println "drawing job $job"
            job.sources.each { source ->
                job.sinks.each { sink ->
                    gv.addln("""${source}->${sink} [label="${job.label}"];""")

                }
            }
        }


        gv.addln(gv.end_graph());
//        System.out.println(gv.getDotSource());
        8.times {
            gv.decreaseDpi(); // 106 dpi
        }

        return gv.getGraph(gv.getDotSource(), 'svg')
    }

    def getDatasetOverview(def studyId) {
        addStringFunctions()

        def study = Study.get(studyId)

        // we will want to create links to the objects we are drawing
        def g = new org.codehaus.groovy.grails.plugins.web.taglib.ApplicationTagLib()
        GraphViz gv = new GraphViz();
        gv.addln(gv.start_graph());

        8.times {gv.decreaseDpi()}

//        gv.addln("ratio = 1;")
        gv.addln("layout=dot;")
        gv.addln("ranksep=1;")

        gv.addln("""
            subgraph cluster_0 {
		    color=blue;
   		    label = < <font color="blue">study</font> >;
		    labeljust = "l";
        """);

        gv.addln(buildStudyLine(study))

        gv.addln("}")

        gv.addln("""
            subgraph cluster_1 {
		    color=red;
		    label = < <font color="red">samples</font> >;
		    labeljust = "l";
        """);

        study.samples.each {
            gv.addln(buildSampleLine(it))
            gv.addln("$study.id->$it.id [arrowhead=none];")
        }
        gv.addln("}")


        gv.addln("""
                    subgraph cluster_2 {
        		    color=orange;
        		    label = < <font color="orange">experiments</font> >;
        		    labeljust = "l";
                """)


        study.samples.each { sample ->
            sample.experiments.each {
                gv.addln(buildExperimentLine(it))
                gv.addln("$sample.id->$it.id  [arrowhead=none];;")
            }
        }

        gv.addln("}")

        gv.addln("""
                   subgraph cluster_3 {
                    color=green;
                    label = < <font color="green">runs</font> >;
                    labeljust = "l";
        """)


        study.samples.each { sample ->
            sample.experiments.each { experiment ->
                experiment.runs.each {
                    gv.addln(buildRunLine(it))
                    gv.addln("$experiment.id->$it.rawReadsFile.id [arrowhead=none];")
                }
            }
        }

        gv.addln("}")

        gv.addln(gv.end_graph());
//        System.out.println(gv.getDotSource());
        return gv.getGraph(gv.getDotSource(), 'svg')
    }

    def getReadsFileOverview(ReadsFile readsFile) {
        addStringFunctions()

        // we will want to create links to the objects we are drawing
        def g = new org.codehaus.groovy.grails.plugins.web.taglib.ApplicationTagLib()
        GraphViz gv = new GraphViz();
        gv.addln(gv.start_graph());

//        gv.addln("ratio = 1;")
        gv.addln("layout=dot;")
        gv.addln("ranksep=1;")
        gv.addln("rankdir=LR;")

        // add all trimmed reads
        gv.addln(buildReadsFileLine(readsFile))

        // draw connections going to the file in question
        BackgroundJob.findAllByStatus(BackgroundJobStatus.FINISHED).findAll({it.sinks.contains(readsFile.id)}).each { job ->
            job.sources.each { source ->
                gv.addln(buildRunLine(Run.get(source)))
                gv.addln("""${source}->${readsFile.id} [label="${job.label}"];""")
            }
        }

        // draw connections going to the file in question
        BackgroundJob.findAllByStatus(BackgroundJobStatus.FINISHED).findAll({it.sources.contains(readsFile.id)}).each { job ->
            job.sinks.each { sink ->
                if (job.type == BackgroundJobType.TRIM) {
                    gv.addln(buildReadsFileLine(ReadsFile.get(sink)))
                }

                if (job.type == BackgroundJobType.ASSEMBLE) {
                    gv.addln(buildAssemblyLine(Assembly.get(sink)))
                }

                gv.addln("""${readsFile.id}->${sink} [label="${job.label}"];""")
            }
        }


        gv.addln(gv.end_graph());
//        System.out.println(gv.getDotSource());
        10.times {
            gv.decreaseDpi(); // 106 dpi
        }

        return gv.getGraph(gv.getDotSource(), 'svg')
    }

}
