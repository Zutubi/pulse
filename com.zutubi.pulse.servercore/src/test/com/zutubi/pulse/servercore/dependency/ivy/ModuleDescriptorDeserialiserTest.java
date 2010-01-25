package com.zutubi.pulse.servercore.dependency.ivy;

import com.caucho.hessian.io.AbstractHessianInput;
import com.zutubi.pulse.core.dependency.ivy.IvyManager;
import org.apache.ivy.core.module.descriptor.ModuleDescriptor;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.stub;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

import java.io.IOException;
import java.text.ParseException;

public class ModuleDescriptorDeserialiserTest extends AbstractHessianTestCase
{
    private ModuleDescriptorDeserialiser deserialiser;

    protected void setUp() throws Exception
    {
        super.setUp();

        deserialiser = new ModuleDescriptorDeserialiser();
        deserialiser.setTmpDir(tmpDir);

        IvyManager ivyManager = new IvyManager();
        ivyManager.init();
    }

    public void testDeserialisation() throws IOException, ParseException
    {
        String descriptorName = "ivy.xml";

        String data = readDescriptor(descriptorName);

        AbstractHessianInput input = mock(AbstractHessianInput.class);
        stub(input.isEnd()).toReturn(false).toReturn(true);
        stub(input.readString()).toReturn("value").toReturn(data);

        ModuleDescriptor descriptor = (ModuleDescriptor) deserialiser.readMap(input);
        assertEquals(parseDescriptor(descriptorName).toString(), descriptor.toString());

        verify(input, times(1)).readMapEnd();

        // assert that we do not leave anything behind.
        assertEmptyTmpDir();
    }
}
