package com.zutubi.pulse.acceptance;

import com.zutubi.pulse.master.model.ProjectManager;
import com.zutubi.pulse.master.tove.config.MasterConfigurationRegistry;
import com.zutubi.pulse.master.tove.config.agent.AgentConfigurationActions;
import com.zutubi.pulse.master.tove.config.project.ProjectAclConfiguration;
import com.zutubi.pulse.master.tove.config.user.SetPasswordConfiguration;
import com.zutubi.pulse.master.tove.config.user.UserConfiguration;
import com.zutubi.pulse.master.tove.config.user.UserConfigurationActions;
import com.zutubi.tove.security.AccessManager;
import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.util.Sort;

import java.util.*;

import static com.zutubi.tove.type.record.PathUtils.WILDCARD_ANY_ELEMENT;
import static com.zutubi.tove.type.record.PathUtils.getPath;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

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
        assertEquals(false, call("canCloneConfig", getPath(MasterConfigurationRegistry.PROJECTS_SCOPE, ProjectManager.GLOBAL_PROJECT_NAME)));
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
        assertTrue(xmlRpcHelper.configPathExists(getPath(MasterConfigurationRegistry.PROJECTS_SCOPE, cloneName)));
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

        String stagesPath = getPath(childPath, "stages");
        String hidePath = getPath(stagesPath, "default");
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
        String propertiesPath = getPath(path, "properties");
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
        assertTrue(xmlRpcHelper.isConfigPermanent(getPath(path, "preferences")));
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
        assertEquals(getPath(childPath, Constants.Project.STAGES, "default"), stages.get(0));
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

        assertTrue(xmlRpcHelper.canPushDownConfig(parentPropertyPath, childProject));
        assertFalse(xmlRpcHelper.canPushDownConfig(childPropertyPath, childProject));
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
        assertEquals(new Vector<String>(asList(pushedDownToPath)), xmlRpcHelper.pushDownConfig(propertyPath, new Vector<String>(asList(childProject))));
        assertFalse(xmlRpcHelper.configPathExists(propertyPath));
        assertTrue(xmlRpcHelper.configPathExists(pushedDownToPath));
    }

    public void testPreviewMoveConfig() throws Exception
    {
        String random = randomName();
        String childProject = random + "-child";
        String newTemplateParentProject = random + "-new-parent";

        String childProjectPath = xmlRpcHelper.insertProject(childProject, ProjectManager.GLOBAL_PROJECT_NAME, false, xmlRpcHelper.getGitConfig(Constants.getGitUrl()), xmlRpcHelper.createVersionedConfig("path"));
        String newTemplateParentProjectPath = xmlRpcHelper.insertSimpleProject(newTemplateParentProject, true);

        Hashtable<String, Object> result = xmlRpcHelper.previewMoveConfig(childProjectPath, newTemplateParentProject);
        
        String scmPath = getPath(childProjectPath, "scm");
        String typePath = getPath(childProjectPath, "type");
        checkExpectedDeletedPaths(result, scmPath, typePath);
        
        // Make sure no changes were made.
        assertEquals(ProjectManager.GLOBAL_PROJECT_NAME, xmlRpcHelper.getTemplateParent(childProjectPath));
        assertTrue(xmlRpcHelper.configPathExists(scmPath));
        assertTrue(xmlRpcHelper.configPathExists(typePath));
    }

    public void testMoveConfig() throws Exception
    {
        String random = randomName();
        String childProject = random + "-child";
        String newTemplateParentProject = random + "-new-parent";

        String childProjectPath = xmlRpcHelper.insertProject(childProject, ProjectManager.GLOBAL_PROJECT_NAME, false, xmlRpcHelper.getGitConfig(Constants.getGitUrl()), xmlRpcHelper.createVersionedConfig("path"));
        String newTemplateParentProjectPath = xmlRpcHelper.insertSimpleProject(newTemplateParentProject, true);
        xmlRpcHelper.insertProjectProperty(newTemplateParentProject, "prop", "val");

        Hashtable<String, Object> result = xmlRpcHelper.moveConfig(childProjectPath, newTemplateParentProject);
        
        String scmPath = getPath(childProjectPath, "scm");
        String typePath = getPath(childProjectPath, "type");
        checkExpectedDeletedPaths(result, scmPath, typePath);
        
        assertEquals(newTemplateParentProject, xmlRpcHelper.getTemplateParent(childProjectPath));
        // These paths were incompatible, so should have changed to the new
        // parent's type.
        checkPathType(scmPath, "zutubi.subversionConfig");
        checkPathType(typePath, "zutubi.multiRecipeTypeConfig");
        // This path should have been newly-added from the new parent.
        assertTrue(xmlRpcHelper.configPathExists(getPath(childProjectPath, "properties", "prop")));
    }

    public void testMoveConfigWithSubtree() throws Exception
    {
        String random = randomName();
        String childProject = random + "-child";
        String grandchild1Project = random + "-grandchild1";
        String grandchild2Project = random + "-grandchild2";
        String newTemplateParentProject = random + "-new-parent";

        String childProjectPath = xmlRpcHelper.insertTrivialProject(childProject, true);
        String grandchild1ProjectPath = xmlRpcHelper.insertSimpleProject(grandchild1Project, childProject, false);
        String grandchild2ProjectPath = xmlRpcHelper.insertProject(grandchild2Project, childProject, false, xmlRpcHelper.getSubversionConfig(Constants.FAIL_ANT_REPOSITORY), xmlRpcHelper.createVersionedConfig("path"));
        String newTemplateParentProjectPath = xmlRpcHelper.insertSimpleProject(newTemplateParentProject, true);
        xmlRpcHelper.insertProjectProperty(newTemplateParentProject, "prop", "val");

        Hashtable<String, Object> result = xmlRpcHelper.moveConfig(childProjectPath, newTemplateParentProject);
        
        String grandhchild2TypePath = getPath(grandchild2ProjectPath, "type");
        checkExpectedDeletedPaths(result, grandhchild2TypePath);
        
        assertEquals(newTemplateParentProject, xmlRpcHelper.getTemplateParent(childProjectPath));
        
        // Override of Subversion URL should be maintained.
        Hashtable<String, Object> grandchild2Scm = xmlRpcHelper.getConfig(getPath(grandchild2ProjectPath, "scm"));
        assertEquals(Constants.FAIL_ANT_REPOSITORY, grandchild2Scm.get("url"));
        
        // Incompatible, deleted.
        checkPathType(grandhchild2TypePath, "zutubi.multiRecipeTypeConfig");
        
        // This path should have been newly-added from the new parent.
        assertTrue(xmlRpcHelper.configPathExists(getPath(childProjectPath, "properties", "prop")));
        assertTrue(xmlRpcHelper.configPathExists(getPath(grandchild1ProjectPath, "properties", "prop")));
        assertTrue(xmlRpcHelper.configPathExists(getPath(grandchild2ProjectPath, "properties", "prop")));
    }
    
    public void testMoveConfigJustEnoughPermissions() throws Exception
    {
        String random = randomName();
        String user = random + "-user";
        String group = random + "-group";
        String childProject = random + "-child";
        String newTemplateParentProject = random + "-new-parent";

        String childProjectPath = xmlRpcHelper.insertSimpleProject(childProject, false);
        String newTemplateParentProjectPath = xmlRpcHelper.insertSimpleProject(newTemplateParentProject, true);

        String userPath = xmlRpcHelper.insertTrivialUser(user);
        String groupPath = xmlRpcHelper.insertGroup(group, asList(userPath));
        Hashtable<String, Object> acl = xmlRpcHelper.createDefaultConfig(ProjectAclConfiguration.class);
        acl.put("group", groupPath);
        acl.put("allowedActions", new Vector<String>(asList(AccessManager.ACTION_WRITE)));
        xmlRpcHelper.insertConfig(PathUtils.getPath(childProjectPath, "permissions"), acl);
        
        xmlRpcHelper.logout();
        xmlRpcHelper.login(user, "");
        xmlRpcHelper.moveConfig(childProjectPath, newTemplateParentProject);
    }
    
    public void testMoveConfigNoWritePermissionForPath() throws Exception
    {
        String random = randomName();
        String user = random + "-user";
        String childProject = random + "-child";
        String newTemplateParentProject = random + "-new-parent";

        String childProjectPath = xmlRpcHelper.insertSimpleProject(childProject, false);
        String newTemplateParentProjectPath = xmlRpcHelper.insertSimpleProject(newTemplateParentProject, true);

        xmlRpcHelper.insertTrivialUser(user);
        xmlRpcHelper.logout();
        xmlRpcHelper.login(user, "");
        try
        {
            xmlRpcHelper.moveConfig(childProjectPath, newTemplateParentProject);
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

        String childProjectPath = xmlRpcHelper.insertSimpleProject(childProject, true);
        String grandChildProjectPath = xmlRpcHelper.insertTrivialProject(grandChildProject, childProject, false);
        String newTemplateParentProjectPath = xmlRpcHelper.insertSimpleProject(newTemplateParentProject, true);

        String userPath = xmlRpcHelper.insertTrivialUser(user);
        String groupPath = xmlRpcHelper.insertGroup(group, asList(userPath));
        Hashtable<String, Object> acl = xmlRpcHelper.createDefaultConfig(ProjectAclConfiguration.class);
        acl.put("group", groupPath);
        acl.put("allowedActions", new Vector<String>(asList(AccessManager.ACTION_WRITE)));
        xmlRpcHelper.insertConfig(PathUtils.getPath(childProjectPath, "permissions"), acl);

        xmlRpcHelper.deleteAllConfigs(getPath(grandChildProjectPath, "permissions", WILDCARD_ANY_ELEMENT));
        
        xmlRpcHelper.logout();
        xmlRpcHelper.login(user, "");
        try
        {
            xmlRpcHelper.moveConfig(childProjectPath, newTemplateParentProject);
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
        
        String childProjectPath = xmlRpcHelper.insertSimpleProject(childProject, false);
        String newTemplateParentProjectPath = xmlRpcHelper.insertSimpleProject(newTemplateParentProject, true);

        xmlRpcHelper.deleteAllConfigs(getPath(newTemplateParentProjectPath, "permissions", WILDCARD_ANY_ELEMENT));
        
        xmlRpcHelper.insertTrivialUser(user);
        xmlRpcHelper.logout();
        xmlRpcHelper.login(user, "");
        try
        {
            xmlRpcHelper.moveConfig(childProjectPath, newTemplateParentProject);
            fail("Should not be able to move to new template parent that we cannot view");
        }
        catch (Exception e)
        {
            assertThat(e.getMessage(), containsString("Permission to view at path '" + newTemplateParentProjectPath + "' denied"));            
        }
    }

    private void checkPathType(String path, String expectedType) throws Exception
    {
        Hashtable<String, Object> config = xmlRpcHelper.getConfig(path);
        assertEquals(expectedType, config.get("meta.symbolicName"));
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

        String projectPath = xmlRpcHelper.insertSimpleProject(randomName(), template);
        String stagePath = getPath(projectPath, Constants.Project.STAGES, "default");
        Hashtable<String, Object> stage = xmlRpcHelper.getConfig(stagePath);
        stage.put(Constants.Project.Stage.RECIPE, ORIGINAL_NAME);
        xmlRpcHelper.saveConfig(stagePath, stage, false);


        String typePath = renameRecipe(projectPath, ORIGINAL_NAME, NEW_NAME);


        Hashtable<String, Object> type = xmlRpcHelper.getConfig(typePath);
        assertEquals(NEW_NAME, type.get(Constants.Project.MultiRecipeType.DEFAULT_RECIPE));

        stage = xmlRpcHelper.getConfig(stagePath);
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
        String parentPath = xmlRpcHelper.insertSimpleProject(parentName, true);
        String childPath = xmlRpcHelper.insertSimpleProject(childName, parentName, false);

        String parentDefaultStagePath = getPath(parentPath, Constants.Project.STAGES, STAGE_DEFAULT);
        Hashtable<String, Object> parentDefaultStage = xmlRpcHelper.getConfig(parentDefaultStagePath);
        parentDefaultStage.put(Constants.Project.Stage.RECIPE, ORIGINAL_NAME);
        xmlRpcHelper.saveConfig(parentDefaultStagePath, parentDefaultStage, false);

        String childTypePath = getPath(childPath, Constants.Project.TYPE);
        Hashtable<String, Object> childType = xmlRpcHelper.getConfig(childTypePath);
        childType.put(Constants.Project.MultiRecipeType.DEFAULT_RECIPE, OTHER_NAME);
        xmlRpcHelper.saveConfig(childTypePath, childType, false);
        String childStagesPath = getPath(childPath, Constants.Project.STAGES);
        Hashtable<String, String> keyMap = new Hashtable<String, String>();
        keyMap.put(STAGE_DEFAULT, STAGE_OTHER);
        xmlRpcHelper.cloneConfig(childStagesPath, keyMap);


        String parentTypePath = renameRecipe(parentPath, ORIGINAL_NAME, NEW_NAME);


        // Parent references (default recipe, stage) both updated.
        Hashtable<String, Object> parentType = xmlRpcHelper.getConfig(parentTypePath);
        assertEquals(NEW_NAME, parentType.get(Constants.Project.MultiRecipeType.DEFAULT_RECIPE));

        parentDefaultStage = xmlRpcHelper.getConfig(parentDefaultStagePath);
        assertEquals(NEW_NAME, parentDefaultStage.get(Constants.Project.Stage.RECIPE));

        // Overridden child default recipe unchanged.
        childType = xmlRpcHelper.getConfig(childTypePath);
        assertEquals(OTHER_NAME, childType.get(Constants.Project.MultiRecipeType.DEFAULT_RECIPE));
        
        // Both child stages (one inherited, other local) updated.
        Hashtable<String, Object> childStage = xmlRpcHelper.getConfig(getPath(childStagesPath, STAGE_DEFAULT));
        assertEquals(NEW_NAME, childStage.get(Constants.Project.Stage.RECIPE));
        childStage = xmlRpcHelper.getConfig(getPath(childStagesPath, STAGE_OTHER));
        assertEquals(NEW_NAME, childStage.get(Constants.Project.Stage.RECIPE));
    }

    private String renameRecipe(String projectPath, String originalName, String newName) throws Exception
    {
        String typePath = getPath(projectPath, Constants.Project.TYPE);
        String recipePath = getPath(typePath, Constants.Project.MultiRecipeType.RECIPES, originalName);
        Hashtable<String, Object> recipe = xmlRpcHelper.getConfig(recipePath);
        recipe.put(Constants.Project.MultiRecipeType.Recipe.NAME, newName);
        xmlRpcHelper.saveConfig(recipePath, recipe, false);
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
}
