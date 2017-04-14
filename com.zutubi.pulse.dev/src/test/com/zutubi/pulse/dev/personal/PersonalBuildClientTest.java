/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zutubi.pulse.dev.personal;

import com.google.common.base.Predicate;
import static com.google.common.collect.Iterables.any;
import static com.google.common.collect.Iterables.find;
import com.zutubi.pulse.core.scm.WorkingCopyContextImpl;
import com.zutubi.pulse.core.scm.api.*;
import com.zutubi.pulse.core.ui.api.MenuChoice;
import com.zutubi.pulse.core.ui.api.MenuOption;
import com.zutubi.pulse.core.ui.api.UserInterface;
import com.zutubi.pulse.core.ui.api.YesNoResponse;
import com.zutubi.pulse.dev.client.ClientException;
import com.zutubi.pulse.dev.util.AbstractDevTestCase;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.IOException;
import java.util.EnumSet;
import java.util.List;

public class PersonalBuildClientTest extends AbstractDevTestCase
{
    private static final String TEST_REVISION = "my-rev";

    private PersonalBuildConfig config;
    private UserInterface ui;
    private PersonalBuildContext context;
    private PersonalBuildClient client;

    private List<MenuOption<String>> revisionOptions;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        // Use a separate UI for the config to just ignore its interactions.
        config = new PersonalBuildConfig(baseDir, mock(UserInterface.class));
        
        ui = mock(UserInterface.class);
        WorkingCopy workingCopy = mock(WorkingCopy.class);
        stub(workingCopy.getCapabilities()).toReturn(EnumSet.allOf(WorkingCopyCapability.class));
        WorkingCopyContext wcContext = new WorkingCopyContextImpl(baseDir, config, ui);
        context = new PersonalBuildContext(workingCopy, wcContext, null, null);
        client = new PersonalBuildClient(config, ui);
    }

    public void testChooseRevision() throws IOException, ClientException
    {
        setRevisionChoice(PersonalBuildClient.REVISION_OPTION_FLOATING, false);

        PersonalBuildRevision buildRevision = client.chooseRevision(context);

        assertEquals(WorkingCopy.REVISION_FLOATING, buildRevision.getRevision());
        assertFalse(buildRevision.isUpdateSupported());
    }

    public void testChooseRevisionAlreadyInConfig() throws IOException, ClientException
    {
        config.setRevision(PersonalBuildClient.REVISION_OPTION_GOOD);

        PersonalBuildRevision buildRevision = client.chooseRevision(context);

        verifyZeroInteractions(ui);
        assertEquals(WorkingCopy.REVISION_LAST_KNOWN_GOOD, buildRevision.getRevision());
        assertFalse(buildRevision.isUpdateSupported());
    }

    public void testChooseRevisionPersist() throws IOException, ClientException
    {
        assertNull(config.getRevision());
        setRevisionChoice(PersonalBuildClient.REVISION_OPTION_FLOATING, true);

        client.chooseRevision(context);

        assertEquals(PersonalBuildClient.REVISION_OPTION_FLOATING, config.getRevision());
    }

    public void testChooseRevisionCustom() throws IOException, ClientException
    {
        setRevisionChoice(PersonalBuildClient.REVISION_OPTION_CUSTOM, false);
        stub(ui.inputPrompt(anyString())).toReturn(TEST_REVISION);

        PersonalBuildRevision buildRevision = client.chooseRevision(context);

        assertEquals(TEST_REVISION, buildRevision.getRevision().getRevisionString());
    }

    public void testChooseRevisionCustomAlreadyInConfig() throws IOException, ClientException
    {
        config.setRevision(PersonalBuildClient.REVISION_OPTION_CUSTOM);
        stub(ui.inputPrompt(anyString())).toReturn(TEST_REVISION);

        PersonalBuildRevision buildRevision = client.chooseRevision(context);

        verify(ui).inputPrompt(anyString());
        verifyNoMoreInteractions(ui);
        assertEquals(TEST_REVISION, buildRevision.getRevision().getRevisionString());
    }

    public void testChooseRevisionSetInConfig() throws IOException, ClientException
    {
        config.setRevision(TEST_REVISION);

        PersonalBuildRevision buildRevision = client.chooseRevision(context);

        verifyZeroInteractions(ui);
        assertEquals(TEST_REVISION, buildRevision.getRevision().getRevisionString());
    }

    public void testChooseRevisionLatestRemote() throws IOException, ClientException, ScmException
    {
        setRevisionChoice(PersonalBuildClient.REVISION_OPTION_LATEST, false);
        stub(context.getWorkingCopy().getLatestRemoteRevision(context.getWorkingCopyContext())).toReturn(new Revision(TEST_REVISION));

        PersonalBuildRevision buildRevision = client.chooseRevision(context);

        assertEquals(TEST_REVISION, buildRevision.getRevision().getRevisionString());
        assertTrue(buildRevision.isUpdateSupported());
    }

    public void testChooseRevisionLocal() throws IOException, ClientException, ScmException
    {
        setRevisionChoice(PersonalBuildClient.REVISION_OPTION_LOCAL, false);
        stub(context.getWorkingCopy().guessLocalRevision(context.getWorkingCopyContext())).toReturn(new Revision(TEST_REVISION));

        PersonalBuildRevision buildRevision = client.chooseRevision(context);

        assertEquals(TEST_REVISION, buildRevision.getRevision().getRevisionString());
        assertTrue(buildRevision.isUpdateSupported());
    }

    public void testCapabilitiesRespectedByRevisionPrompt() throws ClientException
    {
        stub(context.getWorkingCopy().getCapabilities()).toReturn(EnumSet.complementOf(EnumSet.of(WorkingCopyCapability.LOCAL_REVISION, WorkingCopyCapability.REMOTE_REVISION)));
        setRevisionChoice(PersonalBuildClient.REVISION_OPTION_FLOATING, false);

        PersonalBuildRevision revision = client.chooseRevision(context);
        assertEquals(WorkingCopy.REVISION_FLOATING, revision.getRevision());

        assertEquals(3, revisionOptions.size());
        assertFalse(any(revisionOptions, new OptionValuePredicate(PersonalBuildClient.REVISION_OPTION_LOCAL)));
        assertFalse(any(revisionOptions, new OptionValuePredicate(PersonalBuildClient.REVISION_OPTION_LATEST)));

        MenuOption<String> floatingOption = find(revisionOptions, new OptionValuePredicate(PersonalBuildClient.REVISION_OPTION_FLOATING));
        assertTrue(floatingOption.isDefaultOption());
    }
    
    public void testUpdateIfDesiredPromptYes() throws ClientException, ScmException
    {
        Revision revision = new Revision(TEST_REVISION);
        setUpdateChoice(YesNoResponse.YES);

        client.updateIfDesired(context, revision);

        verify(context.getWorkingCopy()).update(context.getWorkingCopyContext(), revision);
        assertNull(config.getUpdate());
    }

    public void testUpdateIfDesiredPromptNo() throws ClientException, ScmException
    {
        setUpdateChoice(YesNoResponse.NO);

        client.updateIfDesired(context, new Revision(TEST_REVISION));

        WorkingCopy workingCopy = context.getWorkingCopy();
        verify(workingCopy).getCapabilities();
        verifyNoMoreInteractions(workingCopy);
        assertNull(config.getUpdate());
    }

    public void testUpdateIfDesiredDisabledInConfig() throws ClientException, ScmException
    {
        config.setUpdate(false);
        Revision revision = new Revision(TEST_REVISION);

        client.updateIfDesired(context, revision);

        WorkingCopy workingCopy = context.getWorkingCopy();
        verifyZeroInteractions(ui);
        verify(workingCopy).getCapabilities();
        verifyNoMoreInteractions(workingCopy);
    }

    public void testUpdateIfDesiredPersistence() throws ClientException, ScmException
    {
        assertNull(config.getUpdate());
        setUpdateChoice(YesNoResponse.NEVER);

        client.updateIfDesired(context, new Revision(TEST_REVISION));

        assertFalse(config.getUpdate());
    }

    public void testUpdateRespectsCapabilities() throws ClientException, ScmException
    {
        stub(context.getWorkingCopy().getCapabilities()).toReturn(EnumSet.complementOf(EnumSet.of(WorkingCopyCapability.UPDATE)));
        Revision revision = new Revision(TEST_REVISION);

        client.updateIfDesired(context, revision);

        WorkingCopy workingCopy = context.getWorkingCopy();
        verifyZeroInteractions(ui);
        verify(workingCopy).getCapabilities();
        verifyNoMoreInteractions(workingCopy);
    }

    @SuppressWarnings({"unchecked"})
    private void setRevisionChoice(final String option, final boolean persistent)
    {
        stub(ui.<String>menuPrompt(anyString(), anyList())).toAnswer(new Answer<Object>()
        {
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable
            {
                revisionOptions = (List<MenuOption<String>>) invocationOnMock.getArguments()[1];
                return new MenuChoice<String>(option, persistent);
            }
        });
    }

    private void setUpdateChoice(YesNoResponse choice)
    {
        stub(ui.yesNoPrompt(anyString(), eq(true), eq(true), eq(YesNoResponse.YES))).toReturn(choice);
    }

    private static class OptionValuePredicate implements Predicate<MenuOption<String>>
    {
        private String value;

        private OptionValuePredicate(String value)
        {
            this.value = value;
        }

        public boolean apply(MenuOption<String> option)
        {
            return option.getValue().equals(value);
        }
    }
}