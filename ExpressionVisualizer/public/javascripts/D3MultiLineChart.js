var margin = {top: 20, right: 80, bottom: 30, left: 50},
    width = 960 - margin.left - margin.right,
    height = 500 - margin.top - margin.bottom;

var x;
var y;
var data = [];

var s3BucketURL ;
var QuestionInfo = [];

var tooltipDiv;
var svg;
var line;
var color;

var xAxis;
var yAxis;

var parseDate = d3.time.format("%Y-%m-%dT%H:%M:%S.%LZ").parse;

function D3MultiLineChart (RawData) {

    QuestionInfo.length = 0
    data.length = 0
    s3BucketURL = RawData["serverImagesPath"]
    RawData["questionSurveyData"].forEach(
        function(r){
            QuestionInfo.push( {
                imageURI : r["imageURI"],
                questionEndTime : parseDate( r["questionEndTime"] ),
                questionStartTime : parseDate( r["questionStartTime"]),
                motorActionPerformed : parseDate( r["motorActionPerformed"] )
            });

            r["frameData"].forEach(function(s){

                var obj = {};
                obj["time"] = parseDate( s["datetime"]);
                var scoreObject = {};
                for(property in s["score"]["frameEmotionInfo"]){
                    scoreObject[property] = s["score"]["frameEmotionInfo"][property];
                }
                for(property in s["score"]["frameExpressionInfo"]){
                    scoreObject[property] = s["score"]["frameExpressionInfo"][property];
                }
                obj["score"] = scoreObject;
                obj["userCameraImagePath"] = s["score"]["userCameraImagePath"];
                obj["afterMotorAction"] = s["score"]["afterMotorAction"];
                data.push(obj);
            });
        }
    );

    x = d3.time.scale()
        .range([0, width]);

    y = d3.scale.linear()
        .range([height, 0]);

    color = d3.scale.category10();

    xAxis = d3.svg.axis()
        .scale(x)
        .orient("bottom");

    yAxis = d3.svg.axis()
        .scale(y)
        .orient("left");

    line = d3.svg.line()
        .interpolate("basis")
        .x(function(d) { return x(d.time); })
        .y(function(d) { return y(d.emotionScore); });

    svg = d3.select("#chart").insert("svg",":first-child")
        .attr("width", width + margin.left + margin.right)
        .attr("height", height + margin.top + margin.bottom)
        .append("g")
        .attr("transform", "translate(" + margin.left + "," + margin.top + ")");


    color.domain(d3.keys(data[0].score));

    var cities = color.domain().map(function(name) {
        return {
            name: name,
            values: data.map(function(d) {
                return {time: d.time, emotionScore: +d.score[name]};
            })
        };
    });

    x.domain(d3.extent(data, function(d) { return d.time; }));

    y.domain([
        d3.min(cities, function(c) { return d3.min(c.values, function(v) { return v.emotionScore; }); }),
        d3.max(cities, function(c) { return d3.max(c.values, function(v) { return v.emotionScore; }); })
    ]);

    svg.append("g")
        .attr("class", "xaxis")
        .attr("transform", "translate(0," + height + ")")
        .call(xAxis);

    svg.append("g")
        .attr("class", "y axis")
        .call(yAxis)
        .append("text")
        .attr("transform", "rotate(-90)")
        .attr("y", 6)
        .attr("dy", ".71em")
        .style("text-anchor", "end")
        .text("Emotion Score ");


    svg.append("defs").append("clipPath")		//clippath to display only the correct size
        .attr("id", "clip")
        .append("rect")
        .attr("width", width)
        .attr("height", height);

    var city = svg.selectAll(".city")
        .data(cities)
        .enter().append("g")
        .attr("class", "city");

    city.append("path")
        .attr("clip-path", "url(#clip)")
        .attr("class", "line")
        .attr("d", function(d) { return line(d.values); })
        .attr("id",function(d) { return d.name})
        .style("stroke", function(d) { return color(d.name); })
        .style("opacity",0);


    // Define 'div' for tooltips
    tooltipDiv = d3.select("body")
        .append("div")  // declare the tooltip div
        .attr("class", "tooltip")              // apply the 'tooltip' class
        .style("opacity", 0);


    svg.append("circle")
        .attr("class", "y")
        .style("fill", "green")
        .style("stroke", "blue")
        .attr("r", 4)
        .attr("opacity",0)

    var bisectDate = d3.bisector(function(d) { return d.time; }).left;

    svg.append("rect")
        .classed("DatePickerMousePosition",true)
        .attr(
        {
            "x":x(x.domain()[0]),
            "y":y(y.domain()[0])-20,
            "width": x(x.domain()[1])-x(x.domain()[0]),
            "height":40
        })
        .attr("fill","gray")
        .attr("opacity","0")
        .on("mouseout", function(){

            svg.select("circle.y")
                .attr("opacity",0)
        })
        .on("mousemove", function(){
            var x0 = x.invert(d3.mouse(this)[0]);
            var ycoordinate = d3.mouse(this)[1];

            var i = bisectDate(data, x0, 1);
            var d0 = data[i - 1];
            var d1 = data[i];
            var d = x0 - d0.time > d1.time - x0 ? d1 : d0;

            svg.select("circle.y")
                .attr("transform",
                "translate(" + x(d.time) + "," +
                y(y.domain()[0]) + ")")
                .attr("opacity",1)
                .style("fill",function(){
                    if(d.afterMotorAction == true){
                        return "red";
                    }
                    return "black";
                })

            if( Math.abs( x(d.time) - x(x0) ) < 6 && Math.abs( y(y.domain()[0]) - ycoordinate ) < 6 ){
                console.log("inside the circle")
                svg.selectAll(".tooltipLine")
                    .attr("stroke","gray")
                    .remove()

                svg.append("line")
                    .attr("class","tooltipLine")
                    .attr( {
                        "x1":x(d.time),
                        "y1":y(y.domain()[0]),
                        "x2":x(d.time),
                        "y2":y(y.domain()[1]),
                        "stroke":"gray"
                    } )
                    .style("pointer-events", "none");

                tooltipDiv
                    .style("opacity", 1)
                    .style("pointer-events", "none");;

                tooltipDiv	.html(
                    d.userCameraImagePath != "" ?
                    '<img class="RotateImage" height = "90" width = "140"  src = "' + s3BucketURL + d.userCameraImagePath +'">' : ""
                )
                    .style("left", (d3.event.pageX) + "px")
                    .style("top", (d3.event.pageY - 28) + "px");
            }else{
                console.log("out of the small circle")
                tooltipDiv
                    .style("opacity", 0);
                svg.selectAll(".tooltipLine").transition().duration(100).attr("stroke","gray").transition().duration(100).remove()

            }
        });

    var allEmotions = d3.select("#jsonDataDisplay").append("div")
        .attr("class","container-fluid row")
        .selectAll(".emotionConfigurations").data(color.domain())
        .enter().append("div").attr("class","col-xs-4 col-sm-2 col-md-2");

    allEmotions.append("span").style("cursor","pointer")
        .style("color",function(v){return color(v);})
        .text(function(v){return v;})
        .style("font-weight","bold");

    allEmotions.append("input").attr("type","checkbox").on("change",function(){
        d3.selectAll("#"+this.__data__).style("opacity",this.checked?1:0);});

    var infoSVGDivs = d3.select("#jsonDataDisplay").append("ul")
        .selectAll(".imagedivs").data(QuestionInfo)
        .enter().append("li");

    infoSVGDivs.append("img")
        .attr("id",function(d,i){

        })
        .attr("src", function(d){
            return d.imageURI;
        })
        .attr("width", function(d){
            return width/QuestionInfo.length;
        })
        .attr("height", function(d){
            //make height same as width so that we can have square spaces for images
            return width/QuestionInfo.length;
        })
        .on("click",ClickedQuestionImage);

};

function ClickedQuestionImage (d){

    resetAxisZoom();
    var isSelected = d3.select(this).classed("selected");

    d3.selectAll("img,.selected")
        .attr({"width":width/QuestionInfo.length,"height":width/QuestionInfo.length});

    d3.selectAll("img,.selected").classed("selected",false);


    d3.select(this).classed("selected",!isSelected);

    if(!isSelected){
        d3.select(this)
            .attr("width",(width/QuestionInfo.length)*1.5)
            .attr("height",(width/QuestionInfo.length)*1.5);

        svg.append("line")
            .attr("class","BoundryLine")
            .attr(
            {
                "x1":x(d.motorActionPerformed),
                "y1":y(y.domain()[1]),
                "x2":x(d.motorActionPerformed),
                "y2":y(y.domain()[0]),
                "stroke":"red",
                "stroke-width":2,
                "stroke-dasharray":"5,5"
            }
        );

        svg.append("rect")
            .classed("BoundryLine",true)
            .attr(
            {
                "x":x(d.questionStartTime),
                "y":y(y.domain()[1]),
                "width": function(b){
                    return x(d.questionEndTime)-x(d.questionStartTime);
                },
                "height":y(y.domain()[0])-y(y.domain()[1])
            })
            .attr("fill","grey")
            .attr("stroke","black")
            .attr("opacity",".5")
            .style("pointer-events", "none")
            //.on("click", BoundingBoxClicked);
    }
};

function BoundingBoxClicked(d) {
    tooltipDiv
        .style("opacity", 0);
    svg.selectAll(".tooltipLine").remove()

    var clickedBoundry = d3.select(this);
    var isActiveAlready = d3.select(this).classed("active");

    clickedBoundry.classed("active", true);
    var boundryWidth = clickedBoundry.attr("width"),
        boundryHeight = clickedBoundry.attr("height"),
        boundaryX = clickedBoundry.attr("x"),
        boundaryY = clickedBoundry.attr("y")

    x.domain([x.invert(boundaryX),x.invert(parseFloat( boundaryX ) + parseFloat( boundryWidth ))])
    d3.selectAll(".xaxis")
        .transition().duration(1000).ease("sin-in-out")  // https://github.com/mbostock/d3/wiki/Transitions#wiki-d3_ease
        .call(xAxis);

    d3.selectAll(".line")
        .transition()
        .duration(1000)
        .ease("linear")
        .attr("d", function(d) { return line(d.values); })

    d3.selectAll(".BoundryLine").remove();

};
function resetAxisZoom() {

    tooltipDiv.style("opacity", 0);
    svg.selectAll(".tooltipLine").remove()


    x.domain(d3.extent(data, function(d) { return d.time; }));
    d3.selectAll(".xaxis")
        .transition().duration(1500).ease("sin-in-out")
        .call(xAxis);

    d3.selectAll(".line")
        .transition()
        .duration(1500)
        .ease("linear")
        .attr("d", function(d) { return line(d.values); })

    d3.selectAll(".BoundryLine").remove();

};