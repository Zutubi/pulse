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

package com.zutubi.pulse.master.tove.velocity;

import org.apache.commons.collections.ExtendedProperties;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.resource.Resource;
import org.apache.velocity.runtime.resource.loader.ResourceLoader;

import java.io.InputStream;

/**
 * A custom resource loader that will look for a resource within the context
 * of a specific class.  That class is specified via the CONTEXT thread local.
 * This is a somewhat round about way to control the velocity resource lookups
 * on each lookup.
 */
public class VelocityClasspathResourceLoader extends ResourceLoader
{
    public final static ThreadLocal<Class> CONTEXT = new ThreadLocal<Class>();

    public void init(ExtendedProperties configuration)
    {
        // noop.
    }

    public InputStream getResourceStream(String source) throws ResourceNotFoundException
    {
        Class context = CONTEXT.get();
        if (context != null)
        {
            return context.getResourceAsStream("/" + source);
        }
        return null;
    }

    public boolean isSourceModified(Resource resource)
    {
        return false;
    }

    public long getLastModified(Resource resource)
    {
        return 0;
    }
}
