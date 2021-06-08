/*
* Copyright (c) Ergon Informatik AG, Switzerland
*/
package ch.ergon.gradle.goodies.versioning

import static ch.ergon.gradle.goodies.versioning.VersioningPluginTestProject.*

/**
* Tests the effects of applying the 'egg-versioning' plugin and
* post-processing the version.
*/
class VersioningPluginPostprocessingSpec extends VersioningPluginSpecification {

	def "short version is left alone by default"() {
		when:
		apply plugin: 'ch.ergon.gradle.goodies.versioning'

		and:
		checkout ON_TAG

		then:
		version() == "1.0.2"
	}

	def "long version is left alone by default" () {
		when:
		apply plugin: 'ch.ergon.gradle.goodies.versioning'

		and:
		checkout NOT_ON_TAG

		then:
		version() == "1.0.0-2-g59622ed"
	}

	def "short version is left alone by STRIP_NR_COMMITS_AND_G"() {
		when:
		apply plugin: 'ch.ergon.gradle.goodies.versioning'

		and:
		checkout ON_TAG

		then:
		version(postProcessVersion: PostProcessVersion.STRIP_NR_COMMITS_AND_G) == "1.0.2"
	}

	def "long version is properly processed by STRIP_NR_COMMITS_AND_G"() {
		when:
		apply plugin: 'ch.ergon.gradle.goodies.versioning'

		and:
		checkout NOT_ON_TAG

		then:
		version(postProcessVersion: PostProcessVersion.STRIP_NR_COMMITS_AND_G) == "1.0.0-59622ed"
	}

	def "one can add a custom closure to process the version"() {
		when:
		apply plugin: 'ch.ergon.gradle.goodies.versioning'

		and:
		checkout NOT_ON_TAG

		then:
		version(
				postProcessVersion: { version ->
					"can-haz-special-k-${version}"
				}
				) == "can-haz-special-k-1.0.0-2-g59622ed"
	}
}