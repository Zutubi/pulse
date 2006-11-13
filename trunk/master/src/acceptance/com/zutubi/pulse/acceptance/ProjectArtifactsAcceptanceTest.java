package com.zutubi.pulse.acceptance;

import com.zutubi.pulse.acceptance.forms.*;
import com.zutubi.pulse.util.RandomUtils;

/**
 */
public class ProjectArtifactsAcceptanceTest extends ProjectAcceptanceTestBase
{
    private static final String ADD_LINK_ID = "project.artifact.add";

    public ProjectArtifactsAcceptanceTest()
    {
        super(Type.ANT);
    }

    public ProjectArtifactsAcceptanceTest(String name)
    {
        super(name, Type.ANT);
    }

    public void testArtifactsTableAppears()
    {
        assertProjectArtifactsTable(null);
    }

    public void testEditOutputProcessors()
    {
        clickLink("edit_command_output");
        ArtifactProcessorsForm form = new ArtifactProcessorsForm(tester, true);
        form.assertFormPresent();
        form.assertFormElements("");
        form.saveFormElements("ant,junit,make");

        assertProjectArtifactsTable(null);
        clickLink("edit_command_output");
        form.assertFormPresent();
        form.assertFormElements("ant,junit,make");
    }

    public void testEditOutputProcessorsCancel()
    {
        clickLink("edit_command_output");
        ArtifactProcessorsForm form = new ArtifactProcessorsForm(tester, true);
        form.assertFormPresent();
        form.assertFormElements("");
        form.cancelFormElements("ant,junit,make");

        assertProjectArtifactsTable(null);
        clickLink("edit_command_output");
        form.assertFormPresent();
        form.assertFormElements("");
    }

    public void testAddArtifactValidation()
    {
        clickLink(ADD_LINK_ID);
        ArtifactTypeForm type = new ArtifactTypeForm(tester);
        type.assertFormPresent();
        type.nextFormElements("", "dir");
        type.assertFormPresent();
        assertTextPresent("name is required");
    }

    public void testAddFileArtifact()
    {
        addFileArtifact(RandomUtils.randomString(5));
    }

    private void addFileArtifact(String name)
    {
        addFileArtifact(name, true);
    }

    private void addFileArtifact(String name, boolean check)
    {
        clickLink(ADD_LINK_ID);

        ArtifactTypeForm type = new ArtifactTypeForm(tester);
        type.assertFormPresent();
        type.nextFormElements(name, "file");

        FileArtifactForm file = new FileArtifactForm(tester);
        file.assertFormPresent();
        file.nextFormElements("filename", "text/plain");

        ArtifactProcessorsForm processors = new ArtifactProcessorsForm(tester, false);
        processors.nextFormElements("ant,make");

        if(check)
        {
            assertProjectArtifactsTable(new String[][]{ getArtifactRow(name, "file") });

            clickLink("edit_" + name);
            FileArtifactEditForm edit = new FileArtifactEditForm(tester);
            edit.assertFormPresent();
            edit.assertFormElements(name, "filename", "text/plain", "ant,make");
            edit.cancelFormElements(null, null, null, null);
        }
    }

    public void testAddFileArtifactValidation()
    {
        clickLink(ADD_LINK_ID);
        ArtifactTypeForm type = new ArtifactTypeForm(tester);
        type.assertFormPresent();
        type.nextFormElements("validate", "file");

        FileArtifactForm file = new FileArtifactForm(tester);
        file.assertFormPresent();
        file.nextFormElements("", "text/plain");
        file.assertFormPresent();
        assertTextPresent("file is required");
    }

    public void testEditFileArtifact()
    {
        String name = RandomUtils.randomString(5);
        addFileArtifact(name);

        clickLink("edit_" + name);
        FileArtifactEditForm form = new FileArtifactEditForm(tester);
        form.assertFormPresent();
        form.saveFormElements("edited", "edited file", "edited type", "maven");

        assertProjectArtifactsTable(new String[][]{ getArtifactRow("edited", "file")});

        clickLink("edit_edited");
        form.assertFormPresent();
        form.assertFormElements("edited", "edited file", "edited type", "maven");
    }

    public void testEditFileArtifactValidation()
    {
        String name = RandomUtils.randomString(5);
        addFileArtifact(name);

        clickLink("edit_" + name);
        FileArtifactEditForm form = new FileArtifactEditForm(tester);
        form.assertFormPresent();
        form.saveFormElements("", "", "edited type", "maven");
        form.assertFormPresent();
        assertTextPresent("name is required");
        assertTextPresent("file is required");
    }

    public void testEditFileArtifactSameName()
    {
        String name1 = RandomUtils.randomString(5);
        String name2 = RandomUtils.randomString(5);

        addFileArtifact(name1, false);
        addFileArtifact(name2, false);

        clickLink("edit_" + name1);
        FileArtifactEditForm form = new FileArtifactEditForm(tester);
        form.assertFormPresent();
        form.saveFormElements(name2, "edited file", "edited type", "maven");
        form.assertFormPresent();
        assertTextPresent(name2 + " is already being used");
    }

    public void testEditFileArtifactCancel()
    {
        String name = RandomUtils.randomString(5);
        addFileArtifact(name);

        clickLink("edit_" + name);
        FileArtifactEditForm form = new FileArtifactEditForm(tester);
        form.assertFormPresent();
        form.cancelFormElements("edited", "", "", "");
        assertProjectArtifactsTable(new String[][]{ getArtifactRow(name, "file")});
    }

    public void testAddDirectoryArtifact()
    {
        addDirectoryArtifact(RandomUtils.randomString(5));
    }

    private void addDirectoryArtifact(String name)
    {
        addDirectoryArtifact(name, true);
    }

    private void addDirectoryArtifact(String name, boolean check)
    {
        clickLink(ADD_LINK_ID);

        ArtifactTypeForm type = new ArtifactTypeForm(tester);
        type.assertFormPresent();
        type.nextFormElements(name, "dir");

        DirectoryArtifactForm file = new DirectoryArtifactForm(tester);
        file.assertFormPresent();
        file.nextFormElements("base", "include1 include2", "exclude", "text/plain");

        ArtifactProcessorsForm processors = new ArtifactProcessorsForm(tester, false);
        processors.nextFormElements("junit,make");

        if (check)
        {
            assertProjectArtifactsTable(new String[][]{ getArtifactRow(name, "directory") });

            clickLink("edit_" + name);
            DirectoryArtifactEditForm edit = new DirectoryArtifactEditForm(tester);
            edit.assertFormPresent();
            edit.assertFormElements(name, "base", "include1 include2", "exclude", "text/plain", "junit,make");
            edit.cancelFormElements("", "", "", "", "", "");
        }
    }

    public void testEditDirectoryArtifact()
    {
        String name = RandomUtils.randomString(5);
        addDirectoryArtifact(name);

        clickLink("edit_" + name);
        DirectoryArtifactEditForm form = new DirectoryArtifactEditForm(tester);
        form.assertFormPresent();
        form.saveFormElements("edited", "edited base", "edited includes", "edited excludes", "edited type", "maven");

        assertProjectArtifactsTable(new String[][]{ getArtifactRow("edited", "directory")});

        clickLink("edit_edited");
        form.assertFormPresent();
        form.assertFormElements("edited", "edited base", "edited includes", "edited excludes", "edited type", "maven");
    }

    public void testEditDirectoryArtifactSameName()
    {
        String name1 = RandomUtils.randomString(5);
        String name2 = RandomUtils.randomString(5);

        addDirectoryArtifact(name1, false);
        addDirectoryArtifact(name2, false);

        clickLink("edit_" + name1);
        DirectoryArtifactEditForm form = new DirectoryArtifactEditForm(tester);
        form.assertFormPresent();
        form.saveFormElements(name2, "edited base", "edited includes", "edited excludes", "edited type", "maven");
        form.assertFormPresent();
        assertTextPresent(name2 + " is already being used");
    }

    public void testEditDirectoryArtifactCancel()
    {
        String name = RandomUtils.randomString(5);
        addDirectoryArtifact(name);

        clickLink("edit_" + name);
        DirectoryArtifactEditForm form = new DirectoryArtifactEditForm(tester);
        form.assertFormPresent();
        form.cancelFormElements("edited", "", "", "", "", "");
        assertProjectArtifactsTable(new String[][]{ getArtifactRow(name, "directory")});
    }

    public void testAddArtifactSameName()
    {
        String name = "duplicate";
        addFileArtifact(name);

        clickLink(ADD_LINK_ID);

        ArtifactTypeForm form = new ArtifactTypeForm(tester);
        form.nextFormElements(name, "file");
        form.assertFormPresent();
        assertTextPresent(name + " is already being used");
    }

    private String[] getArtifactRow(String name, String type)
    {
        return new String[]{name, type, "edit", "delete"};
    }

    private void assertProjectArtifactsTable(String[][] rows)
    {
        assertTablePresent("project.artifacts");
        if(rows != null)
        {
            String[][] allRows = new String[rows.length + 3][4];
            allRows[0] = new String[]{"name", "type", "actions", "actions"};
            allRows[1] = new String[]{"command output", "output", "edit", ""};
            System.arraycopy(rows, 0, allRows, 2, rows.length);
            allRows[rows.length + 2] = new String[]{"add new artifact", "add new artifact", "add new artifact", "add new artifact"};
            assertTableRowsEqual("project.artifacts", 1, allRows);
        }
        else
        {
            assertTableRowsEqual("project.artifacts", 1, new String[][] {
                    new String[]{"name", "type", "actions"},
                    new String[]{"command output", "output", "edit"},
                    new String[]{"add new artifact", "add new artifact", "add new artifact"}
            });
        }
    }
}
