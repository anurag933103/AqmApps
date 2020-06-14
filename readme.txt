There are 2 applications, one a web application in WebContent that interacts with
the AQM API to map air quality readings. The other is a command-line program to 
simulate air quality readings over time in a given rectangular region define by lat/long.
The mapping app supprots Leaflet and Google Maps, though Google maps requires you to obtain
your own API key on GCP. You then put your key in the apikey.txt file and the build script
will automagically incorporate it into the generated app. 

This project holds 2 simple mapping applications that render data from the
AQM REST API. Pins are dropped with a D or S dependening on which air quality
sensor the read was taken from (Dylos or Sensordrone), and colored according
to default sensor thresholds.

Data format: GeoJSON
{
	"type": "FeatureCollection",
	"features": [
		{
			"type":"Feature",
			"properties": {
				"type":"Dylos",
				"deviceId":"aqm5",
				"dateTime":"Fri Mar 28 07:24:04 MST 2014",
				"method":"manual",
				"userId":"user5",
				"smallParticle":71,
				"largeParticle":14
			},
			"geometry": {
				"type":"Point",
				"coordinates":[-111.680775,33.301878]
			}
		},
		{
			"type":"Feature",
			"properties": {
				"type":"Sensordrone",
				"deviceId":"SensorDroneB8:FF:FE:B9:C3:FA",
				"dateTime":"Thu Mar 27 07:22:42 MST 2014",
				"method":"Network",
				"co2SensorId":"unknown",
				"coData":-2,
				"co2Data":-1,
				"presureData":96264,
				"tempData":25,
				"humidityData":35
			},
			"geometry": {
				"type":"Point",
				"coordinates":[-111.6725,33.309986]
			}		
		}
	]
}

Air Quality rating
Dylos:   Small Particle Reading
	1000 +   	= Hazardous	red	4
	350-1000 	= Bad		orange	3
	100-350  	= Moderate		yellow	2
	0-100   	= Good		green 	1

	none paleblue	
	
Sensordrone:   CO Reading
	0-5 	= Good
	5-10 	= Moderate
	10-35 	= Bad
	35 + 	= Hazardous
	
	CO2 Reading
	0-1000 Good
	1000-1500 Moderate
	1500 +  Bad
	
