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

package com.zutubi.pulse.core.plugins.util;

import com.zutubi.util.io.FileSystemUtils;
import com.zutubi.util.io.IOUtils;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.jar.JarFile;

/**
 * A FileFilter implementation that does some basic verification that
 * the provided file is a valid plugin.
 * <p/>
 * The checks include that the file is either a directory or a jar file,
 * and that both contain a META-INF/MANIFEST.MF file.
 */
public class PluginFileFilter implements FileFilter
{
    public boolean accept(File file)
    {
        if (file.isFile() && file.getName().endsWith(".jar"))
        {
            // check that the required manifest file is present.
            JarFile jarFile = null;
            try
            {
                jarFile = new JarFile(file);
                return jarFile.getJarEntry("META-INF/MANIFEST.MF") != null;
            }
            catch (IOException e)
            {
                return false;
            }
            finally
            {
                IOUtils.close(jarFile);
            }
        }
        else if (file.isDirectory())
        {
            // do a simple check for the required plugin manifest file.
            File manifestFile = new File(file, FileSystemUtils.composeFilename("META-INF", "MANIFEST.MF"));
            return manifestFile.isFile();
        }
        return false;
    }   
}
