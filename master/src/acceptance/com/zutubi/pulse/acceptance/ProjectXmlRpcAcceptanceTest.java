package com.zutubi.pulse.acceptance;

import org.apache.xmlrpc.XmlRpcException;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Vector;
import java.util.List;
import java.util.LinkedList;
import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.beans.IntrospectionException;

import com.zutubi.pulse.util.RandomUtils;
import com.zutubi.pulse.model.Project;

/**
 * <class comment/>
 */
public class ProjectXmlRpcAcceptanceTest extends BaseXmlRpcAcceptanceTest
{
    private String adminToken;

    public ProjectXmlRpcAcceptanceTest()
    {
    }

    public ProjectXmlRpcAcceptanceTest(String testName)
    {
        super(testName);
    }

    protected void setUp() throws Exception
    {
        super.setUp();

        adminToken = (String) xmlRpcClient.execute("RemoteApi.login", getVector("admin", "admin"));
    }

    protected void tearDown() throws Exception
    {
        adminToken = null;

        super.tearDown();
    }

    public void testCreateProject() throws IOException, XmlRpcException
    {
        String project = String.format("project-%s", RandomUtils.randomString(4));

        Hashtable<String, Object> projectDetails = new Hashtable<String, Object>();
        projectDetails.put("name", project);

        Hashtable<String, Object> scmDetails = new Hashtable<String, Object>();
        scmDetails.put("type", "cvs");
        scmDetails.put("root", ":pserser:someone@localhost:/cvsroot");
        scmDetails.put("module", "project");

        Hashtable<String, Object> typeDetails = new Hashtable<String, Object>();
        typeDetails.put("type", "ant");
        typeDetails.put("buildFile", "build.xml");

        Object result = xmlRpcClient.execute("RemoteApi.createProject", getVector(adminToken, projectDetails, scmDetails, typeDetails));
        assertEquals(Boolean.TRUE, result);
    }

    public void testDeleteProject() throws IOException, XmlRpcException
    {
        Object result = xmlRpcClient.execute("RemoteApi.getAllProjectNames", getVector(adminToken));
        assertNotNull(result);

        String projectName;
        Vector<String> projectNames = (Vector<String>) result;
        if (projectNames.size() == 0)
        {
            projectName = createProject();
        }
        else
        {
            projectName = projectNames.get(0);
        }

        result = xmlRpcClient.execute("RemoteApi.deleteProject", getVector(adminToken, projectName));
        assertEquals(Boolean.TRUE, result);
    }

    public void testEditProject() throws IOException, XmlRpcException
    {
        // create a project with a specific name.
        String name = createProject();

        String newName = String.format("project-%s", RandomUtils.randomString(4));
        
        Hashtable<String, Object> projectDetails = new Hashtable<String, Object>();
        projectDetails.put("name", newName);

        Object result = xmlRpcClient.execute("RemoteApi.editProject", getVector(adminToken, name, projectDetails));
        assertEquals(Boolean.TRUE, result);
    }

    public void testGetProject() throws IOException, XmlRpcException, IntrospectionException
    {
        // create a project with a specific name.
        String projectName = createProject();

        Object result = xmlRpcClient.execute("RemoteApi.getProject", getVector(adminToken, projectName));
        assertNotNull(result);

        Hashtable<String, Object> details = (Hashtable<String, Object>) result;

        // check that the necessary entries exist.
        for (String name : getExpectedProjectKeys())
        {
            assertTrue(details.containsKey(name));
            details.remove(name);
        }
        assertEquals(0, details.size());
    }

    private List<String> getExpectedProjectKeys() throws IntrospectionException
    {
        List<String> propertyNames = new LinkedList<String>();

        BeanInfo beanInfo = Introspector.getBeanInfo(Project.class, Object.class);

        // Handle the first pass analysis.  Here, all of the fields are considered on an individual basis.
        for (PropertyDescriptor pd : beanInfo.getPropertyDescriptors())
        {
            if (pd.getReadMethod() != null && pd.getWriteMethod() != null)
            {
                propertyNames.add(pd.getName());
            }
        }
        // currently, these are the project properties that we do not include in the project xml rpc
        // structure.
        propertyNames.remove("scm");
        propertyNames.remove("pulseFileDetails");
        propertyNames.remove("nextBuildNumber");
        propertyNames.remove("id");
        return propertyNames;
    }

    private String createProject() throws IOException, XmlRpcException
    {
        String project = String.format("project-%s", RandomUtils.randomString(4));

        Hashtable<String, Object> projectDetails = new Hashtable<String, Object>();
        projectDetails.put("name", project);

        Hashtable<String, Object> scmDetails = new Hashtable<String, Object>();
        scmDetails.put("type", "cvs");
        scmDetails.put("root", ":pserser:someone@localhost:/cvsroot");
        scmDetails.put("module", "project");

        Hashtable<String, Object> typeDetails = new Hashtable<String, Object>();
        typeDetails.put("type", "ant");
        typeDetails.put("buildFile", "build.xml");

        Object result = xmlRpcClient.execute("RemoteApi.createProject", getVector(adminToken, projectDetails, scmDetails, typeDetails));
        assertEquals(Boolean.TRUE, result);
        
        return project;
    }
}
