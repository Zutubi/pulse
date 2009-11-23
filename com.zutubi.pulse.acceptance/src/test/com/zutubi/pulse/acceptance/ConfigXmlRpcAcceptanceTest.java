package com.zutubi.pulse.acceptance;

import com.zutubi.pulse.master.model.ProjectManager;
import com.zutubi.pulse.master.tove.config.MasterConfigurationRegistry;
import com.zutubi.pulse.master.tove.config.agent.AgentConfigurationActions;
import com.zutubi.pulse.master.tove.config.user.SetPasswordConfiguration;
import com.zutubi.pulse.master.tove.config.user.UserConfiguration;
import com.zutubi.pulse.master.tove.config.user.UserConfigurationActions;
import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.util.Sort;

import java.util.*;
import static java.util.Arrays.asList;

/**
 * Tests for the remote API functions dealing with configuration.  Other
 * tests deal with specific configuration types, this test just concentrates
 * on the general capabilities and boundaries of the config functions.
 */
public class ConfigXmlRpcAcceptanceTest extends BaseXmlRpcAcceptanceTest
{
    private static final String TEST_PROJECT_NAME = "config-xml-rpc";
    private static final String TEST_PROJECT_PATH = "projects/config-xml-rpc";

    protected void setUp() throws Exception
    {
        super.setUp();
        loginAsAdmin();
    }

    protected void tearDown() throws Exception
    {
        logout();
        super.tearDown();
    }

    public void testConfigPathExistsEmptyPath() throws Exception
    {
        assertEquals(false, call("configPathExists", ""));
    }

    public void testConfigPathExistsNonExistant() throws Exception
    {
        assertEquals(false, call("configPathExists", "nonexistant"));
    }

    public void testConfigPathExistsExistant() throws Exception
    {
        assertEquals(true, call("configPathExists", "projects"));
    }

    public void testGetConfigListingEmptyPath() throws Exception
    {
        Vector<String> listing = call("getConfigListing", "");
        assertSortedEquals(listing, "agents", "groups", "projects", "settings", "users");
    }

    public void testGetConfigListingNonExistantPath() throws Exception
    {
        callAndExpectError("Invalid path 'nonexistant'", "getConfigListing", "nonexistant");
    }

    public void testGetConfigListingComposite() throws Exception
    {
        Vector<String> listing = call("getConfigListing", "settings");
        assertSortedEquals(listing, "agentPing", "backup", "email", "jabber", "ldap", "license", "logging", "repository");
    }

    public void testGetConfigListingCollection() throws Exception
    {
        Vector<String> listing = call("getConfigListing", "projects");
        int sizeBefore = listing.size();
        String project = randomName();
        insertSimpleProject(project);
        listing = call("getConfigListing", "projects");
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
        String handle = call("getConfigHandle", path);
        assertEquals(path, call("getConfigPath", handle));
    }

    public void testGetConfig() throws Exception
    {
        Hashtable<String, Object> emailConfig = new Hashtable<String, Object>();
        emailConfig.put(SYMBOLIC_NAME_KEY, "zutubi.emailConfig");
        emailConfig.put("host", "localhost");
        emailConfig.put("from", "pulse@zutubi.com");
        emailConfig.put("ssl", true);
        call("saveConfig", "settings/email", emailConfig, false);

        emailConfig = call("getConfig", "settings/email");
        assertEquals("zutubi.emailConfig", emailConfig.get(SYMBOLIC_NAME_KEY));
        assertEquals("localhost", emailConfig.get("host"));
        assertEquals("pulse@zutubi.com", emailConfig.get("from"));
        assertEquals(true, emailConfig.get("ssl"));
    }

    public void testGetConfigTemplated() throws Exception
    {
        String agentName = randomName();
        String path = insertSimpleAgent(agentName);
        Hashtable<String, Object> agentConfig = call("getConfig", path);
        try
        {
            assertEquals("zutubi.agentConfig", agentConfig.get(SYMBOLIC_NAME_KEY));
            assertEquals(agentName, agentConfig.get("name"));
            assertEquals(agentName, agentConfig.get("host"));
            assertEquals(8890, agentConfig.get("port"));
            assertEquals(true, agentConfig.get("remote"));
        }
        finally
        {
            call("deleteConfig", path);
        }
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
        Hashtable<String, Object> emailConfig = call("getRawConfig", "settings/email");
        assertEquals("zutubi.emailConfig", emailConfig.get(SYMBOLIC_NAME_KEY));
        assertTrue(emailConfig.containsKey("host"));
        assertTrue(emailConfig.containsKey("port"));
        assertTrue(emailConfig.containsKey("username"));
        assertTrue(emailConfig.containsKey("ssl"));
    }

    public void testGetRawConfigTemplated() throws Exception
    {
        String agentName = randomName();
        String path = insertSimpleAgent(agentName);
        Hashtable<String, Object> agentConfig = call("getRawConfig", path);
        try
        {
            assertEquals("zutubi.agentConfig", agentConfig.get(SYMBOLIC_NAME_KEY));
            assertEquals(agentName, agentConfig.get("name"));
            assertEquals(agentName, agentConfig.get("host"));
            assertEquals(8890, agentConfig.get("port"));
            assertFalse(agentConfig.containsKey("remote"));
        }
        finally
        {
            call("deleteConfig", path);
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
        callAndExpectError("record already exists", "insertConfig", "settings/email", config);
    }

    public void testInsertConfigNoSymbolicName() throws Exception
    {
        ensureProject(TEST_PROJECT_NAME);
        Hashtable<String, Object> config = new Hashtable<String, Object>();
        callAndExpectError("No symbolic name found in XML-RPC struct", "insertConfig", TEST_PROJECT_PATH + "/stages", config);
    }

    public void testInsertConfigInvalidSymbolicName() throws Exception
    {
        ensureProject(TEST_PROJECT_NAME);
        Hashtable<String, Object> config = new Hashtable<String, Object>();
        config.put(SYMBOLIC_NAME_KEY, "zutubi.nosuchConfig");
        callAndExpectError("Unrecognised symbolic name", "insertConfig", TEST_PROJECT_PATH + "/stages", config);
    }

    public void testInsertConfigTypeMismatch() throws Exception
    {
        ensureProject(TEST_PROJECT_NAME);
        Hashtable<String, Object> config = new Hashtable<String, Object>();
        config.put(SYMBOLIC_NAME_KEY, "zutubi.emailConfig");
        callAndExpectError("Expected type: class com.zutubi.pulse.master.tove.config.project.BuildStageConfiguration", "insertConfig", TEST_PROJECT_PATH + "/stages", config);
    }

    public void testInsertConfigValidates() throws Exception
    {
        ensureProject(TEST_PROJECT_NAME);
        Hashtable<String, Object> config = new Hashtable<String, Object>();
        config.put(SYMBOLIC_NAME_KEY, "zutubi.stageConfig");
        callAndExpectError("name requires a value", "insertConfig", TEST_PROJECT_PATH + "/stages", config);
    }

    public void testInsertConfigIntoCollection() throws Exception
    {
        String projectPath = insertSimpleProject(randomName());
        Hashtable<String, Object> stage = new Hashtable<String, Object>();
        stage.put(SYMBOLIC_NAME_KEY, "zutubi.stageConfig");
        stage.put("name", "my stage");
        stage.put("recipe", "a recipe");
        String stagePath = call("insertConfig", projectPath + "/stages", stage);
        
        Hashtable<String, Object> loadedStage = call("getConfig", stagePath);
        assertEquals("zutubi.stageConfig", loadedStage.get(SYMBOLIC_NAME_KEY));
        assertEquals("my stage", loadedStage.get("name"));
        assertEquals("a recipe", loadedStage.get("recipe"));
    }

    public void testInsertConfigIntoSingleton() throws Exception
    {
        String projectPath = insertSimpleProject(randomName());
        String scmPath = projectPath + "/scm";
        assertEquals(true, call("deleteConfig", scmPath));

        Hashtable<String, Object> scm = new Hashtable<String, Object>();
        scm.put(SYMBOLIC_NAME_KEY, "zutubi.subversionConfig");
        scm.put("url", "svn://localhost/test/trunk");
        scm.put("monitor", false);
        call("insertConfig", scmPath, scm);

        Hashtable<String, Object> loadedScm = call("getConfig", scmPath);
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
        callAndExpectError("parent path 'settings' is not a templated collection", "insertTemplatedConfig", "settings/email", new Hashtable(), false);
    }

    public void testInsertTemplatedConfigConcreteParentPath() throws Exception
    {
        ensureProject(TEST_PROJECT_NAME);
        callAndExpectError("template parent is concrete and thus cannot be inherited from", "insertTemplatedConfig", TEST_PROJECT_PATH, new Hashtable(), false);
    }

    public void testInsertTemplatedConfig() throws Exception
    {
        String projectName = randomName();
        String projectPath = insertSimpleProject(projectName);
        
        Hashtable<String, Object> project = call("getConfig", projectPath);
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

        String projectPath = call("insertTemplatedConfig", "projects/global project template", project, true);
        Hashtable<String, Object> loadedProject = call("getConfig", projectPath);
        Hashtable<String, Object> loadedScm = (Hashtable<String, Object>) loadedProject.get("scm");
        assertFalse(loadedScm.containsKey("url"));
    }

    public void testInsertTemplatedConfigTemplate() throws Exception
    {
        Hashtable<String, Object> project = new Hashtable<String, Object>();
        project.put(SYMBOLIC_NAME_KEY, "zutubi.projectConfig");
        String projectName = randomName();
        project.put("name", projectName);

        String projectPath = call("insertTemplatedConfig", "projects/global project template", project, true);
        Hashtable<String, Object> loadedProject = call("getConfig", projectPath);
        assertEquals(projectName, loadedProject.get("name"));
        assertEquals(1, ((Hashtable<String, Object>) loadedProject.get("cleanup")).size());
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
        String projectPath = insertSimpleProject(randomName());
        Hashtable<String, Object> options = new Hashtable<String, Object>();
        callAndExpectError("No symbolic name found in XML-RPC struct", "saveConfig", projectPath + "/options", options, false);
    }

    public void testSaveConfigInvalidType() throws Exception
    {
        String projectPath = insertSimpleProject(randomName());
        Hashtable<String, Object> options = new Hashtable<String, Object>();
        options.put(SYMBOLIC_NAME_KEY, "invalid");
        callAndExpectError("Expecting type 'zutubi.buildOptionsConfig', found 'invalid'", "saveConfig", projectPath + "/options", options, false);
    }

    public void testSaveConfigValidates() throws Exception
    {
        String projectPath = insertSimpleProject(randomName());
        String optionsPath = projectPath + "/options";
        Hashtable<String, Object> options = call("getConfig", optionsPath);
        options.put("timeout", -10);
        callAndExpectError("timeout must not be negative", "saveConfig", projectPath + "/options", options, false);
    }

    public void testSaveConfigValidatesShallow() throws Exception
    {
        String projectPath = insertSimpleProject(randomName());
        Hashtable<String, Object> project = call("getConfig", projectPath);
        Hashtable<String, Object> options = (Hashtable<String, Object>) project.get("options");
        assertEquals(0, options.get("timeout"));
        options.put("timeout", -1);
        call("saveConfig", projectPath, project, false);
    }
    
    public void testSaveConfigValidatesDeep() throws Exception
    {
        String projectPath = insertSimpleProject(randomName());
        Hashtable<String, Object> project = call("getConfig", projectPath);
        Hashtable<String, Object> options = (Hashtable<String, Object>) project.get("options");
        assertEquals(0, options.get("timeout"));
        options.put("timeout", -1);
        callAndExpectError("timeout must not be negative", "saveConfig", projectPath, project, true);
    }

    public void testSaveConfig() throws Exception
    {
        String projectPath = insertSimpleProject(randomName());
        String optionsPath = projectPath + "/options";
        Hashtable<String, Object> options = call("getConfig", optionsPath);
        assertEquals(false, options.get("prompt"));
        assertEquals(0, options.get("timeout"));

        options.put("prompt", true);
        options.put("timeout", 10);
        call("saveConfig", projectPath + "/options", options, false);

        Hashtable<String, Object> loadedOptions = call("getConfig", optionsPath);
        assertEquals(true, loadedOptions.get("prompt"));
        assertEquals(10, loadedOptions.get("timeout"));
    }

    public void testSaveConfigShallow() throws Exception
    {
        String projectPath = insertSimpleProject(randomName());
        Hashtable<String, Object> project = call("getConfig", projectPath);
        Hashtable<String, Object> options = (Hashtable<String, Object>) project.get("options");
        assertEquals(false, options.get("prompt"));
        options.put("prompt", true);
        call("saveConfig", projectPath, project, false);

        Hashtable<String, Object> loadedOptions = call("getConfig", projectPath + "/options");
        assertEquals(false, loadedOptions.get("prompt"));
    }

    public void testSaveConfigDeep() throws Exception
    {
        String projectPath = insertSimpleProject(randomName());
        Hashtable<String, Object> project = call("getConfig", projectPath);
        Hashtable<String, Object> options = (Hashtable<String, Object>) project.get("options");
        assertEquals(false, options.get("prompt"));
        options.put("prompt", true);
        call("saveConfig", projectPath, project, true);

        Hashtable<String, Object> loadedOptions = call("getConfig", projectPath + "/options");
        assertEquals(true, loadedOptions.get("prompt"));
    }

    public void testDeleteConfigEmptyPath() throws Exception
    {
        callAndExpectError("path is empty", "deleteConfig", "");
    }

    public void testCanCloneNonexistantPath() throws Exception
    {
        assertEquals(false, call("canCloneConfig", "foo"));
    }

    public void testCanCloneUncloneablePath() throws Exception
    {
        assertEquals(false, call("canCloneConfig", PathUtils.getPath(MasterConfigurationRegistry.PROJECTS_SCOPE, ProjectManager.GLOBAL_PROJECT_NAME)));
    }

    public void testCanCloneCloneablePath() throws Exception
    {
        String path = xmlRpcHelper.insertTrivialProject(randomName(), false);
        assertEquals(true, call("canCloneConfig", path));
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
        xmlRpcHelper.insertTrivialProject(name, false);
        Hashtable<String, String> keyMap = new Hashtable<String, String>(1);
        keyMap.put(name, cloneName);
        assertEquals(true, call("cloneConfig", MasterConfigurationRegistry.PROJECTS_SCOPE, keyMap));
        assertTrue(xmlRpcHelper.configPathExists(PathUtils.getPath(MasterConfigurationRegistry.PROJECTS_SCOPE, cloneName)));
    }

    public void testDeleteConfigNonExistantPath() throws Exception
    {
        callAndExpectError("Invalid path", "deleteConfig", "nonexistant");
    }

    public void testDelete() throws Exception
    {
        String path = insertSimpleProject(randomName());
        assertEquals(true, call("configPathExists", path));
        assertEquals(true, call("deleteConfig", path));
        assertEquals(false, call("configPathExists", path));
    }

    public void testDeleteSingleton() throws Exception
    {
        String path = insertSimpleProject(randomName());
        String scmPath = path + "/scm";
        assertEquals(true, call("configPathExists", scmPath));
        assertEquals(true, call("deleteConfig", scmPath));
        assertEquals(false, call("configPathExists", scmPath));
    }

    public void testDeleteInheritedFrom() throws Exception
    {
        Hashtable<String, Object> project = new Hashtable<String, Object>();
        project.put(SYMBOLIC_NAME_KEY, "zutubi.projectConfig");
        project.put("name", randomName());
        String parentPath = call("insertTemplatedConfig", "projects/global project template", project, true);

        project.put("name", randomName());
        String childPath = call("insertTemplatedConfig", parentPath, project, true);
        assertEquals(true, call("configPathExists", parentPath));
        assertEquals(true, call("configPathExists", childPath));

        assertEquals(true, call("deleteConfig", parentPath));
        assertEquals(false, call("configPathExists", parentPath));
        assertEquals(false, call("configPathExists", childPath));
    }

    public void testDeleteAll() throws Exception
    {
        String path = insertSimpleProject(randomName());
        Hashtable<String, Object> project = call("getConfig", path);
        Hashtable<String, Object> properties = (Hashtable<String, Object>) project.get("properties");
        properties.put("p1", xmlRpcHelper.createProperty("p1", "v1"));
        properties.put("p2", xmlRpcHelper.createProperty("p2", "v2"));
        call("saveConfig", path, project, true);

        Hashtable<String, Object> loadedProperties = call("getConfig", path + "/properties");
        assertEquals(2, loadedProperties.size());

        assertEquals(2, call("deleteAllConfigs", path + "/properties/*"));
        loadedProperties = call("getConfig", path + "/properties");
        assertEquals(0, loadedProperties.size());
    }

    public void testDeleteAllHitsInherited() throws Exception
    {
        String random = randomName();
        String parent = random + "-parent";
        String child = random + "-child";

        String parentPath = xmlRpcHelper.insertSimpleProject(parent, true);
        String childPath = xmlRpcHelper.insertSimpleProject(child, parent, false);

        Hashtable<String, Object> project = call("getConfig", parentPath);
        Hashtable<String, Object> properties = (Hashtable<String, Object>) project.get("properties");
        properties.put("p1", xmlRpcHelper.createProperty("p1", "v1"));
        call("saveConfig", parentPath, project, true);

        assertEquals(1, call("deleteAllConfigs", childPath+ "/properties/*"));
        Hashtable<String, Object> loadedProperties = call("getConfig", childPath + "/properties");
        assertEquals(0, loadedProperties.size());

        call("restoreConfig", childPath + "/properties/p1");
        loadedProperties = call("getConfig", childPath + "/properties");
        assertEquals(1, loadedProperties.size());
    }

    public void testRestore() throws Exception
    {
        String random = randomName();
        String parentName = random + "-parent";
        String childName = random + "-child";
        xmlRpcHelper.insertSimpleProject(parentName, true);
        String childPath = xmlRpcHelper.insertSimpleProject(childName, parentName, false);

        String stagesPath = PathUtils.getPath(childPath, "stages");
        String hidePath = PathUtils.getPath(stagesPath, "default");
        xmlRpcHelper.deleteConfig(hidePath);
        assertEquals(0, xmlRpcHelper.getConfigListing(stagesPath).size());
        xmlRpcHelper.restoreConfig(hidePath);
        Vector<String> listing = xmlRpcHelper.getConfigListing(stagesPath);
        assertEquals(1, listing.size());
        assertEquals("default", listing.get(0));
    }

    public void testSetOrder() throws Exception
    {
        String random = randomName();
        String path = xmlRpcHelper.insertSimpleProject(random, true);
        String propertiesPath = PathUtils.getPath(path, "properties");
        xmlRpcHelper.insertProjectProperty(random, "p1", "v1");
        xmlRpcHelper.insertProjectProperty(random, "p2", "v2");

        assertEquals(asList("p1", "p2"), new LinkedList<String>(xmlRpcHelper.getConfigListing(propertiesPath)));
        xmlRpcHelper.setConfigOrder(propertiesPath, "p2", "p1");
        assertEquals(asList("p2", "p1"), new LinkedList<String>(xmlRpcHelper.getConfigListing(propertiesPath)));
    }

    public void testGetConfigActions() throws Exception
    {
        String agentName = randomName();
        String path = xmlRpcHelper.insertSimpleAgent(agentName);
        try
        {
            Vector<String> actions = xmlRpcHelper.getConfigActions(path);
            assertEquals(asList(AgentConfigurationActions.ACTION_DISABLE, AgentConfigurationActions.ACTION_PING),
                         new LinkedList<String>(actions));
        }
        finally
        {
            xmlRpcHelper.deleteConfig(path);
        }
    }

    public void testDoConfigAction() throws Exception
    {
        String agentName = randomName();
        String path = xmlRpcHelper.insertSimpleAgent(agentName);
        try
        {
            xmlRpcHelper.doConfigAction(path, AgentConfigurationActions.ACTION_DISABLE);
            Vector<String> actions = xmlRpcHelper.getConfigActions(path);
            assertEquals(asList(AgentConfigurationActions.ACTION_ENABLE),
                     new LinkedList<String>(actions));
        }
        finally
        {
            xmlRpcHelper.deleteConfig(path);
        }
    }

    public void testDoConfigActionWithArgument() throws Exception
    {
        String userName = randomName();
        String path = xmlRpcHelper.insertTrivialUser(userName);
        try
        {
            Hashtable<String, Object> password = xmlRpcHelper.createDefaultConfig(SetPasswordConfiguration.class);
            password.put("password", "foo");
            password.put("confirmPassword", "foo");
            xmlRpcHelper.doConfigActionWithArgument(path, UserConfigurationActions.ACTION_SET_PASSWORD, password);
            xmlRpcHelper.logout();
            xmlRpcHelper.login(userName, "foo");
        }
        finally
        {
            xmlRpcHelper.deleteConfig(path);
        }
    }

    public void testDoConfigActionWithInvalidArgument() throws Exception
    {
        String userName = randomName();
        String path = xmlRpcHelper.insertTrivialUser(userName);
        try
        {
            Hashtable<String, Object> password = xmlRpcHelper.createDefaultConfig(SetPasswordConfiguration.class);
            password.put("password", "foo");
            password.put("confirmPassword", "bar");
            xmlRpcHelper.doConfigActionWithArgument(path, UserConfigurationActions.ACTION_SET_PASSWORD, password);
            fail();
        }
        catch(Exception e)
        {
            assertEquals("java.lang.Exception: com.zutubi.pulse.master.api.ValidationException: password: passwords do not match", e.getMessage());
        }
        finally
        {
            xmlRpcHelper.deleteConfig(path);
        }
    }

    public void testDoConfigActionWithIncorrectArgument() throws Exception
    {
        String userName = randomName();
        String path = xmlRpcHelper.insertTrivialUser(userName);
        try
        {
            // Deliberately pass wrong type as argument
            Hashtable<String, Object> password = xmlRpcHelper.createDefaultConfig(UserConfiguration.class);
            password.put("login", randomName());
            password.put("name", randomName());
            xmlRpcHelper.doConfigActionWithArgument(path, UserConfigurationActions.ACTION_SET_PASSWORD, password);
            fail();
        }
        catch(Exception e)
        {
            assertEquals("java.lang.Exception: java.lang.RuntimeException: java.lang.IllegalArgumentException: Invoking action 'setPassword' of type 'com.zutubi.pulse.master.tove.config.user.UserConfiguration': argument instance is of wrong type: expecting 'com.zutubi.pulse.master.tove.config.user.SetPasswordConfiguration', got 'com.zutubi.pulse.master.tove.config.user.UserConfiguration'",
                         e.getMessage());
        }
        finally
        {
            xmlRpcHelper.deleteConfig(path);
        }
    }

    public void testDefaultConstructorAppliedToUser() throws Exception
    {
        String random = randomName();

        // Use empty config so we don't get bits from default config.
        Hashtable<String, Object> user = xmlRpcHelper.createEmptyConfig(UserConfiguration.class);
        user.put("login", random);
        user.put("name", random);
        String path = xmlRpcHelper.insertConfig(MasterConfigurationRegistry.USERS_SCOPE, user);
        assertTrue(xmlRpcHelper.isConfigPermanent(PathUtils.getPath(path, "preferences")));
    }

    public void testGetConfigDoesntReturnExternalState() throws Exception
    {
        String projectPath = insertSimpleProject(randomName());
        Hashtable<String, Object> project = call("getConfig", projectPath);
        assertNull(project.get("projectId"));
    }

    public void testSaveConfigPreservesState() throws Exception
    {
        // CIB-1542: check that a normal save does not touch the projectId
        // external state field.
        String name = randomName();
        String projectPath = insertSimpleProject(name);
        Hashtable<String, Object> project = call("getConfig", projectPath);
        project.put("url", "http://i.feel.like.a.change.com");
        call("saveConfig", projectPath, project, false);

        assertEquals("idle", call("getProjectState", name));
    }

    public void testSaveConfigConfigChangeExternalState() throws Exception
    {
        String name = randomName();
        String projectPath = insertSimpleProject(name);
        Hashtable<String, Object> project = call("getConfig", projectPath);
        project.put("projectId", "1");
        callAndExpectError("Unrecognised property 'projectId'", "saveConfig", projectPath, project, false);
    }

    public void testInheritanceOfInternalReference() throws Exception
    {
        String random = randomName();
        String parentProject = random + "-parent";
        String childProject = random + "-child";

        xmlRpcHelper.insertSimpleProject(parentProject, true);
        String hookPath = xmlRpcHelper.insertPostStageHook(parentProject, "hokey", "default");
        String childPath = xmlRpcHelper.insertProject(childProject, parentProject, false, null, null);
        
        String childHookPath = hookPath.replace(parentProject, childProject);
        Hashtable<String, Object> childHook = xmlRpcHelper.getConfig(childHookPath);
        @SuppressWarnings({"unchecked"})
        Vector<String> stages = (Vector<String>) childHook.get("stages");
        assertEquals(PathUtils.getPath(childPath, Constants.Project.STAGES, "default"), stages.get(0));
    }

    public void testCanPullUp() throws Exception
    {
        String random = randomName();
        String parentProject = random + "-parent";
        String childProject = random + "-child";

        xmlRpcHelper.insertSimpleProject(parentProject, true);
        xmlRpcHelper.insertProject(childProject, parentProject, false, null, null);

        String parentPropertyPath = xmlRpcHelper.insertProjectProperty(parentProject, "pp", "foo");
        String childPropertyPath = xmlRpcHelper.insertProjectProperty(childProject, "cp", "foo");

        assertTrue(xmlRpcHelper.canPullUpConfig(childPropertyPath, parentProject));
        assertFalse(xmlRpcHelper.canPullUpConfig(parentPropertyPath.replace(parentProject, childProject), parentProject));
    }

    public void testPullUp() throws Exception
    {
        String random = randomName();
        String parentProject = random + "-parent";
        String childProject = random + "-child";

        xmlRpcHelper.insertSimpleProject(parentProject, true);
        xmlRpcHelper.insertProject(childProject, parentProject, false, null, null);

        String propertyPath = xmlRpcHelper.insertProjectProperty(childProject, "cp", "foo");
        String pulledUpPath = propertyPath.replace(childProject, parentProject);

        assertFalse(xmlRpcHelper.configPathExists(pulledUpPath));
        assertEquals(pulledUpPath, xmlRpcHelper.pullUpConfig(propertyPath, parentProject));
        assertTrue(xmlRpcHelper.configPathExists(pulledUpPath));
    }

    public void testCanPushDown() throws Exception
    {
        String random = randomName();
        String parentProject = random + "-parent";
        String childProject = random + "-child";

        xmlRpcHelper.insertSimpleProject(parentProject, true);
        xmlRpcHelper.insertProject(childProject, parentProject, false, null, null);

        String parentPropertyPath = xmlRpcHelper.insertProjectProperty(parentProject, "pp", "foo");
        String childPropertyPath = xmlRpcHelper.insertProjectProperty(childProject, "cp", "foo");

        assertTrue(xmlRpcHelper.canPushDown(parentPropertyPath, childProject));
        assertFalse(xmlRpcHelper.canPushDown(childPropertyPath, childProject));
    }

    public void testPushDown() throws Exception
    {
        String random = randomName();
        String parentProject = random + "-parent";
        String childProject = random + "-child";

        xmlRpcHelper.insertSimpleProject(parentProject, true);
        xmlRpcHelper.insertProject(childProject, parentProject, false, null, null);

        String propertyPath = xmlRpcHelper.insertProjectProperty(parentProject, "pp", "foo");
        String pushedDownToPath = propertyPath.replace(parentProject, childProject);

        assertTrue(xmlRpcHelper.configPathExists(propertyPath));
        assertEquals(new Vector<String>(asList(pushedDownToPath)), xmlRpcHelper.pushDown(propertyPath, new Vector<String>(asList(childProject))));
        assertFalse(xmlRpcHelper.configPathExists(propertyPath));
        assertTrue(xmlRpcHelper.configPathExists(pushedDownToPath));
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
}
