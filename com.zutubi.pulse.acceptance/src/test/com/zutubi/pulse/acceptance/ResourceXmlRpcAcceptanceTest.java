package com.zutubi.pulse.acceptance;

import com.zutubi.pulse.core.resources.api.ResourceConfiguration;
import com.zutubi.pulse.core.resources.api.ResourceVersionConfiguration;
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
        random = RandomUtils.insecureRandomString(10);
        rpcClient.loginAsAdmin();
        String agentPath = rpcClient.RemoteApi.ensureAgent("localhost");
        resourcesPath = PathUtils.getPath(agentPath, "resources");
    }

    protected void tearDown() throws Exception
    {
        rpcClient.logout();
        super.tearDown();
    }

    public void testRenameResource() throws Exception
    {
        String resourceName = random;
        String resourceName2 = random + "2";
        String resourcePath = insertResource(resourceName);
        insertResource(resourceName2);

        String projectPath = rpcClient.RemoteApi.insertTrivialProject(random, false);
        String requirementPath = insertRequirement(projectPath, resourceName, null);
        String requirementPath2 = insertRequirement(projectPath, resourceName2, null);

        Hashtable<String, Object> resource = rpcClient.RemoteApi.getConfig(resourcePath);
        String editedName = random + "-edited";
        resource.put("name", editedName);
        rpcClient.RemoteApi.saveConfig(resourcePath,resource, false);
        
        Hashtable<String, Object> requirement = rpcClient.RemoteApi.getConfig(requirementPath);
        assertEquals(editedName, requirement.get("resource"));
        requirement = rpcClient.RemoteApi.getConfig(requirementPath2);
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

        String projectPath = rpcClient.RemoteApi.insertTrivialProject(random, false);
        String requirementPath = insertRequirement(projectPath, resourceName, versionName);
        String requirementPath2 = insertRequirement(projectPath, resourceName, versionName2);
        String requirement2Path = insertRequirement(projectPath, resourceName2, versionName);

        Hashtable<String, Object> version = rpcClient.RemoteApi.getConfig(versionPath);
        String editedVersion = "v1-edited";
        version.put("value", editedVersion);
        rpcClient.RemoteApi.saveConfig(versionPath, version, false);

        Hashtable<String, Object> requirement = rpcClient.RemoteApi.getConfig(requirementPath);
        assertEquals(editedVersion, requirement.get("version"));
        requirement = rpcClient.RemoteApi.getConfig(requirementPath2);
        assertEquals(versionName2, requirement.get("version"));
        requirement = rpcClient.RemoteApi.getConfig(requirement2Path);
        assertEquals(versionName, requirement.get("version"));
    }

    private String insertVersion(String resourceName, String versionName) throws Exception
    {
        Hashtable<String, Object> version = rpcClient.RemoteApi.createDefaultConfig(ResourceVersionConfiguration.class);
        version.put("value", versionName);
        return rpcClient.RemoteApi.insertConfig(PathUtils.getPath(resourcesPath, resourceName, "versions"), version);
    }

    private String insertResource(String resourceName) throws Exception
    {
        Hashtable<String, Object> resource = rpcClient.RemoteApi.createDefaultConfig(ResourceConfiguration.class);
        resource.put("name", resourceName);
        return rpcClient.RemoteApi.insertConfig(resourcesPath, resource);
    }

    private String insertRequirement(String projectPath, String resourceName, String version) throws Exception
    {
        String requirementsPath = PathUtils.getPath(projectPath, "requirements");
        Hashtable<String, Object> requirement = rpcClient.RemoteApi.createDefaultConfig(ResourceRequirementConfiguration.class);
        requirement.put("resource", resourceName);
        if (StringUtils.stringSet(version))
        {
            requirement.put("version", version);
            requirement.put("defaultVersion", false);
        }
        return rpcClient.RemoteApi.insertConfig(requirementsPath, requirement);
    }
}
