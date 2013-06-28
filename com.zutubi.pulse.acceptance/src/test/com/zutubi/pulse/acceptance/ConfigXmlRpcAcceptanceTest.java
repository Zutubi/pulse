package com.zutubi.pulse.acceptance;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.zutubi.pulse.core.test.TestUtils;
import com.zutubi.pulse.master.agent.AgentStatus;
import com.zutubi.pulse.master.model.Project;
import com.zutubi.pulse.master.model.ProjectManager;
import com.zutubi.pulse.master.tove.config.MasterConfigurationRegistry;
import com.zutubi.pulse.master.tove.config.agent.AgentConfigurationActions;
import com.zutubi.pulse.master.tove.config.project.BootstrapConfiguration;
import com.zutubi.pulse.master.tove.config.project.ProjectAclConfiguration;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.pulse.master.tove.config.project.ResourceRequirementConfiguration;
import com.zutubi.pulse.master.tove.config.project.triggers.ScmBuildTriggerConfiguration;
import com.zutubi.pulse.master.tove.config.user.SetPasswordConfiguration;
import com.zutubi.pulse.master.tove.config.user.UserConfiguration;
import com.zutubi.pulse.master.tove.config.user.UserConfigurationActions;
import com.zutubi.pulse.master.tove.webwork.ToveUtils;
import com.zutubi.tove.security.AccessManager;
import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.util.Condition;
import com.zutubi.util.Sort;
import com.zutubi.util.io.FileSystemUtils;

import java.io.File;
import java.util.*;

import static com.google.common.collect.Iterables.transform;
import static com.zutubi.pulse.acceptance.rpc.RemoteApiClient.SYMBOLIC_NAME_KEY;
import static com.zutubi.pulse.master.tove.config.agent.AgentConfigurationActions.*;
import static com.zutubi.tove.type.record.PathUtils.WILDCARD_ANY_ELEMENT;
import static com.zutubi.tove.type.record.PathUtils.getPath;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;

/**
 * Tests for the remote API functions dealing with configuration.  Other
 * tests deal with specific configuration types, this test just concentrates
 * on the general capabilities and boundaries of the config functions.
 */
@SuppressWarnings({"unchecked"})
public class ConfigXmlRpcAcceptanceTest extends AcceptanceTestBase
{
    private static final String TEST_PROJECT_NAME = "config-xml-rpc";
    private static final String TEST_PROJECT_PATH = "projects/config-xml-rpc";
    
    private static final String PATH_EMAIL_SETTINGS = "settings/email";

    private static final long AGENT_STATUS_TIMEOUT = 60000;
    
    protected void setUp() throws Exception
    {
        super.setUp();
        rpcClient.loginAsAdmin();
    }

    protected void tearDown() throws Exception
    {
        rpcClient.logout();
        super.tearDown();
    }

    public void testConfigPathExistsEmptyPath() throws Exception
    {
        assertEquals(false, rpcClient.RemoteApi.configPathExists(""));
    }

    public void testConfigPathExistsNonExistant() throws Exception
    {
        assertEquals(false, rpcClient.RemoteApi.configPathExists("nonexistant"));
    }

    public void testConfigPathExistsExistant() throws Exception
    {
        assertEquals(true, rpcClient.RemoteApi.configPathExists("projects"));
    }

    public void testGetConfigListingEmptyPath() throws Exception
    {
        Vector<String> listing = rpcClient.RemoteApi.getConfigListing("");
        assertSortedEquals(listing, "agents", "groups", "projects", "settings", "users");
    }

    public void testGetConfigListingNonExistantPath() throws Exception
    {
        callAndExpectError("Invalid path 'nonexistant'", "getConfigListing", "nonexistant");
    }

    public void testGetConfigListingComposite() throws Exception
    {
        Vector<String> listing = rpcClient.RemoteApi.getConfigListing("settings");
        assertSortedEquals(listing, "agentPing", "backup", "email", "jabber", "ldap", "license", "logging", "repository", "resources");
    }

    public void testGetConfigListingCollection() throws Exception
    {
        Vector<String> listing = rpcClient.RemoteApi.getConfigListing("projects");
        int sizeBefore = listing.size();
        String project = randomName();
        rpcClient.RemoteApi.insertSimpleProject(project);
        listing = rpcClient.RemoteApi.getConfigListing("projects");
        assertEquals(sizeBefore + 1, listing.size());
        assertTrue(listing.contains(project));
    }

    public void testGetConfigEmptyPath() throws Exception
    {
        callAndExpectError("does not exist", "getConfig", "");
    }

    public void testGetConfigNonExistantPath() throws Exception
    {
        callAndExpectError("does not exist", "getConfig", "nonexistant");
    }

    public void testGetConfigPathForHandle() throws Exception
    {
        String path = "settings/email";
        String handle = rpcClient.RemoteApi.getConfigHandle(path);
        assertEquals(path, rpcClient.RemoteApi.getConfigPath(handle));
    }

    public void testGetConfig() throws Exception
    {
        Hashtable<String, Object> emailConfig = new Hashtable<String, Object>();
        emailConfig.put(SYMBOLIC_NAME_KEY, "zutubi.emailConfig");
        emailConfig.put("host", "localhost");
        emailConfig.put("from", "pulse@zutubi.com");
        emailConfig.put("ssl", true);
        rpcClient.RemoteApi.saveConfig("settings/email", emailConfig, false);

        emailConfig = rpcClient.RemoteApi.getConfig(PATH_EMAIL_SETTINGS);
        assertEquals("zutubi.emailConfig", emailConfig.get(SYMBOLIC_NAME_KEY));
        assertEquals("localhost", emailConfig.get("host"));
        assertEquals("pulse@zutubi.com", emailConfig.get("from"));
        assertEquals(true, emailConfig.get("ssl"));
    }

    public void testGetConfigTemplated() throws Exception
    {
        String agentName = randomName();
        String path = rpcClient.RemoteApi.insertSimpleAgent(agentName, "localhost");
        Hashtable<String, Object> agentConfig = rpcClient.RemoteApi.getConfig(path);
        try
        {
            assertEquals("zutubi.agentConfig", agentConfig.get(SYMBOLIC_NAME_KEY));
            assertEquals(agentName, agentConfig.get("name"));
            assertEquals("localhost", agentConfig.get("host"));
            assertEquals(8890, agentConfig.get("port"));
            assertEquals(true, agentConfig.get("remote"));
        }
        finally
        {
            rpcClient.RemoteApi.deleteConfig(path);
        }
    }

    public void testGetConfigWithPassword() throws Exception
    {
        Hashtable<String, Object> emailConfig = rpcClient.RemoteApi.getConfig(PATH_EMAIL_SETTINGS);
        assertEquals(ToveUtils.SUPPRESSED_PASSWORD, emailConfig.get("password"));
    }
        
    public void testGetRawConfigEmptyPath() throws Exception
    {
        callAndExpectError("does not exist", "getConfig", "");
    }

    public void testGetRawConfigNonExistantPath() throws Exception
    {
        callAndExpectError("does not exist", "getConfig", "nonexistant");
    }

    public void testGetRawConfig() throws Exception
    {
        Hashtable<String, Object> emailConfig = rpcClient.RemoteApi.getRawConfig(PATH_EMAIL_SETTINGS);
        assertEquals("zutubi.emailConfig", emailConfig.get(SYMBOLIC_NAME_KEY));
        assertTrue(emailConfig.containsKey("host"));
        assertTrue(emailConfig.containsKey("port"));
        assertTrue(emailConfig.containsKey("username"));
        assertTrue(emailConfig.containsKey("ssl"));
    }

    public void testGetRawConfigWithPassword() throws Exception
    {
        Hashtable<String, Object> emailConfig = rpcClient.RemoteApi.getRawConfig(PATH_EMAIL_SETTINGS);
        assertEquals(ToveUtils.SUPPRESSED_PASSWORD, emailConfig.get("password"));
    }
    
    public void testGetRawConfigTemplated() throws Exception
    {
        String agentName = randomName();
        String path = rpcClient.RemoteApi.insertSimpleAgent(agentName, "localhost");
        Hashtable<String, Object> agentConfig = rpcClient.RemoteApi.getRawConfig(path);
        try
        {
            assertEquals("zutubi.agentConfig", agentConfig.get(SYMBOLIC_NAME_KEY));
            assertEquals(agentName, agentConfig.get("name"));
            assertEquals("localhost", agentConfig.get("host"));
            assertEquals(8890, agentConfig.get("port"));
            assertFalse(agentConfig.containsKey("remote"));
        }
        finally
        {
            rpcClient.RemoteApi.deleteConfig(path);
        }
    }

    public void testInsertConfigEmptyPath() throws Exception
    {
        callAndExpectError("Invalid path", "insertConfig", "", new Hashtable());
    }

    public void testInsertConfigNonExistantPath() throws Exception
    {
        callAndExpectError("Invalid path", "insertConfig", "nonexistant", new Hashtable());
    }

    public void testInsertConfigTemplatedPath() throws Exception
    {
        callAndExpectError("use insertTemplatedConfig to insert into templated collections", "insertConfig", "projects", new Hashtable());
    }

    public void testInsertConfigExistingPath() throws Exception
    {
        Hashtable<String, Object> config = new Hashtable<String, Object>();
        config.put(SYMBOLIC_NAME_KEY, "zutubi.emailConfig");
        callAndExpectError("record already exists", "insertConfig", PATH_EMAIL_SETTINGS, config);
    }

    public void testInsertConfigNoSymbolicName() throws Exception
    {
        rpcClient.RemoteApi.ensureProject(TEST_PROJECT_NAME);
        Hashtable<String, Object> config = new Hashtable<String, Object>();
        callAndExpectError("No symbolic name found in XML-RPC struct", "insertConfig", TEST_PROJECT_PATH + "/stages", config);
    }

    public void testInsertConfigInvalidSymbolicName() throws Exception
    {
        rpcClient.RemoteApi.ensureProject(TEST_PROJECT_NAME);
        Hashtable<String, Object> config = new Hashtable<String, Object>();
        config.put(SYMBOLIC_NAME_KEY, "zutubi.nosuchConfig");
        callAndExpectError("Unrecognised symbolic name", "insertConfig", TEST_PROJECT_PATH + "/stages", config);
    }

    public void testInsertConfigTypeMismatch() throws Exception
    {
        rpcClient.RemoteApi.ensureProject(TEST_PROJECT_NAME);
        Hashtable<String, Object> config = new Hashtable<String, Object>();
        config.put(SYMBOLIC_NAME_KEY, "zutubi.emailConfig");
        callAndExpectError("Expected type: class com.zutubi.pulse.master.tove.config.project.BuildStageConfiguration", "insertConfig", TEST_PROJECT_PATH + "/stages", config);
    }

    public void testInsertConfigValidates() throws Exception
    {
        rpcClient.RemoteApi.ensureProject(TEST_PROJECT_NAME);
        Hashtable<String, Object> config = new Hashtable<String, Object>();
        config.put(SYMBOLIC_NAME_KEY, "zutubi.stageConfig");
        callAndExpectError("name requires a value", "insertConfig", TEST_PROJECT_PATH + "/stages", config);
    }

    public void testInsertConfigIntoCollection() throws Exception
    {
        String projectPath = rpcClient.RemoteApi.insertSimpleProject(randomName());
        Hashtable<String, Object> stage = new Hashtable<String, Object>();
        stage.put(SYMBOLIC_NAME_KEY, "zutubi.stageConfig");
        stage.put("name", "my stage");
        stage.put("recipe", "a recipe");
        String stagePath = rpcClient.RemoteApi.insertConfig(projectPath + "/stages", stage);
        
        Hashtable<String, Object> loadedStage = rpcClient.RemoteApi.getConfig(stagePath);
        assertEquals("zutubi.stageConfig", loadedStage.get(SYMBOLIC_NAME_KEY));
        assertEquals("my stage", loadedStage.get("name"));
        assertEquals("a recipe", loadedStage.get("recipe"));
    }

    public void testInsertConfigIntoSingleton() throws Exception
    {
        String projectPath = rpcClient.RemoteApi.insertSimpleProject(randomName());
        String scmPath = projectPath + "/scm";
        assertEquals(true, rpcClient.RemoteApi.deleteConfig(scmPath));

        Hashtable<String, Object> scm = new Hashtable<String, Object>();
        scm.put(SYMBOLIC_NAME_KEY, "zutubi.subversionConfig");
        scm.put("url", "svn://localhost/test/trunk");
        scm.put("monitor", false);
        rpcClient.RemoteApi.insertConfig(scmPath, scm);

        Hashtable<String, Object> loadedScm = rpcClient.RemoteApi.getConfig(scmPath);
        assertEquals("zutubi.subversionConfig", loadedScm.get(SYMBOLIC_NAME_KEY));
        assertEquals("svn://localhost/test/trunk", loadedScm.get("url"));
        assertEquals(false, loadedScm.get("monitor"));
    }

    public void testInsertTemplatedConfigEmptyParentPath() throws Exception
    {
        callAndExpectError("no parent path", "insertTemplatedConfig", "", new Hashtable(), false);
    }

    public void testInsertTemplatedConfigNonExistantParentPath() throws Exception
    {
        callAndExpectError("template parent does not exist", "insertTemplatedConfig", "projects/no such template", new Hashtable(), false);
    }

    public void testInsertTemplatedConfigNonTemplatedParentPath() throws Exception
    {
        callAndExpectError("parent path 'settings' is not a templated collection", "insertTemplatedConfig", PATH_EMAIL_SETTINGS, new Hashtable(), false);
    }

    public void testInsertTemplatedConfigConcreteParentPath() throws Exception
    {
        rpcClient.RemoteApi.ensureProject(TEST_PROJECT_NAME);
        callAndExpectError("template parent is concrete and thus cannot be inherited from", "insertTemplatedConfig", TEST_PROJECT_PATH, new Hashtable(), false);
    }

    public void testInsertTemplatedConfig() throws Exception
    {
        String projectName = randomName();
        String projectPath = rpcClient.RemoteApi.insertSimpleProject(projectName);
        
        Hashtable<String, Object> project = rpcClient.RemoteApi.getConfig(projectPath);
        assertEquals("zutubi.projectConfig", project.get(SYMBOLIC_NAME_KEY));
        assertEquals(projectName, project.get("name"));
        Hashtable<String, Object> scm = (Hashtable<String, Object>) project.get("scm");
        assertNotNull(scm);
        assertEquals("zutubi.subversionConfig", scm.get(SYMBOLIC_NAME_KEY));
    }

    public void testInsertTemplatedConfigValidates() throws Exception
    {
        Hashtable<String, Object> scm = new Hashtable<String, Object>();
        scm.put(SYMBOLIC_NAME_KEY, "zutubi.subversionConfig");

        Hashtable<String, Object> project = new Hashtable<String, Object>();
        project.put(SYMBOLIC_NAME_KEY, "zutubi.projectConfig");
        String projectName = randomName();
        project.put("name", projectName);
        project.put("scm", scm);

        callAndExpectError("url requires a value", "insertTemplatedConfig", "projects/global project template", project, false);
    }

    public void testInsertTemplatedConfigValidatesAsTemplate() throws Exception
    {
        Hashtable<String, Object> scm = new Hashtable<String, Object>();
        scm.put(SYMBOLIC_NAME_KEY, "zutubi.subversionConfig");

        Hashtable<String, Object> project = new Hashtable<String, Object>();
        project.put(SYMBOLIC_NAME_KEY, "zutubi.projectConfig");
        String projectName = randomName();
        project.put("name", projectName);
        project.put("scm", scm);

        String projectPath = rpcClient.RemoteApi.insertTemplatedConfig("projects/global project template", project, true);
        Hashtable<String, Object> loadedProject = rpcClient.RemoteApi.getConfig(projectPath);
        Hashtable<String, Object> loadedScm = (Hashtable<String, Object>) loadedProject.get("scm");
        assertFalse(loadedScm.containsKey("url"));
    }

    public void testInsertTemplatedConfigTemplate() throws Exception
    {
        Hashtable<String, Object> project = new Hashtable<String, Object>();
        project.put(SYMBOLIC_NAME_KEY, "zutubi.projectConfig");
        String projectName = randomName();
        project.put("name", projectName);

        String projectPath = rpcClient.RemoteApi.insertTemplatedConfig("projects/global project template", project, true);
        Hashtable<String, Object> loadedProject = rpcClient.RemoteApi.getConfig(projectPath);
        assertEquals(projectName, loadedProject.get("name"));
    }

    public void testInsertTemplatedConfigDefaults() throws Exception
    {
        final String OVERRIDE = "override pattern";

        String parentProject = random + "-parent";
        String childProject = random + "-child";
        String parentPath = rpcClient.RemoteApi.insertProject(parentProject, ProjectManager.GLOBAL_PROJECT_NAME, true, null, null);

        // Defaults should be added to this project.
        BootstrapConfiguration defaults = new BootstrapConfiguration();
        String parentBootstrapPath = getPath(parentPath, Constants.Project.BOOTSTRAP);
        Hashtable<String, Object> parentBootstrap = rpcClient.RemoteApi.getConfig(parentBootstrapPath);
        assertEquals(defaults.getPersistentDirPattern(), parentBootstrap.get(Constants.Project.Bootstrap.PERSISTENT_DIR_PATTERN));
        assertEquals(defaults.getTempDirPattern(), parentBootstrap.get(Constants.Project.Bootstrap.TEMP_DIR_PATTERN));
        parentBootstrap.put(Constants.Project.Bootstrap.TEMP_DIR_PATTERN, OVERRIDE);
        rpcClient.RemoteApi.saveConfig(parentBootstrapPath, parentBootstrap, false);

        // The child should see the parent's override as-is.
        Hashtable<String, Object> child = rpcClient.RemoteApi.createEmptyConfig(ProjectConfiguration.class);
        child.put(Constants.Project.NAME, childProject);
        String childPath = rpcClient.RemoteApi.insertTemplatedConfig(parentPath, child, true);
        String childBootstrapPath = getPath(childPath, Constants.Project.BOOTSTRAP);
        Hashtable<String, Object> childBootstrap = rpcClient.RemoteApi.getConfig(childBootstrapPath);
        assertEquals(defaults.getPersistentDirPattern(), childBootstrap.get(Constants.Project.Bootstrap.PERSISTENT_DIR_PATTERN));
        assertEquals(OVERRIDE, childBootstrap.get(Constants.Project.Bootstrap.TEMP_DIR_PATTERN));
    }

    public void testSaveConfigEmptyPath() throws Exception
    {
        callAndExpectError("Invalid path: path is empty", "saveConfig", "", new Hashtable(), false);
    }

    public void testSaveConfigNonExistantPath() throws Exception
    {
        callAndExpectError("Invalid path", "saveConfig", "nonexistant", new Hashtable(), false);
    }

    public void testSaveConfigNoSymbolicName() throws Exception
    {
        String projectPath = rpcClient.RemoteApi.insertSimpleProject(randomName());
        Hashtable<String, Object> options = new Hashtable<String, Object>();
        callAndExpectError("No symbolic name found in XML-RPC struct", "saveConfig", projectPath + "/options", options, false);
    }

    public void testSaveConfigInvalidType() throws Exception
    {
        String projectPath = rpcClient.RemoteApi.insertSimpleProject(randomName());
        Hashtable<String, Object> options = new Hashtable<String, Object>();
        options.put(SYMBOLIC_NAME_KEY, "invalid");
        callAndExpectError("Expecting type 'zutubi.buildOptionsConfig', found 'invalid'", "saveConfig", projectPath + "/options", options, false);
    }

    public void testSaveConfigValidates() throws Exception
    {
        String projectPath = rpcClient.RemoteApi.insertSimpleProject(randomName());
        String optionsPath = projectPath + "/options";
        Hashtable<String, Object> options = rpcClient.RemoteApi.getConfig(optionsPath);
        options.put("timeout", -10);
        callAndExpectError("timeout must not be negative", "saveConfig", projectPath + "/options", options, false);
    }

    public void testSaveConfigValidatesShallow() throws Exception
    {
        String projectPath = rpcClient.RemoteApi.insertSimpleProject(randomName());
        Hashtable<String, Object> project = rpcClient.RemoteApi.getConfig(projectPath);
        Hashtable<String, Object> options = (Hashtable<String, Object>) project.get("options");
        assertEquals(0, options.get("timeout"));
        options.put("timeout", -1);
        rpcClient.RemoteApi.saveConfig(projectPath, project, false);
    }
    
    public void testSaveConfigValidatesDeep() throws Exception
    {
        String projectPath = rpcClient.RemoteApi.insertSimpleProject(randomName());
        Hashtable<String, Object> project = rpcClient.RemoteApi.getConfig(projectPath);
        Hashtable<String, Object> options = (Hashtable<String, Object>) project.get("options");
        assertEquals(0, options.get("timeout"));
        options.put("timeout", -1);
        callAndExpectError("timeout must not be negative", "saveConfig", projectPath, project, true);
    }

    public void testSaveConfig() throws Exception
    {
        String projectPath = rpcClient.RemoteApi.insertSimpleProject(randomName());
        String optionsPath = projectPath + "/options";
        Hashtable<String, Object> options = rpcClient.RemoteApi.getConfig(optionsPath);
        assertEquals(0, options.get("timeout"));

        options.put("timeout", 10);
        rpcClient.RemoteApi.saveConfig(projectPath + "/options", options, false);

        Hashtable<String, Object> loadedOptions = rpcClient.RemoteApi.getConfig(optionsPath);
        assertEquals(10, loadedOptions.get("timeout"));
    }

    public void testSaveConfigShallow() throws Exception
    {
        String projectPath = rpcClient.RemoteApi.insertSimpleProject(randomName());
        Hashtable<String, Object> project = rpcClient.RemoteApi.getConfig(projectPath);
        Hashtable<String, Object> options = (Hashtable<String, Object>) project.get("options");
        assertEquals(0, options.get("timeout"));
        options.put("timeout", 10);
        rpcClient.RemoteApi.saveConfig(projectPath, project, false);

        Hashtable<String, Object> loadedOptions = rpcClient.RemoteApi.getConfig(projectPath + "/options");
        assertEquals(0, loadedOptions.get("timeout"));
    }

    public void testSaveConfigDeep() throws Exception
    {
        String projectPath = rpcClient.RemoteApi.insertSimpleProject(randomName());
        Hashtable<String, Object> project = rpcClient.RemoteApi.getConfig(projectPath);
        Hashtable<String, Object> options = (Hashtable<String, Object>) project.get("options");
        assertEquals(0, options.get("timeout"));
        options.put("timeout", 10);
        rpcClient.RemoteApi.saveConfig(projectPath, project, true);

        Hashtable<String, Object> loadedOptions = rpcClient.RemoteApi.getConfig(projectPath + "/options");
        assertEquals(10, loadedOptions.get("timeout"));
    }

    public void testSaveConfigDeepListItems() throws Exception
    {
        // New project, no requirements.
        String projectPath = rpcClient.RemoteApi.insertSimpleProject(random, true);
        Hashtable<String, Object> project = rpcClient.RemoteApi.getConfig(projectPath);
        Vector<Hashtable<String, Object>> requirements = (Vector<Hashtable<String, Object>>) project.get("requirements");
        verifyRequirements(requirements);

        // Add requirement, save project deeply and verify requirement
        requirements.add(createRequirement("r1"));
        rpcClient.RemoteApi.saveConfig(projectPath, project, true);

        project = rpcClient.RemoteApi.getConfig(projectPath);
        requirements = (Vector<Hashtable<String, Object>>) project.get("requirements");
        verifyRequirements(requirements, "r1");

        // Add another requirement, save project deeply and verify both requirements
        requirements.add(createRequirement("r2"));
        rpcClient.RemoteApi.saveConfig(projectPath, project, true);

        project = rpcClient.RemoteApi.getConfig(projectPath);
        requirements = (Vector<Hashtable<String, Object>>) project.get("requirements");
        verifyRequirements(requirements, "r1", "r2");
    }

    public void testSaveConfigDeepInheritedListItems() throws Exception
    {
        String random = randomName();
        String parentName = random + "-parent";
        String childName = random + "-child";

        // Template project with one requirement
        String parentPath = rpcClient.RemoteApi.insertSimpleProject(parentName, true);
        rpcClient.RemoteApi.insertConfig(getPath(parentPath, "requirements"), createRequirement("p1"));

        // Child project, add requirement and deeply save, then verify requirements
        String childPath = rpcClient.RemoteApi.insertSimpleProject(childName, parentName, false);
        Hashtable<String, Object> child = rpcClient.RemoteApi.getConfig(childPath);
        Vector<Hashtable<String, Object>> requirements = (Vector<Hashtable<String, Object>>) child.get("requirements");
        verifyRequirements(requirements, "p1");
        requirements.add(createRequirement("c1"));
        rpcClient.RemoteApi.saveConfig(childPath, child, true);

        child = rpcClient.RemoteApi.getConfig(childPath);
        requirements = (Vector<Hashtable<String, Object>>) child.get("requirements");
        verifyRequirements(requirements, "p1", "c1");

        // Now verify that we are actually seeing the parent's requirement in
        // the child, not just a local requirement with the same name.
        List<String> parentHandles = rpcClient.RemoteApi.getConfigListing(getPath(parentPath, "requirements"));
        List<String> childHandles = rpcClient.RemoteApi.getConfigListing(getPath(childPath, "requirements"));
        assertThat(childHandles, hasItem(parentHandles.get(0)));
    }

    private Hashtable<String, Object> createRequirement(String resource)
    {
        Hashtable<String, Object> requirement = rpcClient.RemoteApi.createEmptyConfig(ResourceRequirementConfiguration.class);
        requirement.put("resource", resource);
        return requirement;
    }

    private void verifyRequirements(Vector<Hashtable<String, Object>> requirements, String... expectedNames)
    {
        assertEquals(sorted(asList(expectedNames)), sorted(transform(requirements, new Function<Hashtable<String, Object>, String>()
        {
            public String apply(Hashtable<String, Object> requirement)
            {
                return (String) requirement.get("resource");
            }
        })));
    }

    private List<String> sorted(Iterable<String> l)
    {
        List<String> copy = Lists.newArrayList(l);
        Collections.sort(copy);
        return copy;
    }

    public void testDeleteConfigEmptyPath() throws Exception
    {
        callAndExpectError("path is empty", "deleteConfig", "");
    }

    public void testCanCloneNonexistantPath() throws Exception
    {
        assertEquals(false, rpcClient.RemoteApi.canCloneConfig("foo"));
    }

    public void testCanCloneUncloneablePath() throws Exception
    {
        assertEquals(false, rpcClient.RemoteApi.canCloneConfig(getPath(MasterConfigurationRegistry.PROJECTS_SCOPE, ProjectManager.GLOBAL_PROJECT_NAME)));
    }

    public void testCanCloneCloneablePath() throws Exception
    {
        String path = rpcClient.RemoteApi.insertTrivialProject(randomName(), false);
        assertEquals(true, rpcClient.RemoteApi.canCloneConfig(path));
    }

    public void testCloneInvalidPath() throws Exception
    {
        Hashtable<String, String> keyMap = new Hashtable<String, String>(1);
        keyMap.put("thisprojectdoesnotexist", "soicantcloneit");
        callAndExpectError("Invalid path 'projects/thisprojectdoesnotexist': path does not exist", "cloneConfig", MasterConfigurationRegistry.PROJECTS_SCOPE, keyMap);
    }

    public void testClone() throws Exception
    {
        String name = randomName();
        String cloneName = name + " clone";
        rpcClient.RemoteApi.insertTrivialProject(name, false);
        Hashtable<String, String> keyMap = new Hashtable<String, String>(1);
        keyMap.put(name, cloneName);
        assertEquals(true, rpcClient.RemoteApi.cloneConfig(MasterConfigurationRegistry.PROJECTS_SCOPE, keyMap));
        assertTrue(rpcClient.RemoteApi.configPathExists(getPath(MasterConfigurationRegistry.PROJECTS_SCOPE, cloneName)));
    }

    public void testDeleteConfigNonExistantPath() throws Exception
    {
        callAndExpectError("Invalid path", "deleteConfig", "nonexistant");
    }

    public void testDelete() throws Exception
    {
        String path = rpcClient.RemoteApi.insertSimpleProject(randomName());
        assertEquals(true, rpcClient.RemoteApi.configPathExists(path));
        assertEquals(true, rpcClient.RemoteApi.deleteConfig(path));
        assertEquals(false, rpcClient.RemoteApi.configPathExists(path));
    }

    public void testDeleteSingleton() throws Exception
    {
        String path = rpcClient.RemoteApi.insertSimpleProject(randomName());
        String scmPath = path + "/scm";
        assertEquals(true, rpcClient.RemoteApi.configPathExists(scmPath));
        assertEquals(true, rpcClient.RemoteApi.deleteConfig(scmPath));
        assertEquals(false, rpcClient.RemoteApi.configPathExists(scmPath));
    }

    public void testDeleteInheritedFrom() throws Exception
    {
        Hashtable<String, Object> project = new Hashtable<String, Object>();
        project.put(SYMBOLIC_NAME_KEY, "zutubi.projectConfig");
        project.put("name", randomName());
        String parentPath = rpcClient.RemoteApi.insertTemplatedConfig("projects/global project template", project, true);

        project.put("name", randomName());
        String childPath = rpcClient.RemoteApi.insertTemplatedConfig(parentPath, project, true);
        assertEquals(true, rpcClient.RemoteApi.configPathExists(parentPath));
        assertEquals(true, rpcClient.RemoteApi.configPathExists(childPath));

        assertEquals(true, rpcClient.RemoteApi.deleteConfig(parentPath));
        assertEquals(false, rpcClient.RemoteApi.configPathExists(parentPath));
        assertEquals(false, rpcClient.RemoteApi.configPathExists(childPath));
    }

    public void testDeleteAll() throws Exception
    {
        String path = rpcClient.RemoteApi.insertSimpleProject(randomName());
        Hashtable<String, Object> project = rpcClient.RemoteApi.getConfig(path);
        Hashtable<String, Object> properties = (Hashtable<String, Object>) project.get("properties");
        properties.put("p1", rpcClient.RemoteApi.createProperty("p1", "v1"));
        properties.put("p2", rpcClient.RemoteApi.createProperty("p2", "v2"));
        rpcClient.RemoteApi.saveConfig(path, project, true);

        Hashtable<String, Object> loadedProperties = rpcClient.RemoteApi.getConfig(path + "/properties");
        assertEquals(2, loadedProperties.size());

        assertEquals(2, rpcClient.RemoteApi.deleteAllConfigs(path + "/properties/*"));
        loadedProperties = rpcClient.RemoteApi.getConfig(path + "/properties");
        assertEquals(0, loadedProperties.size());
    }

    public void testDeleteAllHidesInherited() throws Exception
    {
        String random = randomName();
        String parent = random + "-parent";
        String child = random + "-child";

        String parentPath = rpcClient.RemoteApi.insertSimpleProject(parent, true);
        String childPath = rpcClient.RemoteApi.insertSimpleProject(child, parent, false);

        Hashtable<String, Object> project = rpcClient.RemoteApi.getConfig(parentPath);
        Hashtable<String, Object> properties = (Hashtable<String, Object>) project.get("properties");
        properties.put("p1", rpcClient.RemoteApi.createProperty("p1", "v1"));
        rpcClient.RemoteApi.saveConfig(parentPath, project, true);

        assertEquals(1, rpcClient.RemoteApi.deleteAllConfigs(childPath+ "/properties/*"));
        Hashtable<String, Object> loadedProperties = rpcClient.RemoteApi.getConfig(childPath + "/properties");
        assertEquals(0, loadedProperties.size());

        rpcClient.RemoteApi.restoreConfig(childPath + "/properties/p1");
        loadedProperties = rpcClient.RemoteApi.getConfig(childPath + "/properties");
        assertEquals(1, loadedProperties.size());
    }

    public void testRestore() throws Exception
    {
        String random = randomName();
        String parentName = random + "-parent";
        String childName = random + "-child";
        rpcClient.RemoteApi.insertSimpleProject(parentName, true);
        String childPath = rpcClient.RemoteApi.insertSimpleProject(childName, parentName, false);

        String stagesPath = getPath(childPath, "stages");
        String hidePath = getPath(stagesPath, "default");
        rpcClient.RemoteApi.deleteConfig(hidePath);
        assertEquals(0, rpcClient.RemoteApi.getConfigListing(stagesPath).size());
        rpcClient.RemoteApi.restoreConfig(hidePath);
        Vector<String> listing = rpcClient.RemoteApi.getConfigListing(stagesPath);
        assertEquals(1, listing.size());
        assertEquals("default", listing.get(0));
    }

    public void testSetOrder() throws Exception
    {
        String random = randomName();
        String path = rpcClient.RemoteApi.insertSimpleProject(random, true);
        String propertiesPath = getPath(path, "properties");
        rpcClient.RemoteApi.insertProjectProperty(random, "p1", "v1");
        rpcClient.RemoteApi.insertProjectProperty(random, "p2", "v2");

        assertEquals(asList("p1", "p2"), new LinkedList<String>(rpcClient.RemoteApi.getConfigListing(propertiesPath)));
        rpcClient.RemoteApi.setConfigOrder(propertiesPath, "p2", "p1");
        assertEquals(asList("p2", "p1"), new LinkedList<String>(rpcClient.RemoteApi.getConfigListing(propertiesPath)));
    }

    public void testGetConfigActions() throws Exception
    {
        String agentName = randomName();
        String path = rpcClient.RemoteApi.insertSimpleAgent(agentName);
        try
        {
            rpcClient.RemoteApi.waitForAgentStatus(agentName, AgentStatus.IDLE, AGENT_STATUS_TIMEOUT);
            Vector<String> actions = rpcClient.RemoteApi.getConfigActions(path);
            assertEquals(asList(ACTION_DISABLE, ACTION_PING, ACTION_GC), new LinkedList<String>(actions));
        }
        finally
        {
            rpcClient.RemoteApi.deleteConfig(path);
        }
    }

    public void testDoConfigAction() throws Exception
    {
        String agentName = randomName();
        final String path = rpcClient.RemoteApi.insertSimpleAgent(agentName);
        try
        {
            rpcClient.RemoteApi.doConfigAction(path, ACTION_DISABLE);
            final List<String> expectedActions = asList(AgentConfigurationActions.ACTION_ENABLE);
            TestUtils.waitForCondition(new Condition()
            {
                public boolean satisfied()
                {
                    try
                    {
                        return rpcClient.RemoteApi.getConfigActions(path).equals(expectedActions);
                    }
                    catch (Exception e)
                    {
                        fail(e.getMessage());
                        return false;
                    }
                }
            }, AGENT_STATUS_TIMEOUT, "agent to be disabled");
        }
        finally
        {
            rpcClient.RemoteApi.deleteConfig(path);
        }
    }

    public void testDoConfigActionWithArgument() throws Exception
    {
        String userName = randomName();
        String path = rpcClient.RemoteApi.insertTrivialUser(userName);
        try
        {
            Hashtable<String, Object> password = rpcClient.RemoteApi.createDefaultConfig(SetPasswordConfiguration.class);
            password.put("password", "foo");
            password.put("confirmPassword", "foo");
            rpcClient.RemoteApi.doConfigActionWithArgument(path, UserConfigurationActions.ACTION_SET_PASSWORD, password);
            rpcClient.logout();
            rpcClient.login(userName, "foo");
        }
        finally
        {
            rpcClient.RemoteApi.deleteConfig(path);
        }
    }

    public void testDoConfigActionWithInvalidArgument() throws Exception
    {
        String userName = randomName();
        String path = rpcClient.RemoteApi.insertTrivialUser(userName);
        try
        {
            Hashtable<String, Object> password = rpcClient.RemoteApi.createDefaultConfig(SetPasswordConfiguration.class);
            password.put("password", "foo");
            password.put("confirmPassword", "bar");
            rpcClient.RemoteApi.doConfigActionWithArgument(path, UserConfigurationActions.ACTION_SET_PASSWORD, password);
            fail();
        }
        catch(Exception e)
        {
            assertEquals("java.lang.Exception: com.zutubi.pulse.master.api.ValidationException: password: passwords do not match", e.getMessage());
        }
        finally
        {
            rpcClient.RemoteApi.deleteConfig(path);
        }
    }

    public void testDoConfigActionWithIncorrectArgument() throws Exception
    {
        String userName = randomName();
        String path = rpcClient.RemoteApi.insertTrivialUser(userName);
        try
        {
            // Deliberately pass wrong type as argument
            Hashtable<String, Object> password = rpcClient.RemoteApi.createDefaultConfig(UserConfiguration.class);
            password.put("login", randomName());
            password.put("name", randomName());
            rpcClient.RemoteApi.doConfigActionWithArgument(path, UserConfigurationActions.ACTION_SET_PASSWORD, password);
            fail();
        }
        catch(Exception e)
        {
            assertEquals("java.lang.Exception: java.lang.RuntimeException: java.lang.IllegalArgumentException: Invoking action 'setPassword' of type 'com.zutubi.pulse.master.tove.config.user.UserConfiguration': argument instance is of wrong type: expecting 'com.zutubi.pulse.master.tove.config.user.SetPasswordConfiguration', got 'com.zutubi.pulse.master.tove.config.user.UserConfiguration'",
                         e.getMessage());
        }
        finally
        {
            rpcClient.RemoteApi.deleteConfig(path);
        }
    }

    public void testDefaultConstructorAppliedToUser() throws Exception
    {
        String random = randomName();

        // Use empty config so we don't get bits from default config.
        Hashtable<String, Object> user = rpcClient.RemoteApi.createEmptyConfig(UserConfiguration.class);
        user.put("login", random);
        user.put("name", random);
        String path = rpcClient.RemoteApi.insertConfig(MasterConfigurationRegistry.USERS_SCOPE, user);
        assertTrue(rpcClient.RemoteApi.isConfigPermanent(getPath(path, "preferences")));
    }

    public void testGetConfigDoesntReturnExternalState() throws Exception
    {
        String projectPath = rpcClient.RemoteApi.insertSimpleProject(randomName());
        Hashtable<String, Object> project = rpcClient.RemoteApi.getConfig(projectPath);
        assertNull(project.get("projectId"));
    }

    public void testSaveConfigPreservesState() throws Exception
    {
        // CIB-1542: check that a normal save does not touch the projectId
        // external state field.
        String name = randomName();
        String projectPath = rpcClient.RemoteApi.insertSimpleProject(name);
        Hashtable<String, Object> project = rpcClient.RemoteApi.getConfig(projectPath);
        project.put("url", "http://i.feel.like.a.change.com");
        rpcClient.RemoteApi.saveConfig(projectPath, project, false);

        assertEquals(Project.State.IDLE, rpcClient.RemoteApi.getProjectState(name));
    }

    public void testSaveConfigConfigChangeExternalState() throws Exception
    {
        String name = randomName();
        String projectPath = rpcClient.RemoteApi.insertSimpleProject(name);
        Hashtable<String, Object> project = rpcClient.RemoteApi.getConfig(projectPath);
        project.put("projectId", "1");
        callAndExpectError("Unrecognised property 'projectId'", "saveConfig", projectPath, project, false);
    }

    public void testInheritanceOfInternalReference() throws Exception
    {
        String random = randomName();
        String parentProject = random + "-parent";
        String childProject = random + "-child";

        rpcClient.RemoteApi.insertSimpleProject(parentProject, true);
        String hookPath = rpcClient.RemoteApi.insertPostStageHook(parentProject, "hokey", "default");
        String childPath = rpcClient.RemoteApi.insertProject(childProject, parentProject, false, null, null);
        
        String childHookPath = hookPath.replace(parentProject, childProject);
        Hashtable<String, Object> childHook = rpcClient.RemoteApi.getConfig(childHookPath);
        Vector<String> stages = (Vector<String>) childHook.get("stages");
        assertEquals(getPath(childPath, Constants.Project.STAGES, "default"), stages.get(0));
    }

    public void testCanPullUp() throws Exception
    {
        String random = randomName();
        String parentProject = random + "-parent";
        String childProject = random + "-child";

        rpcClient.RemoteApi.insertSimpleProject(parentProject, true);
        rpcClient.RemoteApi.insertProject(childProject, parentProject, false, null, null);

        String parentPropertyPath = rpcClient.RemoteApi.insertProjectProperty(parentProject, "pp", "foo");
        String childPropertyPath = rpcClient.RemoteApi.insertProjectProperty(childProject, "cp", "foo");

        assertTrue(rpcClient.RemoteApi.canPullUpConfig(childPropertyPath, parentProject));
        assertFalse(rpcClient.RemoteApi.canPullUpConfig(parentPropertyPath.replace(parentProject, childProject), parentProject));
    }

    public void testPullUp() throws Exception
    {
        String random = randomName();
        String parentProject = random + "-parent";
        String childProject = random + "-child";

        rpcClient.RemoteApi.insertSimpleProject(parentProject, true);
        rpcClient.RemoteApi.insertProject(childProject, parentProject, false, null, null);

        String propertyPath = rpcClient.RemoteApi.insertProjectProperty(childProject, "cp", "foo");
        String pulledUpPath = propertyPath.replace(childProject, parentProject);

        assertFalse(rpcClient.RemoteApi.configPathExists(pulledUpPath));
        assertEquals(pulledUpPath, rpcClient.RemoteApi.pullUpConfig(propertyPath, parentProject));
        assertTrue(rpcClient.RemoteApi.configPathExists(pulledUpPath));
    }

    public void testCanPushDown() throws Exception
    {
        String random = randomName();
        String parentProject = random + "-parent";
        String childProject = random + "-child";

        rpcClient.RemoteApi.insertSimpleProject(parentProject, true);
        rpcClient.RemoteApi.insertProject(childProject, parentProject, false, null, null);

        String parentPropertyPath = rpcClient.RemoteApi.insertProjectProperty(parentProject, "pp", "foo");
        String childPropertyPath = rpcClient.RemoteApi.insertProjectProperty(childProject, "cp", "foo");

        assertTrue(rpcClient.RemoteApi.canPushDownConfig(parentPropertyPath, childProject));
        assertFalse(rpcClient.RemoteApi.canPushDownConfig(childPropertyPath, childProject));
    }

    public void testPushDown() throws Exception
    {
        String random = randomName();
        String parentProject = random + "-parent";
        String childProject = random + "-child";

        rpcClient.RemoteApi.insertSimpleProject(parentProject, true);
        rpcClient.RemoteApi.insertProject(childProject, parentProject, false, null, null);

        String propertyPath = rpcClient.RemoteApi.insertProjectProperty(parentProject, "pp", "foo");
        String pushedDownToPath = propertyPath.replace(parentProject, childProject);

        assertTrue(rpcClient.RemoteApi.configPathExists(propertyPath));
        assertEquals(new Vector<String>(asList(pushedDownToPath)), rpcClient.RemoteApi.pushDownConfig(propertyPath, new Vector<String>(asList(childProject))));
        assertFalse(rpcClient.RemoteApi.configPathExists(propertyPath));
        assertTrue(rpcClient.RemoteApi.configPathExists(pushedDownToPath));
    }

    public void testIntroduceParentTemplateConfig() throws Exception
    {
        String random = randomName();
        String originalProject = random + "-original";
        String newTemplateParentProject = random + "-new-parent";

        String childProjectPath = rpcClient.RemoteApi.insertProject(originalProject, ProjectManager.GLOBAL_PROJECT_NAME, false, rpcClient.RemoteApi.getGitConfig(Constants.getGitUrl()), rpcClient.RemoteApi.createVersionedConfig("path"));

        String result = rpcClient.RemoteApi.introduceParentTemplateConfig(childProjectPath, newTemplateParentProject, true);
        
        assertEquals(PathUtils.getPath(MasterConfigurationRegistry.PROJECTS_SCOPE, newTemplateParentProject), result);
        assertEquals(newTemplateParentProject, rpcClient.RemoteApi.getTemplateParent(childProjectPath));
    }
    
    public void testPreviewMoveConfig() throws Exception
    {
        String random = randomName();
        String childProject = random + "-child";
        String newTemplateParentProject = random + "-new-parent";

        String childProjectPath = rpcClient.RemoteApi.insertProject(childProject, ProjectManager.GLOBAL_PROJECT_NAME, false, rpcClient.RemoteApi.getGitConfig(Constants.getGitUrl()), rpcClient.RemoteApi.createVersionedConfig("path"));
        rpcClient.RemoteApi.insertSimpleProject(newTemplateParentProject, true);

        Hashtable<String, Object> result = rpcClient.RemoteApi.previewMoveConfig(childProjectPath, newTemplateParentProject);
        
        String scmPath = getPath(childProjectPath, "scm");
        String typePath = getPath(childProjectPath, "type");
        checkExpectedDeletedPaths(result, scmPath, typePath);
        
        // Make sure no changes were made.
        assertEquals(ProjectManager.GLOBAL_PROJECT_NAME, rpcClient.RemoteApi.getTemplateParent(childProjectPath));
        assertTrue(rpcClient.RemoteApi.configPathExists(scmPath));
        assertTrue(rpcClient.RemoteApi.configPathExists(typePath));
    }

    public void testMoveConfig() throws Exception
    {
        String random = randomName();
        String childProject = random + "-child";
        String newTemplateParentProject = random + "-new-parent";

        String childProjectPath = rpcClient.RemoteApi.insertProject(childProject, ProjectManager.GLOBAL_PROJECT_NAME, false, rpcClient.RemoteApi.getGitConfig(Constants.getGitUrl()), rpcClient.RemoteApi.createVersionedConfig("path"));
        rpcClient.RemoteApi.insertSimpleProject(newTemplateParentProject, true);
        rpcClient.RemoteApi.insertProjectProperty(newTemplateParentProject, "prop", "val");

        Hashtable<String, Object> result = rpcClient.RemoteApi.moveConfig(childProjectPath, newTemplateParentProject);
        
        String scmPath = getPath(childProjectPath, "scm");
        String typePath = getPath(childProjectPath, "type");
        checkExpectedDeletedPaths(result, scmPath, typePath);
        
        assertEquals(newTemplateParentProject, rpcClient.RemoteApi.getTemplateParent(childProjectPath));
        // These paths were incompatible, so should have changed to the new
        // parent's type.
        checkPathType(scmPath, "zutubi.subversionConfig");
        checkPathType(typePath, "zutubi.multiRecipeTypeConfig");
        // This path should have been newly-added from the new parent.
        assertTrue(rpcClient.RemoteApi.configPathExists(getPath(childProjectPath, "properties", "prop")));
    }

    public void testMoveConfigWithSubtree() throws Exception
    {
        String random = randomName();
        String childProject = random + "-child";
        String grandchild1Project = random + "-grandchild1";
        String grandchild2Project = random + "-grandchild2";
        String newTemplateParentProject = random + "-new-parent";

        String childProjectPath = rpcClient.RemoteApi.insertTrivialProject(childProject, true);
        String grandchild1ProjectPath = rpcClient.RemoteApi.insertSimpleProject(grandchild1Project, childProject, false);
        String grandchild2ProjectPath = rpcClient.RemoteApi.insertProject(grandchild2Project, childProject, false, rpcClient.RemoteApi.getSubversionConfig(Constants.FAIL_ANT_REPOSITORY), rpcClient.RemoteApi.createVersionedConfig("path"));
        rpcClient.RemoteApi.insertSimpleProject(newTemplateParentProject, true);
        rpcClient.RemoteApi.insertProjectProperty(newTemplateParentProject, "prop", "val");

        Hashtable<String, Object> result = rpcClient.RemoteApi.moveConfig(childProjectPath, newTemplateParentProject);
        
        String grandhchild2TypePath = getPath(grandchild2ProjectPath, "type");
        checkExpectedDeletedPaths(result, grandhchild2TypePath);
        
        assertEquals(newTemplateParentProject, rpcClient.RemoteApi.getTemplateParent(childProjectPath));
        
        // Override of Subversion URL should be maintained.
        Hashtable<String, Object> grandchild2Scm = rpcClient.RemoteApi.getConfig(getPath(grandchild2ProjectPath, "scm"));
        assertEquals(Constants.FAIL_ANT_REPOSITORY, grandchild2Scm.get("url"));
        
        // Incompatible, deleted.
        checkPathType(grandhchild2TypePath, "zutubi.multiRecipeTypeConfig");
        
        // This path should have been newly-added from the new parent.
        assertTrue(rpcClient.RemoteApi.configPathExists(getPath(childProjectPath, "properties", "prop")));
        assertTrue(rpcClient.RemoteApi.configPathExists(getPath(grandchild1ProjectPath, "properties", "prop")));
        assertTrue(rpcClient.RemoteApi.configPathExists(getPath(grandchild2ProjectPath, "properties", "prop")));
    }
    
    public void testMoveConfigJustEnoughPermissions() throws Exception
    {
        String random = randomName();
        String user = random + "-user";
        String group = random + "-group";
        String childProject = random + "-child";
        String newTemplateParentProject = random + "-new-parent";

        String childProjectPath = rpcClient.RemoteApi.insertSimpleProject(childProject, false);
        rpcClient.RemoteApi.insertSimpleProject(newTemplateParentProject, true);

        String userPath = rpcClient.RemoteApi.insertTrivialUser(user);
        String groupPath = rpcClient.RemoteApi.insertGroup(group, asList(userPath));
        Hashtable<String, Object> acl = rpcClient.RemoteApi.createDefaultConfig(ProjectAclConfiguration.class);
        acl.put("group", groupPath);
        acl.put("allowedActions", new Vector<String>(asList(AccessManager.ACTION_WRITE)));
        rpcClient.RemoteApi.insertConfig(PathUtils.getPath(childProjectPath, "permissions"), acl);
        
        rpcClient.logout();
        rpcClient.login(user, "");
        rpcClient.RemoteApi.moveConfig(childProjectPath, newTemplateParentProject);
    }
    
    public void testMoveConfigNoWritePermissionForPath() throws Exception
    {
        String random = randomName();
        String user = random + "-user";
        String childProject = random + "-child";
        String newTemplateParentProject = random + "-new-parent";

        String childProjectPath = rpcClient.RemoteApi.insertSimpleProject(childProject, false);
        rpcClient.RemoteApi.insertSimpleProject(newTemplateParentProject, true);

        rpcClient.RemoteApi.insertTrivialUser(user);
        rpcClient.logout();
        rpcClient.login(user, "");
        try
        {
            rpcClient.RemoteApi.moveConfig(childProjectPath, newTemplateParentProject);
            fail("Should not be able to move a project that we cannot write to");
        }
        catch (Exception e)
        {
            assertThat(e.getMessage(), containsString("Permission to write at path '" + childProjectPath + "' denied"));
        }
    }

    public void testMoveConfigNoWritePermissionForSubtree() throws Exception
    {
        String random = randomName();
        String user = random + "-user";
        String group = random + "-group";
        String childProject = random + "-child";
        String grandChildProject = random + "-grandchild";
        String newTemplateParentProject = random + "-new-parent";

        String childProjectPath = rpcClient.RemoteApi.insertSimpleProject(childProject, true);
        String grandChildProjectPath = rpcClient.RemoteApi.insertTrivialProject(grandChildProject, childProject, false);
        rpcClient.RemoteApi.insertSimpleProject(newTemplateParentProject, true);

        String userPath = rpcClient.RemoteApi.insertTrivialUser(user);
        String groupPath = rpcClient.RemoteApi.insertGroup(group, asList(userPath));
        Hashtable<String, Object> acl = rpcClient.RemoteApi.createDefaultConfig(ProjectAclConfiguration.class);
        acl.put("group", groupPath);
        acl.put("allowedActions", new Vector<String>(asList(AccessManager.ACTION_WRITE)));
        rpcClient.RemoteApi.insertConfig(PathUtils.getPath(childProjectPath, "permissions"), acl);

        rpcClient.RemoteApi.deleteAllConfigs(getPath(grandChildProjectPath, "permissions", WILDCARD_ANY_ELEMENT));
        
        rpcClient.logout();
        rpcClient.login(user, "");
        try
        {
            rpcClient.RemoteApi.moveConfig(childProjectPath, newTemplateParentProject);
            fail("Should not be able to move with descendant we cannot write to");
        }
        catch (Exception e)
        {
            assertThat(e.getMessage(), containsString("Permission to write at path '" + grandChildProjectPath + "' denied"));            
        }
    }
    
    public void testMoveConfigNoReadPermissionForNewTemplateParent() throws Exception
    {
        String random = randomName();
        String user = random + "-user";
        String childProject = random + "-child";
        String newTemplateParentProject = random + "-new-parent";
        
        String childProjectPath = rpcClient.RemoteApi.insertSimpleProject(childProject, false);
        String newTemplateParentProjectPath = rpcClient.RemoteApi.insertSimpleProject(newTemplateParentProject, true);

        rpcClient.RemoteApi.deleteAllConfigs(getPath(newTemplateParentProjectPath, "permissions", WILDCARD_ANY_ELEMENT));
        
        rpcClient.RemoteApi.insertTrivialUser(user);
        rpcClient.logout();
        rpcClient.login(user, "");
        try
        {
            rpcClient.RemoteApi.moveConfig(childProjectPath, newTemplateParentProject);
            fail("Should not be able to move to new template parent that we cannot view");
        }
        catch (Exception e)
        {
            assertThat(e.getMessage(), containsString("Permission to view at path '" + newTemplateParentProjectPath + "' denied"));            
        }
    }

    private void checkPathType(String path, String expectedType) throws Exception
    {
        Hashtable<String, Object> config = rpcClient.RemoteApi.getConfig(path);
        assertEquals(expectedType, config.get(SYMBOLIC_NAME_KEY));
    }

    private void checkExpectedDeletedPaths(Hashtable<String, Object> result, String... expected)
    {
        @SuppressWarnings({"unchecked"})
        List<String> deletedPaths = new LinkedList<String>((Collection<? extends String>) result.get("deletedPaths"));
        Collections.sort(deletedPaths);
        assertEquals(asList(expected), deletedPaths);
    }

    public void testRenameRecipe() throws Exception
    {
        // Simple case: rename concrete recipe, check default and stages
        // updated.
        recipeRenameHelper(false);
    }

    public void testRenameRecipeInTemplate() throws Exception
    {
        // A template with no ancestors will raise no instance events on change
        // - ensuring that we don't depend on these events.
        recipeRenameHelper(true);
    }

    private void recipeRenameHelper(boolean template) throws Exception
    {
        final String ORIGINAL_NAME = "default";
        final String NEW_NAME = "edited";

        String projectPath = rpcClient.RemoteApi.insertSimpleProject(randomName(), template);
        String stagePath = getPath(projectPath, Constants.Project.STAGES, "default");
        Hashtable<String, Object> stage = rpcClient.RemoteApi.getConfig(stagePath);
        stage.put(Constants.Project.Stage.RECIPE, ORIGINAL_NAME);
        rpcClient.RemoteApi.saveConfig(stagePath, stage, false);


        String typePath = renameRecipe(projectPath, ORIGINAL_NAME, NEW_NAME);


        Hashtable<String, Object> type = rpcClient.RemoteApi.getConfig(typePath);
        assertEquals(NEW_NAME, type.get(Constants.Project.MultiRecipeType.DEFAULT_RECIPE));

        stage = rpcClient.RemoteApi.getConfig(stagePath);
        assertEquals(NEW_NAME, stage.get(Constants.Project.Stage.RECIPE));
    }

    public void testRenameRecipeInTemplateUpdatesDescendant() throws Exception
    {
        // In the descendant we override the default recipe (so it should not
        // be updated) and we add a second parentDefaultStage that uses the recipe (which
        // should be updated).
        final String ORIGINAL_NAME = "default";
        final String NEW_NAME = "edited";
        final String OTHER_NAME = "other";

        final String STAGE_DEFAULT = "default";
        final String STAGE_OTHER = "otherstage";

        String random = randomName();
        String parentName = random + "-parent";
        String childName = random + "-child";
        String parentPath = rpcClient.RemoteApi.insertSimpleProject(parentName, true);
        String childPath = rpcClient.RemoteApi.insertSimpleProject(childName, parentName, false);

        String parentDefaultStagePath = getPath(parentPath, Constants.Project.STAGES, STAGE_DEFAULT);
        Hashtable<String, Object> parentDefaultStage = rpcClient.RemoteApi.getConfig(parentDefaultStagePath);
        parentDefaultStage.put(Constants.Project.Stage.RECIPE, ORIGINAL_NAME);
        rpcClient.RemoteApi.saveConfig(parentDefaultStagePath, parentDefaultStage, false);

        String childTypePath = getPath(childPath, Constants.Project.TYPE);
        Hashtable<String, Object> childType = rpcClient.RemoteApi.getConfig(childTypePath);
        childType.put(Constants.Project.MultiRecipeType.DEFAULT_RECIPE, OTHER_NAME);
        rpcClient.RemoteApi.saveConfig(childTypePath, childType, false);
        String childStagesPath = getPath(childPath, Constants.Project.STAGES);
        Hashtable<String, String> keyMap = new Hashtable<String, String>();
        keyMap.put(STAGE_DEFAULT, STAGE_OTHER);
        rpcClient.RemoteApi.cloneConfig(childStagesPath, keyMap);


        String parentTypePath = renameRecipe(parentPath, ORIGINAL_NAME, NEW_NAME);


        // Parent references (default recipe, stage) both updated.
        Hashtable<String, Object> parentType = rpcClient.RemoteApi.getConfig(parentTypePath);
        assertEquals(NEW_NAME, parentType.get(Constants.Project.MultiRecipeType.DEFAULT_RECIPE));

        parentDefaultStage = rpcClient.RemoteApi.getConfig(parentDefaultStagePath);
        assertEquals(NEW_NAME, parentDefaultStage.get(Constants.Project.Stage.RECIPE));

        // Overridden child default recipe unchanged.
        childType = rpcClient.RemoteApi.getConfig(childTypePath);
        assertEquals(OTHER_NAME, childType.get(Constants.Project.MultiRecipeType.DEFAULT_RECIPE));
        
        // Both child stages (one inherited, other local) updated.
        Hashtable<String, Object> childStage = rpcClient.RemoteApi.getConfig(getPath(childStagesPath, STAGE_DEFAULT));
        assertEquals(NEW_NAME, childStage.get(Constants.Project.Stage.RECIPE));
        childStage = rpcClient.RemoteApi.getConfig(getPath(childStagesPath, STAGE_OTHER));
        assertEquals(NEW_NAME, childStage.get(Constants.Project.Stage.RECIPE));
    }
    
    public void testGetConfigState() throws Exception
    {
        String projectPath = rpcClient.RemoteApi.insertSimpleProject(randomName());
        Hashtable<String, Object> trigger = rpcClient.RemoteApi.createEmptyConfig(ScmBuildTriggerConfiguration.class);
        trigger.put("name", "test");
        String triggerPath = rpcClient.RemoteApi.insertConfig(getPath(projectPath, "triggers"), trigger);
        Hashtable<String, String> state = rpcClient.RemoteApi.getConfigState(triggerPath);
        assertEquals("scheduled", state.get("state"));
    }

    public void testGetConfigStateForCollection() throws Exception
    {
        String projectPath = rpcClient.RemoteApi.insertSimpleProject(randomName());
        Hashtable<String, String> state = rpcClient.RemoteApi.getConfigState(getPath(projectPath, Constants.Project.REQUIREMENTS));
        assertEquals("all agents", state.get("compatibleAgents"));
    }

    public void testExportAndImport() throws Exception
    {
        String random = randomName();
        String template = random + "-template";
        String child1 = random + "-child1";
        String child2 = random + "-child2";

        String templatePath = rpcClient.RemoteApi.insertTrivialProject(template, true);
        String child1Path = rpcClient.RemoteApi.insertSimpleProject(child1, template, false);
        String child2Path = rpcClient.RemoteApi.insertProject(child2, template, false, rpcClient.RemoteApi.getSubversionConfig(Constants.FAIL_ANT_REPOSITORY), rpcClient.RemoteApi.createVersionedConfig("path"));

        Hashtable<String, Object> child2Before = rpcClient.RemoteApi.getConfig(child2Path);

        File temp = FileSystemUtils.createTempFile(getName(), ".tmp", "");
        try
        {
            rpcClient.RemoteApi.exportConfig(temp.getAbsolutePath(), false, templatePath, child2Path);

            assertTrue(rpcClient.RemoteApi.deleteConfig(templatePath));
            assertFalse(rpcClient.RemoteApi.configPathExists(templatePath));
            assertFalse(rpcClient.RemoteApi.configPathExists(child1Path));
            assertFalse(rpcClient.RemoteApi.configPathExists(child2Path));

            rpcClient.RemoteApi.importConfig(temp.getAbsolutePath());
            assertTrue(rpcClient.RemoteApi.configPathExists(templatePath));
            assertFalse(rpcClient.RemoteApi.configPathExists(child1Path));
            assertTrue(rpcClient.RemoteApi.configPathExists(child2Path));
            assertTrue(rpcClient.RemoteApi.getAllProjectNames().contains(child2));

            Hashtable<String, Object> child2After = rpcClient.RemoteApi.getConfig(child2Path);
            assertEquals(child2Before, child2After);
        }
        finally
        {
            assertTrue(temp.delete());
        }
    }

    private String renameRecipe(String projectPath, String originalName, String newName) throws Exception
    {
        String typePath = getPath(projectPath, Constants.Project.TYPE);
        String recipePath = getPath(typePath, Constants.Project.MultiRecipeType.RECIPES, originalName);
        Hashtable<String, Object> recipe = rpcClient.RemoteApi.getConfig(recipePath);
        recipe.put(Constants.Project.MultiRecipeType.Recipe.NAME, newName);
        rpcClient.RemoteApi.saveConfig(recipePath, recipe, false);
        return typePath;
    }

    private void assertSortedEquals(Collection<String> got, String... expected)
    {
        assertEquals(expected.length, got.size());
        String[] gotArray = got.toArray(new String[got.size()]);
        Arrays.sort(gotArray, new Sort.StringComparator());
        for (int i = 0; i < gotArray.length; i++)
        {
            assertEquals(expected[i], gotArray[i]);
        }
    }

    private void callAndExpectError(String error, String function, Object... args)
    {
        try
        {
            rpcClient.RemoteApi.call(function, args);
            fail();
        }
        catch (Exception e)
        {
            assertTrue("Message '" + e.getMessage() + "' does not contain '" + error + "'", e.getMessage().contains(error));
        }
    }
}
