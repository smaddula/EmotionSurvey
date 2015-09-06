function D3MultiLineChart (data) {
    var margin = {top: 20, right: 80, bottom: 30, left: 50},
        width = 960 - margin.left - margin.right,
        height = 500 - margin.top - margin.bottom;

    var parseDate = d3.time.format("%Y-%m-%dT%H:%M:%S.%LZ").parse;

    var x = d3.time.scale()
        .range([0, width]);

    var y = d3.scale.linear()
        .range([height, 0]);

    var color = d3.scale.category10();

    var xAxis = d3.svg.axis()
        .scale(x)
        .orient("bottom");

    var yAxis = d3.svg.axis()
        .scale(y)
        .orient("left");

    var line = d3.svg.line()
        .interpolate("basis")
        .x(function(d) { return x(d.time); })
        .y(function(d) { return y(d.emotionScore); });

    var svg = d3.select("#chart").insert("svg",":first-child")
        .attr("width", width + margin.left + margin.right)
        .attr("height", height + margin.top + margin.bottom)
        .append("g")
        .attr("transform", "translate(" + margin.left + "," + margin.top + ")");


    color.domain(d3.keys(data[0].score));

    data.forEach(function(d) {
        d.time = parseDate(d.datetime);
    });

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
        .attr("class", "x axis")
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
        .text("Emotion Score (??F)");

    var city = svg.selectAll(".city")
        .data(cities)
        .enter().append("g")
        .attr("class", "city");

    city.append("path")
        .attr("class", "line")
        .attr("d", function(d) { return line(d.values); })
        .attr("data-legend",function(d) { return d.name})
        .style("stroke", function(d) { return color(d.name); });

    /*city.append("text")
     .datum(function(d) { return {name: d.name, value: d.values[d.values.length - 1]}; })
     .attr("transform", function(d) { return "translate(" + x(d.value.time) + "," + y(d.value.emotionScore) + ")"; })
     .attr("x", 3)
     .attr("dy", ".35em")
     .text(function(d) { return d.name; });*/
    legend = svg.append("g")
        .attr("class","legend")
        .attr("transform","translate(50,30)")
        .style("font-size","12px")
        .call(d3.legend);

    /*                            setTimeout(function() {
     legend
     .style("font-size","20px")
     .attr("data-style-padding",10)
     .call(d3.legend)
     },1000)
     */
};