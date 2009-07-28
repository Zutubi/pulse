package com.zutubi.pulse.master.tove.config.project.types;

import com.zutubi.pulse.core.marshal.FileResolver;
import com.zutubi.pulse.core.test.api.PulseTestCase;
import org.hibernate.lob.ReaderInputStream;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.stub;

import java.io.StringReader;

public class VersionedTypeConfigurationTest extends PulseTestCase
{
    private static final String VERSIONED_PULSE_FILE_PATH = "a/path";
    private static final String VERSIONED_PULSE_FILE_CONTENT = "versioned content";

    private VersionedTypeConfiguration configuration;
    private FileResolver mockResolver;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        configuration = new VersionedTypeConfiguration();
        configuration.setPulseFileName(VERSIONED_PULSE_FILE_PATH);

        mockResolver = mock(FileResolver.class);
        ReaderInputStream inputStream = new ReaderInputStream(new StringReader(VERSIONED_PULSE_FILE_CONTENT));
        stub(mockResolver.resolve(VERSIONED_PULSE_FILE_PATH)).toReturn(inputStream);
    }

    public void testSimple() throws Exception
    {
        assertEquals(VERSIONED_PULSE_FILE_CONTENT, configuration.getPulseFile().getFileContent(mockResolver));
    }
}
