package afterparty

import java.math.MathContext


class AnnotationDrawer {

    StringBuilder result = new StringBuilder()

    // copied from the normal NCBI blast results viewer as these will presumably be familiar to most
    static bitScoreToColour = [
            40 : 'black',
            50 : 'blue',
            80 : 'lime',
            200 : 'magenta',
            1000000000: 'red'
    ]

    int paperWidth = 1000
    int padding = 10
    int drawingWidth = paperWidth - padding*2

    float pixelsPerBase

    int yPos = 0

    def addLine(String line) {
        result.append(line + "\n")
    }

    def start() {
        return """
        window.onload = function() {
        var paper = Raphael('annotation', $paperWidth, $yPos);
        """
    }

    def finish() {
        return """
        }
        """
    }



    def sequence(def properties) {
        pixelsPerBase = drawingWidth / properties.length
        def interval = new BigDecimal(properties.length / 10, new MathContext(1))

        println "$pixelsPerBase pixels per base, interval is $interval"

        // draw horizontal line
        addLine "paper.rect($padding, $yPos, $drawingWidth, 2).attr({fill: 'black', 'stroke-width' : '0'})"
//        addLine "seq"

        // draw tick marks and labels
        for (int base = 0; base <= properties.length; base += interval) {
            int xPos = base * pixelsPerBase
            addLine "paper.rect(${xPos+padding}, $yPos, 1, 10).attr({fill: 'black', 'stroke-width' : '0'})"
            addLine "paper.text(${xPos+padding}, ${yPos+12}, $base)"
        }

        // move the cursor down
        yPos += 20

    }

    def quality(def properties){
        int chartHeight = 100
        // get quality values by splitting the string and converting to integers
        def yValues = properties.qualityString.split(/\s/).collect{it.toInteger()}
        // x values have to be scaled so that they fit the width
        def xValues = (0..yValues.size()).collect({it * pixelsPerBase})
        addLine "paper.g.linechart($padding, $yPos, $drawingWidth, $chartHeight, $xValues, $yValues, {axis: '0 1 0 1'})"

        yPos += chartHeight + 20
    }

    def blastHit(def myHit){
        def start = myHit.start * pixelsPerBase
        def width = (myHit.stop - myHit.start) * pixelsPerBase
        def colour =  bitScoreToColour.find({it.key > myHit.bitscore}).value
        addLine "var $myHit.accession = paper.rect(${start+padding}, $yPos, $width, 8).attr({fill: '$colour', 'stroke-width' : '0', 'title' : '$myHit.description', 'cursor' : 'pointer'})"
        addLine """
        ${myHit.accession}.hover(
                function (event) {
                    this.attr({stroke: 'black', 'stroke-width' : '5'});
                    \$('#${myHit.accession}').css("background-color","bisque");
                },
                function (event) {
                    this.attr({stroke: 'black', 'stroke-width' : '0'});
                    \$('#${myHit.accession}').css("background-color","white");
                }
        );
        """
        yPos += 20

    }

    def title(def properties){
        addLine "paper.text(${paperWidth/2}, ${yPos+10}, '$properties.text').attr({'font-size' : 14})"
        yPos += 30

    }

    static String drawAnnotation(def cls) {
        AnnotationDrawer drawer = new AnnotationDrawer()

        drawer.with(cls)

//        println "rendering :"
//        println drawer.start() + drawer.result.toString() + drawer.finish()

        return drawer.start() + drawer.result.toString() + drawer.finish()
    }

}

