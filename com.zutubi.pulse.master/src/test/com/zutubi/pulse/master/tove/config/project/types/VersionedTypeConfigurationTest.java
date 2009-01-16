package com.zutubi.pulse.master.tove.config.project.types;

import com.zutubi.pulse.core.personal.PatchArchive;
import com.zutubi.pulse.core.scm.api.Revision;
import com.zutubi.pulse.core.scm.api.ScmClient;
import com.zutubi.pulse.core.scm.api.ScmContext;
import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.pulse.master.scm.ScmManager;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import org.hibernate.lob.ReaderInputStream;
import static org.mockito.Mockito.*;

import java.io.StringReader;

public class VersionedTypeConfigurationTest extends PulseTestCase
{
    private static final String VERSIONED_PULSE_FILE_PATH = "a/path";
    private static final String VERSIONED_PULSE_FILE_CONTENT = "versioned content";

    private VersionedTypeConfiguration configuration;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        configuration = new VersionedTypeConfiguration();
        configuration.setPulseFileName(VERSIONED_PULSE_FILE_PATH);

        ReaderInputStream inputStream = new ReaderInputStream(new StringReader(VERSIONED_PULSE_FILE_CONTENT));
        ScmClient mockClient = mock(ScmClient.class);
        stub(mockClient.retrieve((ScmContext) anyObject(), eq(VERSIONED_PULSE_FILE_PATH), (Revision) anyObject())).toReturn(inputStream);

        ScmManager mockScmManager = mock(ScmManager.class);
        stub(mockScmManager.createClient(null)).toReturn(mockClient);
        configuration.setScmManager(mockScmManager);
    }

    public void testNoPatch() throws Exception
    {
        assertEquals(VERSIONED_PULSE_FILE_CONTENT, configuration.getPulseFile(new ProjectConfiguration(), null, null));
    }

    public void testPatchNoChangeToPath() throws Exception
    {
        PatchArchive mockPatch = mock(PatchArchive.class);
        stub(mockPatch.containsPath(VERSIONED_PULSE_FILE_PATH)).toReturn(false);
        assertEquals(VERSIONED_PULSE_FILE_CONTENT, configuration.getPulseFile(new ProjectConfiguration(), null, mockPatch));
    }

    public void testPatchWithChangeToPath() throws Exception
    {
        final String NEW_CONTENT = "new content";

        PatchArchive mockPatch = mock(PatchArchive.class);
        stub(mockPatch.containsPath(VERSIONED_PULSE_FILE_PATH)).toReturn(true);
        stub(mockPatch.retrieveFile(VERSIONED_PULSE_FILE_PATH)).toReturn(NEW_CONTENT);
        assertEquals(NEW_CONTENT, configuration.getPulseFile(new ProjectConfiguration(), null, mockPatch));
    }
}
