/*
 * Copyright (c) Ergon Informatik AG, Switzerland
 */
package ch.ergon.gradle.goodies.versioning.jgit;

public interface GitDescribe {
	/**
	 * Mimics behaviour of 'git describe --tags --always --first-parent --match=${prefix}*' Method can assume repo is
	 * not empty but should never throw.
	 * 
	 * @param options: The options to use to describe
	 * @return A string identifying this version.
	 */
	String describe(DescribeOptions options);

	/**
	 * @return true if HEAD is exactly a tagged commit.
	 */
	boolean exactMatch();
}
