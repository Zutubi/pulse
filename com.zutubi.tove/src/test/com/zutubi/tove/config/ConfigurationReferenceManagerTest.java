package com.zutubi.tove.config;

import com.google.common.base.Function;
import com.zutubi.tove.annotations.Reference;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.config.api.AbstractNamedConfiguration;
import com.zutubi.tove.config.api.Configuration;
import com.zutubi.tove.config.api.NamedConfiguration;
import com.zutubi.tove.type.CompositeType;
import com.zutubi.tove.type.MapType;
import com.zutubi.tove.type.TemplatedMapType;
import com.zutubi.tove.type.record.MutableRecord;
import com.zutubi.util.Sort;
import com.zutubi.validation.annotations.Required;

import java.util.*;

import static com.google.common.collect.Collections2.transform;

public class ConfigurationReferenceManagerTest extends AbstractConfigurationSystemTestCase
{
    private static final String SCOPE_SUBSCRIPTION = "subscription";

    private static final String PATH_PROJECT1 = "project/p1";
    private static final String PATH_PROJECT2 = "project/p2";

    private static final String PATH_GLOBAL_TEMPLATE = "template/global";
    private static final String PATH_TEMPLATE_CHILD = "template/child";
    private static final String PATH_CONCRETE_CHILD = "template/concreteChild";

    private CompositeType projectType;
    private CompositeType stageType;

    protected void setUp() throws Exception
    {
        super.setUp();
        
        projectType = typeRegistry.register(Project.class);
        stageType = typeRegistry.register(Stage.class);
        typeRegistry.register(Hook.class);
        CompositeType subscriptionType = typeRegistry.register(Subscription.class);

        TemplatedMapType templated = new TemplatedMapType(projectType, typeRegistry);
        configurationPersistenceManager.register("template", templated);

        MapType project = new MapType(projectType, typeRegistry);
        configurationPersistenceManager.register("project", project);

        MapType subscription = new MapType(subscriptionType, typeRegistry);
        configurationPersistenceManager.register(SCOPE_SUBSCRIPTION, subscription);

        Project p1 = new Project("p1");
        p1.addStage(new Stage("p1s1"));
        p1.addStage(new Stage("p1s2"));
        configurationTemplateManager.insertInstance("project", p1);

        Project p2 = new Project("p2");
        p2.addStage(new Stage("p2s1"));
        configurationTemplateManager.insertInstance("project", p2);

        Project p3 = new Project("p3");
        configurationTemplateManager.insertInstance("project", p3);

        MutableRecord global = projectType.createNewRecord(false);
        global.put("name", "global");
        configurationTemplateManager.markAsTemplate(global);
        configurationTemplateManager.insertRecord("template", global);

        long globalHandle = configurationTemplateManager.getRecord("template/global").getHandle();

        MutableRecord stage = stageType.createNewRecord(false);
        stage.put("name", "default");
        configurationTemplateManager.insertRecord("template/global/stages", stage);

        MutableRecord child = projectType.createNewRecord(false);
        child.put("name", "child");
        configurationTemplateManager.markAsTemplate(child);
        configurationTemplateManager.setParentTemplate(child, globalHandle);
        configurationTemplateManager.insertRecord("template", child);

        stage = stageType.createNewRecord(false);
        stage.put("name", "childStage");
        configurationTemplateManager.insertRecord("template/child/stages", stage);

        long childHandle = configurationTemplateManager.getRecord(PATH_TEMPLATE_CHILD).getHandle();

        MutableRecord concreteChild = projectType.createNewRecord(false);
        concreteChild.put("name", "concreteChild");
        configurationTemplateManager.setParentTemplate(concreteChild, globalHandle);
        configurationTemplateManager.insertRecord("template", concreteChild);

        stage = stageType.createNewRecord(false);
        stage.put("name", "concreteChildStage");
        configurationTemplateManager.insertRecord("template/concreteChild/stages", stage);

        MutableRecord concreteGrandchild = projectType.createNewRecord(false);
        concreteGrandchild.put("name", "concreteGrandchild");
        configurationTemplateManager.setParentTemplate(concreteGrandchild, childHandle);
        configurationTemplateManager.insertRecord("template", concreteGrandchild);

        stage = stageType.createNewRecord(false);
        stage.put("name", "concreteGrandchildStage");
        configurationTemplateManager.insertRecord("template/concreteGrandchild/stages", stage);
    }

    public void testReferenceable()
    {
        assertConcreteProjects(configurationReferenceManager.getReferencableInstances(projectType, "subscriptions/project"));
    }

    public void testReferenceableWithinSameType()
    {
        Collection<Configuration> ref = configurationReferenceManager.getReferencableInstances(projectType, "project/p1/foo");
        assertNamedConfigurations(ref, "p1", "p2", "p3");
    }

    public void testReferenceableWithinTemplateSameType()
    {
        Collection<Configuration> ref = configurationReferenceManager.getReferencableInstances(projectType, "template/global/foo");
        assertNamedConfigurations(ref, "concreteChild", "concreteGrandchild");
    }

    public void testStagesWithinProject()
    {
        Collection<Configuration> ref = configurationReferenceManager.getReferencableInstances(stageType, "project/p1/somewhere");
        assertNamedConfigurations(ref, "p1s1", "p1s2");
    }

    public void testStagesFromStages()
    {
        Collection<Configuration> ref = configurationReferenceManager.getReferencableInstances(stageType, "project/p1/stages");
        assertNamedConfigurations(ref, "p1s1", "p1s2");
    }

    public void testStagesUnderStages()
    {
        Collection<Configuration> ref = configurationReferenceManager.getReferencableInstances(stageType, "project/p1/stages/under");
        assertNamedConfigurations(ref, "p1s1", "p1s2");
    }

    public void testTemplatedVisibleWithinTemplate()
    {
        Collection<Configuration> ref = configurationReferenceManager.getReferencableInstances(stageType, "template/global/somewhere");
        assertNamedConfigurations(ref, "default");
    }

    public void testInheritedVisibleWithinTemplate()
    {
        Collection<Configuration> ref = configurationReferenceManager.getReferencableInstances(stageType, "template/child/somewhere");
        assertNamedConfigurations(ref, "default", "childStage");
    }

    public void testInheritedVisibleWithinConcrete()
    {
        Collection<Configuration> ref = configurationReferenceManager.getReferencableInstances(stageType, "template/concreteGrandchild/somewhere");
        assertNamedConfigurations(ref, "default", "childStage", "concreteGrandchildStage");
    }

    public void testGetReferencedPathReferenceToNonTemplatedProject()
    {
        assertEquals(PATH_PROJECT1, configurationReferenceManager.getReferencedPathForHandle(null, getHandle(PATH_PROJECT1)));
    }

    public void testGetReferencedPathReferenceToRootTemplateProject()
    {
        assertEquals(PATH_GLOBAL_TEMPLATE, configurationReferenceManager.getReferencedPathForHandle(PATH_GLOBAL_TEMPLATE, getHandle(PATH_GLOBAL_TEMPLATE)));
    }

    public void testGetReferencedPathReferenceToChildProject()
    {
        assertEquals(PATH_TEMPLATE_CHILD, configurationReferenceManager.getReferencedPathForHandle(PATH_TEMPLATE_CHILD, getHandle(PATH_TEMPLATE_CHILD)));
    }

    public void testGetReferencedPathReferenceToOtherProjectNonTemplated()
    {
        assertEquals(PATH_PROJECT2, configurationReferenceManager.getReferencedPathForHandle(null, getHandle(PATH_PROJECT2)));
    }

    public void testGetReferencedPathReferenceToOtherProjectFromRootTemplate()
    {
        assertEquals(PATH_CONCRETE_CHILD, configurationReferenceManager.getReferencedPathForHandle(PATH_GLOBAL_TEMPLATE, getHandle(PATH_CONCRETE_CHILD)));
    }

    public void testGetReferencedPathReferenceToOtherProjectFromChildProject()
    {
        assertEquals(PATH_CONCRETE_CHILD, configurationReferenceManager.getReferencedPathForHandle(PATH_TEMPLATE_CHILD, getHandle(PATH_CONCRETE_CHILD)));
    }

    public void testGetReferencedPathReferenceToConcreteScopeFromTemplatedScope()
    {
        assertEquals(PATH_PROJECT1, configurationReferenceManager.getReferencedPathForHandle(PATH_TEMPLATE_CHILD, getHandle(PATH_PROJECT1)));
    }

    public void testGetReferencedPathReferenceToStageNonTemplated()
    {
        final String PATH = PATH_PROJECT1 + "/stages/p1s1";
        assertEquals(PATH, configurationReferenceManager.getReferencedPathForHandle(null, getHandle(PATH)));
    }

    public void testGetReferencedPathReferenceToStageInRootTemplate()
    {
        final String PATH_STAGE = PATH_GLOBAL_TEMPLATE + "/stages/default";
        assertEquals(PATH_STAGE, configurationReferenceManager.getReferencedPathForHandle(PATH_GLOBAL_TEMPLATE, getHandle(PATH_STAGE)));
    }

    public void testGetReferencedPathReferenceToStageInChildProject()
    {
        final String PATH_STAGE = PATH_TEMPLATE_CHILD + "/stages/childStage";
        assertEquals(PATH_STAGE, configurationReferenceManager.getReferencedPathForHandle(PATH_TEMPLATE_CHILD, getHandle(PATH_STAGE)));
    }

    public void testGetReferencedPathReferenceToStageInheritedByChildProject()
    {
        assertEquals(PATH_TEMPLATE_CHILD + "/stages/default", configurationReferenceManager.getReferencedPathForHandle(PATH_TEMPLATE_CHILD, getHandle(PATH_GLOBAL_TEMPLATE + "/stages/default")));
    }

    public void testReferenceResolutionSubscriptionReferencesNonTemplated()
    {
        subscriptionReferencesProjectHelper(PATH_PROJECT1);
    }

    public void testReferenceResolutionSubscriptionReferencesTemplated()
    {
        subscriptionReferencesProjectHelper(PATH_CONCRETE_CHILD);
    }

    private void subscriptionReferencesProjectHelper(String projectPath)
    {
        Subscription subscription = new Subscription(getName());
        subscription.getProjectRefs().add(configurationTemplateManager.getInstance(projectPath, Project.class));

        String path = configurationTemplateManager.insertInstance(SCOPE_SUBSCRIPTION, subscription);
        subscription = configurationTemplateManager.getInstance(path, Subscription.class);
        assertSame(configurationTemplateManager.getInstance(projectPath), subscription.getProjectRefs().get(0));
    }

    public void testReferenceResolutionSubscriptionReferencesNonTemplatedStage()
    {
        subscriptionReferencesStageHelper(PATH_PROJECT1 + "/stages/p1s1");
    }

    public void testReferenceResolutionSubscriptionReferencesGlobalTemplateStage()
    {
        subscriptionReferencesStageHelper(PATH_GLOBAL_TEMPLATE + "/stages/default");
    }

    public void testReferenceResolutionSubscriptionReferencesChildTemplateStage()
    {
        subscriptionReferencesStageHelper(PATH_TEMPLATE_CHILD + "/stages/childStage");
    }

    public void testReferenceResolutionSubscriptionReferencesInheritedStage()
    {
        subscriptionReferencesStageHelper(PATH_TEMPLATE_CHILD + "/stages/default");
    }

    private void subscriptionReferencesStageHelper(String stagePath)
    {
        Subscription subscription = new Subscription(getName());
        subscription.setStageRef(configurationTemplateManager.getInstance(stagePath, Stage.class));

        String path = configurationTemplateManager.insertInstance(SCOPE_SUBSCRIPTION, subscription);
        subscription = configurationTemplateManager.getInstance(path, Subscription.class);
        assertSame(configurationTemplateManager.getInstance(stagePath), subscription.getStageRef());
    }

    public void testHookReferencesStageNonTemplated()
    {
        hookReferencesStageHelper(PATH_PROJECT1, "p1s1");
    }

    public void testHookReferencesStageGlobalTemplate()
    {
        hookReferencesStageHelper(PATH_GLOBAL_TEMPLATE, "default");
    }

    public void testHookReferencesStageChildTemplate()
    {
        hookReferencesStageHelper(PATH_TEMPLATE_CHILD, "childStage");
    }

    public void testHookReferencesStageInheritedStage()
    {
        // Create a subscription that references the project.  If the
        // subscription scope is instantiated first, this triggers CIB-2622.
        Subscription subscription = new Subscription(getName());
        subscription.getProjectRefs().add(configurationTemplateManager.getInstance(PATH_TEMPLATE_CHILD, Project.class));
        configurationTemplateManager.insertInstance(SCOPE_SUBSCRIPTION, subscription);

        hookReferencesStageHelper(PATH_TEMPLATE_CHILD, "default");
    }

    private void hookReferencesStageHelper(String projectPath, String stageName)
    {
        Project p1 = configurationTemplateManager.deepClone(configurationTemplateManager.getInstance(projectPath, Project.class));
        Hook hook = new Hook(getName());
        hook.setStage(p1.getStages().get(stageName));
        p1.addHook(hook);
        configurationTemplateManager.save(p1);

        p1 = configurationTemplateManager.getInstance(projectPath, Project.class);
        assertSame(p1.getStages().get(stageName), p1.getHooks().get(getName()).getStage());
    }

    private long getHandle(String path)
    {
        return recordManager.select(path).getHandle();
    }

    private void assertConcreteProjects(Collection<Configuration> referenceable)
    {
        assertNamedConfigurations(referenceable, "p1", "p2", "p3", "concreteChild", "concreteGrandchild");
    }

    private void assertNamedConfigurations(Collection<Configuration> collection, String... expected)
    {
        Sort.StringComparator comparator = new Sort.StringComparator();
        String[] got = transform(collection, new Function<Configuration, String>()
        {
            public String apply(Configuration configuration)
            {
                return ((NamedConfiguration) configuration).getName();
            }
        }).toArray(new String[collection.size()]);

        Arrays.sort(expected, comparator);
        Arrays.sort(got, comparator);
        assertTrue(Arrays.equals(expected, got));
    }

    @SymbolicName("hook")
    public static class Hook extends AbstractNamedConfiguration
    {
        @Reference
        private Stage stage;

        public Hook()
        {
        }

        public Hook(String name)
        {
            super(name);
        }

        public Stage getStage()
        {
            return stage;
        }

        public void setStage(Stage stage)
        {
            this.stage = stage;
        }
    }

    @SymbolicName("stage")
    public static class Stage extends AbstractNamedConfiguration
    {
        public Stage()
        {
        }

        public Stage(String name)
        {
            super(name);
        }
    }

    @SymbolicName("project")
    public static class Project extends AbstractNamedConfiguration
    {
        @Reference
        private Stage stageRef;
        private Map<String, Stage> stages = new HashMap<String, Stage>();
        private Map<String, Hook> hooks = new HashMap<String, Hook>();

        public Project()
        {
        }

        public Project(String name)
        {
            super(name);
        }

        public Stage getStageRef()
        {
            return stageRef;
        }

        public void setStageRef(Stage stageRef)
        {
            this.stageRef = stageRef;
        }

        public Map<String, Stage> getStages()
        {
            return stages;
        }

        public void setStages(Map<String, Stage> stages)
        {
            this.stages = stages;
        }

        public void addStage(Stage stage)
        {
            stages.put(stage.getName(), stage);
        }

        public Map<String, Hook> getHooks()
        {
            return hooks;
        }

        public void setHooks(Map<String, Hook> hooks)
        {
            this.hooks = hooks;
        }

        public void addHook(Hook hook)
        {
            hooks.put(hook.getName(), hook);
        }
    }

    @SymbolicName("subscription")
    public static class Subscription extends AbstractNamedConfiguration
    {
        @Required @Reference
        private Stage stageRef;
        @Reference
        private List<Project> projectRefs = new LinkedList<Project>();

        public Subscription()
        {
        }

        public Subscription(String name)
        {
            super(name);
        }

        public Stage getStageRef()
        {
            return stageRef;
        }

        public void setStageRef(Stage stageRef)
        {
            this.stageRef = stageRef;
        }

        public List<Project> getProjectRefs()
        {
            return projectRefs;
        }

        public void setProjectRefs(List<Project> projectRefs)
        {
            this.projectRefs = projectRefs;
        }
    }
}
