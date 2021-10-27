/*
* Copyright (c) Ergon Informatik AG, Switzerland
*/
package ch.ergon.gradle.goodies.versioning

import static ch.ergon.gradle.goodies.versioning.VersioningPluginTestProject.*

import spock.lang.Specification

/**
* Base class for specifications that tests the effects of applying the 'egg-versioning' plugin.
*/
class VersioningPluginSpecification extends Specification {

	VersioningPluginTestProject project

	void setup() {
		project = new VersioningPluginTestProject()
	}

	void cleanup() {
		project.cleanup()
	}

	void checkout(String sha) {
		project.checkout(sha)
	}

	String version(options = [:]) {
		project.version(options)
	}

	String longVersion(options = [:]) {
		project.longVersion(options)
	}

	boolean exactMatch() {
		project.exactMatch()
	}

	void apply(plugins) {
		project.apply plugins
	}
}