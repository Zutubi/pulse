package com.zutubi.pulse.master.scm;

import com.zutubi.pulse.core.scm.ScmContextImpl;
import com.zutubi.pulse.core.scm.api.Revision;
import com.zutubi.pulse.core.scm.api.ScmClient;
import com.zutubi.pulse.core.scm.api.ScmContext;
import com.zutubi.pulse.core.scm.config.api.ScmConfiguration;
import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import static org.mockito.Mockito.*;

import java.io.InputStream;

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
        stub(mockScmManager.createContext((ProjectConfiguration) anyObject())).toReturn(new ScmContextImpl());
        stub(mockScmManager.createClient((ScmConfiguration) anyObject())).toReturn(mockScmClient);

        ProjectConfiguration projectConfiguration = new ProjectConfiguration();
        resolver1 = new ScmFileResolver(projectConfiguration, REVISION_1, mockScmManager);
        resolver2 = new ScmFileResolver(projectConfiguration, REVISION_2, mockScmManager);
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
