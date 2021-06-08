/*
* Copyright (c) Ergon Informatik AG, Switzerland
*/
package ch.ergon.gradle.goodies.versioning

/**
* If, for some reason you need to postprocess the final version, do it with a closure taking a string and
* returning a string.
*/
class PostProcessVersion {
	/** Default is NOP. **/
	static DEFAULT = { version -> version }

	/**
	* When a long version is returned (i.e HEAD is not on a tag), strips the number of commits and
	* the git abbreviation in the described version.
	*/
	static STRIP_NR_COMMITS_AND_G = { version ->
		def pattern = /(.*)-\d+-g(.*)/
		def matcher = (version =~ pattern)

		if (matcher) {
			return "${matcher[0][1]}-${matcher[0][2]}"
		}
		return version
	}
}