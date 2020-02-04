path = require 'path'
sass = require 'node-sass'

module.exports = (env, callback) ->

  options = env.config.sass ? {}

  # output source map and uncompressed output, defaults to true if in preview mode
  options.debug ?= (env.mode is 'preview')

  # extra include paths
  includePaths = (options.includePaths ? []).map (path) -> env.resolvePath path

  class SassPlugin extends env.ContentPlugin

    constructor: (@filepath) ->

    render: (callback) ->
      # ignore partials starting with underscore
      if (path.basename @filename)[0] is '_'
        do callback
        return
      opts =
        file: @filepath.full
        includePaths: includePaths
        sourceMapEmbed: options.debug
        sourceMapContents: options.debug
        outputStyle: if options.debug then 'nested' else 'compressed'
      sass.render opts, (error, result) ->
        unless error?
          rv = result.css
        callback error, rv

    getFilename: -> @filepath.relative.replace /s(a|c)ss$/, 'css'

    getView: -> (..., callback) -> @render callback

  SassPlugin.fromFile = (filepath, callback) ->
    callback null, new SassPlugin filepath

  env.registerContentPlugin 'styles', '**/*.*(sass|scss)', SassPlugin

  callback()
