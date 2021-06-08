/*
* Copyright (c) Ergon Informatik AG, Switzerland
*/
package ch.ergon.gradle.goodies.versioning

import static ch.ergon.gradle.goodies.versioning.VersioningPluginTestProject.*

/**
* Tests the effects of applying the 'egg-versioning' plugin.
*/
class VersioningPluginSpec extends VersioningPluginSpecification {

	def "version exactly on tag is described as 1.0.2" () {
		when:
		apply plugin: 'ch.ergon.gradle.goodies.versioning'

		and:
		checkout ON_TAG

		then:
		version() == "1.0.2"
	}

	def "version after tag is described properly" () {
		when:
		apply plugin: 'ch.ergon.gradle.goodies.versioning'

		and:
		checkout NOT_ON_TAG

		then:
		version() == "1.0.0-2-g59622ed"
	}

	def "version on a branch is described with tags from branch" () {
		when:
		apply plugin: 'ch.ergon.gradle.goodies.versioning'

		and:
		checkout '8cc2b58'

		then:
		version() == 'random_tag'
	}


	def "can add match clause to match database-* and replace the prefix" () {
		when:
		apply plugin: 'ch.ergon.gradle.goodies.versioning'

		and:
		checkout DATABASE_TAG

		then:
		version(match: 'database-*') == "1.0.0"
	}

	def "can add match clause and leave the tag alone" () {
		when:
		apply plugin: 'ch.ergon.gradle.goodies.versioning'

		and:
		checkout DATABASE_TAG

		then:
		version(match: "database-*", replaceGlobWith: GlobReplace.NO_REPLACE) == "database-1.0.0"
	}

	def "can find tag of main 'artifact' using no match" () {
		when:
		apply plugin: 'ch.ergon.gradle.goodies.versioning'

		and:
		checkout '6bff2b88e5'

		then:
		version() == "snap-1.0.3"
	}

	def "project has versioning extension" () {
		given:
		apply plugin: 'ch.ergon.gradle.goodies.versioning'

		expect:
		project.project.ergon.versioning != null
	}

	def "long version should be the same as non-tag version" () {
		when:
		apply plugin: 'ch.ergon.gradle.goodies.versioning'

		and:
		checkout DATABASE_TAG

		then:
		version(match: 'database-*', longFormat: true) == project.longVersion()
	}

	def "find tag of snap even if there are newer tags (which do not match)" () {
		when:
		apply plugin: 'ch.ergon.gradle.goodies.versioning'

		and:
		checkout WITH_MERGED_TAGS_LATER_COMMIT

		then:
		version(match: 'snap-*') == "1.0.3-18-gef20676"
	}

	def "find tag of database even if there are newer tags (which do not match)" () {
		when:
		apply plugin: 'ch.ergon.gradle.goodies.versioning'

		and:
		checkout WITH_MERGED_TAGS_LATER_COMMIT

		then:
		version(match: 'database-*', firstParentOnly: false) == "1.0.0-27-gef20676"
	}

	def "version() should find lightweight tags on first parent and use them" () {
		when:
		apply plugin: 'ch.ergon.gradle.goodies.versioning'

		and:
		checkout WITH_MERGED_TAGS_LATER_COMMIT

		then:
		version() == "1.3.0-11-gef20676"
	}

	def "early version() should find lightweight tags on first parent and use them" () {
		when:
		apply plugin: 'ch.ergon.gradle.goodies.versioning'

		and:
		checkout WITH_MERGED_TAGS_EARLY_COMMIT

		then:
		version() == "1.3.0-7-gd9b05af"
	}

	def "version(annotatedTagsOnly: true) should find annotated tags on first parent and use them" () {
		when:
		apply plugin: 'ch.ergon.gradle.goodies.versioning'

		and:
		checkout WITH_MERGED_TAGS_LATER_COMMIT

		then:
		version(annotatedTagsOnly: true) == "1.3.0-11-gef20676"
	}

	def "exactMatch() should return true when on a tag" () {
		when:
		apply plugin: 'ch.ergon.gradle.goodies.versioning'

		and:
		checkout ON_TAG

		then:
		version() == "1.0.2"
		exactMatch()
	}

	def "early version(annotatedTagsOnly: true) should find annotated tags on first parent and use them" () {
		when:
		apply plugin: 'ch.ergon.gradle.goodies.versioning'

		and:
		checkout WITH_MERGED_TAGS_EARLY_COMMIT

		then:
		version(annotatedTagsOnly: true) == "1.3.0-7-gd9b05af"
	}


	def "exactMatch() should return false when NOT on a tag" () {
		when:
		apply plugin: 'ch.ergon.gradle.goodies.versioning'

		and:
		checkout NOT_ON_TAG

		then:
		!exactMatch()
	}

	def "exactMatch() should find annotated tags and use them" () {
		when:
		apply plugin: 'ch.ergon.gradle.goodies.versioning'

		and:
		checkout ANNOTATED_TAG

		then:
		exactMatch()
	}

	def "early version(annotatedTagsOnly: false) should find lightweight tags and use them" () {
		when:
		apply plugin: 'ch.ergon.gradle.goodies.versioning'

		and:
		checkout WITH_MERGED_TAGS_EARLY_COMMIT

		then:
		version(firstParentOnly: false) == "1.2.1-9-gd9b05af"
	}

	def "early version(annotatedTagsOnly: true) should find annotated tags and use them" () {
		when:
		apply plugin: 'ch.ergon.gradle.goodies.versioning'

		and:
		checkout WITH_MERGED_TAGS_EARLY_COMMIT

		then:
		version(annotatedTagsOnly: true, firstParentOnly: false) == "1.2.1-9-gd9b05af"
	}


	def "version() should find lightweight tags and use them" () {
		when:
		apply plugin: 'ch.ergon.gradle.goodies.versioning'

		and:
		checkout WITH_MERGED_TAGS_LATER_COMMIT

		then:
		version(firstParentOnly: false) == "1.2.2-13-gef20676"
	}

	def "version(annotatedTagsOnly: true) should find annotated tags and use them" () {
		when:
		apply plugin: 'ch.ergon.gradle.goodies.versioning'

		and:
		checkout WITH_MERGED_TAGS_LATER_COMMIT

		then:
		version(annotatedTagsOnly: true, firstParentOnly: false) == "1.2.2-13-gef20676"
	}

	def "version() should return the proper tag on a commit with merged branches with tags" () {
		when:
		apply plugin: 'ch.ergon.gradle.goodies.versioning'

		and:
		checkout WITH_MERGED_TAGS_LATER_COMMIT

		then:
		version() == "1.3.0-11-gef20676"
	}

	def "version() should match the behaviour of CLI git without --first-parent" () {
		when:
		apply plugin: 'ch.ergon.gradle.goodies.versioning'

		and:
		checkout WITH_MERGED_TAGS_LATER_COMMIT

		then:
		version(firstParentOnly: false) == "1.2.2-13-gef20676"
	}

	def "version(abbreviate: 13) should reflect cli git behaviour" () {
		when:
		apply plugin: 'ch.ergon.gradle.goodies.versioning'

		and:
		checkout WITH_MERGED_TAGS_LATER_COMMIT

		then:
		version(abbreviate: 13) == "1.3.0-11-gef2067630d194"
	}
}
