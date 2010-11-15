package com.zutubi.pulse.acceptance;

import com.zutubi.pulse.core.config.ResourceConfiguration;
import com.zutubi.pulse.core.config.ResourceVersionConfiguration;
import com.zutubi.pulse.master.tove.config.project.ResourceRequirementConfiguration;
import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.util.RandomUtils;
import com.zutubi.util.StringUtils;

import java.util.Hashtable;

/**
 * Test for agent resources and project resource requirements.
 */
public class ResourceXmlRpcAcceptanceTest extends AcceptanceTestBase
{
    private String random;
    private String resourcesPath;

    public void setUp() throws Exception
    {
        super.setUp();
        random = RandomUtils.randomString(10);
        xmlRpcHelper.loginAsAdmin();
        String agentPath = xmlRpcHelper.ensureAgent("localhost");
        resourcesPath = PathUtils.getPath(agentPath, "resources");
    }

    protected void tearDown() throws Exception
    {
        xmlRpcHelper.logout();
        super.tearDown();
    }

    public void testRenameResource() throws Exception
    {
        String resourceName = random;
        String resourceName2 = random + "2";
        String resourcePath = insertResource(resourceName);
        insertResource(resourceName2);

        String projectPath = xmlRpcHelper.insertTrivialProject(random, false);
        String requirementPath = insertRequirement(projectPath, resourceName, null);
        String requirementPath2 = insertRequirement(projectPath, resourceName2, null);

        Hashtable<String, Object> resource = xmlRpcHelper.getConfig(resourcePath);
        String editedName = random + "-edited";
        resource.put("name", editedName);
        xmlRpcHelper.saveConfig(resourcePath,resource, false);
        
        Hashtable<String, Object> requirement = xmlRpcHelper.getConfig(requirementPath);
        assertEquals(editedName, requirement.get("resource"));
        requirement = xmlRpcHelper.getConfig(requirementPath2);
        assertEquals(resourceName2, requirement.get("resource"));
    }

    public void testRenameResourceVersion() throws Exception
    {
        String resourceName = random;
        String resourceName2 = random + "2";
        String versionName = "v1";
        String versionName2 = "v2";
        insertResource(resourceName);
        insertResource(resourceName2);
        String versionPath = insertVersion(resourceName, versionName);
        insertVersion(resourceName, versionName2);
        insertVersion(resourceName2, versionName);

        String projectPath = xmlRpcHelper.insertTrivialProject(random, false);
        String requirementPath = insertRequirement(projectPath, resourceName, versionName);
        String requirementPath2 = insertRequirement(projectPath, resourceName, versionName2);
        String requirement2Path = insertRequirement(projectPath, resourceName2, versionName);

        Hashtable<String, Object> version = xmlRpcHelper.getConfig(versionPath);
        String editedVersion = "v1-edited";
        version.put("value", editedVersion);
        xmlRpcHelper.saveConfig(versionPath, version, false);

        Hashtable<String, Object> requirement = xmlRpcHelper.getConfig(requirementPath);
        assertEquals(editedVersion, requirement.get("version"));
        requirement = xmlRpcHelper.getConfig(requirementPath2);
        assertEquals(versionName2, requirement.get("version"));
        requirement = xmlRpcHelper.getConfig(requirement2Path);
        assertEquals(versionName, requirement.get("version"));
    }

    private String insertVersion(String resourceName, String versionName) throws Exception
    {
        Hashtable<String, Object> version = xmlRpcHelper.createDefaultConfig(ResourceVersionConfiguration.class);
        version.put("value", versionName);
        return xmlRpcHelper.insertConfig(PathUtils.getPath(resourcesPath, resourceName, "versions"), version);
    }

    private String insertResource(String resourceName) throws Exception
    {
        Hashtable<String, Object> resource = xmlRpcHelper.createDefaultConfig(ResourceConfiguration.class);
        resource.put("name", resourceName);
        return xmlRpcHelper.insertConfig(resourcesPath, resource);
    }

    private String insertRequirement(String projectPath, String resourceName, String version) throws Exception
    {
        String requirementsPath = PathUtils.getPath(projectPath, "requirements");
        Hashtable<String, Object> requirement = xmlRpcHelper.createDefaultConfig(ResourceRequirementConfiguration.class);
        requirement.put("resource", resourceName);
        if (StringUtils.stringSet(version))
        {
            requirement.put("version", version);
            requirement.put("defaultVersion", false);
        }
        return xmlRpcHelper.insertConfig(requirementsPath, requirement);
    }
}
