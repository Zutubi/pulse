package com.zutubi.pulse.acceptance;

import com.zutubi.pulse.acceptance.forms.CommitMessageLinkForm;

/**
 * <class-comment/>
 */
public class CommitMessageLinkAcceptanceTest extends BaseAcceptanceTestCase
{
    public CommitMessageLinkAcceptanceTest()
    {
    }

    public CommitMessageLinkAcceptanceTest(String name)
    {
        super(name);
    }

    protected void setUp() throws Exception
    {
        super.setUp();

        callRemoteApi("deleteAllCommitMessageLinks");
        loginAsAdmin();
        beginAt("/");
        ensureProject("cml1");
        ensureProject("cml2");
        clickLink(Navigation.TAB_ADMINISTRATION);
    }

    protected void tearDown() throws Exception
    {
        super.tearDown();
    }

    public void testAddCommitLink() throws Exception
    {
        assertCommitLinks(null);
        String id = addLink("mylink");

        assertCommitLinks(new String[][] { getLinkRow("mylink", "expr") });
        CommitMessageLinkForm editForm = new CommitMessageLinkForm(tester, false);
        assertAndClick("edit_mylink");
        editForm.assertFormPresent();
        editForm.assertFormElements("mylink", "expr", "repl", id);
    }
    
    public void testCancelAddCommitLink() throws Exception
    {
        assertCommitLinks(null);
        clickLink("commit.message.link.add");
        CommitMessageLinkForm form = new CommitMessageLinkForm(tester, true);
        form.cancelFormElements("link", "expr", "repl", null);
        assertCommitLinks(null);
    }

    public void testAddCommitLinkNoProjects() throws Exception
    {
        assertAndClick("commit.message.link.add");
        CommitMessageLinkForm addForm = new CommitMessageLinkForm(tester, true);
        addForm.assertFormPresent();
        addForm.saveFormElements("mylink", "expr", "repl", "");

        assertCommitLinks(new String[][] { getLinkRow("mylink", "expr") });
        CommitMessageLinkForm editForm = new CommitMessageLinkForm(tester, false);
        assertAndClick("edit_mylink");
        editForm.assertFormPresent();
        editForm.assertFormElements("mylink", "expr", "repl", "");
    }

    private String addLink(String name)
    {
        assertAndClick("commit.message.link.add");
        CommitMessageLinkForm addForm = new CommitMessageLinkForm(tester, true);
        addForm.assertFormPresent();
        String id = tester.getDialog().getValueForOption("selectedProjects", "cml1");
        addForm.saveFormElements(name, "expr", "repl", id);
        return id;
    }

    public void testAddCommitLinkValidation() throws Exception
    {
        assertAndClick("commit.message.link.add");
        CommitMessageLinkForm addForm = new CommitMessageLinkForm(tester, true);
        addForm.assertFormPresent();
        addForm.saveFormElements("", "", "", null);
        addForm.assertFormPresent();
        assertTextPresent("name is required");
        assertTextPresent("expression is required");
        assertTextPresent("url is required");

        addForm.saveFormElements("name", "(invalid", "url", null);
        addForm.assertFormPresent();
        assertTextPresent("Unclosed group near index 8");
    }

    public void testAddCommitLinkDuplicate() throws Exception
    {
        addLink("todup");
        assertAndClick("commit.message.link.add");
        CommitMessageLinkForm addForm = new CommitMessageLinkForm(tester, true);
        addForm.assertFormPresent();
        addForm.saveFormElements("todup", "d", "d", null);
        addForm.assertFormPresent();
        assertTextPresent("A commit message link with name 'todup' already exists");
    }

    public void testDeleteCommitLink() throws Exception
    {
        addLink("todel");
        assertCommitLinks(new String[][] { getLinkRow("todel", "expr") });
        clickLink("delete_todel");
        assertCommitLinks(null);
    }

    public void testEditCommitLink() throws Exception
    {
        String id = addLink("mylink");
        clickLink("edit_mylink");
        CommitMessageLinkForm form = new CommitMessageLinkForm(tester, false);
        form.assertFormPresent();
        form.assertFormElements("mylink", "expr", "repl", id);
        form.saveFormElements("edited", "edited_expr", "edited_repl", null);

        assertCommitLinks(new String[][] { getLinkRow("edited", "edited_expr") });
        clickLink("edit_edited");
        form.assertFormPresent();
        form.assertFormElements("edited", "edited_expr", "edited_repl", null);
    }

    public void testEditCommitLinkNoProjects() throws Exception
    {
        String id = addLink("mylink");
        clickLink("edit_mylink");
        CommitMessageLinkForm form = new CommitMessageLinkForm(tester, false);
        form.assertFormPresent();
        form.assertFormElements("mylink", "expr", "repl", id);
        form.saveFormElements("edited", "edited_expr", "edited_repl", "");

        assertCommitLinks(new String[][] { getLinkRow("edited", "edited_expr") });
        clickLink("edit_edited");
        form.assertFormPresent();
        form.assertFormElements("edited", "edited_expr", "edited_repl", "");
    }

    public void testEditCommitLinkValidation() throws Exception
    {
        addLink("mylink");
        clickLink("edit_mylink");
        CommitMessageLinkForm form = new CommitMessageLinkForm(tester, false);
        form.assertFormPresent();
        form.assertFormElements("mylink", "expr", "repl", null);
        form.saveFormElements("", "", "", null);
        form.assertFormPresent();
        assertTextPresent("name is required");
        assertTextPresent("expression is required");
        assertTextPresent("url is required");

        form.saveFormElements("mylink", "(invalid", "url", null);
        form.assertFormPresent();
        assertTextPresent("Unclosed group near index 8");
        assertTextNotPresent("already");
    }

    public void testEditCommitLinkDuplicate() throws Exception
    {
        addLink("mylink");
        addLink("todup");
        clickLink("edit_mylink");
        CommitMessageLinkForm form = new CommitMessageLinkForm(tester, false);
        form.assertFormPresent();
        form.assertFormElements("mylink", "expr", "repl", null);
        form.saveFormElements("todup", "expr", "repl", null);
        form.assertFormPresent();
        assertTextPresent("A commit message link with name 'todup' already exists");
    }
    
    private String[] getLinkRow(String name, String expression)
    {
        return new String[] { name, expression, "edit", "delete" };
    }

    private void assertCommitLinks(String[][] linkRows) throws Exception
    {
        if(linkRows == null)
        {
            assertTableRowsEqual("commit.message.links", 2, new String[][] { new String[] {"no commit message links configured", "no commit message links configured", "no commit message links configured" }});
        }
        else
        {
            assertTableRowsEqual("commit.message.links", 2, linkRows);
        }
    }
}
