// run this example with `$ npm run example`
// from the project root

var L = require('leaflet')
var leafletMap = require('./index')

var map = leafletMap().setView([0,0], 4)
L.tileLayer('https://{s}.tiles.mapbox.com/v3/examples.map-i86knfo3/{z}/{x}/{y}.png')
  .addTo(map)

L.marker([0,0])
  .bindPopup('Null Island!')
  .addTo(map)