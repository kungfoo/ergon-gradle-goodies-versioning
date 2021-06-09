/*
 * Copyright (c) Ergon Informatik AG, Switzerland
 */
package ch.ergon.gradle.goodies.versioning.jgit;


import java.io.IOException;
import java.util.List;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.errors.InvalidPatternException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.gradle.api.GradleException;

/**
 * JGit implementation of git describe with required flags. JGit support for describe is minimal and there is no support
 * for --first-parent behavior, thus there is a subclass that has a first-parent filter attached to the (internal)
 * RevWalk, since jgit does not allow specifying a proper filter.
 */
public class JGitDescribe implements GitDescribe {

	private final Git git;

	public JGitDescribe(Git git) {
		this.git = git;
	}

	@Override
	public String describe(DescribeOptions options) {
		try {
			ObjectId head = git.getRepository().resolve(Constants.HEAD);
			BetterDescribeCommand cmd = new BetterDescribeCommand(git.getRepository());
			cmd.setAlways(true);
			cmd.setTarget(head);
			cmd.setFirstParent(options.firstParentOnly);
			cmd.setTags(!options.annotatedTagsOnly);
			cmd.setLongDesc(options.longFormat);
			cmd.setAbbreviate(options.abbreviate);
			if (options.match != null && !options.match.isEmpty()) {
				cmd.setMatch(options.match);
			}

			Status status = git.status().call();
			if (status.hasUncommittedChanges()) {
				return cmd.call() + "-dirty";
			} else {
				return cmd.call();
			}
		} catch (IOException e) {
			throw new GradleException("Could not read HEAD objectId from git repository.", e);
		} catch (InvalidPatternException e) {
			throw new GradleException("Invalid match pattern " + e.getPattern() + ": " + e.getMessage());
		} catch (GitAPIException e) {
			throw new GradleException("Could not determine git status.", e);
		}
	}

	@Override
	public boolean exactMatch() {
		try {
			ObjectId head = git.getRepository().resolve(Constants.HEAD);
			List<Ref> refs = git.tagList().call();

			return refs.stream().anyMatch(ref -> {
				try {
					Ref peeledRef = git.getRepository().getRefDatabase().peel(ref);
					if (peeledRef.isPeeled() && peeledRef.getPeeledObjectId() != null) {
						// this was an annotated tag
						return peeledRef.getPeeledObjectId().equals(head.toObjectId());
					}
					return (ref.getObjectId().equals(head.toObjectId()));
				} catch (IOException e) {
					throw new GradleException("Could not peel ref " + ref);
				}
			});

		} catch (IOException | GitAPIException e) {
			throw new GradleException("Could not read HEAD objectId or list of tags from git repository.", e);
		}
	}
}
