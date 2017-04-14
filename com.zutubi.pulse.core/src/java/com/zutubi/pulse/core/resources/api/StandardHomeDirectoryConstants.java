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

package com.zutubi.pulse.core.resources.api;

import com.zutubi.util.SystemUtils;

import java.io.File;

/**
 * Constants and conventions used by the {@link StandardHomeDirectoryResourceLocator}
 * and the implementations it relies upon.
 */
class StandardHomeDirectoryConstants
{
    public static final String EXTENSION_SCRIPT     = ".bat";
    public static final String EXTENSION_EXECUTABLE = ".exe";

    public static final String DIRECTORY_BINARY = "bin";
    public static final String DIRECTORY_LIBRARY = "lib";

    public static String convertResourceNameToEnvironmentVariable(String resourceName)
    {
        return resourceName.toUpperCase() + "_HOME";
    }

    public static File getBinaryDirectory(File home)
    {
        return new File(home, DIRECTORY_BINARY);
    }

    public static File getLibraryDirectory(File home)
    {
        return new File(home, DIRECTORY_LIBRARY);
    }

    public static File getBinaryFile(File home, String binaryName, boolean script)
    {
        return new File(getBinaryDirectory(home), getSystemBinaryName(binaryName, script));
    }

    public static String getSystemBinaryName(String binaryName, boolean script)
    {
        if (SystemUtils.IS_WINDOWS)
        {
            if (script)
            {
                binaryName += EXTENSION_SCRIPT;
            }
            else
            {
                binaryName += EXTENSION_EXECUTABLE;
            }
        }

        return binaryName;
    }
}
