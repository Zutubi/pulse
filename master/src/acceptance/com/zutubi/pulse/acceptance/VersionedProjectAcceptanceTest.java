package com.zutubi.pulse.acceptance;

import com.zutubi.pulse.acceptance.forms.VersionedProjectEditForm;

/**
 */
public class VersionedProjectAcceptanceTest extends ProjectAcceptanceTestBase
{
    public VersionedProjectAcceptanceTest()
    {
        super(Type.VERSIONED);
    }

    public VersionedProjectAcceptanceTest(String name)
    {
        super(name, Type.VERSIONED);
    }

    public void testEditVersionedSpecifics()
    {
        VersionedProjectEditForm form = new VersionedProjectEditForm(tester);

        // verify what we have to start with.
        assertProjectSpecificsTable("versioned", "pulse.xml");

        assertLinkPresent("project.specifics.edit");
        clickLink("project.specifics.edit");

        // assert that the form is pre populated with the expected data.
        form.assertFormElements("pulse.xml");

        form.saveFormElements("versioned.xml");

        // assert that the data has been updated.
        assertProjectSpecificsTable("versioned", "versioned.xml");
    }

    public void testEditVersionedSpecificsValidation()
    {
        VersionedProjectEditForm form = new VersionedProjectEditForm(tester);

        assertProjectSpecificsTable("versioned", "pulse.xml");

        assertLinkPresent("project.specifics.edit");
        clickLink("project.specifics.edit");

        form.assertFormElements("pulse.xml");

        form.saveFormElements("");

        assertTextPresent("required");
        form.assertFormElements("");
    }

    public void testEditVersionedSpecificsCancel()
    {
        VersionedProjectEditForm form = new VersionedProjectEditForm(tester);

        // test the editing of versioned specifics.
        assertProjectSpecificsTable("versioned", "pulse.xml");

        assertLinkPresent("project.specifics.edit");
        clickLink("project.specifics.edit");

        form.assertFormElements("pulse.xml");

        form.cancelFormElements("versioned.xml");

        assertProjectSpecificsTable("versioned", "pulse.xml");
    }

    private void assertProjectSpecificsTable(String type, String file)
    {
        assertTablePresent("project.specifics");
        assertTableRowsEqual("project.specifics", 1, new String[][]{
                new String[]{"type", type},
                new String[]{"pulse file", file}
        });
    }    
}
