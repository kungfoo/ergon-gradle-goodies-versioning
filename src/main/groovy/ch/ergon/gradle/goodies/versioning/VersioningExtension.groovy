/*
 * Copyright (c) Ergon Informatik AG, Switzerland
 */

package ch.ergon.gradle.goodies.versioning

import groovy.transform.AutoClone
import org.gradle.api.Project

/**
 * Configurable extension properties of the egg-pmd plugin.
 * When applied to a project, it defines the version right away now.
 */
@AutoClone
class VersioningExtension {

    private Project project

    /**
     * The match clause that is used to match the tags describing this projects version.
     */
    String match = null

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
     * Use only annotated tags.
     */
    boolean annotatedTagsOnly = false

    /**
     * Only visit the first parent, when searching for tags. See 'man git describe'
     */
    boolean firstParentOnly = true

    /**
     * Always use the long format?
     */
    boolean longFormat = false

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
    String describeVersion() {
        def command = describeCommand()

        if (match) {
            def tag = figureOutVersion(project, command)
            return applyPostProcessing(
                    replaceGlobWith(match, tag)
            )
        } else {
            return applyPostProcessing(
                    figureOutVersion(project, command)
            )
        }
    }

    private String applyPostProcessing(String version) {
        return postProcessVersion(version)
    }

    /**
     * Takes all options and calculates the long version with them, so
     * longFormat is always set to true.
     **/
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
     * Returns true, if HEAD is currently pointing at a commit that has a tag (that matches
     * the options of git describe).
     **/
    boolean exactMatch() {
        def stdErr = new ByteArrayOutputStream()
        def stdOut = new ByteArrayOutputStream()

        def command = exactMatchCommand()
        def result = project.exec {
            commandLine = command
            ignoreExitValue = true
            errorOutput = stdErr
            standardOutput = stdOut
        }
        project.logger.debug("stdout ouptut of $command was: ${stdOut.toString()}")
        project.logger.debug("stderr ouptut of $command was: ${stdErr.toString()}")

        return result.exitValue == 0
    }

    private String[] exactMatchCommand() {
        List<String> command = [
                'git', 'describe', '--exact-match', 'HEAD'
        ]
        return (command + options()).toArray()
    }

    private String[] describeCommand() {
        List<String> command = [
                'git',
                'describe',
                '--always',
                '--dirty'
        ]
        return (command + options()).toArray()
    }

    private List<String> options() {
        def result = []
        if (!annotatedTagsOnly) {
            result <<= '--tags'
        }
        if (firstParentOnly) {
            result <<= '--first-parent'
        }
        if (match) {
            result <<= "--match=${match}"
        }
        if (longFormat) {
            result <<= "--long"
        }
        return result
    }

    private String figureOutVersion(Project project, String... command) {
        execute(project, command)
    }

    private def execute(Project project, String... command) {
        def stdout = new ByteArrayOutputStream()
        project.exec {
            commandLine(command)
            standardOutput = stdout
        }
        return stdout.toString().trim()
    }

    /**
     * Create a regex from a (simplified) glob pattern.
     * Not currently used except in tests, but maybe we wanna use grgit in the future.
     * So we might need it again to match the tags...
     **/
    static String createRegexFromGlob(String glob) {
        StringBuilder regex = new StringBuilder("^")
        for (int i = 0; i < glob.length(); ++i) {
            final char c = glob.charAt(i)
            switch (c) {
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
}

interface MonotonouslyIncreasingVersionNumber {
    /**
     * Calculate a monotonically rising version number on your own. Have fun.
     */
    long calculate(String version)
}
