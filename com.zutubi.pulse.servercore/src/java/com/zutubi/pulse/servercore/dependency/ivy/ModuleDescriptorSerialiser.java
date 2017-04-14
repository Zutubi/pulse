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
