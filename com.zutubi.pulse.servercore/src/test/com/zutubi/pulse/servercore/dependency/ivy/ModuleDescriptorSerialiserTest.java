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

package com.zutubi.pulse.servercore.dependency.ivy;

import com.caucho.hessian.io.AbstractHessianOutput;
import org.apache.ivy.core.module.descriptor.ModuleDescriptor;
import org.hamcrest.text.IsEqualIgnoringWhiteSpace;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.text.ParseException;

public class ModuleDescriptorSerialiserTest extends AbstractHessianTestCase
{
    private ModuleDescriptorSerialiser serialiser;

    protected void setUp() throws Exception
    {
        super.setUp();

        serialiser = new ModuleDescriptorSerialiser();
        serialiser.setTmpDir(tmpDir);
    }

    public void testSerialisation() throws IOException, ParseException
    {
        String descriptorName = "ivy.xml";

        ModuleDescriptor descriptor = parseDescriptor(descriptorName);
        AbstractHessianOutput output = mock(AbstractHessianOutput.class);
        serialiser.writeObject(descriptor, output);

        String expected = readDescriptor(descriptorName);

        verify(output).writeMapBegin(descriptor.getClass().getName());
        verify(output, times(1)).writeString("value");
        verify(output, times(1)).writeString(argThat(new IsEqualIgnoringWhiteSpace(expected)));
        verify(output).writeMapEnd();

        // assert that we do not leave anything behind.
        assertEmptyTmpDir();
    }
}
