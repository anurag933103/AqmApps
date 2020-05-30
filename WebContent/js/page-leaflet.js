$(document).ready(function() {
	initialize();
	var url = baseurl+ '/data';
	getSensors();
	
	$("#selectDevice").change(function() {
		getDeviceId();
	});
	
	getData(url);
	
	$("#resetBtn").click(function() {
		var url = baseurl+ '/data';
		getData(url);
	});	

	$("#searchBtn").click(function() {
		getSearch();
	});		
});

function initialize() {
	map = L.map('map_canvas').setView([33.3, -111.67], 13);
	L.tileLayer('http://{s}.tile.osm.org/{z}/{x}/{y}.png', {
		maxZoom: 25,
		attribution: '&copy; <a href="http://osm.org/copyright">OpenStreetMap</a> contributors'
	}).addTo(map);

//	var popup = L.popup();
//	function onMapClick(e) {
//		popup
//			.setLatLng(e.latlng)
//			.setContent("You clicked the map at " + e.latlng.toString())
//			.openOn(map);
//	}
//	map.on('click', onMapClick);
}

function processGeoJson(results) {
	var data = arrayReverse(results.features);
	map.removeLayer(geoJsonLayer);
	geoJsonLayer = L.geoJson(data, {
		pointToLayer: function (feature, latlng) {
			var icon;
			switch (feature.properties.type) {
			case 'Dylos': icon = getDylosMarker(feature.properties.smallParticle); break;
			case 'Sensordrone': icon = getSensordroneMarker(feature.properties.coData, feature.properties.co2Data); break;
			default:
				icon = imageURL[0];
			}
			var markerIcon = L.icon({
			    iconUrl: icon,
			    iconSize:     [20, 34], // size of the icon
			    iconAnchor:   [10, 33], // point of the icon which will correspond to marker's location
			    popupAnchor:  [0, -28] // point from which the popup should open relative to the iconAnchor
			});				
	        return new L.Marker(latlng, {
	        	icon: markerIcon
	        });
	    },
		onEachFeature: onEachFeature
	});
	map.addLayer(geoJsonLayer);    
}

function onEachFeature(feature, layer) {
	var popupContent = setContentString(feature.properties);
	layer.bindPopup(popupContent);
}


