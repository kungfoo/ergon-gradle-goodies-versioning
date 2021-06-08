/*
* Copyright (c) Ergon Informatik AG, Switzerland
*/
package ch.ergon.gradle.goodies.versioning

import org.gradle.api.Project
import spock.lang.Specification

/**
* Tests the effects of using the VersionNumbers.DEFAULT implementation
* to generate a monotonously rising version number from git describe format
* versions.
*/
class DefaultMonotonouslyIncreasingVersionNumberSpec extends BuildConfigurationSpecification {

	def Project project = projectWithPlugins('ch.ergon.gradle.goodies.versioning')

	MonotonouslyIncreasingVersionNumber v = new DefaultMonotonouslyIncreasingVersionNumber(project: project)

	def setup() {
		project.versioning.versionNumberPadding = 3
	}

	def versionNumber(String longDescribeFormat) {
		v.calculate longDescribeFormat
	}

	def "returned long should work for '1.0.1'" () {
		expect:
		versionNumber("1.0.1-12-g786312") == 1000001012
	}

	def "returned version number should also work for more dotted version numbers" () {
		expect:
		versionNumber("2.3.4.5.1-3-g12312") == 2003004005001003
	}

	def "version number should also work for short version strings" () {
		expect:
		number == v.calculate(versionString)

		where:
		versionString	| number
		"1.2-0-g43"		| 1002000
		"1.2-12-g32"	| 1002012
		"1.2-123-g373"	| 1002123
		"1.2.1-0-g32" 	| 1002001000
		"0.19.0-0-g736" | 19000000
	}

	def "version number claims in the javadoc should be right right" () {
		given:
		def tag = "2.3.4.1-0-g876"
		def afterTag = "2.3.4.1-12-g987"

		expect:
		versionNumber(tag)      == 2003004001000
		versionNumber(afterTag) == 2003004001012
	}

	def "version number should be monotonously rising for quite some samples" () {
		given:
		// sort these to make sure they are monotonously rising.
		def majorVersions = [
			1,
			2,
			3,
			4,
			5,
			6,
			12,
			14,
			15,
			29
		].sort()
		def minorVersions = [0, 1, 2, 3, 4, 10, 11].sort()
		def fixVersions = [0, 3, 4, 5, 6].sort()
		def commits = [0, 1, 12, 22].sort()

		// note: this is not the same as combinations()
		def versions = majorVersions.collect { maj ->
			minorVersions.collect { min ->
				fixVersions.collect { fix ->
					commits.collect { n ->
						return "$maj.$min.$fix-$n-g8912763"
					}
				}
			}
		}.flatten()

		def numbers = versions.collect { versionNumber(it) }
		def lastNumber = Long.MIN_VALUE

		expect:
		numbers.each { number ->
			assert lastNumber < number
			lastNumber = number
		}
	}

	def "version should be shorter and convert to an int with less padding" () {
		given:
		project.versioning.versionNumberPadding = 2
		def tag = "3.2.1-12-g6723"
		def expectedLong = 3020112L

		expect:
		versionNumber(tag) == expectedLong
		expectedLong == (int) expectedLong
	}

	def "version number should also work with more padding" () {
		given:
		project.versioning.versionNumberPadding = 4

		expect:
		versionNumber("10.2.3-0-g7387") == 10000200030000L
	}

	def "version number should not choke on -dirty versions" () {
		given:
		def tag = "0.1.0-57-g62b1f9a-dirty"

		expect:
		versionNumber(tag) == 1000057
	}

	def "version number should not choke on prefix that is matched" () {
		given:
		project.versioning.match = "version-*"
		def tag = "version-1.9.2"

		expect:
		versionNumber(tag) == 1L // instead of choking, return 1L
	}

	def "version number should not choke on random version" () {
		given:
		def tag = "version-grabbel-zap-foo-10101.11"

		expect:
		versionNumber(tag) == 1L // instead of choking, return 1L
	}
}