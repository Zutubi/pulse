package com.zutubi.pulse.master.build.queue;

import com.google.common.base.Function;
import com.google.common.base.Supplier;
import com.zutubi.events.DefaultEventManager;
import com.zutubi.events.EventManager;
import com.zutubi.events.RecordingEventListener;
import com.zutubi.pulse.core.PulseExecutionContext;
import com.zutubi.pulse.core.engine.api.ResultState;
import com.zutubi.pulse.core.resources.api.ResourcePropertyConfiguration;
import com.zutubi.pulse.core.scm.PersistentContextImpl;
import com.zutubi.pulse.core.scm.ScmContextImpl;
import com.zutubi.pulse.core.scm.api.Revision;
import com.zutubi.pulse.core.scm.api.ScmCapability;
import com.zutubi.pulse.core.scm.api.ScmClient;
import com.zutubi.pulse.core.scm.api.ScmContext;
import com.zutubi.pulse.core.scm.config.api.ScmConfiguration;
import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.pulse.master.build.control.BuildController;
import com.zutubi.pulse.master.build.control.BuildControllerFactory;
import com.zutubi.pulse.master.events.build.BuildCompletedEvent;
import com.zutubi.pulse.master.events.build.BuildRequestEvent;
import com.zutubi.pulse.master.events.build.PersonalBuildRequestEvent;
import com.zutubi.pulse.master.events.build.SingleBuildRequestEvent;
import com.zutubi.pulse.master.model.*;
import com.zutubi.pulse.master.scheduling.Scheduler;
import com.zutubi.pulse.master.scheduling.Trigger;
import com.zutubi.pulse.master.scm.ScmManager;
import com.zutubi.pulse.master.tove.config.project.DependencyConfiguration;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.pulse.master.tove.config.project.triggers.DependentBuildTriggerConfiguration;
import com.zutubi.util.bean.WiringObjectFactory;
import com.zutubi.util.reflection.ReflectionUtils;
import org.mockito.Matchers;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static com.google.common.collect.Iterables.transform;
import static com.google.common.collect.Lists.newArrayList;
import static com.zutubi.pulse.master.tove.config.MasterConfigurationRegistry.EXTENSION_PROJECT_TRIGGERS;
import static org.mockito.Mockito.*;


/**
 * Base test case with loads of goodies needed to test the
 * individual components of the queuing system.
 */
public abstract class BaseQueueTestCase extends PulseTestCase
{
    protected AtomicInteger nextId = new AtomicInteger(1);

    protected WiringObjectFactory objectFactory;

    protected Scheduler scheduler;
    private BuildManager buildManager;
    protected ProjectManager projectManager;
    protected ScmManager scmManager;
    protected UserManager userManager;
    protected BuildRequestRegistry buildRequestRegistry;
    protected BuildControllerFactory buildControllerFactory;
    protected EventManager eventManager;
    protected RecordingEventListener listener;
    protected Map<BuildRequestEvent, BuildController> controllers;
    protected List<ProjectConfiguration> allConfigs = new LinkedList<ProjectConfiguration>();
    protected Map<Long, Project> idToProject = new HashMap<Long, Project>();

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        objectFactory = new WiringObjectFactory();
        
        buildRequestRegistry = mock(BuildRequestRegistry.class);
        buildManager = mock(BuildManager.class);
        projectManager = mock(ProjectManager.class);
        stub(projectManager.getAllProjectConfigs(anyBoolean())).toReturn(allConfigs);
        stub(projectManager.getDownstreamDependencies((ProjectConfiguration) anyObject())).toAnswer(new Answer<List<ProjectConfiguration>>()
        {
            public List<ProjectConfiguration> answer(InvocationOnMock invocationOnMock) throws Throwable
            {
                ProjectConfiguration config = (ProjectConfiguration) invocationOnMock.getArguments()[0];
                List<ProjectConfiguration> result = new LinkedList<ProjectConfiguration>();
                for (ProjectConfiguration project : allConfigs)
                {
                    for (DependencyConfiguration dep : project.getDependencies().getDependencies())
                    {
                        if (dep.getProject().equals(config))
                        {
                            result.add(project);
                            break;
                        }
                    }
                }

                return result;
            }
        });

        stub(projectManager.mapConfigsToProjects(anyList())).toAnswer(new Answer<List<Project>>()
        {
            public List<Project> answer(InvocationOnMock invocationOnMock) throws Throwable
            {
                Collection<ProjectConfiguration> configs = (Collection<ProjectConfiguration>) invocationOnMock.getArguments()[0];
                return newArrayList(transform(configs, new Function<ProjectConfiguration, Project>()
                {
                    public Project apply(ProjectConfiguration projectConfiguration)
                    {
                        return idToProject.get(projectConfiguration.getProjectId());
                    }
                }));
            }
        });
        stub(projectManager.getProject(anyLong(), anyBoolean())).toAnswer(new Answer<Project>()
        {
            public Project answer(InvocationOnMock invocationOnMock) throws Throwable
            {
                Long projectId = (Long) invocationOnMock.getArguments()[0];
                return idToProject.get(projectId);
            }
        });

        Answer answer = new Answer()
        {
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable
            {
                Runnable runnable = (Runnable) invocationOnMock.getArguments()[0];
                runnable.run();
                return null;
            }
        };
        // Is it possible for a matcher to match variable number of arguments, and avoid the following
        // explicit and error prone enumeration of longs?
        doAnswer(answer).when(projectManager).runUnderProjectLocks(Matchers.<Runnable>anyObject());
        doAnswer(answer).when(projectManager).runUnderProjectLocks(Matchers.<Runnable>anyObject(), anyLong());
        doAnswer(answer).when(projectManager).runUnderProjectLocks(Matchers.<Runnable>anyObject(), anyLong(), anyLong());
        doAnswer(answer).when(projectManager).runUnderProjectLocks(Matchers.<Runnable>anyObject(), anyLong(), anyLong(), anyLong());
        doAnswer(answer).when(projectManager).runUnderProjectLocks(Matchers.<Runnable>anyObject(), anyLong(), anyLong(), anyLong(), anyLong());
        doAnswer(answer).when(projectManager).runUnderProjectLocks(Matchers.<Runnable>anyObject(), anyLong(), anyLong(), anyLong(), anyLong(), anyLong());
        doAnswer(answer).when(projectManager).runUnderProjectLocks(Matchers.<Runnable>anyObject(), anyLong(), anyLong(), anyLong(), anyLong(), anyLong(), anyLong());

        stub(projectManager.updateAndGetNextBuildNumber((Project) anyObject(), eq(true))).toReturn((long) nextId.getAndIncrement());

        ScmClient scmClient = mock(ScmClient.class);
        stub(scmClient.getCapabilities(Matchers.<ScmContext>anyObject())).toReturn(new HashSet<ScmCapability>());
        scmManager = mock(ScmManager.class);
        stub(scmManager.createClient(Matchers.<ProjectConfiguration>anyObject(), Matchers.<ScmConfiguration>anyObject())).toReturn(scmClient);
        stub(scmManager.createContext(Matchers.<ProjectConfiguration>anyObject(), Matchers.<Project.State>anyObject(), Matchers.anyString())).toReturn(new ScmContextImpl(new PersistentContextImpl(null), new PulseExecutionContext()));

        userManager = mock(UserManager.class);
        stub(userManager.getConcurrentPersonalBuilds(Matchers.<User>anyObject())).toReturn(1);

        buildControllerFactory = mock(BuildControllerFactory.class);
        stub(buildControllerFactory.create((BuildRequestEvent)anyObject())).toAnswer(new Answer()
        {
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable
            {
                // create a controller only if we have not done so pre-emptively in the test case.
                BuildRequestEvent request = (BuildRequestEvent)invocationOnMock.getArguments()[0];
                if (!controllers.containsKey(request))
                {
                    // for those cases where we do not create the request ourselves.
                    BuildController controller = mock(BuildController.class);
                    doReturn(request.getId()).when(controller).start();
                    doReturn(request.getId()).when(controller).getBuildResultId();
                    controllers.put(request, controller);
                }
                return controllers.get(request);
            }
        });

        scheduler = mock(Scheduler.class);
        stub(scheduler.getTrigger(anyLong())).toAnswer(new Answer<Trigger>()
        {
            public Trigger answer(InvocationOnMock invocationOnMock) throws Throwable
            {
                Trigger trigger = mock(Trigger.class);
                doReturn(Boolean.TRUE).when(trigger).isActive();
                return trigger;
            }
        });

        controllers = new HashMap<BuildRequestEvent, BuildController>();

        eventManager = new DefaultEventManager();
        listener = new RecordingEventListener();
        eventManager.register(listener);

        objectFactory.initProperties(this);
    }

    protected BuildRequestEvent createRebuildRequest(String projectName)
    {
        return createRebuildRequest(createProject(projectName));
    }
    
    protected BuildRequestEvent createRebuildRequest(Project project)
    {
        BuildRequestEvent request = createRequest(project);
        request.getOptions().setRebuild(true);
        return request;
    }

    protected BuildRequestEvent createRequest(String projectName)
    {
        return createRequest(createProject(projectName));
    }

    protected BuildRequestEvent createRequest(Project project)
    {
        return createRequest(project, "source", false, new Revision(1234));
    }

    protected BuildRequestEvent createRequest(Project project, String source, boolean replaceable, boolean jumpQueueAllowed)
    {
        BuildRequestEvent request = createRequest(project, source, replaceable, (replaceable) ? null : new Revision(1234));
        request.getOptions().setJumpQueueAllowed(jumpQueueAllowed);
        return request;
    }

    protected BuildRequestEvent createRequest(Project project, String source, boolean replaceable, Revision revision)
    {
        BuildReason reason = new ManualTriggerBuildReason("tester");
        TriggerOptions options = new TriggerOptions(reason, source);
        options.setReplaceable(replaceable);
        
        BuildRevision buildRevision;
        if (revision != null)
        {
            buildRevision = new BuildRevision(revision, true);
        }
        else
        {
            buildRevision = new BuildRevision(new Supplier<Revision>()
            {
                public Revision get()
                {
                    return new Revision("test");
                }
            });
        }

        BuildRequestEvent request = new SingleBuildRequestEvent(this, project, buildRevision, options);

        BuildController controller = mock(BuildController.class);
        doReturn(request.getId()).when(controller).start();
        doReturn(request.getId()).when(controller).getBuildResultId();
        controllers.put(request, controller);

        return request;
    }

    protected Project createProject(String projectName, DependencyConfiguration... dependencies)
    {
        return createProject(projectName, Project.State.IDLE, dependencies);
    }

    protected Project createProject(String projectName, Project.State state, DependencyConfiguration... dependencies)
    {
        Project project = projectManager.getProject(projectName, true);
        if (project == null)
        {
            project = new Project(state);
            project.setId(nextId.getAndIncrement());
            ProjectConfiguration projectConfiguration = new ProjectConfiguration();
            projectConfiguration.setName(projectName);
            projectConfiguration.setHandle(nextId.getAndIncrement());
            projectConfiguration.setProjectId(project.getId());
            projectConfiguration.getDependencies().getDependencies().addAll(Arrays.asList(dependencies));
            HashMap<String, Object> triggers = new HashMap<String, Object>();
            triggers.put("dependent trigger", new DependentBuildTriggerConfiguration());
            projectConfiguration.addExtension(EXTENSION_PROJECT_TRIGGERS, triggers);
            project.setConfig(projectConfiguration);

            doReturn(project).when(projectManager).getProject(projectName, true);
            doReturn(project).when(projectManager).getProject(project.getId(), true);
            doReturn(project).when(projectManager).getProject(project.getId(), false);

            allConfigs.add(projectConfiguration);
            idToProject.put(project.getId(), project);
        }
        return project;
    }

    protected BuildRequestEvent createPersonalRequest(String projectName)
    {
        return createPersonalRequest(createProject(projectName));
    }

    protected BuildRequestEvent createPersonalRequest(Project project)
    {
        User user = new User();
        user.setId(nextId.getAndIncrement());
        BuildRevision revision = new BuildRevision(new Revision(1234), true);
        TriggerOptions options = new TriggerOptions(new PersonalBuildReason(user.getLogin()), Collections.<ResourcePropertyConfiguration>emptyList());

        BuildRequestEvent request = new PersonalBuildRequestEvent(this, nextId.getAndIncrement(), revision, user, null, null, project.getConfig(), options);

        BuildController controller = mock(BuildController.class);
        doReturn(request.getId()).when(controller).start();
        doReturn(request.getId()).when(controller).getBuildResultId();
        controllers.put(request, controller);

        return request;
    }

    protected QueuedRequest queue(BuildRequestEvent request)
    {
        return queue(request, new QueueThisRequest());
    }

    protected QueuedRequest queue(BuildRequestEvent request, QueuedRequestPredicate... predicates)
    {
        return new QueuedRequest(request, predicates);
    }

    protected QueuedRequest queueRequest(String projectName)
    {
        return queue(createRequest(projectName));
    }

    protected QueuedRequest active(BuildRequestEvent request)
    {
        return active(request, new ActivateThisRequest());
    }

    protected QueuedRequest active(BuildRequestEvent request, QueuedRequestPredicate... predicates)
    {
        return new QueuedRequest(request, predicates);
    }

    protected QueuedRequest activeRequest(String projectName)
    {
        return active((createRequest(projectName)));
    }

    protected BuildCompletedEvent createFailed(BuildRequestEvent request)
    {
        BuildCompletedEvent evt = createCompletedEvent(request);
        evt.getBuildResult().setState(ResultState.FAILURE);
        return evt;
    }

    protected BuildCompletedEvent createSuccessful(BuildRequestEvent request)
    {
        BuildCompletedEvent evt = createCompletedEvent(request);
        evt.getBuildResult().setState(ResultState.SUCCESS);
        return evt;
    }

    protected BuildCompletedEvent createErrored(BuildRequestEvent request)
    {
        BuildCompletedEvent evt = createCompletedEvent(request);
        evt.getBuildResult().setState(ResultState.ERROR);
        return evt;
    }

    protected BuildCompletedEvent createFailed(long metaBuildId, Project owner)
    {
        BuildCompletedEvent evt = createCompletedEvent(metaBuildId, owner);
        evt.getBuildResult().setState(ResultState.FAILURE);
        return evt;
    }

    protected BuildCompletedEvent createSuccessful(long metaBuildId, Project owner)
    {
        BuildCompletedEvent evt = createCompletedEvent(metaBuildId, owner);
        evt.getBuildResult().setState(ResultState.SUCCESS);
        return evt;
    }

    protected BuildCompletedEvent createCompletedEvent(long metaBuildId, Project owner)
    {
        // For build requests that are generated by the handlers internally, we need some way to
        // produce build completed events.  The easiest way is to create a fake one.  The important
        // details are the owner and the metabuildid.

        BuildRequestEvent fakeRequest = createRequest(owner);
        fakeRequest.setMetaBuildId(metaBuildId);
        return createCompletedEvent(fakeRequest);
    }

    private BuildCompletedEvent createCompletedEvent(BuildRequestEvent request)
    {
        return new BuildCompletedEvent(this, createBuildResult(request), new PulseExecutionContext());
    }

    protected BuildResult createBuildResult(BuildRequestEvent request)
    {
        BuildResult result = request.createResult(projectManager, buildManager);
        result.setId(request.getId());
        result.setMetaBuildId(request.getMetaBuildId());
        return result;
    }

    protected DependencyConfiguration dependency(Project project)
    {
        DependencyConfiguration dependencyConfiguration = new DependencyConfiguration();
        dependencyConfiguration.setProject(project.getConfig());
        return dependencyConfiguration;
    }

    protected void setProjectState(Project.State state, Project... projects)
    {
        // update the state property since the actual implemenation is stubbed.
        try
        {
            for (Project project : projects)
            {
                ReflectionUtils.setFieldValue(project, Project.class.getDeclaredField("state"), state);
            }
        }
        catch (NoSuchFieldException e)
        {
            throw new RuntimeException(e);
        }
    }

    private class QueueThisRequest implements QueuedRequestPredicate
    {
        public boolean apply(QueuedRequest queuedRequest)
        {
            return false;
        }
    }

    private class ActivateThisRequest implements QueuedRequestPredicate
    {
        public boolean apply(QueuedRequest queuedRequest)
        {
            return true;
        }
    }
}
