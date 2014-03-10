package com.zutubi.tove.config;

import com.zutubi.tove.annotations.Ordered;
import com.zutubi.tove.annotations.Reference;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.config.api.AbstractConfiguration;
import com.zutubi.tove.config.api.AbstractNamedConfiguration;
import com.zutubi.tove.type.CollectionType;
import com.zutubi.tove.type.CompositeType;
import com.zutubi.tove.type.MapType;
import com.zutubi.tove.type.TemplatedMapType;
import com.zutubi.tove.type.record.MutableRecord;
import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.validation.annotations.Required;

import java.io.File;
import java.util.*;

public class ConfigurationArchiverTest extends AbstractConfigurationSystemTestCase
{
    private static final String ARCHIVE_FILE = "archive.xml";
    private static final String VERSION = "1.2.3";

    private static final String SCOPE_PLAIN = "plain";
    private static final String SCOPE_TEMPLATED = "templated";
    private static final String SCOPE_GROUPS = "groups";

    private static final String NAME_ROOT = "root";

    private ConfigurationArchiver configurationArchiver;
    private File tempDir;
    private File archiveFile;

    private String rootPath;
    private String allUsersPath;
    private String anonUsersPath;

    @Override
    public void setUp() throws Exception
    {
        super.setUp();

        configurationArchiver = new ConfigurationArchiver();
        configurationArchiver.setRecordManager(recordManager);
        configurationArchiver.setConfigurationTemplateManager(configurationTemplateManager);
        configurationArchiver.setConfigurationReferenceManager(configurationReferenceManager);
        configurationArchiver.setConfigurationHealthChecker(configurationHealthChecker);

        CompositeType groupType = typeRegistry.register(ArchiveGroup.class);
        configurationPersistenceManager.register(SCOPE_GROUPS, new MapType(groupType, typeRegistry));

        CompositeType projectType = typeRegistry.register(ArchiveProject.class);
        MapType plainMap = new MapType(projectType, typeRegistry);
        MapType templatedMap = new TemplatedMapType(projectType, typeRegistry);

        configurationPersistenceManager.register(SCOPE_PLAIN, plainMap);
        configurationPersistenceManager.register(SCOPE_TEMPLATED, templatedMap);

        MutableRecord allUsers = unstantiate(new ArchiveGroup("all users"));
        allUsersPath = configurationTemplateManager.insertRecord(SCOPE_GROUPS, allUsers);
        MutableRecord anonUsers = unstantiate(new ArchiveGroup("anonymous users"));
        anonUsersPath = configurationTemplateManager.insertRecord(SCOPE_GROUPS, anonUsers);

        ArchiveProject rootProject = new ArchiveProject(NAME_ROOT);
        rootProject.addPostProcessor(new ArchivePostProcessor("ant.pp"));

        MutableRecord root = unstantiate(rootProject);
        configurationTemplateManager.markAsTemplate(root);
        rootPath = configurationTemplateManager.insertRecord(SCOPE_TEMPLATED, root);

        typeRegistry.register(ArchiveSubversionScm.class);
        typeRegistry.register(ArchiveGitScm.class);
        typeRegistry.register(ArchiveBuildCompletedTrigger.class);
        typeRegistry.register(ArchiveScmTrigger.class);

        tempDir = createTempDirectory();
        archiveFile = new File(tempDir, ARCHIVE_FILE);
    }

    @Override
    public void tearDown() throws Exception
    {
        removeDirectory(tempDir);
        super.tearDown();
    }

    public void testPlainSimpleProjectExportImport()
    {
        simpleExportImport(false);
    }

    public void testTemplatedSimpleProjectExportImport()
    {
        simpleExportImport(true);
    }

    private void simpleExportImport(boolean templated)
    {
        ArchiveProject project = new ArchiveProject("simple");
        project.setDescription("desc");
        project.getOptions().setThings(Arrays.asList("thing1", "thing2"));
        project.setScm(new ArchiveGitScm());
        project.addTrigger(new ArchiveScmTrigger("onchange"));

        String scope = templated ? SCOPE_TEMPLATED : SCOPE_PLAIN;
        String path = templated ? configurationTemplateManager.insertTemplatedInstance(SCOPE_TEMPLATED, project, rootPath, false) : configurationTemplateManager.insertInstance(scope, project);
        project = configurationTemplateManager.getInstance(path, ArchiveProject.class);

        ArchiveProject restored = archiveAndRestore(project);
        assertEquals(project.getName(), restored.getName());
        assertEquals(project.getDescription(), restored.getDescription());
        assertEquals(project.getOptions().getThings(), restored.getOptions().getThings());
        assertEquals(project.getScm().getClass(), restored.getScm().getClass());
        assertEquals(1, restored.getTriggers().size());
        ArchiveTrigger trigger = project.getTriggers().values().iterator().next();
        ArchiveTrigger restoredTrigger = restored.getTriggers().values().iterator().next();
        assertEquals(trigger.getName(), restoredTrigger.getName());
    }

    public void testInheritedRestoreToSameParent()
    {
        ArchiveProject rootProject = configurationTemplateManager.deepClone(configurationTemplateManager.getInstance(rootPath, ArchiveProject.class));
        rootProject.setDescription("root desc");
        rootProject.setScm(new ArchiveGitScm());
        rootProject.addTrigger(new ArchiveScmTrigger("triggy"));
        rootProject.addTrigger(new ArchiveScmTrigger("hideme"));

        configurationTemplateManager.save(rootProject);

        ArchiveProject child  = new ArchiveProject("child");
        child.getOptions().setTimeout(10);
        String childPath = configurationTemplateManager.insertTemplatedInstance(SCOPE_TEMPLATED, child, rootPath, false);
        configurationTemplateManager.delete(PathUtils.getPath(childPath, "triggers", "hideme"));
        child = configurationTemplateManager.getInstance(childPath, ArchiveProject.class);

        ArchiveProject restored = archiveAndRestore(child);

        assertEquals(child.getName(), restored.getName());
        assertEquals(child.getDescription(), restored.getDescription());
        assertEquals(child.getOptions().getTimeout(), restored.getOptions().getTimeout());
        assertEquals(child.getScm().getClass(), restored.getScm().getClass());
        assertEquals(1, restored.getTriggers().size());
        ArchiveTrigger restoredTrigger = restored.getTriggers().values().iterator().next();
        assertEquals("triggy", restoredTrigger.getName());
    }

    public void testInheritedRestoreToDifferentParent()
    {
        ArchiveProject parent = new ArchiveProject("parent");
        parent.setDescription("parent desc");
        parent.setScm(new ArchiveGitScm());
        parent.addTrigger(new ArchiveScmTrigger("triggy"));
        parent.addTrigger(new ArchiveScmTrigger("hideme"));

        String parentPath = configurationTemplateManager.insertTemplatedInstance(SCOPE_TEMPLATED, parent, rootPath, true);

        ArchiveProject child  = new ArchiveProject("child");
        child.getOptions().setTimeout(10);
        String childPath = configurationTemplateManager.insertTemplatedInstance(SCOPE_TEMPLATED, child, parentPath, false);
        child = configurationTemplateManager.getInstance(childPath, ArchiveProject.class);
        configurationTemplateManager.delete(PathUtils.getPath(childPath, "triggers", "hideme"));

        ArchiveProject restored = archiveAndRestore(child);

        // Despite moving away from the parent that defines the values, we should be restored with them intact (just no
        // longer inherited!).  To make extra sure, we actually delete the parent!
        configurationTemplateManager.delete(parentPath);

        assertEquals(child.getName(), restored.getName());
        assertEquals(child.getDescription(), restored.getDescription());
        assertEquals(child.getOptions().getTimeout(), restored.getOptions().getTimeout());
        assertEquals(child.getScm().getClass(), restored.getScm().getClass());
        assertEquals(1, restored.getTriggers().size());
        ArchiveTrigger restoredTrigger = restored.getTriggers().values().iterator().next();
        assertEquals("triggy", restoredTrigger.getName());
    }

    public void testExportImportSubtree()
    {
        ArchiveProject parent = new ArchiveProject("parent");
        parent.setDescription("parent desc");
        parent.setScm(new ArchiveGitScm());
        parent.addTrigger(new ArchiveScmTrigger("triggy"));
        parent.addTrigger(new ArchiveScmTrigger("hideme"));

        String parentPath = configurationTemplateManager.insertTemplatedInstance(SCOPE_TEMPLATED, parent, rootPath, true);
        parent = configurationTemplateManager.getInstance(parentPath, ArchiveProject.class);

        ArchiveProject child  = new ArchiveProject("child");
        child.getOptions().setTimeout(10);
        String childPath = configurationTemplateManager.insertTemplatedInstance(SCOPE_TEMPLATED, child, parentPath, false);
        configurationTemplateManager.delete(PathUtils.getPath(childPath, "triggers", "hideme"));
        child = configurationTemplateManager.getInstance(childPath, ArchiveProject.class);

        archiveAndRestore(parent, child);

        ArchiveProject restoredParent = configurationTemplateManager.getInstance(parentPath, ArchiveProject.class);
        assertFalse(restoredParent.isConcrete());
        assertEquals(parent.getName(), restoredParent.getName());
        assertEquals(parent.getDescription(), restoredParent.getDescription());
        assertEquals(parent.getOptions().getTimeout(), restoredParent.getOptions().getTimeout());
        assertEquals(parent.getScm().getClass(), restoredParent.getScm().getClass());
        assertEquals(parent.getTriggers().keySet(), restoredParent.getTriggers().keySet());

        ArchiveProject restoredChild = configurationTemplateManager.getInstance(childPath, ArchiveProject.class);
        assertTrue(restoredChild.isConcrete());
        assertEquals(child.getName(), restoredChild.getName());
        assertEquals(child.getDescription(), restoredChild.getDescription());
        assertEquals(child.getOptions().getTimeout(), restoredChild.getOptions().getTimeout());
        assertEquals(child.getScm().getClass(), restoredChild.getScm().getClass());
        assertEquals(child.getTriggers().keySet(), restoredChild.getTriggers().keySet());
    }

    public void testReferencesToOutsideArchive()
    {
        ArchiveProject upstream = new ArchiveProject("upstream");
        String upstreamPath = configurationTemplateManager.insertTemplatedInstance(SCOPE_TEMPLATED, upstream, rootPath, false);
        upstream = configurationTemplateManager.getInstance(upstreamPath, ArchiveProject.class);

        ArchiveProject downstream = new ArchiveProject("downstream");
        ArchiveBuildCompletedTrigger trigger = new ArchiveBuildCompletedTrigger("post up");
        trigger.setProject(upstream);
        downstream.addTrigger(trigger);
        downstream.getDependencies().addProject(upstream);

        ArchiveGroup allUsers = configurationTemplateManager.getInstance(allUsersPath, ArchiveGroup.class);
        downstream.getPermissions().add(new ArchiveProjectAcl(allUsers));

        String downstreamPath = configurationTemplateManager.insertTemplatedInstance(SCOPE_TEMPLATED, downstream, rootPath, false);
        downstream = configurationTemplateManager.getInstance(downstreamPath, ArchiveProject.class);

        ArchiveProject restoredDownstream = archiveAndRestore(downstream);

        ArchiveBuildCompletedTrigger restoredTrigger = (ArchiveBuildCompletedTrigger) restoredDownstream.getTriggers().get(trigger.getName());
        assertNull(restoredTrigger.getProject());
        assertEquals(0, restoredDownstream.getDependencies().getProjects().size());

        assertEquals(0, restoredDownstream.getPermissions().size());
    }

    public void testReferencesToInsideArchive()
    {
        ArchiveProject upstream = new ArchiveProject("upstream");
        String upstreamPath = configurationTemplateManager.insertTemplatedInstance(SCOPE_TEMPLATED, upstream, rootPath, false);
        upstream = configurationTemplateManager.getInstance(upstreamPath, ArchiveProject.class);

        ArchiveProject downstream = new ArchiveProject("downstream");
        ArchiveBuildCompletedTrigger trigger = new ArchiveBuildCompletedTrigger("post up");
        trigger.setProject(upstream);
        downstream.addTrigger(trigger);
        downstream.getDependencies().addProject(upstream);

        String downstreamPath = configurationTemplateManager.insertTemplatedInstance(SCOPE_TEMPLATED, downstream, rootPath, false);
        downstream = configurationTemplateManager.getInstance(downstreamPath, ArchiveProject.class);

        ArchiveProject restoredDownstream = archiveAndRestore(downstream, upstream);
        ArchiveProject restoredUpstream = configurationTemplateManager.getInstance(upstreamPath, ArchiveProject.class);

        ArchiveBuildCompletedTrigger restoredTrigger = (ArchiveBuildCompletedTrigger) restoredDownstream.getTriggers().get(trigger.getName());
        assertSame(restoredUpstream, restoredTrigger.getProject());
        assertEquals(1, restoredDownstream.getDependencies().getProjects().size());
        assertSame(restoredUpstream, restoredDownstream.getDependencies().getProjects().iterator().next());
    }

    public void testReferencesToInsideArchiveViaInheritance()
    {
        ArchiveProject root = configurationTemplateManager.getInstance(rootPath, ArchiveProject.class);

        ArchiveProject project = new ArchiveProject("pro");
        ArchivePostProcessor processor = root.getPostProcessors().get("ant.pp");
        project.getProcessorRefs().add(processor);
        String projectPath = configurationTemplateManager.insertTemplatedInstance(SCOPE_TEMPLATED, project, rootPath, false);
        project = configurationTemplateManager.getInstance(projectPath, ArchiveProject.class);

        ArchiveProject restoredProject = archiveAndRestore(project);

        assertEquals(1, restoredProject.getProcessorRefs().size());
        assertEquals(processor.getName(), restoredProject.getProcessorRefs().get(0).getName());
    }

    public void testExplicitOrdersPreserved()
    {
        ArchiveProject project = new ArchiveProject("pro");
        project.addTrigger(new ArchiveScmTrigger("t1"));
        project.addTrigger(new ArchiveScmTrigger("t2"));
        project.addTrigger(new ArchiveScmTrigger("t3"));
        project.addTrigger(new ArchiveScmTrigger("t4"));

        String projectPath = configurationTemplateManager.insertTemplatedInstance(SCOPE_TEMPLATED, project, rootPath, false);

        project = configurationTemplateManager.getInstance(projectPath, ArchiveProject.class);

        String triggersPath = PathUtils.getPath(projectPath, "triggers");
        List<String> triggersOrder = shuffleOrder(triggersPath);

        ArchiveProject restoredProject = archiveAndRestore(project);

        assertEquals(triggersOrder, getOrder(PathUtils.getPath(restoredProject.getConfigurationPath(), "triggers")));
    }

    public void testInheritedOrdersPreserved()
    {
        ArchiveProject root = configurationProvider.deepClone(configurationTemplateManager.getInstance(rootPath, ArchiveProject.class));
        root.addTrigger(new ArchiveScmTrigger("t1"));
        root.addTrigger(new ArchiveScmTrigger("t2"));
        root.addTrigger(new ArchiveScmTrigger("t3"));
        root.addTrigger(new ArchiveScmTrigger("t4"));
        configurationTemplateManager.save(root);

        ArchiveProject project = new ArchiveProject("pro");
        String projectPath = configurationTemplateManager.insertTemplatedInstance(SCOPE_TEMPLATED, project, rootPath, false);

        project = configurationTemplateManager.getInstance(projectPath, ArchiveProject.class);

        String triggersPath = PathUtils.getPath(projectPath, "triggers");
        List<String> triggersOrder = shuffleOrder(triggersPath);

        ArchiveProject restoredProject = archiveAndRestore(project);

        assertEquals(triggersOrder, getOrder(PathUtils.getPath(restoredProject.getConfigurationPath(), "triggers")));
    }

    private List<String> getOrder(String path)
    {
        CollectionType type = configurationTemplateManager.getType(path, CollectionType.class);
        return type.getOrder(configurationTemplateManager.getRecord(path));
    }

    private List<String> shuffleOrder(String path)
    {
        List<String> order = getOrder(path);
        Collections.shuffle(order);
        configurationTemplateManager.setOrder(path, order);
        return order;
    }

    public void testImportWithIncompatibleTypes()
    {
        ArchiveProject project = new ArchiveProject("p");
        project.setScm(new ArchiveGitScm());
        project.addTrigger(new ArchiveScmTrigger("t"));

        String projectPath = configurationTemplateManager.insertTemplatedInstance(SCOPE_TEMPLATED, project, rootPath, false);
        project = configurationTemplateManager.getInstance(projectPath, ArchiveProject.class);

        configurationArchiver.archive(archiveFile, ConfigurationArchiver.ArchiveMode.MODE_APPEND, VERSION, project.getConfigurationPath());
        configurationTemplateManager.delete(projectPath);

        ArchiveProject root = configurationTemplateManager.deepClone(configurationTemplateManager.getInstance(rootPath, ArchiveProject.class));
        root.setScm(new ArchiveSubversionScm());
        root.addTrigger(new ArchiveBuildCompletedTrigger("t"));
        configurationTemplateManager.save(root);

        configurationArchiver.restore(archiveFile, new TestVersionChecker());
        ArchiveProject restoredProject = configurationTemplateManager.getInstance(projectPath, ArchiveProject.class);

        assertEquals(ArchiveSubversionScm.class, restoredProject.getScm().getClass());
        assertEquals(1, restoredProject.getTriggers().size());
        assertEquals(ArchiveBuildCompletedTrigger.class, restoredProject.getTriggers().get("t").getClass());
    }

    public void testImportExistingItemHasSameName()
    {
        ArchiveProject project = new ArchiveProject("p");
        project.setDescription("exported");

        String projectPath = configurationTemplateManager.insertTemplatedInstance(SCOPE_TEMPLATED, project, rootPath, false);

        configurationArchiver.archive(archiveFile, ConfigurationArchiver.ArchiveMode.MODE_APPEND, VERSION, projectPath);
        project = configurationTemplateManager.deepClone(configurationTemplateManager.getInstance(projectPath, ArchiveProject.class));
        project.setDescription("edited");
        configurationTemplateManager.save(project);

        configurationArchiver.restore(archiveFile, new TestVersionChecker());

        project = configurationTemplateManager.getInstance(projectPath, ArchiveProject.class);
        assertEquals("edited", project.getDescription());

        ArchiveProject restoredProject = configurationTemplateManager.getInstance(projectPath + " (restored)", ArchiveProject.class);
        assertNotNull(restoredProject);
        assertEquals("exported", restoredProject.getDescription());
    }

    private ArchiveProject archiveAndRestore(ArchiveProject... projects)
    {
        for (ArchiveProject project : projects)
        {
            configurationArchiver.archive(archiveFile, ConfigurationArchiver.ArchiveMode.MODE_APPEND, VERSION, project.getConfigurationPath());
        }

        // Deleting a template can delete children, hence we need to do this after all archiving.
        for (ArchiveProject project : projects)
        {
            String path = project.getConfigurationPath();
            if (configurationTemplateManager.pathExists(path))
            {
                configurationTemplateManager.delete(path);
                assertNull(configurationTemplateManager.getInstance(path, ArchiveProject.class));
            }
        }

        configurationArchiver.restore(archiveFile, new TestVersionChecker());

        return configurationTemplateManager.getInstance(projects[0].getConfigurationPath(), ArchiveProject.class);
    }

    @SymbolicName("aproject")
    public static class ArchiveProject extends AbstractNamedConfiguration
    {
        private String description;
        private ArchiveOptions options = new ArchiveOptions();
        private ArchiveScm scm;
        @Ordered
        private Map<String, ArchiveTrigger> triggers = new ConfigurationMap<ArchiveTrigger>();
        private Map<String, ArchivePostProcessor> postProcessors = new ConfigurationMap<ArchivePostProcessor>();
        // Naff but I don't want to replicate recipes just for an internal reference.
        @Reference
        private List<ArchivePostProcessor> processorRefs = new ConfigurationList<ArchivePostProcessor>();
        private ArchiveDependencies dependencies = new ArchiveDependencies();
        private List<ArchiveProjectAcl> permissions = new ConfigurationList<ArchiveProjectAcl>();

        public ArchiveProject()
        {
        }

        public ArchiveProject(String name)
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

        public ArchiveOptions getOptions()
        {
            return options;
        }

        public void setOptions(ArchiveOptions options)
        {
            this.options = options;
        }

        public ArchiveScm getScm()
        {
            return scm;
        }

        public void setScm(ArchiveScm scm)
        {
            this.scm = scm;
        }

        public Map<String, ArchiveTrigger> getTriggers()
        {
            return triggers;
        }

        public void setTriggers(Map<String, ArchiveTrigger> triggers)
        {
            this.triggers = triggers;
        }

        public void addTrigger(ArchiveTrigger trigger)
        {
            triggers.put(trigger.getName(), trigger);
        }

        public Map<String, ArchivePostProcessor> getPostProcessors()
        {
            return postProcessors;
        }

        public void setPostProcessors(Map<String, ArchivePostProcessor> postProcessors)
        {
            this.postProcessors = postProcessors;
        }

        public void addPostProcessor(ArchivePostProcessor archivePostProcessor)
        {
            postProcessors.put(archivePostProcessor.getName(), archivePostProcessor);
        }

        public List<ArchivePostProcessor> getProcessorRefs()
        {
            return processorRefs;
        }

        public void setProcessorRefs(List<ArchivePostProcessor> processorRefs)
        {
            this.processorRefs = processorRefs;
        }

        public ArchiveDependencies getDependencies()
        {
            return dependencies;
        }

        public void setDependencies(ArchiveDependencies dependencies)
        {
            this.dependencies = dependencies;
        }

        public List<ArchiveProjectAcl> getPermissions()
        {
            return permissions;
        }

        public void setPermissions(List<ArchiveProjectAcl> permissions)
        {
            this.permissions = permissions;
        }
    }

    @SymbolicName("aoptions")
    public static class ArchiveOptions extends AbstractConfiguration
    {
        private int timeout;
        private List<String> things;

        public int getTimeout()
        {
            return timeout;
        }

        public void setTimeout(int timeout)
        {
            this.timeout = timeout;
        }

        public List<String> getThings()
        {
            return things;
        }

        public void setThings(List<String> things)
        {
            this.things = things;
        }
    }

    @SymbolicName("ascm")
    public abstract static class ArchiveScm extends AbstractConfiguration
    {
    }

    @SymbolicName("asubversionscm")
    public static class ArchiveSubversionScm extends ArchiveScm
    {
    }

    @SymbolicName("agitscm")
    public static class ArchiveGitScm extends ArchiveScm
    {
    }

    @SymbolicName("atrigger")
    public abstract static class ArchiveTrigger extends AbstractNamedConfiguration
    {
        public ArchiveTrigger()
        {
        }

        public ArchiveTrigger(String name)
        {
            super(name);
        }
    }

    @SymbolicName("ascmtrigger")
    public static class ArchiveScmTrigger extends ArchiveTrigger
    {
        public ArchiveScmTrigger()
        {
        }

        public ArchiveScmTrigger(String name)
        {
            super(name);
        }
    }


    @SymbolicName("abuildcompletedtrigger")
    public static class ArchiveBuildCompletedTrigger extends ArchiveTrigger
    {
        @Reference
        private ArchiveProject project;
        private boolean cascadeRevision;

        public ArchiveBuildCompletedTrigger()
        {
        }

        public ArchiveBuildCompletedTrigger(String name)
        {
            super(name);
        }

        public ArchiveProject getProject()
        {
            return project;
        }

        public void setProject(ArchiveProject project)
        {
            this.project = project;
        }

        public boolean isCascadeRevision()
        {
            return cascadeRevision;
        }

        public void setCascadeRevision(boolean cascadeRevision)
        {
            this.cascadeRevision = cascadeRevision;
        }
    }

    @SymbolicName("apostprocessor")
    public static class ArchivePostProcessor extends AbstractNamedConfiguration
    {
        public ArchivePostProcessor()
        {
        }

        public ArchivePostProcessor(String name)
        {
            super(name);
        }
    }

    @SymbolicName("aacl")
    public static class ArchiveProjectAcl extends AbstractConfiguration
    {
        @Reference @Required
        private ArchiveGroup group;
        private List<String> allowedActions = new LinkedList<String>();

        public ArchiveProjectAcl()
        {
        }

        public ArchiveProjectAcl(ArchiveGroup group)
        {
            this.group = group;
        }

        public ArchiveGroup getGroup()
        {
            return group;
        }

        public void setGroup(ArchiveGroup group)
        {
            this.group = group;
        }

        public List<String> getAllowedActions()
        {
            return allowedActions;
        }

        public void setAllowedActions(List<String> allowedActions)
        {
            this.allowedActions = allowedActions;
        }
    }

    @SymbolicName("adependencies")
    public static class ArchiveDependencies extends AbstractConfiguration
    {
        @Reference
        private List<ArchiveProject> projects = new ArrayList<ArchiveProject>();

        public List<ArchiveProject> getProjects()
        {
            return projects;
        }

        public void setProjects(List<ArchiveProject> projects)
        {
            this.projects = projects;
        }

        public void addProject(ArchiveProject project)
        {
            projects.add(project);
        }
    }

    private static class TestVersionChecker implements ConfigurationArchiver.VersionChecker
    {
        public void checkVersion(String version) throws ToveRuntimeException
        {
            assertEquals(VERSION, version);
        }
    }

    @SymbolicName("agroup")
    public static class ArchiveGroup extends AbstractNamedConfiguration
    {
        public ArchiveGroup()
        {
        }

        public ArchiveGroup(String name)
        {
            super(name);
        }
    }
}
