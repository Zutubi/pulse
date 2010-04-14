package com.zutubi.tove.config.health;

import com.zutubi.tove.annotations.Ordered;
import com.zutubi.tove.annotations.Reference;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.config.AbstractConfigurationSystemTestCase;
import com.zutubi.tove.config.api.AbstractConfiguration;
import com.zutubi.tove.config.api.AbstractNamedConfiguration;
import com.zutubi.tove.type.CompositeType;
import com.zutubi.tove.type.MapType;
import com.zutubi.tove.type.TemplatedMapType;
import com.zutubi.tove.type.record.MutableRecord;
import com.zutubi.tove.type.record.MutableRecordImpl;
import com.zutubi.tove.type.record.Record;
import com.zutubi.tove.type.record.TemplateRecord;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static com.zutubi.tove.type.record.PathUtils.getPath;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

public class ConfigurationHealthCheckerTest extends AbstractConfigurationSystemTestCase
{
    private static final String SCOPE_NORMAL    = "normal";
    private static final String SCOPE_TEMPLATED = "templated";
    
    private ConfigurationHealthChecker configurationHealthChecker;
    
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        disableHealthCheckOnTeardown();

        CompositeType projectType = typeRegistry.register(Project.class);
        typeRegistry.register(Subversion.class);
        typeRegistry.register(Perforce.class);
        configurationPersistenceManager.register(SCOPE_NORMAL, new MapType(projectType, typeRegistry));
        configurationPersistenceManager.register(SCOPE_TEMPLATED, new TemplatedMapType(projectType, typeRegistry));
        
        configurationHealthChecker = objectFactory.buildBean(ConfigurationHealthChecker.class);
    }
    
    public void testCheckAllEmpty()
    {
        checkAllTest();
    }
    
    public void testCheckAllValidData()
    {
        Project normalProject = new Project("i am normal");
        normalProject.addLabel(new Label("groupy"));
        normalProject.addStage(new Stage("default"));
        normalProject.addHook(new Hook("a hook"));
        configurationTemplateManager.insert(SCOPE_NORMAL, normalProject);

        Project global = new Project("global template");
        global.addStage(new Stage("default"));
        String globalPath = configurationTemplateManager.insertTemplated(SCOPE_TEMPLATED, global, null, true);

        Project child = createChildProject(globalPath, "child");
        child.addStage(new Stage("another stage"));
        configurationTemplateManager.insertTemplated(SCOPE_TEMPLATED, child, globalPath, false);
        
        checkAllTest();
    }
    
    public void testCheckAllRootContainsSimpleKey()
    {
        Record rootRecord = recordManager.select();
        MutableRecord mutable = rootRecord.copy(false, true);
        mutable.put("simple", "value");
        recordManager.update("", mutable);

        checkAllTest(new ConfigurationHealthProblem("", "Root record contains unexpected simple key 'simple'."));
    }
    
    public void testCheckAllRootContainsUnknownScope()
    {
        recordManager.insert("unknown", new MutableRecordImpl());

        checkAllTest(new ConfigurationHealthProblem("", "Root record contains unexpected nested record 'unknown' (no matching scope registered)."));
    }

    public void testCheckAllCollectionWithSymbolicName()
    {
        String projectPath = configurationTemplateManager.insert(SCOPE_NORMAL, new Project("pro"));
        String stagesPath = getPath(projectPath, "stages");
        MutableRecord stagesRecord = recordManager.select(stagesPath).copy(false, true);
        stagesRecord.setSymbolicName("atype");
        recordManager.update(stagesPath, stagesRecord);

        checkAllTest(new ConfigurationHealthProblem(stagesPath, "Expected a collection, but got symbolic name 'atype'."));
    }

    public void testCheckAllCollectionWithSimpleKey()
    {
        String projectPath = configurationTemplateManager.insert(SCOPE_NORMAL, new Project("pro"));
        String stagesPath = getPath(projectPath, "stages");
        MutableRecord stagesRecord = recordManager.select(stagesPath).copy(false, true);
        stagesRecord.put("simple", "value");
        recordManager.update(stagesPath, stagesRecord);

        checkAllTest(new ConfigurationHealthProblem(stagesPath, "Unexpected simple key 'simple'."));
    }

    public void testCheckAllCollectionWithBadOrder()
    {
        String projectPath = configurationTemplateManager.insert(SCOPE_NORMAL, new Project("pro"));
        String stagesPath = getPath(projectPath, "stages");
        MutableRecord stagesRecord = recordManager.select(stagesPath).copy(false, true);
        MapType.setOrder(stagesRecord, asList("bad"));
        recordManager.update(stagesPath, stagesRecord);

        checkAllTest(new ConfigurationHealthProblem(stagesPath, "Order contains reference to unknown item 'bad'."));
    }
    
    public void testCheckAllInheritedOrderRefersToHiddenItem()
    {
        String rootPath = insertRootProjectWithStages("s1", "s2");
        String childPath = configurationTemplateManager.insertTemplated(SCOPE_TEMPLATED, createChildProject(rootPath, "child"), rootPath, false);
        
        configurationTemplateManager.setOrder(getPath(rootPath, "stages"), asList("s2", "s1"));
        configurationTemplateManager.delete(getPath(childPath, "stages", "s2"));

        checkAllTest();
    }

    public void testCheckAllCollectionWithBadHiddenItem()
    {
        String rootPath = insertRootProjectWithStages("s1", "s2");
        String childPath = configurationTemplateManager.insertTemplated(SCOPE_TEMPLATED, createChildProject(rootPath, "child"), rootPath, false);

        String stagesPath = getPath(childPath, "stages");
        MutableRecord stagesRecord = recordManager.select(stagesPath).copy(false, true);
        TemplateRecord.hideItem(stagesRecord, "invalid");
        recordManager.update(stagesPath, stagesRecord);

        checkAllTest(new ConfigurationHealthProblem(stagesPath, "Hidden key 'invalid' does not exist in template parent."));
    }

    public void testCheckAllCollectionItemTypeInvalid()
    {
        MutableRecordImpl record = new MutableRecordImpl();
        record.setSymbolicName("invalid");
        String path = getPath(SCOPE_NORMAL, "item");
        recordManager.insert(path, record);
        
        checkAllTest(new ConfigurationHealthProblem(path, "Unrecognised symbolic name 'invalid' when expecting type 'class com.zutubi.tove.config.health.ConfigurationHealthCheckerTest$Project'"));
    }

    public void testCheckAllCollectionItemTypeMismatch()
    {
        MutableRecordImpl record = new MutableRecordImpl();
        record.setSymbolicName("stage");
        String path = getPath(SCOPE_NORMAL, "item");
        recordManager.insert(path, record);
        
        checkAllTest(new ConfigurationHealthProblem(path, "Expected type: class com.zutubi.tove.config.health.ConfigurationHealthCheckerTest$Project but instead found class com.zutubi.tove.config.health.ConfigurationHealthCheckerTest$Stage"));
    }

    public void testCheckAllCompositeUnrecognisedSimpleProperty()
    {
        String path = configurationTemplateManager.insert(SCOPE_NORMAL, new Project("p"));
        MutableRecord record = configurationTemplateManager.getRecord(path).copy(false, true);
        record.put("unknown", "value");
        recordManager.update(path, record);
        
        checkAllTest(new ConfigurationHealthProblem(path, "Record contains unrecognised key 'unknown'."));
    }
    
    public void testCheckAllCompositeUnrecognisedComplexProperty()
    {
        String path = configurationTemplateManager.insert(SCOPE_NORMAL, new Project("p"));
        recordManager.insert(getPath(path, "unknown"), new MutableRecordImpl());
        
        checkAllTest(new ConfigurationHealthProblem(path, "Record contains unrecognised key 'unknown'."));
    }
    
    public void testCheckAllCompositeCompositePropertySimple()
    {
        String path = configurationTemplateManager.insert(SCOPE_NORMAL, new Project("p"));
        MutableRecord record = configurationTemplateManager.getRecord(path).copy(false, true);
        record.put("options", "value");
        recordManager.update(path, record);
        
        checkAllTest(new ConfigurationHealthProblem(path, "Simple value found at key 'options' but corresponding property has complex type."));
    }
    
    public void testCheckAllCompositeCollectionPropertySimple()
    {
        String path = configurationTemplateManager.insert(SCOPE_NORMAL, new Project("p"));
        MutableRecord record = configurationTemplateManager.getRecord(path).copy(false, true);
        record.put("stages", "value");
        recordManager.update(path, record);
        
        checkAllTest(new ConfigurationHealthProblem(path, "Simple value found at key 'stages' but corresponding property has complex type."));
    }
    
    public void testCheckAllCompositeCollectionPropertyMissing()
    {
        String rootPath = insertRootProjectWithStages();
        String stagesPath = getPath(rootPath, "stages");
        recordManager.delete(stagesPath);
        
        checkAllTest(new ConfigurationHealthProblem(rootPath, "Expected nested record for key 'stages' was missing."));
    }
    
    public void testCheckAllCompositeSimplePropertyCollection()
    {
        String path = configurationTemplateManager.insert(SCOPE_NORMAL, new Project("p"));
        recordManager.insert(getPath(path, "description"), new MutableRecordImpl());
        
        checkAllTest(new ConfigurationHealthProblem(path, "Nested record found at key 'description' but corresponding property has simple type."));
    }
    
    public void testCheckAllReferenceHandleInvalid()
    {
        Project project = new Project("p");
        project.addHook(new Hook("h"));
        String projectPath = configurationTemplateManager.insert(SCOPE_NORMAL, project);
        String hookPath = getPath(projectPath, "hooks", "h");
        MutableRecord hookRecord = recordManager.select(hookPath).copy(false, true);
        hookRecord.put("stage", "invalid");
        recordManager.update(hookPath, hookRecord);
        
        checkAllTest(new ConfigurationHealthProblem(hookPath, "Getting handle for reference property 'stage': Illegal reference 'invalid'"));
    }

    public void testCheckAllReferenceHandleUnknown()
    {
        Project project = new Project("p");
        project.addHook(new Hook("h"));
        String projectPath = configurationTemplateManager.insert(SCOPE_NORMAL, project);
        String hookPath = getPath(projectPath, "hooks", "h");
        MutableRecord hookRecord = recordManager.select(hookPath).copy(false, true);
        hookRecord.put("stage", "388765");
        recordManager.update(hookPath, hookRecord);
        
        checkAllTest(new ConfigurationHealthProblem(hookPath, "Broken reference for property 'stage': raw handle does not exist."));
    }

    public void testCheckAllReferenceHandleInvalidInChild()
    {
        String rootPath = insertRootProjectWithStages("default");
        
        Project childProject = createChildProject(rootPath, "child");
        String childPath = configurationTemplateManager.insertTemplated(SCOPE_TEMPLATED, childProject, rootPath, false);
        configurationTemplateManager.delete(getPath(childPath, "stages", "default"));

        Project rootProject = configurationTemplateManager.getInstance(rootPath, Project.class);
        rootProject.addHook(new Hook("h", rootProject.getStages().get("default")));
        configurationTemplateManager.save(rootProject);
        
        checkAllTest(new ConfigurationHealthProblem(getPath(childPath, "hooks", "h"), "Broken reference for property 'stage': path is invalid when pushed down."));
    }

    public void testCheckAllReferenceHandleNotPulledUp()
    {
        String rootPath = insertRootProjectWithStages("default");
        Project rootProject = configurationTemplateManager.getInstance(rootPath, Project.class);
        rootProject.addHook(new Hook("h", rootProject.getStages().get("default")));
        configurationTemplateManager.save(rootProject);
        
        Project childProject = createChildProject(rootPath, "child");
        String childPath = configurationTemplateManager.insertTemplated(SCOPE_TEMPLATED, childProject, rootPath, false);

        String childHookPath = getPath(childPath, "hooks", "h");
        long childStageHandle = recordManager.select(getPath(childPath, "stages", "default")).getHandle();
        MutableRecord childHookRecord = recordManager.select(childHookPath).copy(false, true);
        childHookRecord.put("stage", Long.toString(childStageHandle));
        recordManager.update(childHookPath, childHookRecord);
        
        checkAllTest(new ConfigurationHealthProblem(childHookPath, "Reference for property 'stage' is not pulled up to highest level."));
    }
    
    public void testCheckAllInheritanceTypeMismatch()
    {
        Project rootProject = new Project("root");
        rootProject.setScm(new Perforce());
        String rootPath = configurationTemplateManager.insertTemplated(SCOPE_TEMPLATED, rootProject, null, true);
        
        Project childProject = createChildProject(rootPath, "child");
        String childPath = configurationTemplateManager.insertTemplated(SCOPE_TEMPLATED, childProject, rootPath, false);

        MutableRecordImpl childScmRecord = new MutableRecordImpl();
        childScmRecord.setSymbolicName("subversion");
        String childScmPath = getPath(childPath, "scm");
        recordManager.delete(childScmPath);
        recordManager.insert(childScmPath, childScmRecord);
        
        checkAllTest(new ConfigurationHealthProblem(childScmPath, "Type does not match template parent: this type 'subversion', parent type 'perforce'."));
    }
    
    public void testCheckAllMissingSkeleton()
    {
        Project rootProject = new Project("root");
        rootProject.setScm(new Perforce());
        String rootPath = configurationTemplateManager.insertTemplated(SCOPE_TEMPLATED, rootProject, null, true);
        
        Project childProject = createChildProject(rootPath, "child");
        String childPath = configurationTemplateManager.insertTemplated(SCOPE_TEMPLATED, childProject, rootPath, false);

        recordManager.delete(getPath(childPath, "scm"));
        
        checkAllTest(new ConfigurationHealthProblem(childPath, "Template parent contains nested record 'scm' not present or hidden in this child."));
    }

    public void testCheckAllInheritedSimpleValueNotScrubbed()
    {
        Project rootProject = new Project("root");
        rootProject.setDescription("mundane");
        String rootPath = configurationTemplateManager.insertTemplated(SCOPE_TEMPLATED, rootProject, null, true);
        
        Project childProject = createChildProject(rootPath, "child");
        String childPath = configurationTemplateManager.insertTemplated(SCOPE_TEMPLATED, childProject, rootPath, false);

        MutableRecord childRecord = recordManager.select(childPath).copy(false, true);
        childRecord.put("description", "mundane");
        recordManager.update(childPath, childRecord);
        
        checkAllTest(new ConfigurationHealthProblem(childPath, "Value of simple key 'description' should be scrubbed as it is identical in the template parent."));
    }
    
    public void testCheckAllTemplateParentInvalid()
    {
        String rootPath = insertRootProjectWithStages();
        Project childProject = createChildProject(rootPath, "child");
        String childPath = configurationTemplateManager.insertTemplated(SCOPE_TEMPLATED, childProject, rootPath, false);
        
        MutableRecord childRecord = recordManager.select(childPath).copy(false, true);
        childRecord.putMeta(TemplateRecord.PARENT_KEY, "invalid");
        recordManager.update(childPath, childRecord);
        
        checkAllTest(new ConfigurationHealthProblem(childPath, "Illegal parent handle value 'invalid'."));
    }
    
    public void testCheckAllTemplateParentUnknown()
    {
        String rootPath = insertRootProjectWithStages();
        Project childProject = createChildProject(rootPath, "child");
        String childPath = configurationTemplateManager.insertTemplated(SCOPE_TEMPLATED, childProject, rootPath, false);
        
        MutableRecord childRecord = recordManager.select(childPath).copy(false, true);
        childRecord.putMeta(TemplateRecord.PARENT_KEY, "33115599");
        recordManager.update(childPath, childRecord);
        
        checkAllTest(new ConfigurationHealthProblem(childPath, "Unknown parent handle '33115599'."));
    }

    public void testCheckAllTemplateParentNotItemOfTemplateCollection()
    {
        String normalPath = configurationTemplateManager.insert(SCOPE_NORMAL, new Project("norm"));
        
        String rootPath = insertRootProjectWithStages();
        Project childProject = createChildProject(rootPath, "child");
        String childPath = configurationTemplateManager.insertTemplated(SCOPE_TEMPLATED, childProject, rootPath, false);
        
        MutableRecord childRecord = recordManager.select(childPath).copy(false, true);
        long normalHandle = recordManager.select(normalPath).getHandle();
        childRecord.putMeta(TemplateRecord.PARENT_KEY, Long.toString(normalHandle));
        recordManager.update(childPath, childRecord);
        
        checkAllTest(new ConfigurationHealthProblem(childPath, "Parent handle references invalid path 'normal/norm': not an item of the same templated collection."));
    }

    public void testCheckAllTemplateParentNotTemplate()
    {
        String rootPath = insertRootProjectWithStages();
        Project child1Project = createChildProject(rootPath, "child1");
        Project child2Project = createChildProject(rootPath, "child2");
        String child1Path = configurationTemplateManager.insertTemplated(SCOPE_TEMPLATED, child1Project, rootPath, false);
        String child2Path = configurationTemplateManager.insertTemplated(SCOPE_TEMPLATED, child2Project, rootPath, false);
        
        MutableRecord child1Record = recordManager.select(child1Path).copy(false, true);
        long child2Handle = recordManager.select(child2Path).getHandle();
        child1Record.putMeta(TemplateRecord.PARENT_KEY, Long.toString(child2Handle));
        recordManager.update(child1Path, child1Record);
        
        checkAllTest(new ConfigurationHealthProblem(child1Path, "Parent handle references invalid path 'templated/child2': record is not a template."));
    }
    
    public void testCheckPathEmpty()
    {
        breakBothScopes();
        assertEquals(configurationHealthChecker.checkAll(), configurationHealthChecker.checkPath(""));
    }

    public void testCheckPathScope()
    {
        breakScope(SCOPE_NORMAL);
        checkPathTest(SCOPE_NORMAL, new ConfigurationHealthProblem(SCOPE_NORMAL, "Unexpected simple key 'simple'."));
        checkPathTest(SCOPE_TEMPLATED);
    }

    public void testCheckPathTemplatedCollectionItem()
    {
        String rootPath = insertRootProjectWithStages();
        Project childProject = createChildProject(rootPath, "child");
        String childPath = configurationTemplateManager.insertTemplated(SCOPE_TEMPLATED, childProject, rootPath, false);

        checkPathTest(childPath);
        
        // Now break the parent pointer and ensure it is detected.
        MutableRecord childRecord = recordManager.select(childPath).copy(false, true);
        childRecord.putMeta(TemplateRecord.PARENT_KEY, "invalid");
        recordManager.update(childPath, childRecord);

        checkPathTest(childPath, new ConfigurationHealthProblem(childPath, "Illegal parent handle value 'invalid'."));
    }

    public void testCheckPathWithinTemplatedCollectionItem()
    {
        Project rootProject = new Project("root");
        rootProject.setScm(new Perforce());
        String rootPath = configurationTemplateManager.insertTemplated(SCOPE_TEMPLATED, rootProject, null, true);
        
        Project childProject = createChildProject(rootPath, "child");
        String childPath = configurationTemplateManager.insertTemplated(SCOPE_TEMPLATED, childProject, rootPath, false);

        checkPathTest(childPath);

        // Now mess up the structure and ensure it is detected.
        recordManager.delete(getPath(childPath, "scm"));

        checkPathTest(childPath, new ConfigurationHealthProblem(childPath, "Template parent contains nested record 'scm' not present or hidden in this child."));
    }

    public void testCheckPathNormalPath()
    {
        String path = configurationTemplateManager.insert(SCOPE_NORMAL, new Project("p"));

        checkPathTest(path);

        // Now mess up a proeprty and ensure it is detected.
        MutableRecord record = configurationTemplateManager.getRecord(path).copy(false, true);
        record.put("stages", "value");
        recordManager.update(path, record);

        checkPathTest(path, new ConfigurationHealthProblem(path, "Simple value found at key 'stages' but corresponding property has complex type."));
    }
    
    public void testCheckPathPathInvalid()
    {
        try
        {
            configurationHealthChecker.checkPath("invalid");
            fail("Should not be able to check invalid path");
        }
        catch (IllegalArgumentException e)
        {
            assertThat(e.getMessage(), containsString("Path 'invalid' does not exist"));
        }
    }
    
    private void breakBothScopes()
    {
        breakScope(SCOPE_NORMAL);
        breakScope(SCOPE_TEMPLATED);
    }

    private void breakScope(String scope)
    {
        MutableRecord normalRecord = recordManager.select(scope).copy(false, true);
        normalRecord.put("simple", "value");
        recordManager.update(scope, normalRecord);
    }

    private String insertRootProjectWithStages(String... stages)
    {
        Project root = new Project("root");
        for (String stage: stages)
        {
            root.addStage(new Stage(stage));
        }
        return configurationTemplateManager.insertTemplated(SCOPE_TEMPLATED, root, null, true);
    }

    private Project createChildProject(String parentPath, String name)
    {
        Project child = configurationTemplateManager.deepClone(configurationTemplateManager.getInstance(parentPath, Project.class));
        child.setName(name);
        child.setConcrete(true);
        child.putMeta(TemplateRecord.TEMPLATE_KEY, "");
        child.putMeta(TemplateRecord.PARENT_KEY, "");
        return child;
    }

    private void checkAllTest(ConfigurationHealthProblem... expectedProblems)
    {
        assertEquals(new ConfigurationHealthReport(expectedProblems), configurationHealthChecker.checkAll());        
    }

    private void checkPathTest(String path, ConfigurationHealthProblem... expectedProblems)
    {
        assertEquals(new ConfigurationHealthReport(expectedProblems), configurationHealthChecker.checkPath(path));
    }

    @SymbolicName("project")
    public static class Project extends AbstractNamedConfiguration
    {
        private String description;
        private Options options;
        private Scm scm;
        private List<Label> labels = new LinkedList<Label>();
        @Ordered
        private Map<String, Stage> stages = new HashMap<String, Stage>();
        private Map<String, Hook> hooks = new HashMap<String, Hook>();

        public Project()
        {
        }

        public Project(String name)
        {
            super(name);
        }

        public String getDescription()
        {
            return description;
        }

        public void setDescription(String description)
        {
            this.description = description;
        }

        public Options getOptions()
        {
            return options;
        }

        public void setOptions(Options options)
        {
            this.options = options;
        }

        public Scm getScm()
        {
            return scm;
        }

        public void setScm(Scm scm)
        {
            this.scm = scm;
        }

        public List<Label> getLabels()
        {
            return labels;
        }

        public void setLabels(List<Label> labels)
        {
            this.labels = labels;
        }

        public void addLabel(Label label)
        {
            labels.add(label);
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
    
    @SymbolicName("options")
    public static class Options extends AbstractConfiguration
    {
        private boolean magic;

        public boolean isMagic()
        {
            return magic;
        }

        public void setMagic(boolean magic)
        {
            this.magic = magic;
        }
    }

    @SymbolicName("scm")
    public static abstract class Scm extends AbstractConfiguration
    {
        private String common;

        public String getCommon()
        {
            return common;
        }

        public void setCommon(String common)
        {
            this.common = common;
        }
    }
    
    @SymbolicName("subversion")
    public static class Subversion extends Scm
    {
        private String url;

        public String getUrl()
        {
            return url;
        }

        public void setUrl(String url)
        {
            this.url = url;
        }
    }
    
    @SymbolicName("perforce")
    public static class Perforce extends Scm
    {
        private String client;

        public String getClient()
        {
            return client;
        }

        public void setClient(String client)
        {
            this.client = client;
        }
    }
    
    @SymbolicName("label")
    public static class Label extends AbstractConfiguration
    {
        private String label;

        public Label()
        {
        }

        public Label(String label)
        {
            this.label = label;
        }

        public String getLabel()
        {
            return label;
        }

        public void setLabel(String label)
        {
            this.label = label;
        }
    }
    
    @SymbolicName("stage")
    public static class Stage extends AbstractNamedConfiguration
    {
        private String recipe;

        public Stage()
        {
        }

        public Stage(String name)
        {
            super(name);
        }

        public String getRecipe()
        {
            return recipe;
        }

        public void setRecipe(String recipe)
        {
            this.recipe = recipe;
        }
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

        public Hook(String name, Stage stage)
        {
            this(name);
            this.stage = stage;
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
}
