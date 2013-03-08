package com.zutubi.pulse.servercore.dependency.ivy;

import com.caucho.hessian.io.AbstractHessianOutput;
import com.caucho.hessian.io.AbstractSerializer;
import com.google.common.io.Files;
import com.zutubi.util.io.FileSystemUtils;
import org.apache.ivy.core.module.descriptor.ModuleDescriptor;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

/**
 * An implementation of the hessian serialiser interface that handles
 * the serialisation of the ivy ModuleDescriptor.
 */
public class ModuleDescriptorSerialiser extends AbstractSerializer
{
    private static final String FIELD_VALUE = "value";

    private File tmpDir;

    public void writeObject(Object obj, AbstractHessianOutput out) throws IOException
    {
        if (obj == null)
        {
            out.writeNull();
        }
        else
        {
            ModuleDescriptor descriptor = (ModuleDescriptor) obj;

            Class cl = obj.getClass();
            out.writeMapBegin(cl.getName());
            out.writeString(FIELD_VALUE);
            out.writeString(toString(descriptor));
            out.writeMapEnd();
        }
    }

    private String toString(ModuleDescriptor descriptor) throws IOException
    {
        File tmp = null;
        try
        {
            tmp = createTempFile();
            descriptor.toIvyFile(tmp);
            return Files.toString(tmp, Charset.defaultCharset());
        }
        catch (Exception e)
        {
            throw new IOException(e.getMessage());
        }
        finally
        {
            deleteTempFile(tmp);
        }
    }

    private void deleteTempFile(File file)
    {
        if (file != null && file.isFile())
        {
            if (!file.delete())
            {
                file.deleteOnExit();
            }
        }
    }

    private File createTempFile() throws IOException
    {
        if (tmpDir != null && tmpDir.isDirectory())
        {
            return FileSystemUtils.createTempFile(tmpDir);
        }
        else
        {
            return File.createTempFile("tmp", "xml");
        }
    }

    public void setTmpDir(File tmpDir)
    {
        this.tmpDir = tmpDir;
    }
}
