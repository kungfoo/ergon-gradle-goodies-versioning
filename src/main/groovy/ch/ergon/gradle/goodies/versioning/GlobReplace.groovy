/*
* Copyright (c) Ergon Informatik AG, Switzerland
*/
package ch.ergon.gradle.goodies.versioning

/**
* How to treat the matched tag. Often times you want to remove the prefix
* because otherwise the versioned artifact includes the artifact name twice.
* For that look at REPLACE_PREFIX.
* If you want it left alone, look at NO_REPLACE.
*
* Or roll your own closure and pass it to describeVersion().
*/
class GlobReplace {
	static REPLACE_PREFIX = { glob, tag ->
		def pattern = glob.replaceFirst("\\*", "")
		return tag.replaceFirst(pattern, "")
	}

	static NO_REPLACE = { glob, tag -> tag }
}
