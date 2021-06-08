/*
* Copyright (c) Ergon Informatik AG, Switzerland
*/
package ch.ergon.gradle.goodies.versioning.jgit

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.Status
import org.eclipse.jgit.lib.Constants

/**
* JGit implementation of git describe with required flags. JGit support for describe is minimal and there is no support
* for --first-parent behavior, thus there is a subclass that has a first-parent filter attached to the (internal) RevWalk,
* since jgit does not allow specifying a proper filter.
*/
class JGitDescribe implements GitDescribe {

	private final Git git

	JGitDescribe(Git git) {
		this.git = git
	}

	@Override
	String describe(Map options) {
		def head = git.repository.resolve(Constants.HEAD)
		BetterDescribeCommand cmd = new BetterDescribeCommand(git.repository).with {
			firstParent = options.firstParentOnly
			always = true
			tags = !options.annotatedTagsOnly
			target = head.name()
			longDesc = options.longFormat as boolean
			abbreviate = options.abbreviate as int
			if (options.match) {
				setMatch(options.match)
			}
			return it
		}

		Status status = git.status().call()
		if (status.hasUncommittedChanges()) {
			return "${cmd.call()}-dirty"
		} else {
			return cmd.call()
		}
	}

	@Override
	boolean exactMatch() {
		def head = git.repository.resolve(Constants.HEAD)
		return git.tagList()
				.call()
				.find { ref ->
					def peeledRef = git.getRepository().getRefDatabase().peel(ref)
					if (peeledRef.isPeeled() && peeledRef.getPeeledObjectId() != null) {
						// this was an annotated tag
						return peeledRef.getPeeledObjectId() == head.toObjectId()
					}
					return (ref.objectId == head.toObjectId())
				}
	}
}

