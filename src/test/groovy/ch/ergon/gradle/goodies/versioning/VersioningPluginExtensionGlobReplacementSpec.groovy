/*
* Copyright (c) Ergon Informatik AG, Switzerland
*/
package ch.ergon.gradle.goodies.versioning

import spock.lang.Specification
import java.util.regex.Pattern

/**
* Tests the effects of applying the 'egg-versioning' plugin.
*/
class VersioningPluginExtentionGlobReplacementSpec extends Specification {

	def pattern(String glob) {
		Pattern.compile VersioningExtension.createRegexFromGlob(glob)
	}

	def "returned regex should match a trivial case" () {
		expect:
		pattern("database-*")
				.matcher("database-1.0.0")
				.matches()
	}

	def "replace prefix should replace the prefix" () {
		def version = GlobReplace.REPLACE_PREFIX("database-*", "database-1.0.0")
		expect:
		version == "1.0.0"
	}

	def "no_replace should leave the tag alone" () {
		def version = GlobReplace.NO_REPLACE("database-*", "database-1.0.1")
		expect:
		version == "database-1.0.1"
	}
}