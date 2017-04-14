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

package com.zutubi.pulse.master.restore;

import com.zutubi.util.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

/**
 *
 *
 */
public class ArchiveManifest
{
    public static final String CREATED = "created";

    public static final String VERSION = "version";

    private String created;
    
    private String version;

    protected ArchiveManifest(String created, String version)
    {
        this.created = created;
        this.version = version;
    }

    public String getCreated()
    {
        return created;
    }

    public String getVersion()
    {
        return version;
    }

    protected void writeTo(File f) throws IOException
    {
        Properties properties = new Properties();
        properties.put(CREATED, created);
        properties.put(VERSION, version);
        IOUtils.write(properties, f);
    }

    protected static ArchiveManifest readFrom(File f) throws IOException
    {
        Properties properties = IOUtils.read(f);
        return new ArchiveManifest(
                properties.getProperty(CREATED),
                properties.getProperty(VERSION)
        );
    }
}
