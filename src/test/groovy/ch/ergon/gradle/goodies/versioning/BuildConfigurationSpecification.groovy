/*
* Copyright (c) Ergon Informatik AG, Switzerland
*/
package ch.ergon.gradle.goodies.versioning;

import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import spock.lang.Specification;

/**
* Base class for specifications that verify changes to a build's configuration but do not require the build's task
* to actually execute.
*/
class BuildConfigurationSpecification extends Specification {

	/**
	* Creates a dummy project with the specified list of plugins applied.
	* 
	* The project can only be used for the evaluation phase of the build. If you need to actually execute tasks
	* and assert their output, base your test on a {#getBuildFile()}.
	* 
	* @param pluginIds the sequence of Gradle plugins to apply to the created project.
	* @return a dummy project as created by Gradle's {@link ProjectBuilder}.
	*/
	Project projectWithPlugins(String ... pluginIds) {
		Project project = ProjectBuilder.builder().build()
		applyPlugins(project, pluginIds)
	}

	/**
	* Applies the specified plugins to a project.
	* 
	* @param project the project to which to apply the plugins.
	* @param pluginIds the sequence of Gradle plugins to apply to the project.
	* @return the passed project.
	*/
	Project applyPlugins(Project project, String ... pluginIds) {
		pluginIds.each {
			project.apply plugin: it
		}
		project
	}
}
