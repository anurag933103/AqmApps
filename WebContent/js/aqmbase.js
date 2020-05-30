//don't move the imageURL array's order
const imageURL = [
		    'http://maps.google.com/mapfiles/marker.png',
		    'images/paleblue_MarkerD.png',  //dylos
			'images/darkgreen_MarkerD.png',
			'images/yellow_MarkerD.png',
			'images/orange_MarkerD.png',
			'images/red_MarkerD.png',
			'images/paleblue_MarkerS.png',  //sensordrone
			'images/darkgreen_MarkerS.png',
			'images/yellow_MarkerS.png',
			'images/orange_MarkerS.png',
			'images/red_MarkerS.png',
		];

function getDylosMarker(rd) {
	var img;
	var rate;
	if (rd >= 0) {rate = 2;} //Good 
	else {rate = 1;} //no data
	if (rd >= 100) rate = 3; //Moderate
	if (rd >= 350) rate = 4; //Bad
	if (rd >= 1000) rate = 5; //Hazardous
	img = imageURL[rate];
	return img;
}

function getSensordroneMarker(co, co2) {
	var img;
	var rateco, rateco2;
	if (co >= -3) {rateco = 7;} //Good 
	else {rateco = 6;} //no data
	if (co >= 5) rateco = 8; //Moderate
	if (co >= 10) rateco = 9; //Bad
	if (co >= 35) rateco = 10; //Hazardous	
	
	if (co2 >= 0) {rateco2 = 7;} //Good 
	else {rateco2 = 6;} //no data
	if (co2 >= 1000) rateco2 = 8; //Moderate
	if (co2 >= 1500) rateco2 = 9; //Bad

	var rate;
	if(rateco >= rateco2) {
		rate = rateco;
	} else {rate = rateco2;}

	img = imageURL[rate];
	return img;
}

function setContentString(prop) {
	  var string = '<div id="infor_string">'+
	  '<div><b>';
	  string += prop.type + ' - ' + prop.deviceId + '</b></div>';
	  string += '<div>' + prop.dateTime + '</div>';
	  string += '<div>' + 'method: ' + prop.method + '</div>';
	  string += '<p> </p>';
	  
	  switch (prop.type) {
	  case 'Dylos': 
		  string += '<div>' + 'userId: ' + prop.userId + '</div>';
		  string += '<div>' + 'smallParticle: ' + prop.smallParticle + '</div>';
		  string += '<div>' + 'largeParticle: ' + prop.largeParticle + '</div></div>';
		  break;
	  case 'Sensordrone': 
		  string += '<div>' + 'co2SensorId: ' + prop.co2SensorId + '</div>';
		  string += '<div>' + 'coData: ' + prop.coData + '</div>';
		  string += '<div>' + 'co2Data: ' + prop.co2Data + '</div>';
		  string += '<div>' + 'pressureData: ' + prop.presureData + '</div>';
		  string += '<div>' + 'tempData: ' + prop.tempData + '</div>';
		  string += '<div>' + 'humidityData: ' + prop.humidityData + '</div></div>';
		  break;
	  default:
		  string += '<div> No Data Read</div></div>';
	  }

	  return string;
}

function getData(url) {
	console.log("getData on URL " + url);
	$.ajax({
		type:"GET",
		url: url,
		dataType: 'json',
		success: function(results) {
			processGeoJson(results);
		}
	});	
}

function getSensors() {
	$.ajax({
		type:"GET",
		url: baseurl+ '/sensors',
		dataType: 'json',
		success: function(results) {
			var $select = $('#selectDevice');
			$select.html('<option value="All">Select Sensor</option>');
			for (var i = 0; i < results.length; i++) {
				$select.append('<option value="' + results[i] + '">' + results[i] + '</option>');
			}
		}
	});	
}

function getDeviceId() {
	var value = $("#selectDevice").val();
	if (value == 'All') {
		$("#selectId").hide();
	} else {
		var url = baseurl+ '/sensors/' + value;
		$.ajax({
			type:"GET",
			url: url,
			dataType: 'json',
			success: function(results) {
				var $select = $('#selectId');
				$select.show();
				$select.html('<option value="All">Select DeviceId</option>');
				for (var i = 0; i < results.length; i++) {
					$select.append('<option value="' + results[i] + '">' + results[i] + '</option>');
				}					
			}
		});				
	}	
}

function updateQueryStringParameter(uri, key, value) {
	  var re = new RegExp("([?&])" + key + "=.*?(&|$)", "i");
	  var separator = uri.indexOf('?') !== -1 ? "&" : "?";
	  if (uri.match(re)) {
	    return uri.replace(re, '$1' + key + "=" + value + '$2');
	  }
	  else {
	    return uri + separator + key + "=" + value;
	  }
	}

function getDateTimeVal(url) {
	var datetime = [];
	var startdate, enddate, starttime, enddate;
	var patt1 = /\d\d\d\d-\d\d-\d\d/;
	var patt2 = /\d\d:\d\d/;
	
	startdate = $("#datefrom").val();
	enddate = $("#dateto").val();

	startdate = startdate.match(patt1);
	startdate = ""+startdate;
	startdate = startdate.replace(/-/g, "");
	//startdate = ""+startdate;
	enddate = enddate.match(patt1);
	enddate = ""+enddate;
	enddate = enddate.replace(/-/g, "");
	//enddate = ""+enddate;
	
	if(startdate != 'null') {
		starttime = $("#timefrom").val();
		starttime = starttime.match(patt2);
		starttime = ""+starttime+"00";
		starttime = starttime.replace(/:/, "");

		startdate += starttime;
		
		url = updateQueryStringParameter(url, "start", startdate);
	}
	if(enddate != 'null') {
		endtime = $("#timeto").val();
		endtime = endtime.match(patt2);
		endtime = ""+endtime+"00";
		endtime = endtime.replace(/:/, "");
		
		enddate += endtime;
		
		url = updateQueryStringParameter(url, "end", enddate);
	}
	return url;
}


function getSearch() {
	var url = baseurl + '/data';
	var device = $("#selectDevice").val();
	var num = $("#selectNum").val();
	var id = $("#selectId").val();
	if (device != 'All') {
		url = url+ '/' + device;
		if (id != 'All') {
			url += '/' + id;
		}
	}
	
	if(!num) num=0;
	url = updateQueryStringParameter(url, "num", num);
	url = getDateTimeVal(url);
	
	//alert(url);
	console.log(url);
	getData(url);		
}

function arrayReverse(data) {
	var reversedata = [];
	for(var i = data.length-1; i>= 0; i--) {
		reversedata.push(data[i]);
	}
	return reversedata;
}

