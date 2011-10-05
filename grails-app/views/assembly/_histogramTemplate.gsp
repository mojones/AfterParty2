<h1>${title}</h1>
<gvisualization:lineCoreChart
        dynamicLoading="${true}"
        elementId="HistogramPlot"
        width="${1000}" height="${1000}"
        columns="[['string', xLabel], ['number', yLabel]]"
        data="${plotData}"
        pointSize="${1}"
        legend="none"
        hAxis="${new Expando(title : yLabel)}"
        vAxis="${new Expando(title : 'frequency')}"/>
<div id="HistogramPlot"></div>