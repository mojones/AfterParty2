<h1>${title}</h1>
<gvisualization:scatterCoreChart
        dynamicLoading="${true}"
        elementId="ScatterPlot"
        width="${1000}" height="${1000}"
        columns="[['number', xLabel], ['number', yLabel]]"
        data="${plotData}"
        pointSize="${1}"
        legend="none"
        vAxis="${new Expando(title : yLabel)}"
        hAxis="${new Expando(title : xLabel)}"/>
<div id="ScatterPlot"></div>