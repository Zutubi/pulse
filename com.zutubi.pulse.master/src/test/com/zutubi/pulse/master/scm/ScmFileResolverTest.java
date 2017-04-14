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

package com.zutubi.pulse.master.scm;

import com.zutubi.pulse.core.PulseExecutionContext;
import com.zutubi.pulse.core.scm.PersistentContextImpl;
import com.zutubi.pulse.core.scm.ScmContextImpl;
import com.zutubi.pulse.core.scm.api.Revision;
import com.zutubi.pulse.core.scm.api.ScmClient;
import com.zutubi.pulse.core.scm.api.ScmContext;
import com.zutubi.pulse.core.scm.config.api.ScmConfiguration;
import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.pulse.master.model.Project;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import org.mockito.Matchers;

import java.io.InputStream;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.stub;

public class ScmFileResolverTest extends PulseTestCase
{
    private static final String PATH_TOP = "pulse.xml";
    private static final String PATH_NESTED = "include/macros.xml";

    private static final Revision REVISION_1 = new Revision(1);
    private static final Revision REVISION_2 = new Revision(2);

    private static final InputStream INPUT_TOP_1 = mock(InputStream.class);
    private static final InputStream INPUT_TOP_2 = mock(InputStream.class);
    private static final InputStream INPUT_NESTED_1 = mock(InputStream.class);

    private ScmFileResolver resolver1;
    private ScmFileResolver resolver2;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        ScmClient mockScmClient = mock(ScmClient.class);
        stub(mockScmClient.retrieve((ScmContext) anyObject(), eq(PATH_TOP), eq(REVISION_1))).toReturn(INPUT_TOP_1);
        stub(mockScmClient.retrieve((ScmContext) anyObject(), eq(PATH_TOP), eq(REVISION_2))).toReturn(INPUT_TOP_2);
        stub(mockScmClient.retrieve((ScmContext) anyObject(), eq(PATH_NESTED), eq(REVISION_1))).toReturn(INPUT_NESTED_1);

        ScmManager mockScmManager = mock(ScmManager.class);
        stub(mockScmManager.createContext((ProjectConfiguration) anyObject(), Matchers.<Project.State>anyObject(), anyString())).toReturn(new ScmContextImpl(new PersistentContextImpl(null), new PulseExecutionContext()));
        stub(mockScmManager.createClient((ProjectConfiguration) anyObject(), (ScmConfiguration) anyObject())).toReturn(mockScmClient);

        ProjectConfiguration projectConfiguration = new ProjectConfiguration();
        Project project = new Project();
        project.stateTransition(Project.Transition.INITIALISE);
        project.stateTransition(Project.Transition.INITIALISE_SUCCESS);
        project.setConfig(projectConfiguration);
        resolver1 = new ScmFileResolver(project, REVISION_1, mockScmManager);
        resolver2 = new ScmFileResolver(project, REVISION_2, mockScmManager);
    }

    public void testSimpleResolve() throws Exception
    {
        assertSame(INPUT_TOP_1, resolver1.resolve(PATH_TOP));
    }

    public void testRevisionApplied() throws Exception
    {
        InputStream i1 = resolver1.resolve(PATH_TOP);
        InputStream i2 = resolver2.resolve(PATH_TOP);
        assertNotSame(i1, i2);
        assertEquals(INPUT_TOP_1, i1);
        assertEquals(INPUT_TOP_2, i2);
    }

    public void testAbsolutePath() throws Exception
    {
        assertSame(INPUT_NESTED_1, resolver1.resolve("/" + PATH_NESTED));
    }
}
