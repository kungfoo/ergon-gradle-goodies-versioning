/*
 * Copyright (c) Ergon Informatik AG, Switzerland
 */

package ch.ergon.gradle.goodies.versioning

import org.eclipse.jgit.api.Git
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder

import java.nio.file.Files

/**
 * Wraps around setting up the versioning-test-data folder properly.
 * Clones the history to a new temp directory for each instance, because then it is thread safe
 * and can be used in tests that are run in parallel.
 */
class VersioningPluginTestProject {

    public static final String ON_TAG = '38a4393'
    public static final String NOT_ON_TAG = '59622ed'
    public static final String DATABASE_TAG = '586fe91f0d'

    final Project project

    VersioningPluginTestProject() {
        assert new File("versioning-test-data/.git").exists(): """versioning-test-data/.git is not present,
		please run 'git submodule init && git submodule update'"""

        String sourceRepoPath = new File(".git/modules/versioning-test-data/").getAbsolutePath()
        File tempDir = Files.createTempDirectory("versioning-test-data").toFile()
        tempDir.deleteOnExit()

        Git.cloneRepository()
                .setURI(sourceRepoPath)
                .setDirectory(tempDir)
                .call()

        project = ProjectBuilder
                .builder()
                .withProjectDir(tempDir)
                .build()

        project.apply plugin: 'egg-versioning'
    }

    String version(options = [:]) {
        options.each { k, v ->
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
        project.exec {
            commandLine 'git', 'checkout', "${sha}"
        }
    }

    void apply(plugins) {
        project.apply plugins
    }
}
