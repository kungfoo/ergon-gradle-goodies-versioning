/*
* Copyright (c) Ergon Informatik AG, Switzerland
*/
package ch.ergon.gradle.goodies.versioning

import ch.ergon.gradle.goodies.versioning.jgit.DescribeOptions
import ch.ergon.gradle.goodies.versioning.jgit.JGitDescribe
import groovy.transform.AutoClone
import groovy.transform.Memoized
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import org.gradle.api.Project

/**
* Configurable extension properties of the egg-versioning plugin.
* When applied to a project, it defines the version right away now.
*/
@AutoClone
class VersioningExtension {

	private Project project

	/**
	* The match clause that is used to match the tags describing this projects version.
	*/
	String match = ""

	/**
	* How to figure out the version from the matched tag. Default is to trim the prefix.
	*/
	Closure replaceGlobWith = GlobReplace.REPLACE_PREFIX

	/**
	* If you need to post-process the version yielded
	*/
	Closure postProcessVersion = PostProcessVersion.DEFAULT

	/**
	* A scheme to define a monotonously increasing number from the versioning data.
	*/
	MonotonouslyIncreasingVersionNumber versionNumber

	/**
	* How much padding should be used on the different places of the versionNumber.
	* Use this when your version number CAN be a long instead of an int.
	**/
	int versionNumberPadding = 2

	/**
	* Always use the long format?
	*/
	boolean longFormat = false

	/**
	* Use only annotated tags.
	*/
	boolean annotatedTagsOnly = false

	/**
	* Whether or not to consider any tags on merged branches for describing.
	*/
	boolean firstParentOnly = true

	/**
	* How long should 'long' description hashes be. Default matches the CLI git default of 7.
	*/
	int abbreviate = 7

	VersioningExtension(Project project) {
		this.project = project
		this.versionNumber = new DefaultMonotonouslyIncreasingVersionNumber(project: project)
	}

	/**
	* Describe the version using git describe.
	* You can add an optional 'match' glob, that will cause git describe to only match certain tags.
	* By default, this is considered a prefix to the 'real' version number and is stripped away using
	* GlobReplace.REPLACE_PREFIX.
	* If this is not what you want, you can pass a closure, taking two arguments (glob, tag) to
	* figure out how you want the version called.
	* Example uses: <br/>
	* describeVersion(), describeVersion(match: "database-*")
	*
	* For all options, @See DescribeVersionOptions
	**/
	@Memoized
	String describeVersion() {
		def jgitDescribe = new JGitDescribe(findGitRepo())
		def options = new DescribeOptions(match, longFormat, annotatedTagsOnly, firstParentOnly, abbreviate);
		def tag = jgitDescribe.describe(options)
		if(match) {
			return applyPostProcessing(replaceGlobWith(match, tag))
		} else {
			applyPostProcessing(tag)
		}
	}

	/**
	* Returns true, if HEAD is currently pointing at a commit that has a tag (that matches
	* the options of git describe).
	**/
	@Memoized
	boolean exactMatch() {
		def jgitDescribe = new JGitDescribe(findGitRepo())
		return jgitDescribe.exactMatch()
	}

	@Memoized
	private Git findGitRepo() {
		FileRepositoryBuilder repositoryBuilder = new FileRepositoryBuilder()
		repositoryBuilder.addCeilingDirectory( new File( project.projectDir.absolutePath ))
		def found = repositoryBuilder.findGitDir(project.rootDir)
		assert found.gitDir != null, "Could not locate .git directory starting from ${project.rootDir.absolutePath}"
		return Git.open(found.getGitDir())
	}

	/**
	* Takes all options and calculates the long version with them, so
	* longFormat is always set to true.
	**/
	@Memoized
	String describeLongVersion() {
		def cloned = this.clone()
		cloned.longFormat = true
		cloned.describeVersion()
	}

	/**
	* Determine a monotonously increasing version number from the tags and number of commits.
	* @See DefaultMonotonouslyIncreasingVersionNumber for details on how this is done by default.
	* You can also override this by overwriting the property 'versionNumber'.
	* @return a version number that is (should your tags be properly ordered) monotonously increasing.
	*/
	long versionNumber() {
		return versionNumber.calculate()
	}

	/**
	* Create a regex from a (simplified) glob pattern.
	* Not currently used except in tests, but maybe we wanna use grgit in the future.
	* So we might need it again to match the tags...
	**/
	static String createRegexFromGlob(String glob) {
		StringBuilder regex = new StringBuilder("^")
		for(int i = 0; i < glob.length(); ++i) {
			final char c = glob.charAt(i)
			switch(c) {
				case '*': regex.append(".*"); break
				case '?': regex.append('.'); break
				case '.': regex.append("\\."); break
				case '\\': regex.append("\\\\"); break
				default: regex.append(c)
			}
		}
		regex.append('$')

		return regex.toString()
	}

	private String applyPostProcessing (String version) {
		return postProcessVersion(version)
	}
}