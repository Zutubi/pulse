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

package com.zutubi.pulse.core.commands.maven2;

import com.zutubi.pulse.core.resources.api.FileSystemResourceLocator;
import com.zutubi.pulse.core.resources.api.ResourceConfiguration;
import com.zutubi.pulse.core.resources.api.ResourceLocator;
import com.zutubi.pulse.core.resources.api.StandardHomeDirectoryResourceBuilder;

import java.io.File;
import java.util.List;

/**
 * Locates a maven 2 installation.
 */
public class Maven2ResourceLocator implements ResourceLocator
{
    public List<ResourceConfiguration> locate()
    {
        final Maven2HomeDirectoryLocator homeDirectoryLocator = new Maven2HomeDirectoryLocator();
        StandardHomeDirectoryResourceBuilder builder = new StandardHomeDirectoryResourceBuilder("maven2", "mvn", true)
        {
            @Override
            protected String getVersionName(File homeDir, File binary)
            {
                String captured = homeDirectoryLocator.getCapturedVersion();
                if (captured != null)
                {
                    return captured;
                }
                
                return super.getVersionName(homeDir, binary);
            }
        };
        
        FileSystemResourceLocator delegate = new FileSystemResourceLocator(homeDirectoryLocator, builder);
        return delegate.locate();
    }
}
