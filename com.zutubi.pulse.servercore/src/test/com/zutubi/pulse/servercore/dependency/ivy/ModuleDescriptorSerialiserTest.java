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
