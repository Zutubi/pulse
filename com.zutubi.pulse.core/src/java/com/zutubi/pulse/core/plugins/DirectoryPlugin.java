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

package com.zutubi.pulse.core.plugins;

import com.zutubi.util.io.FileSystemUtils;
import com.zutubi.util.io.IOUtils;
import org.eclipse.osgi.framework.util.Headers;
import org.osgi.framework.BundleException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * An implementation of the local plugin that represents a bundle deployed
 * as an expanded directory
 */
public class DirectoryPlugin extends LocalPlugin
{
    public DirectoryPlugin(File source)
    {
        super(source);
    }

    protected Headers loadPluginManifest(File pluginFile)
    {
        try
        {
            InputStream manifestIn = new FileInputStream(new File(pluginFile, FileSystemUtils.composeFilename("META-INF", "MANIFEST.MF")));
            try
            {
                return parseManifest(manifestIn);
            }
            finally
            {
                IOUtils.close(manifestIn);
            }
        }
        catch (IOException e)
        {
            throw new IllegalArgumentException(e);
        }
        catch (BundleException e)
        {
            throw new IllegalArgumentException(e);
        }
    }

    protected void delete() throws IOException
    {
        FileSystemUtils.rmdir(source);
    }
}
