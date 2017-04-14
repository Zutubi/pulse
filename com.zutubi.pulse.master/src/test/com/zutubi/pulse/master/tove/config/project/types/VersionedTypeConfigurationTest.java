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

package com.zutubi.pulse.master.tove.config.project.types;

import com.zutubi.pulse.core.marshal.FileResolver;
import com.zutubi.pulse.core.test.api.PulseTestCase;
import org.hibernate.lob.ReaderInputStream;

import java.io.InputStream;
import java.io.StringReader;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.stub;

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
        InputStream inputStream = new ReaderInputStream(new StringReader(VERSIONED_PULSE_FILE_CONTENT));
        stub(mockResolver.resolve(VERSIONED_PULSE_FILE_PATH)).toReturn(inputStream);
    }

    public void testSimple() throws Exception
    {
        assertEquals(VERSIONED_PULSE_FILE_CONTENT, configuration.getPulseFile().getFileContent(mockResolver));
    }
}
