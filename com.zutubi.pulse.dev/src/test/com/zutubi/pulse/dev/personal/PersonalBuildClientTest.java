package com.zutubi.pulse.dev.personal;

import com.zutubi.pulse.core.personal.PersonalBuildException;
import com.zutubi.pulse.core.scm.WorkingCopyContextImpl;
import com.zutubi.pulse.core.scm.api.*;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

import java.io.IOException;

public class PersonalBuildClientTest extends AbstractPersonalBuildTestCase
{
    private static final String TEST_REVISION = "my-rev";

    private PersonalBuildConfig config;
    private PersonalBuildUI ui;
    private WorkingCopy workingCopy;
    private WorkingCopyContext context;
    private PersonalBuildClient client;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        config = new PersonalBuildConfig(baseDir);
        ui = mock(PersonalBuildUI.class);
        workingCopy = mock(WorkingCopy.class);
        context = new WorkingCopyContextImpl(baseDir, config, ui);
        client = new PersonalBuildClient(config, ui);
    }

    public void testChooseRevision() throws IOException, PersonalBuildException
    {
        setRevisionChoice(PersonalBuildClient.REVISION_OPTION_FLOATING, false);

        PersonalBuildRevision buildRevision = client.chooseRevision(workingCopy, context);

        assertEquals(WorkingCopy.REVISION_FLOATING, buildRevision.getRevision());
        assertFalse(buildRevision.isUpdateSupported());
    }

    public void testChooseRevisionAlreadyInConfig() throws IOException, PersonalBuildException
    {
        config.setRevision(PersonalBuildClient.REVISION_OPTION_GOOD);

        PersonalBuildRevision buildRevision = client.chooseRevision(workingCopy, context);

        verifyZeroInteractions(ui);
        assertEquals(WorkingCopy.REVISION_LAST_KNOWN_GOOD, buildRevision.getRevision());
        assertFalse(buildRevision.isUpdateSupported());
    }

    public void testChooseRevisionPersist() throws IOException, PersonalBuildException
    {
        assertNull(config.getRevision());
        setRevisionChoice(PersonalBuildClient.REVISION_OPTION_FLOATING, true);

        client.chooseRevision(workingCopy, context);

        assertEquals(PersonalBuildClient.REVISION_OPTION_FLOATING, config.getRevision());
    }

    public void testChooseRevisionCustom() throws IOException, PersonalBuildException
    {
        setRevisionChoice(PersonalBuildClient.REVISION_OPTION_CUSTOM, false);
        stub(ui.inputPrompt(anyString())).toReturn(TEST_REVISION);

        PersonalBuildRevision buildRevision = client.chooseRevision(workingCopy, context);

        assertEquals(TEST_REVISION, buildRevision.getRevision().getRevisionString());
    }

    public void testChooseRevisionCustomAlreadyInConfig() throws IOException, PersonalBuildException
    {
        config.setRevision(PersonalBuildClient.REVISION_OPTION_CUSTOM);
        stub(ui.inputPrompt(anyString())).toReturn(TEST_REVISION);

        PersonalBuildRevision buildRevision = client.chooseRevision(workingCopy, context);

        verify(ui).inputPrompt(anyString());
        verifyNoMoreInteractions(ui);
        assertEquals(TEST_REVISION, buildRevision.getRevision().getRevisionString());
    }

    public void testChooseRevisionSetInConfig() throws IOException, PersonalBuildException
    {
        config.setRevision(TEST_REVISION);

        PersonalBuildRevision buildRevision = client.chooseRevision(workingCopy, context);

        verifyZeroInteractions(ui);
        assertEquals(TEST_REVISION, buildRevision.getRevision().getRevisionString());
    }

    public void testChooseRevisionLatestRemote() throws IOException, PersonalBuildException, ScmException
    {
        setRevisionChoice(PersonalBuildClient.REVISION_OPTION_LATEST, false);
        stub(workingCopy.getLatestRemoteRevision(context)).toReturn(new Revision(TEST_REVISION));

        PersonalBuildRevision buildRevision = client.chooseRevision(workingCopy, context);

        assertEquals(TEST_REVISION, buildRevision.getRevision().getRevisionString());
        assertTrue(buildRevision.isUpdateSupported());
    }

    public void testChooseRevisionLocal() throws IOException, PersonalBuildException, ScmException
    {
        setRevisionChoice(PersonalBuildClient.REVISION_OPTION_LOCAL, false);
        stub(workingCopy.guessLocalRevision(context)).toReturn(new Revision(TEST_REVISION));

        PersonalBuildRevision buildRevision = client.chooseRevision(workingCopy, context);

        assertEquals(TEST_REVISION, buildRevision.getRevision().getRevisionString());
        assertTrue(buildRevision.isUpdateSupported());
    }

    public void testUpdateIfDesiredPromptYes() throws PersonalBuildException, ScmException
    {
        Revision revision = new Revision(TEST_REVISION);
        setUpdateChoice(YesNoResponse.YES);

        client.updateIfDesired(workingCopy, context, revision);

        verify(workingCopy).update(context, revision);
        assertNull(config.getUpdate());
    }

    public void testUpdateIfDesiredPromptNo() throws PersonalBuildException, ScmException
    {
        setUpdateChoice(YesNoResponse.NO);

        client.updateIfDesired(workingCopy, context, new Revision(TEST_REVISION));

        verifyZeroInteractions(workingCopy);
        assertNull(config.getUpdate());
    }

    public void testUpdateIfDesiredDisabledInConfig() throws PersonalBuildException, ScmException
    {
        config.setUpdate(false);
        Revision revision = new Revision(TEST_REVISION);

        client.updateIfDesired(workingCopy, context, revision);

        verifyZeroInteractions(ui);
        verifyZeroInteractions(workingCopy);
    }

    public void testUpdateIfDesiredPersistence() throws PersonalBuildException, ScmException
    {
        assertNull(config.getUpdate());
        setUpdateChoice(YesNoResponse.NEVER);

        client.updateIfDesired(workingCopy, context, new Revision(TEST_REVISION));

        assertFalse(config.getUpdate());
    }

    private void setRevisionChoice(String option, boolean persistent)
    {
        stub(ui.<String>menuPrompt(anyString(), anyList())).toReturn(new MenuChoice<String>(option, persistent));
    }

    private void setUpdateChoice(YesNoResponse choice)
    {
        stub(ui.yesNoPrompt(anyString(), eq(true), eq(true), eq(YesNoResponse.YES))).toReturn(choice);
    }
}