$(document).ready(function() {
	initialize();
	var url = baseurl+ '/data';
	getSensors();
	
	$("#selectDevice").change(function() {
		getDeviceId();
	});
	
	markers = [];
	getData(url);
	
	$("#resetBtn").click(function() {
		setAllMap(null);
		markers = [];
		var url = baseurl+ '/data';
		getData(url);
	});	
	
	$("#searchBtn").click(function() {
		setAllMap(null);
		markers = [];
		getSearch();
	});	
	
	$(function() {
		$("#datefrom").datepicker({
			changeMonth: true,
			numberOfMonths: 2,
			dateFormat: "yy-mm-dd",
			onClose: function(selectedDate) {
				$("#dateto").datepicker("option", "minDate", selectedDate);
			}
		});
		$("#dateto").datepicker({
			changeMonth: true,
			numberOfMonths: 2,
			dateFormat: "yy-mm-dd",
			onClose: function(selectedDate) {
				$("#datefrom").datepicker("option", "maxDate", selectedDate);
			}
		});
	});
	
//	$(function() {
//		$(document).tooltip();
//	});
	
	$("#timefrom").timepicker();
	$("#timeto").timepicker();
	
});

function initialize() {
	var mapOptions = {
		zoom : 13,
		center : new google.maps.LatLng(33.3, -111.67),
		panControl:false,
		streetViewControl:false,
		zoomControlOptions: {
			//style:google.maps.ZoomControlStyle.SMALL,
			position:google.maps.ControlPosition.LEFT_CENTER
		}
	};
	map = new google.maps.Map(document.getElementById('map_canvas'),
			mapOptions);
	infowindow = new google.maps.InfoWindow();
}

function processGeoJson(results) {
	var data = arrayReverse(results.features);
	for (var i = 0; i < data.length; i++) {
		var rd = data[i];
		var coords = rd.geometry.coordinates;
		var latLng = new google.maps.LatLng(coords[1],coords[0]);
		var markImage;
		console.log("processGeoJson " + rd.properties.type);
		switch (rd.properties.type) {
		case 'Dylos': markImage = getDylosMarker(rd.properties.smallParticle); break;
		case 'Sensordrone': markImage = getSensordroneMarker(rd.properties.coData, rd.properties.co2Data); break;
		default:
		  markImage = imageURL[0];
		}
		
		var marker = new google.maps.Marker({
			position: latLng,
			map: map,
			title: rd.properties.type,
			icon: markImage,
			zIndex: i
			});
		markers.push(marker);
		//setAllMap(map);
		
		google.maps.event.addListener(marker, 'click', (function(marker, i, rd) {
			return function() {
			infowindow.close();
			infowindow.setContent(setContentString(rd.properties));
			infowindow.open(map, marker);
			}
			})(marker, i, rd));
	}		
}

function setAllMap(map) {
	  for (var i = 0; i < markers.length; i++) {
		markers[i].setMap(map);
	  }
}


