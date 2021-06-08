/*
* Copyright (c) Ergon Informatik AG, Switzerland
*/
package ch.ergon.gradle.goodies.versioning.jgit

interface GitDescribe {
	/**
	* Mimics behaviour of 'git describe --tags --always --first-parent --match=${prefix}*'
	* Method can assume repo is not empty but should never throw.
	*/
	String describe(Map options);

	/**
	* @return true if HEAD is exactly a tagged commit.
	*/
	boolean exactMatch();
}