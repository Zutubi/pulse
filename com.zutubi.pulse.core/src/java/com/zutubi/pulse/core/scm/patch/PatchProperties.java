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

package com.zutubi.pulse.core.scm.patch;

import com.zutubi.util.config.ConfigSupport;
import com.zutubi.util.config.FileConfig;

import java.io.File;

/**
 * Handles the storage and manipulation of extra properties stored with patch
 * files.
 */
public class PatchProperties
{
    private static final String PROPERTY_FORMAT = "patch.format";

    private ConfigSupport properties;

    public PatchProperties(File file)
    {
        properties = new ConfigSupport(new FileConfig(file));
    }

    public String getPatchFormat()
    {
        return properties.getProperty(PROPERTY_FORMAT, DefaultPatchFormatFactory.FORMAT_STANDARD);
    }

    public void setPatchFormat(String format)
    {
        properties.setProperty(PROPERTY_FORMAT, format);
    }
}
