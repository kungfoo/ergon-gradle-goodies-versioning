/*
 * Copyright (c) Ergon Informatik AG, Switzerland
 */
package ch.ergon.gradle.goodies.versioning.jgit;

public class DescribeOptions {
	final String match;
	final boolean longFormat;
	final boolean annotatedTagsOnly;
	final boolean firstParentOnly;
	final int abbreviate;

	public DescribeOptions(String match, boolean longFormat, boolean annotatedTagsOnly, boolean firstParentOnly, int abbreviate) {
		this.match = match;
		this.longFormat = longFormat;
		this.annotatedTagsOnly = annotatedTagsOnly;
		this.firstParentOnly = firstParentOnly;
		this.abbreviate = abbreviate;
	}
}
