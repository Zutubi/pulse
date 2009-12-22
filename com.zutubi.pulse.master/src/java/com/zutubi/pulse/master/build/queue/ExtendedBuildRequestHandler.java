package com.zutubi.pulse.master.build.queue;

import com.zutubi.pulse.core.BuildRevision;
import com.zutubi.pulse.master.build.queue.graph.GraphBuilder;
import com.zutubi.pulse.master.build.queue.graph.BuildGraphData;
import com.zutubi.pulse.master.build.queue.graph.GraphFilters;
import com.zutubi.pulse.master.events.build.BuildRequestEvent;
import com.zutubi.pulse.master.events.build.SingleBuildRequestEvent;
import com.zutubi.pulse.master.model.Project;
import com.zutubi.pulse.master.model.TriggerOptions;
import static com.zutubi.pulse.master.tove.config.MasterConfigurationRegistry.EXTENSION_PROJECT_TRIGGERS;
import com.zutubi.pulse.master.tove.config.project.triggers.DependentBuildTriggerConfiguration;
import com.zutubi.pulse.master.tove.config.project.triggers.TriggerConfiguration;
import com.zutubi.util.*;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * The extended build request handler is responsible for handling all
 * non personal build requests.
 *
 * If the request is for a rebuild, then a set of upstream builds are
 * queued and configured accordingly.
 *
 * For all builds, the downstream builds that will be triggered by
 * successful builds is determined and produces queue requests.
 *
 * For all of the queue requests that are generated, each build will only
 * contain a single build for each project.  The queued request will be
 * configured to ensure that the ordering in which the builds are activated
 * is correct.
 */
public class ExtendedBuildRequestHandler extends BaseBuildRequestHandler
{
    public List<QueuedRequest> prepare(final BuildRequestEvent request)
    {
        if (request.getMetaBuildId() != 0)
        {
            throw new IllegalArgumentException("The build request has already been handled by another handler.");
        }
        request.setMetaBuildId(getMetaBuildId());

        List<QueuedRequest> requestsToQueue = new LinkedList<QueuedRequest>();

        GraphBuilder builder = objectFactory.buildBean(GraphBuilder.class);
        GraphFilters filters = objectFactory.buildBean(GraphFilters.class);

        Project project = projectManager.getProject(request.getProjectId(), false);

        LinkedList<QueuedRequest> upstreamRequests = new LinkedList<QueuedRequest>();
        QueuedRequest upstreamRoot = null;
        if (request.getOptions().isRebuild())
        {
            // a rebuild indicates that we should build our upstream dependencies.
            TreeNode<BuildGraphData> upstream = builder.buildUpstreamGraph(project,
                    filters.status(request.getStatus()),
                    filters.transitive(),
                    filters.duplicate());
            upstreamRequests = prepareUpstreamRequests(request, upstream);
            upstreamRoot = upstreamRequests.removeLast();
        }

        TreeNode<BuildGraphData> downstream = builder.buildDownstreamGraph(project,
                filters.trigger(),
                filters.duplicate());
        LinkedList<QueuedRequest> downstreamRequests = prepareDownstreamRequests(request, downstream);
        QueuedRequest downstreamRoot = downstreamRequests.removeFirst();

        QueuedRequest mergedRoot = (upstreamRoot != null) ? upstreamRoot : downstreamRoot;

        requestsToQueue.addAll(upstreamRequests);
        requestsToQueue.add(mergedRoot);
        requestsToQueue.addAll(downstreamRequests);

        return requestsToQueue;
    }

    private LinkedList<QueuedRequest> prepareDownstreamRequests(final BuildRequestEvent request, TreeNode<BuildGraphData> downstream)
    {
        final HashMap<Project, QueuedRequest> ownerRequests = new HashMap<Project, QueuedRequest>();
        final LinkedList<QueuedRequest> requestsToQueue = new LinkedList<QueuedRequest>();

        downstream.breadthFirstWalk(new UnaryProcedure<TreeNode<BuildGraphData>>()
        {
            public void run(TreeNode<BuildGraphData> node)
            {
                Project owner = node.getData().getProject();
                if (!ownerRequests.containsKey(owner))
                {
                    BuildRequestEvent newRequest = (request.getOwner().equals(owner)) ? request : cloneRequest(request, owner);
                    buildRequestRegistry.register(newRequest);

                    DependentBuildTriggerConfiguration trigger = getTrigger(owner);
                    if (trigger != null && !node.isRoot())
                    {
                        Project upstreamOwner = node.getParent().getData().getProject();
                        QueuedRequest upstreamRequest = ownerRequests.get(upstreamOwner);

                        TriggerOptions options = newRequest.getOptions();
                        if (trigger.isPropagateStatus())
                        {
                            options.setStatus(upstreamRequest.getRequest().getStatus());
                        }
                        if (trigger.isPropagateVersion())
                        {
                            options.setVersion(upstreamRequest.getRequest().getVersion());
                        }
                    }

                    QueuedRequest queuedRequest = newQueuedRequest(newRequest);
                    requestsToQueue.add(queuedRequest);
                    ownerRequests.put(owner, queuedRequest);
                }

                if (!node.isRoot())
                {
                    QueuedRequest queuedRequest = ownerRequests.get(owner);
                    Project dependentProject = node.getParent().getData().getProject();
                    queuedRequest.getRequest().addDependentOwner(dependentProject);
                    queuedRequest.addPredicate(new DependencyCompleteQueuePredicate(buildQueue, dependentProject));
                }
            }
        });

        return requestsToQueue;
    }

    private DependentBuildTriggerConfiguration getTrigger(Project project)
    {
        Map<String, TriggerConfiguration> triggers = (Map<String, TriggerConfiguration>) project.getConfig().getExtensions().get(EXTENSION_PROJECT_TRIGGERS);
        return (DependentBuildTriggerConfiguration) CollectionUtils.find(triggers.values(), new InstanceOfPredicate<TriggerConfiguration>(DependentBuildTriggerConfiguration.class));
    }

    private LinkedList<QueuedRequest> prepareUpstreamRequests(final BuildRequestEvent request, final TreeNode<BuildGraphData> upstream)
    {
        final Map<Object, QueuedRequest> ownerRequests = new HashMap<Object, QueuedRequest>();
        final LinkedList<QueuedRequest> requestsToQueue = new LinkedList<QueuedRequest>();

        upstream.depthFirstWalk(new UnaryProcedure<TreeNode<BuildGraphData>>()
        {
            public void run(TreeNode<BuildGraphData> node)
            {
                Project owner = node.getData().getProject();

                if (!ownerRequests.containsKey(owner))
                {
                    BuildRequestEvent newRequest = (request.getOwner().equals(owner)) ? request : cloneRequest(request, owner);
                    buildRequestRegistry.register(newRequest);

                    // create queued request.
                    QueuedRequest request = newQueuedRequest(newRequest);
                    ownerRequests.put(owner, request);
                    requestsToQueue.add(request);
                }

                QueuedRequest queuedRequest = ownerRequests.get(owner);

                for (TreeNode<BuildGraphData> child : node.getChildren())
                {
                    Project childProject = child.getData().getProject();
                    queuedRequest.getRequest().addDependentOwner(childProject);
                    queuedRequest.addPredicate(new DependencyCompleteQueuePredicate(buildQueue, childProject));
                }
            }
        });
        return requestsToQueue;
    }

    private BuildRequestEvent cloneRequest(BuildRequestEvent sourceRequest, Project owner)
    {
        TriggerOptions options = new TriggerOptions(sourceRequest.getOptions());
        BuildRevision sourceRevision = sourceRequest.getRevision();
        BuildRevision revision = sourceRevision.isUser() ? new BuildRevision(sourceRevision.getRevision(), sourceRevision.isUser()) : new BuildRevision();
        BuildRequestEvent request = new SingleBuildRequestEvent(this, owner, revision, options);
        request.setMetaBuildId(sourceRequest.getMetaBuildId());
        return request;
    }

    private QueuedRequest newQueuedRequest(BuildRequestEvent request)
    {
        List<QueuedRequestPredicate> defaultPredicates = new LinkedList<QueuedRequestPredicate>();
        defaultPredicates.add(new OneActiveBuildPerOwnerPredicate(buildQueue));
        if (request.canJumpQueue())
        {
            defaultPredicates.add(new HeadOfOwnersCanBuildNowQueuePredicate(buildQueue));
        }
        else
        {
            defaultPredicates.add(new HeadOfOwnerQueuePredicate(buildQueue));
        }
        return new QueuedRequest(request, defaultPredicates);
    }
}
