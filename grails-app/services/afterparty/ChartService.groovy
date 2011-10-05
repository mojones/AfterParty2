package afterparty

import java.awt.Color
import java.util.regex.Matcher
import org.jfree.chart.ChartFactory
import org.jfree.chart.ChartUtilities
import org.jfree.chart.axis.LogarithmicAxis
import org.jfree.chart.plot.PlotOrientation
import org.jfree.chart.plot.XYPlot
import org.jfree.chart.renderer.xy.XYDotRenderer
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer
import org.jfree.data.statistics.HistogramDataset
import org.jfree.data.xy.DefaultXYDataset
import org.jfree.data.xy.XYSeries

class ChartService {

    static transactional = true

    def statisticsService
    def taxonomyService

    def getScatterplot(Long assemblyId, def xLabel, def yLabel, Integer cutoff, String colourBy) {
        println "getting graph"

//        sleep(1000*10000)
        def start = System.currentTimeMillis()
        def stats = statisticsService.getContigStatsForAssembly(assemblyId)
        println "got ${stats.id.size()}contigs : " + (System.currentTimeMillis() - start)

        def dataset = new DefaultXYDataset()


        if (colourBy == 'none') {
            XYSeries series = new XYSeries('nothing')
            stats.id.eachWithIndex { id, i->
                series.add(stats.get(xLabel)[i], stats.get(yLabel)[i])
            }
            dataset.addSeries('contigs', series.toArray())
        }
        else {


            def species2dataSeries = [:]

            stats.id.eachWithIndex {id, i ->
                if (stats.get('topBlast')[i] && stats.get('topBlast')[i].bitscore > cutoff) {
                    Matcher m = (stats.get('topBlast')[i].description =~ /OS=(\w+ \w+)/)
//                Matcher m = (stats.get('topBlast')[i].description =~ /OS=(.+?) \w\w=/)

                    if (m.find()) {
                        def species = m[0][1]
                        def phylum = taxonomyService.getParentForName(species, 'phylum')
                        if (!phylum) {
                            phylum = 'undetermined'
                        }
                        if (!species2dataSeries.containsKey(phylum)) {
                            species2dataSeries.put(phylum, new XYSeries(phylum))
                        }
                        species2dataSeries.get(phylum).add(stats.get(xLabel)[i], stats.get(yLabel)[i])
                    }
                    else {
                        println "no match for " + stats.get('topBlast')[i].description

                    }
                }

            }




            println "got ${species2dataSeries.size()} dataSeries"
            species2dataSeries.sort({-it.value.itemCount}).each {
                XYSeries series = it.value
                if (series.itemCount > 0) {
                    dataset.addSeries(it.key, series.toArray())
                }
            }
        }

        println "built data : " + (System.currentTimeMillis() - start)

        def chart = ChartFactory.createScatterPlot("$xLabel vs $yLabel", xLabel, yLabel, dataset, PlotOrientation.HORIZONTAL, true, true, false)
        def plot = (XYPlot) chart.getPlot()
        plot.setBackgroundPaint(Color.white)
        def myRenderer = new XYDotRenderer()
        myRenderer.setDotHeight(2)
        myRenderer.setDotWidth(2)
//        myRenderer.setSeriesPaint(0, new Color(100,100,100,20))
        plot.setRenderer(myRenderer)

        if (yLabel == 'coverage') {
            plot.setRangeAxis(new LogarithmicAxis(yLabel))
        }
        if (xLabel == 'coverage') {
            plot.setDomainAxis(new LogarithmicAxis(xLabel))
        }


        File tempChartFile = File.createTempFile('chart', '.png')
        ChartUtilities.saveChartAsPNG(tempChartFile, chart, 1000, 1000)
        println "rendered chart : " + (System.currentTimeMillis() - start)

        return tempChartFile.bytes
    }

    def getHistogram(def assemblyId, def xLabel, def scale) {
        def stats = statisticsService.getContigStatsForAssembly(assemblyId)

        HistogramDataset data = new HistogramDataset()

        def values = new ArrayList(stats.get(xLabel))

        // fake some values to prevent zeros in log scale
        if (scale == 'log') {
            (values.min()..values.max()).each {
                values.add(it)
            }
        }



        data.addSeries('test', (double[]) values, 20)

        def chart = ChartFactory.createHistogram(xLabel, xLabel, 'label3', data, PlotOrientation.VERTICAL, false, true, false)
        def plot = (XYPlot) chart.getPlot()
        plot.setRenderer(new XYLineAndShapeRenderer())

        def renderer = (XYLineAndShapeRenderer) plot.getRenderer()
        renderer.setShapesVisible(false)
        if (scale == 'log') {
            plot.setRangeAxis(new LogarithmicAxis('frequency'))
        }


        File tempChartFile = File.createTempFile('chart', '.png')
        ChartUtilities.saveChartAsPNG(tempChartFile, chart, 500, 500)

        return tempChartFile.bytes
    }

}
