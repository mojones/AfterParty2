(function() {
    this.BioDrawing = (function() {
        function BioDrawing(empty) {
            this.empty = empty;
        }

        BioDrawing.prototype.paperWidth = 0;
        BioDrawing.prototype.drawingWidth = 0;
        BioDrawing.prototype.yPos = 0;
        BioDrawing.prototype.padding = 20;
        BioDrawing.prototype.pixelsPerBase = 10;
        BioDrawing.prototype.paper = '';
        BioDrawing.prototype.start = function(paperWidth, containerId) {
            this.paperWidth = paperWidth;
            this.drawingWidth = this.paperWidth - (this.padding * 2);
            return this.paper = Raphael(containerId, this.paperWidth, 2000);
        };
        BioDrawing.prototype.drawScale = function(length) {
            var base, interval, xPos;
            this.pixelsPerBase = this.drawingWidth / length;

            interval = length / 20;
            interval = Math.round(interval / 10) * 10
            this.paper.rect(this.padding, this.yPos, this.drawingWidth, 2).attr({
                fill: 'black',
                'stroke-width': '0'
            });
            for (base = 0; 0 <= length ? base <= length : base >= length; base += interval) {
                xPos = base * this.pixelsPerBase;
                this.paper.rect(xPos + this.padding, this.yPos, 1, 10).attr({
                    fill: 'black',
                    'stroke-width': '0'
                });
                this.paper.text(xPos + this.padding, this.yPos + 12, base);
            }
            return this.yPos = this.yPos + 20;
        };
        BioDrawing.prototype.drawChart = function(data, height) {
            var x, xValues;
            xValues = (function() {
                var _ref, _results;
                _results = [];
                for (x = 0, _ref = data.length; 0 <= _ref ? x <= _ref : x >= _ref; 0 <= _ref ? x++ : x--) {
                    _results.push(x * this.pixelsPerBase);
                }
                return _results;
            }).call(this);
            this.paper.g.linechart(this.padding, this.yPos, this.drawingWidth, height, xValues, data, {
                axis: '0 1 0 1'
            });
            return this.yPos = this.yPos + height + 20;
        };
        BioDrawing.prototype.drawBar = function(start, stop, height, colour, description, text) {
            var bar, width;
            width = (stop * this.pixelsPerBase) - (start * this.pixelsPerBase);
            bar = this.paper.rect((start * this.pixelsPerBase) + this.padding, this.yPos, width, height).attr({
                fill: colour,
                'stroke-width': '0',
                'title': description,
                'cursor': 'pointer'
            });
            var textPosition = ((start * this.pixelsPerBase) + (stop * this.pixelsPerBase)) / 2;
            var textYPosition = this.yPos + (height / 2);
            var textColour;

            if ($.inArray(colour, ['black', 'teal', 'olive', 'navy', 'blue', 'green', 'maroon']) > -1) {
                textColour = 'white';
            }
            else {
                textColour = 'black';
            }
            var title = this.paper.text(textPosition, textYPosition, text).attr({
                'font-size': (height * 0.9),
                'fill' : textColour
            })
            this.yPos = this.yPos + (height * 1.5); //the spacing between bars
            return bar;
        };
        BioDrawing.prototype.drawTitle = function(text) {
            var title;
            title = this.paper.text(this.drawingWidth / 2, this.yPos, text).attr({
                'font-size': 14
            });
            this.yPos = this.yPos + 20;
            return title;
        };
        BioDrawing.prototype.drawColouredTitle = function(text, colour) {
            var title;
            title = this.paper.text(this.drawingWidth / 2, this.yPos, text).attr({
                'font-size': 14,
                'fill' : colour
            });
            this.yPos = this.yPos + 20;
            return title;
        };
        BioDrawing.prototype.drawSpacer = function(pixels) {
            return this.yPos = this.yPos + pixels;
        };
        BioDrawing.prototype.getBLASTColour = function(bitscore) {
            var hitColour;
            if (bitscore < 1000000) {
                hitColour = 'red';
            }
            if (bitscore < 200) {
                hitColour = 'magenta';
            }
            if (bitscore < 80) {
                hitColour = 'lime';
            }
            if (bitscore < 50) {
                hitColour = 'blue';
            }
            if (bitscore < 40) {
                hitColour = 'black';
            }
            return hitColour;
        };
        BioDrawing.prototype.end = function() {
            return this.paper.setSize(this.paperWidth, this.yPos);
        };
        return BioDrawing;
    })();
}).call(this);
