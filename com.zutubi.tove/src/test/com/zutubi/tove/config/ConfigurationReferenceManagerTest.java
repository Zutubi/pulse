package com.zutubi.tove.config;

import com.zutubi.tove.annotations.Reference;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.config.api.AbstractNamedConfiguration;
import com.zutubi.tove.config.api.Configuration;
import com.zutubi.tove.config.api.NamedConfiguration;
import com.zutubi.tove.type.CompositeType;
import com.zutubi.tove.type.MapType;
import com.zutubi.tove.type.TemplatedMapType;
import com.zutubi.tove.type.record.MutableRecord;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Mapping;
import com.zutubi.util.Sort;
import com.zutubi.validation.annotations.Required;

import java.util.*;

/**
 */
public class ConfigurationReferenceManagerTest extends AbstractConfigurationSystemTestCase
{
    private CompositeType projectType;
    private CompositeType stageType;

    protected void setUp() throws Exception
    {
        super.setUp();
        
        projectType = typeRegistry.register(Project.class);
        stageType = typeRegistry.register(Stage.class);
        typeRegistry.register(Action.class);
        CompositeType subscriptionType = typeRegistry.register(Subscription.class);

        TemplatedMapType templated = new TemplatedMapType(projectType, typeRegistry);
        configurationPersistenceManager.register("template", templated);

        MapType project = new MapType(projectType, typeRegistry);
        configurationPersistenceManager.register("project", project);

        MapType subscription = new MapType(subscriptionType, typeRegistry);
        configurationPersistenceManager.register("subscription", subscription);

        Project p1 = new Project("p1");
        p1.addStage(new Stage("p1s1"));
        p1.addStage(new Stage("p1s2"));
        configurationTemplateManager.insert("project", p1);

        Project p2 = new Project("p2");
        p2.addStage(new Stage("p2s1"));
        configurationTemplateManager.insert("project", p2);

        Project p3 = new Project("p3");
        configurationTemplateManager.insert("project", p3);

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

        long childHandle = configurationTemplateManager.getRecord("template/child").getHandle();

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

    private void assertConcreteProjects(Collection<Configuration> referenceable)
    {
        assertNamedConfigurations(referenceable, "p1", "p2", "p3", "concreteChild", "concreteGrandchild");
    }

    private void assertNamedConfigurations(Collection<Configuration> collection, String... expected)
    {
        Sort.StringComparator comparator = new Sort.StringComparator();
        String[] got = new String[collection.size()];
        CollectionUtils.mapToArray(collection, new Mapping<Configuration, String>()
        {
            public String map(Configuration configuration)
            {
                return ((NamedConfiguration)configuration).getName();
            }
        }, got);

        Arrays.sort(expected, comparator);
        Arrays.sort(got, comparator);
        assertTrue(CollectionUtils.equals(expected, got));
    }

    @SymbolicName("action")
    public static class Action extends AbstractNamedConfiguration
    {
        @Reference
        private Stage stage;

        public Action()
        {
        }

        public Action(String name)
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
        private Map<String, Action> actions = new HashMap<String, Action>();

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

        public Map<String, Action> getActions()
        {
            return actions;
        }

        public void setActions(Map<String, Action> actions)
        {
            this.actions = actions;
        }

        public void addAction(Action action)
        {
            actions.put(action.getName(), action);
        }
    }

    @SymbolicName("subscription")
    public static class Subscription extends AbstractNamedConfiguration
    {
        @Required @Reference
        private Stage stageRef;
        @Reference
        private List<Project> projectRefs;

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
