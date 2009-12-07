package com.zutubi.pulse.master.build.queue.graph;

import com.zutubi.pulse.core.dependency.ivy.IvyLatestRevisionMatcher;
import com.zutubi.pulse.master.tove.config.project.DependencyConfiguration;
import com.zutubi.util.TreeNode;

/**
 * The status filter trims the nodes in a dependency tree that
 * the dependency system would not reach.
 *
 * For instance, project A depends on latest.milestone revision
 * of project B.  If the status being built of project B is
 * integration, then the dependency from project A to project B
 * would not be traversed and project B can be filtered.
 *
 * Note: Filtering only occurs for revisions that match ivys'
 * latest revision matcher.  It currently ignores all other
 * cases.
 */
public class StatusFilter extends GraphFilter
{
    private String status;

    public StatusFilter(String status)
    {
        this.status = status;
    }

    public void process(TreeNode<BuildGraphData> node)
    {
        if (!node.isRoot())
        {
            DependencyConfiguration dependency = node.getData().getDependency();
            String revision = dependency.getRevision();

            IvyLatestRevisionMatcher ivyMatcher = new IvyLatestRevisionMatcher();
            if (ivyMatcher.isApplicable(revision) && !ivyMatcher.accept(revision, status))
            {
                toTrim.add(node);
            }
        }
    }
}