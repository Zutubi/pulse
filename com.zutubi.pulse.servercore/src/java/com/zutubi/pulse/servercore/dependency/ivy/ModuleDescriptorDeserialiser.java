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

import com.caucho.hessian.io.AbstractDeserializer;
import com.caucho.hessian.io.AbstractHessianInput;
import com.google.common.io.Files;
import com.zutubi.pulse.core.dependency.ivy.IvyConfiguration;
import com.zutubi.pulse.core.dependency.ivy.IvyModuleDescriptorParser;
import com.zutubi.util.StringUtils;
import com.zutubi.util.io.FileSystemUtils;
import com.zutubi.util.logging.Logger;
import org.apache.ivy.core.module.descriptor.ModuleDescriptor;
import org.apache.ivy.core.settings.IvySettings;
import org.apache.ivy.plugins.repository.Resource;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.ParseException;

/**
 * A hessian deserialiser for the ivy ModuleDescriptor
 */
public class ModuleDescriptorDeserialiser extends AbstractDeserializer
{
    private static final Logger LOG = Logger.getLogger(ModuleDescriptorDeserialiser.class);

    private static final String FIELD_VALUE = "value";

    private File tmpDir;

    public Object readMap(AbstractHessianInput input) throws IOException
    {
        // we serialised the object as a map, so we need to read it as one.

        String descriptor = null;

        while (!input.isEnd())
        {
            String key = input.readString();
            String value = input.readString();

            if (key.equals(FIELD_VALUE))
            {
                descriptor = value;
            }
        }

        // we have the data we need or are at the end of the map, so indicate
        // this accordingly.
        input.readMapEnd();

        // if we did not locate a descriptor (value field) then something went wrong
        // in the transfer.
        if (!StringUtils.stringSet(descriptor))
        {
            throw new IOException("Invalid descriptor string.");
        }

        return parseDescriptor(descriptor);
    }

    private ModuleDescriptor parseDescriptor(String descriptor) throws IOException
    {
        // ivy seems to prefer to parse its descriptors from a file, so we write the
        // string to a temporary file first.

        File tmp = null;
        try
        {
            tmp = createTempFile();

            Files.write(descriptor, tmp, Charset.defaultCharset());

            // the name for this resource is pretty arbitrary at the moment, just something that
            // is at least somewhat indicative of what the resource represents.  I don't think it
            // is getting used anywhere - at least not this particular instances name.
            Resource res = new MemoryResource("ivy.xml", descriptor);
            IvySettings ivySettings = new IvyConfiguration().loadSettings();

            return IvyModuleDescriptorParser.parseDescriptor(ivySettings, tmp.toURI().toURL(), res, ivySettings.doValidate());
        }
        catch (ParseException e)
        {
            LOG.error("Failed to parse descriptor. ", e);
            LOG.error("Descriptor: \n" + descriptor);
            throw new IOException(e.getMessage());
        }
        finally
        {
            deleteTempFile(tmp);
        }
    }

    private void deleteTempFile(File tmp)
    {
        if (tmp != null && tmp.isFile())
        {
            if (!tmp.delete())
            {
                // if we can not delete it right now, mark it for
                // deletion later.
                tmp.deleteOnExit();
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
            return File.createTempFile("ivy", "xml");
        }
    }

    public void setTmpDir(File tmpDir)
    {
        this.tmpDir = tmpDir;
    }
}
