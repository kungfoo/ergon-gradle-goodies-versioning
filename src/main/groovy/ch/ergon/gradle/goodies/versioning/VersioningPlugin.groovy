/*
* Copyright (c) Ergon Informatik AG, Switzerland
*/
package ch.ergon.gradle.goodies.versioning


import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
* Nice and concise versioning of your artifacts using git describe and your git tags.
*/
class VersioningPlugin implements Plugin<Project> {

	/**
	* Will be executed when java, groovy or scala plugin is applied.
	*/
	class ConfigureProjectWithSourceSet implements Action<Plugin> {
		Project project

		ConfigureProjectWithSourceSet(Project project) {
			this.project = project
		}

		def generateVersionPropertiesTaskName = 'generateVersionProperties'

		@Override
		void execute(Plugin plugin) {
			def versioningFolder = "${project.buildDir}/versioning-data/"
			project.sourceSets.main.output.dir(versioningFolder, builtBy: generateVersionPropertiesTaskName)

			if (project.tasks.findByName(generateVersionPropertiesTaskName) == null) {
				project.task(
						generateVersionPropertiesTaskName,
						group: 'versioning',
						description: "Generate a version.properties file in ${versioningFolder} that identifies this version.",
						type: GenerateVersionPropertiesTask)
			}
		}
	}

	@Override
	void apply(Project project) {
		project.getExtensions().create('versioning', VersioningExtension, project)
		def configureProjectWithSourceSet = new ConfigureProjectWithSourceSet(project)

		// trigger on plugins with source sets
		[
			'scala',
			'groovy',
			'kotlin',
			'java'
		].each { id ->
			project.plugins.withId(id, configureProjectWithSourceSet)
		}
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
			def version = project.versioning.describeVersion()
			project.logger.info("Project version is: $version")
			project.version = version

			project.ext.versionNumber = project.versioning.versionNumber()
		}
	}
}