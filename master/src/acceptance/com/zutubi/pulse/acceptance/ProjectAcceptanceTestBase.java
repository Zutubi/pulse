package com.zutubi.pulse.acceptance;

import com.zutubi.pulse.util.RandomUtils;

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
        loginAsAdmin();

        // navigate to the create project wizard.
        // fill in the form details.
        clickLink(Navigation.TAB_PROJECTS);
        assertAndClick(Navigation.Projects.LINK_ADD_PROJECT);

        projectName = "project " + RandomUtils.randomString(5);
        submitProjectBasicsForm(projectName, DESCRIPTION, URL, "cvs", type.toString().toLowerCase());
        submitCvsSetupForm(TEST_CVSROOT, "module", "", "");

        if(type == Type.ANT)
        {
            submitAntSetupForm();
        }
        else
        {
            submitVersionedSetupForm("pulse.xml");
        }

        assertTablePresent("project.basics");
    }
}
