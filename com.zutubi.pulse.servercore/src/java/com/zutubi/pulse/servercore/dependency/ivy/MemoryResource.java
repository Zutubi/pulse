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

import org.apache.ivy.plugins.repository.Resource;

import java.io.InputStream;
import java.io.IOException;
import java.io.ByteArrayInputStream;

/**
 * An in memory resource that represents a string.
 */
public class MemoryResource implements Resource
{
    private byte[] content;

    // Implementation Note:  According to the documentation of the Resource interface,
    // lastModified is not allowed to return 0, else ivy will consider this resource invalid.
    // Until I can work out an appropriate value for this field, we will use a constant.
    private long lastModified = 1;

    private String name;

    public MemoryResource(String name, String content)
    {
        this(name, content.getBytes());
    }

    public MemoryResource(String name, byte[] content)
    {
        this.content = content;
        this.name = name;
    }

    public String getName()
    {
        return name;
    }

    public long getLastModified()
    {
        return lastModified;
    }

    public long getContentLength()
    {
        return content.length;
    }

    public boolean exists()
    {
        return true;
    }

    public boolean isLocal()
    {
        return true;
    }

    public Resource clone(String cloneName)
    {
        return new MemoryResource(cloneName, content);
    }

    public InputStream openStream() throws IOException
    {
        return new ByteArrayInputStream(content);
    }
}
