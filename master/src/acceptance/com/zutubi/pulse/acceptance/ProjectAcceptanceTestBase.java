package com.zutubi.pulse.acceptance;

import com.zutubi.pulse.util.RandomUtils;

import java.util.Hashtable;

/**
 */
public class ProjectAcceptanceTestBase extends BaseAcceptanceTestCase
{
    protected enum Type
    {
        ANT,
        VERSIONED
    }

    protected static final String DESCRIPTION = "test description";
    protected static final String URL = "http://test/url";

    protected String projectName;
    protected Type type;

    public ProjectAcceptanceTestBase(Type type)
    {
        this.type = type;
    }

    public ProjectAcceptanceTestBase(String name, Type type)
    {
        super(name);
        this.type = type;
    }

    protected void setUp() throws Exception
    {
        super.setUp();

        // create project that will be used during this set of tests.
        projectName = "project-" + RandomUtils.randomString(5);

        Hashtable<String, Object> projectDetails = new Hashtable<String, Object>();
        projectDetails.put("name", projectName);
        projectDetails.put("description", DESCRIPTION);
        projectDetails.put("url", URL);

        Hashtable<String, Object> scmDetails = new Hashtable<String, Object>();
        scmDetails.put("type", "cvs");
        scmDetails.put("root", TEST_CVSROOT);
        scmDetails.put("module", "module");
        scmDetails.put("monitor", "false");

        Hashtable<String, Object> typeDetails = new Hashtable<String, Object>();
        if(type == Type.ANT)
        {
            typeDetails.put("type", "ant");
            typeDetails.put("buildFile", "build.xml");
        }
        else
        {
            typeDetails.put("type", "versioned");
            typeDetails.put("pulseFileName", "pulse.xml");
        }
        Object result = callRemoteApi("createProject", projectDetails, scmDetails, typeDetails);
        assertEquals(Boolean.TRUE, result);

        loginAsAdmin();

        // navigate to the create project wizard.
        // fill in the form details.
        beginAt(Navigation.Projects.ACTION_PROJECT_CONFIG + "?projectName=" + projectName);
        assertTablePresent("project.basics");
    }
}
