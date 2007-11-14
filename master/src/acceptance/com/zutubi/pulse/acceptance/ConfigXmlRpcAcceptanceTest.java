package com.zutubi.pulse.acceptance;

import com.zutubi.prototype.type.record.PathUtils;
import com.zutubi.util.RandomUtils;
import com.zutubi.util.Sort;

import java.util.*;

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
        assertSortedEquals(listing, "emailConfig", "generalConfig", "jabberConfig", "ldapConfig", "licenseConfig", "loggingConfig");
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

    public void testGetConfig() throws Exception
    {
        Hashtable<String, Object> emailConfig = new Hashtable<String, Object>();
        emailConfig.put(SYMBOLIC_NAME_KEY, "zutubi.emailConfig");
        emailConfig.put("host", "localhost");
        emailConfig.put("from", "pulse@zutubi.com");
        emailConfig.put("ssl", true);
        call("saveConfig", "settings/emailConfig", emailConfig, false);

        emailConfig = call("getConfig", "settings/emailConfig");
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
        Hashtable<String, Object> emailConfig = call("getRawConfig", "settings/emailConfig");
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
        callAndExpectError("record already exists", "insertConfig", "settings/emailConfig", config);
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
        callAndExpectError("Expected type: class com.zutubi.pulse.prototype.config.project.BuildStageConfiguration", "insertConfig", TEST_PROJECT_PATH + "/stages", config);
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
        callAndExpectError("parent path 'settings' is not a templated collection", "insertTemplatedConfig", "settings/emailConfig", new Hashtable(), false);
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
        assertEquals(1, ((Hashtable<String, Object>) loadedProject.get("stages")).size());
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
        properties.put("p1", createProperty("p1", "v1"));
        properties.put("p2", createProperty("p2", "v2"));
        call("saveConfig", path, project, true);

        Hashtable<String, Object> loadedProperties = call("getConfig", path + "/properties");
        assertEquals(2, loadedProperties.size());

        assertEquals(2, call("deleteAllConfigs", path + "/properties/*"));
        loadedProperties = call("getConfig", path + "/properties");
        assertEquals(0, loadedProperties.size());
    }

    public void testRestore() throws Exception
    {
        String random = RandomUtils.randomString(10);
        String parentName = random + "-parent";
        String childName = random + "-child";
        helper.insertSimpleProject(parentName, true);
        String childPath = helper.insertSimpleProject(childName, parentName, false);

        String stagesPath = PathUtils.getPath(childPath, "stages");
        String hidePath = PathUtils.getPath(stagesPath, "default");
        helper.deleteConfig(hidePath);
        assertEquals(0, helper.getConfigListing(stagesPath).size());
        helper.restoreConfig(hidePath);
        Vector<String> listing = helper.getConfigListing(stagesPath);
        assertEquals(1, listing.size());
        assertEquals("default", listing.get(0));
    }

    public void testSetOrder() throws Exception
    {
        String random = RandomUtils.randomString(10);
        String path = helper.insertSimpleProject(random, true);
        String propertiesPath = PathUtils.getPath(path, "properties");
        Hashtable<String, Object> p1 = createProperty("p1", "v1");
        Hashtable<String, Object> p2 = createProperty("p2", "v2");
        helper.insertConfig(propertiesPath, p1);
        helper.insertConfig(propertiesPath, p2);

        assertEquals(Arrays.asList("p1", "p2"), new LinkedList<String>(helper.getConfigListing(propertiesPath)));
        helper.setConfigOrder(propertiesPath, "p2", "p1");
        assertEquals(Arrays.asList("p2", "p1"), new LinkedList<String>(helper.getConfigListing(propertiesPath)));
    }

    private Hashtable<String, Object> createProperty(String name, String value)
    {
        Hashtable<String, Object> property = new Hashtable<String, Object>();
        property.put(SYMBOLIC_NAME_KEY, "zutubi.resourceProperty");
        property.put("name", name);
        property.put("value", value);
        return property;
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
