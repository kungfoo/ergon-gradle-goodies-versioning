/*
 * Copyright (c) Ergon Informatik AG, Switzerland
 */

package ch.ergon.gradle.goodies.versioning

import ch.ergon.gradle.goodies.Eggs
import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.nativeplatform.Repositories

/**
 * Nice and concise versioning of your artifacts using git describe and your git tags.
 */
class VersioningPlugin implements Plugin<Project> {

    /**
     * Generate a file that describes the project version including tags and hashes and when
     * it was built where, on which operating system with what jdk.
     **/
    def generateVersionPropertiesFile(Project project, String versioningFolder) {
        def longVersion = figureOutLongVersion(project)
        def hostInfo = "${System.getProperty("os.name")} - ${System.getProperty("os.arch")} - ${System.getProperty("os.version")}"
        def javaVersion = System.getProperty("java.version")
        def javaVmVendor = System.getProperty("java.vm.vendor")

        def content = """
		# File generated by build.
		build.host=${hostInfo}
		build.java.version=${javaVersion}
		build.java.vm.vendor=${javaVmVendor}
		build.artifact-id=${project.name}
		build.version=${longVersion}
		""".stripIndent()

        new File(versioningFolder).mkdirs()
        def versionsFile = new File(versioningFolder, "version.properties")
        versionsFile.text = content
    }

    /*
    * Properties that should be used to determine up-to-dateness.
    */

    def versionProperties(Project project) {
        def props = [
                longVersion : figureOutLongVersion(project),
                hostInfo    : "${System.getProperty("os.name")} - ${System.getProperty("os.arch")} - ${System.getProperty("os.version")}",
                javaVersion : System.getProperty("java.version"),
                javaVmVendor: System.getProperty("java.vm.vendor"),
                artifact_id : project.name
        ]
        project.logger.info("version properties input props: $props")
        return props
    }

    def figureOutLongVersion(Project project) {
        return project.ergon.versioning.describeLongVersion()
    }

//	boolean hasMainSourceSet(Project project) {
//		// this has one of those plugins that defines a main sourceset
//		project.plugins.hasPlugin(JavaBasePlugin) ||
//		project.plugins.hasPlugin(GroovyBasePlugin) ||
//		project.plugins.hasPlugin(ScalaBasePlugin)
//	}

    /**
     * Will be executed when java, groovy or scala plugin is applied.
     */
    class ProjectWithSourceSet implements Action<Plugin> {
        Project project

        def generateVersionPropertiesTaskName = 'generateVersionProperties'

        @Override
        void execute(Plugin plugin) {
            def versioningFolder = "${project.buildDir}/versioning-data/"
            project.sourceSets.main.output.dir(versioningFolder, builtBy: 'generateVersionProperties')

            if (project.tasks.findByName(generateVersionPropertiesTaskName) == null) {
                project.task(
                        group: 'versioning',
                        description: 'Generate a version.properties file in ${versioningFolder} that identifies this version.',
                        generateVersionPropertiesTaskName)
                        {
                            // foo is necessary here, so the groovy compiler knows this is a closure and not a block.
                            { foo -> inputs.properties(versionProperties()) }
                            outputs.dir versioningFolder

                            doLast { generateVersionPropertiesFile(project, versioningFolder) }
                        }
            }
        }
    }

    class PublishedProject implements Action<Plugin> {
        Project project

        @Override
        void execute(Plugin plugin) {
            project.logger.info("Applied egg-publish and egg-versioning on $project, reconfiguring nexus urls...")

            if (!project.ergon.versioning.exactMatch()) {
                project.logger.info("Using intermediate nexus repository!")

                def credentials = {
                    authentication(userName: Repositories.PUBLISHING_USERNAME, password: Repositories.PUBLISHING_PASSWORD)
                }
                project.uploadArchives.repositories.mavenDeployer.repository(url: Repositories.INTERMEDIATES_URL, credentials)
            }
        }
    }

    @Override
    void apply(Project project) {
        Eggs.getExtension(project).create('versioning', VersioningExtension, project)
        def projectWithSourceSet = new ProjectWithSourceSet(project: project)

        // trigger on plugins with source sets
        ['scala', 'groovy', 'kotlin', 'java'].each { id ->
            project.plugins.withId(id, projectWithSourceSet)
        }

        // there is no egg-publish here...
//        // trigger on egg-publish
//        project.plugins.withId('egg-publish', new PublishedProject(project: project))

        project.task(
                group: 'versioning',
                description: 'Output the version that would be used when building.',
                'describeVersion')
                {
                    doLast { println "The current version is: ${project.version}" }
                }
        // print version when installing or uploading
        def describeVersion = project.tasks.findByName('describeVersion')
        project.tasks.findByName('install')?.dependsOn(describeVersion)
        project.tasks.findByName('uploadArchives')?.dependsOn(describeVersion)

        // now define the version, after we read all the config.
        project.afterEvaluate {
            def version = project.ergon.versioning.describeVersion()
            project.logger.info("Project version is: $version")
            project.version = version

            project.ext.versionNumber = project.ergon.versioning.versionNumber()
        }
    }
}