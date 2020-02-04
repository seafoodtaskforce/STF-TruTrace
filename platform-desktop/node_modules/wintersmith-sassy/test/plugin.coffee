vows = require 'vows'
assert = require 'assert'
wintersmith = require 'wintersmith'

suite = vows.describe 'Plugin'

suite.addBatch
  'wintersmith environment':
    topic: -> wintersmith './example/config.json'
    'loaded ok': (env) ->
      assert.instanceOf env, wintersmith.Environment
    'contents':
      topic: (env) ->
        env.mode = 'preview'
        env.load @callback
      'loaded ok': (result) ->
        assert.instanceOf result.contents, wintersmith.ContentTree
      'has plugin instances': (result) ->
        assert.instanceOf result.contents['test.scss'], wintersmith.ContentPlugin
        assert.isArray result.contents._.styles
        assert.lengthOf result.contents._.styles, 2
      'sass files':
        topic: (result, env) ->
          result.contents['test.scss'].render @callback
        'has result': (error, result) ->
          assert.ifError error
          assert.ok result?
        'has sourcemap': (error, result) ->
          assert.ok /sourceMappingURL/.test result.toString()

suite.export module
