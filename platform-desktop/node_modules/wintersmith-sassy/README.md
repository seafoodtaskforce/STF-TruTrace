wintersmith-sassy
=================

[Sass](http://sass-lang.com) plugin for [wintersmith](https://github.com/jnordberg/wintersmith) using [node-sass](https://github.com/sass/node-sass).

## Installing

Install globally or locally using npm

```
npm install [-g] wintersmith-sassy
```

and add `wintersmith-sassy` to your wintersmith config

```json
{
  "plugins": [
    "wintersmith-sassy"
  ]
}
```

or just run `wintersmith plugin install sassy`


## Options

In your wintersmith config (defaults shown):

```javascript
{
  "sass": {
    "debug": undefined, // defaults to true when previewing, false otherwise
    "includePaths": [] // include paths relative to your project root, eg ["./my-sass-libs"]
  ]
}
```

If `debug` is `true` source maps will be in-lined and the output will be pretty formatted, if `false` the output will be compressed css without maps.


## Running tests

```
npm install
npm test
```
