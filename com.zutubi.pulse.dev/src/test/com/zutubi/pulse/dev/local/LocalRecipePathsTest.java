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

import com.zutubi.util.junit.ZutubiTestCase;

import java.io.File;

public class LocalRecipePathsTest extends ZutubiTestCase
{
    public void testAbsoluteOutputPath()
    {
        File base = getAbsolute("/c/tmp");
        File output = getAbsolute("/d/tmp");

        LocalRecipePaths paths = new LocalRecipePaths(base, output.getAbsolutePath());

        assertEquals(base, paths.getBaseDir());
        assertEquals(output, paths.getOutputDir());
    }

    public void testRelativeOutputPath()
    {
        File base = getAbsolute("/c/tmp");
        File output = getAbsolute("/c/tmp/relative");

        LocalRecipePaths paths = new LocalRecipePaths(base, "relative");

        assertEquals(base, paths.getBaseDir());
        assertEquals(output, paths.getOutputDir());
    }

    public void testRelativeWork()
    {
        File base = new File("tmp");
        File output = new File("tmp", "relative");

        LocalRecipePaths paths = new LocalRecipePaths(base, "relative");

        assertEquals(base, paths.getBaseDir());
        assertEquals(output, paths.getOutputDir());
    }

    private File getAbsolute(String str)
    {
        return new File(str).getAbsoluteFile();
    }
}