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

package com.zutubi.pulse.dev.local;

import com.zutubi.pulse.core.RecipePaths;
import com.zutubi.pulse.core.engine.api.BuildProperties;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * The implementation of the RecipePaths interface for the local build. The output
 * path is relative to the base directory unless an absolute directory is specified.
 */
public class LocalRecipePaths implements RecipePaths
{
    private File baseDir;
    private File outputDir;

    public LocalRecipePaths(File base, String output)
    {
        baseDir = base;
        outputDir = new File(output);
        if (!outputDir.isAbsolute())
        {
            outputDir = new File(base, output);
        }

    }

    public File getCheckoutDir()
    {
        return null;
    }

    public File getBaseDir()
    {
        return baseDir;
    }

    public File getOutputDir()
    {
        return outputDir;
    }

    public Map<String, String> getPathProperties()
    {
        Map<String, String> properties = new HashMap<String, String>();
        properties.put(BuildProperties.PROPERTY_BASE_DIR, baseDir.getAbsolutePath());
        properties.put(BuildProperties.PROPERTY_OUTPUT_DIR, outputDir.getAbsolutePath());
        return properties;

    }
}
