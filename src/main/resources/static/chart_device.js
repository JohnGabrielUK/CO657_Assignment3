var chartTitle;
var labels;
dataIsReady = false;
chartIsReady = false;
firstData = null;

function setChartReady() {
	chartIsReady = true;
	if (dataIsReady) drawChart(firstData);
}

function setDataReady(dataStuff) {
	if (!dataIsReady) {
		dataIsReady = true;
		firstData = dataStuff;
		if (chartIsReady) drawChart(dataStuff);
	}
}

function drawChart(chartDataStuff) {
	var data = new google.visualization.DataTable();
	data.addColumn('datetime', 'Timestamp');
	for (i = 0; i < labels.length; i++) {
		data.addColumn('number', labels[i]);
	}
	$.each(chartDataStuff, function(i, next) {
		data.addRows([next]);
	});
  	var options = {
		title: chartTitle,
		height: 330,
		legend: { position: 'right' }
	};
	
	var chart = new google.visualization.LineChart(document.getElementById('chart_div'));
	chart.draw(data, options);
}

function getChartData(id, res) {
	var chartDataStuff = new Array();
	$.getJSON("http://localhost:8080/json/deviceData?id=" + id + "\u0026res=" + res,
		function(result) {
		$.each(result.values, function(i, next) {
			var dateOfThisThing = new Date(next[0][0],
				next[0][1],
				next[0][2],
				next[0][3],
				next[0][4],
				next[0][5]);
			var valueOfThisThing = next[1];
			var copy = next.slice();
			copy[0] = dateOfThisThing;
			chartDataStuff.push(copy);
		})
		setDataReady(chartDataStuff);
	});
	return chartDataStuff;
}