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

package com.zutubi.util.config;

import com.zutubi.util.io.IOUtils;
import com.zutubi.util.io.PropertiesWriter;
import com.zutubi.util.logging.Logger;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

public class FileConfig implements Config
{
    private static final Logger LOG = Logger.getLogger(FileConfig.class);

    private final File file;

    private Properties props;

    public FileConfig(File file)
    {
        this.file = file;
    }

    public String getProperty(String key)
    {
        return getProperties().getProperty(key);
    }

    public void setProperty(String key, String value)
    {
        getProperties().setProperty(key, value);
        writeToFile();
    }

    private void writeToFile()
    {
        try
        {
            if (!file.isFile())
            {
                if (!file.getParentFile().isDirectory() && !file.getParentFile().mkdirs())
                {
                    throw new IOException();
                }
                if (!file.createNewFile())
                {
                    throw new IOException();
                }
            }
            PropertiesWriter writer = new PropertiesWriter();
            writer.write(file, getProperties());
        }
        catch (IOException e)
        {
            LOG.error(e);
        }
    }

    public boolean hasProperty(String key)
    {
        return getProperties().containsKey(key);
    }

    public void removeProperty(String key)
    {
        getProperties().remove(key);
        writeToFile();
    }

    public Properties getProperties()
    {
        if (props == null)
        {
            try
            {
                if (file.isFile())
                {
                    props = IOUtils.read(file);
                }
                else
                {
                    props = new Properties();
                }
            }
            catch (IOException e)
            {
                LOG.severe(e);
                props = new Properties();
            }
        }

        return props;
    }

    public boolean isWritable()
    {
        return true;
    }
}
