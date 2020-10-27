package ch.ergon.gradle.goodies.versioning.jgit


import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.Status
import org.eclipse.jgit.lib.Constants
import org.eclipse.jgit.lib.ObjectId
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.revwalk.RevCommit
import org.eclipse.jgit.revwalk.RevWalk
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * JGit implementation of git describe with required flags. JGit support for describe is minimal and there is no support
 * for --first-parent behavior.
 * Heavily influenced by https://github.com/palantir/gradle-git-version.
 */
class JGitDescribe implements GitDescribe {
    private static final Logger log = LoggerFactory.getLogger(JGitDescribe.class)
    private final Git git

    JGitDescribe(Git git) {
        this.git = git
    }

    @Override
    String describe(Map options) {
        def result = null
        try {
            def head = git.repository.resolve(Constants.HEAD)
            String abbrevHead = git.repository.newObjectReader().abbreviate(head).name()

            Map<String, RefWithTagName> commitHashToTag = mapCommitsToTags(git)

            new RevWalk(git.repository).withCloseable{walk ->
                walk.retainBody = false
                RevCommit commit = walk.parseCommit(head)
                int depth = 0

                while (commit.parents != null && commit.parents.length != 0) {
                    String oid = commit.name()

                    if (commitHashToTag.containsKey(oid)) {
                        String exactTag = commitHashToTag[oid].tag
                        if (exactTag.startsWith(options.prefix ?: "")) {
                            result = String.format("%s-%s-g%s", exactTag, depth, abbrevHead)
                            if (!options.longFormat) {
                                result = depth == 0 ? exactTag : result
                            }
                            Status status = git.status().call()
                            result = status.isClean() ? result : String.format("%s-dirty", result)
                            break;
                        }
                    }

                    RevCommit[] parents = commit.parents
                    commit = walk.parseCommit(parents[0])
                    depth++
                }
            }
            return result

        } catch (Exception e) {
            log.debug("JGit describe failed with {}", e)
            return null
        }
    }

    @Override
    boolean exactMatch() {
        def head = git.repository.resolve(Constants.HEAD)
        Map<String, RefWithTagName> commitHashToTag = mapCommitsToTags(git)
        return commitHashToTag.containsKey(head.name())
    }

    // Maps all commits returned by 'git show-ref --tags -d' to output of 'git describe --tags --exact-match <commit>'
    private static Map<String, RefWithTagName> mapCommitsToTags(Git git) {
        RefWithTagNameComparator comparator = new RefWithTagNameComparator(git)

        // Maps commit hash to list of all refs pointing to given commit hash.
        // All keys in this map should be same as commit hashes in 'git show-ref --tags -d'
        Map<String, RefWithTagName> commitHashToTag = [:]
        Repository repository = git.repository

        def tags = repository.refDatabase.getRefsByPrefix(Constants.R_TAGS)
        tags.each { tag ->
            def peeledRef = repository.refDatabase.peel(tag)

            def refWithTagName = new RefWithTagName(
                    ref: peeledRef,
                    tag: peeledRef.name.substring(Constants.R_TAGS.length())
            )

            if(peeledRef.peeledObjectId == null) {
                updateCommitHashMap(commitHashToTag, comparator, peeledRef.objectId, refWithTagName)
            } else {
                // this is an annotated tag
                updateCommitHashMap(commitHashToTag, comparator, peeledRef.peeledObjectId, refWithTagName)
            }
        }
        return commitHashToTag
    }

    private static void updateCommitHashMap(Map<String, RefWithTagName> map, RefWithTagNameComparator comparator,
                                            ObjectId objectId, RefWithTagName ref) {
        // Smallest ref (ordered by this comparator) from list of refs is chosen for each commit.
        // This ensures we get same behavior as in 'git describe --tags --exact-match <commit>'
        String commitHash = objectId.getName()
        if (map.containsKey(commitHash)) {
            if (comparator.compare(ref, map.get(commitHash)) < 0) {
                map.put(commitHash, ref)
            }
        } else {
            map.put(commitHash, ref)
        }
    }

}

