package com.zutubi.pulse.acceptance;

import com.zutubi.pulse.acceptance.forms.AddCommitMessageTransformerWizard;
import com.zutubi.pulse.acceptance.forms.AddTransformersToProjectsForm;

/**
 * <class comment/>
 */
public class CommitMessageTransformerAcceptanceTest extends BaseAcceptanceTestCase
{
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

    public void testAddNewTransformer()
    {
        // click the add link.
        assertAndClick("commit.message.transformer.add");

        // select a type.
        selectCommitMessageTransformerType("link");

        // fill in the blanks.
        AddCommitMessageTransformerWizard.Link link = new AddCommitMessageTransformerWizard.Link(tester);
        link.assertFormPresent();
        link.finishFormElements("name", "expression", "link");
        link.assertFormNotPresent();

        // ensure that the transformer is listed.
        assertLinkPresent("edit_name");
    }

    public void testCancelAddingNewTransformer()
    {
        // click add link
        assertAndClick("commit.message.transformer.add");

        // select a type.
        selectCommitMessageTransformerType("link");

        // fill in the blanks.
        AddCommitMessageTransformerWizard.Link link = new AddCommitMessageTransformerWizard.Link(tester);
        link.assertFormPresent();
        link.cancelFormElements("name", "expression", "link");
        link.assertFormNotPresent();

        // ensure that the transformer has is not listed.
        assertLinkNotPresent("edit_name");
    }

    public void testAddNewTransformerValidation()
    {
        // click the add link.
        assertAndClick("commit.message.transformer.add");

        // select type STANDARD
        selectCommitMessageTransformerType("link");

        // check validation.
        AddCommitMessageTransformerWizard.Link link = new AddCommitMessageTransformerWizard.Link(tester);
        link.assertFormPresent();
        link.finishFormElements("", "expression", "link");
        link.assertFormPresent();
        link.finishFormElements("name", "", "link");
        link.assertFormPresent();
        link.finishFormElements("name", "expression", "");
        link.assertFormPresent();
        link.previous();

        // select type CUSTOM
        selectCommitMessageTransformerType("custom");

        // check validation
        AddCommitMessageTransformerWizard.Custom custom = new AddCommitMessageTransformerWizard.Custom(tester);
        custom.assertFormPresent();
        custom.finishFormElements("", "expression", "replacement");
        custom.assertFormPresent();
        custom.finishFormElements("name", "", "replacement");
        custom.assertFormPresent();
        custom.previous();

        // select type JIRA
        selectCommitMessageTransformerType("jira");

        // check validation
        AddCommitMessageTransformerWizard.Jira jira = new AddCommitMessageTransformerWizard.Jira(tester);
        jira.assertFormPresent();
        jira.finishFormElements("", "url");
        jira.assertFormPresent();
        jira.finishFormElements("name", "");
        jira.assertFormPresent();
    }

    public void testEditExistingTransformer()
    {
        // create transformer.
        createCommitMessageTransformer("link", "name", "expression", "link");

        // click edit link.
        assertAndClick("edit_name");

        // ensure values as expected.
        AddCommitMessageTransformerWizard.Link link = new AddCommitMessageTransformerWizard.Link(tester);
        link.assertFormPresent();
        link.assertFormElements("name", "expression", "link");

        // change the details.
        link.saveFormElements("name1", "expression1", "link1");
        link.assertFormNotPresent();

        // ensure name change as expected.
        assertAndClick("edit_name1");

        // select edit again
        // ensure values have been updated.
        link.assertFormElements("name1", "expression1", "link1");

        // click cancel.
        link.cancel();
    }

    public void testDeleteExistingTransformer()
    {
        // create transformer
        createCommitMessageTransformer("link", "name", "expression", "link");
        assertLinkPresent("edit_name");

        // click the delete link.
        assertAndClick("delete_name");

        // ensure transformer is gone.
        assertLinkNotPresent("edit_name");
    }

    public void testAssignTransformerToProject()
    {
        // create a transformer.
        createCommitMessageTransformer("link", "name", "expression", "link");

        // click the projects link.
        assertAndClick("projects_name");
        
        // ensure non selected.
        AddTransformersToProjectsForm form = new AddTransformersToProjectsForm(tester);
        form.assertFormPresent();

        // ensure that non are selected...

        // select cml1
        String id = tester.getDialog().getValueForOption("selectedProjects", "cml1");
        form.saveFormElements(id);
        form.assertFormNotPresent();

        // click the projects link.
        assertAndClick("projects_name");
        form.assertFormPresent();
        form.assertFormElements(id);
        
        // navigate to project configuration
        clickLink(Navigation.TAB_PROJECTS);
        clickLink("cml1");
        clickLink(Navigation.Projects.TAB_CONFIGURATION);

        // ensure that transformer is listed.
        assertLinkPresent("edit_name");
    }
}
