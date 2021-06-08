/*
* Copyright (c) Ergon Informatik AG, Switzerland
*/
package ch.ergon.gradle.goodies.versioning

import java.nio.file.*

import org.apache.tools.ant.taskdefs.condition.Os
import org.eclipse.jgit.api.*
import org.gradle.api.Project
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.testfixtures.ProjectBuilder

/**
* Wraps around setting up the versioning-test-data folder properly.
* Clones the history to a new temp directory for each instance, because then it is thread safe
* and can be used in tests that are run in parallel.
*/
class VersioningPluginTestProject {

	private static final Logger LOG = Logging.getLogger(VersioningPluginTestProject.class)

	public static final String ON_TAG = '38a4393'
	public static final String NOT_ON_TAG = '59622ed'
	public static final String DATABASE_TAG = '586fe91f0d'
	public static final String ANNOTATED_TAG = '49ad6ef'
	/**
	* A commit that has a tag merged from a branch reachable and close to HEAD than --first-parent reachable ones.
	*/
	public static final String WITH_MERGED_TAGS_EARLY_COMMIT = 'd9b05afc2a7'
	public static final String WITH_MERGED_TAGS_LATER_COMMIT = 'ef2067630d1'

	final Project project

	private File tempDir;

	VersioningPluginTestProject() {
		File file = new File("versioning-test-data/.git")
		assert file.exists() : """versioning-test-data/.git is not present,
		please run 'git submodule init && git submodule update'"""

		String sourceRepoPath = new File(".git/modules/versioning-test-data/").getAbsolutePath()
		Path tempPath= Files.createTempDirectory("versioning-test-data")
		tempDir = tempPath.toFile()
		tempDir.deleteOnExit()

		File gitDir
		if (Os.isFamily(Os.FAMILY_WINDOWS)) {
			// On Windows the git clone will fail because cannot create directory with a name that starts with a ".",
			// e.g. ".git". Workaround: use ".git.". The last "." will be deleted automatically.
			gitDir= new File(tempDir, "/.git.")
		} else {
			gitDir= new File(tempDir, "/.git")
		}
		gitDir.deleteOnExit()

		LOG.debug("sourceRepoPath = ${sourceRepoPath}")
		LOG.debug("tempDir = ${tempDir}")
		LOG.debug("gitDir = ${gitDir}")

		Git.cloneRepository()
				.setURI(sourceRepoPath)
				.setDirectory(tempDir)
				.setGitDir(gitDir)
				.call()

		project = ProjectBuilder
				.builder()
				.withProjectDir(tempDir)
				.build()
	}

	def cleanup() {
		tempDir.deleteDir()
	}

	String version(options = [:]) {
		options.each { k,v ->
			project.ergon.versioning[k] = v
		}
		return project.ergon.versioning.describeVersion()
	}

	String longVersion() {
		project.ergon.versioning.describeLongVersion()
	}

	boolean exactMatch() {
		project.ergon.versioning.exactMatch()
	}

	void checkout(sha) {
		def repo = Git.open(project.rootDir)
		repo.checkout().setName(sha).call()
		repo.clean().setCleanDirectories(true).call()
		repo.reset().setMode(ResetCommand.ResetType.HARD).call()
	}

	void apply(plugins) {
		project.logger.info("project apply plugins {}", plugins)
		project.apply plugins
	}

	void evaluate() {
		project.evaluate()
	}
}
