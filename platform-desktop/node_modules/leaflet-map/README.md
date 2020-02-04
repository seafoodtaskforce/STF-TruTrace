# leaflet-map
simple way to make a leaflet map application

## usage
```js
var L = require('leaflet')
var leafletMap = require('leaflet-map')

var map = leafletMap()
L.tileLayer('https://{s}.tiles.mapbox.com/v3/examples.map-i86knfo3/{z}/{x}/{y}.png')
  .addTo(map)

L.marker([0,0])
  .bindPopup('Null Island!')
  .addTo(map)
```

Use with [browserify](https://npm.im/browserify) for best results!

## run the example
from the project root,
```console
$ npm run example
```


## api
### `leafletMap(opt?: Object) => LeafletMap`

creates a DOM `Element` and initializes a Leaflet map in it.

`opt` is an object with any of the following properties:

- `defaultStyle` boolean (default: `true`). Whether to include CSS and set basic page styling to render a full-page map.

## installation

    $ npm install leaflet-map


## running the tests

From package root:

    $ npm install
    $ npm test


## contributors

- jden <jason@denizac.org>


## license

ISC. (c) MMXIV jden <jason@denizac.org>. See LICENSE.md
