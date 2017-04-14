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

package com.zutubi.pulse.core.test.api;

import com.google.common.base.Predicate;
import static com.google.common.collect.Iterables.find;
import com.zutubi.util.io.IOUtils;
import com.zutubi.util.io.ZipUtils;
import com.zutubi.util.junit.ZutubiTestCase;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.zip.ZipInputStream;

/**
 * Base class for test cases.
 */
public abstract class PulseTestCase extends ZutubiTestCase
{
    /**
     * Unzips an archive named after the current class and test method to the
     * given directory.  Equivalent to unzipInput(getName(), toDir).
     *
     * @see #unzipInput(String, java.io.File)
     *
     * @param toDir directory to extract the archive to, should already exist
     * @throws java.io.IOException if there is a problem locating or extracting the
     *         archive
     */
    public  void unzipInput(File toDir) throws IOException
    {
        unzipInput(getName(), toDir);
    }

    /**
     * Unpacks a test data zip by locating it on the classpath and exploding it
     * into the specified directory.  The zip input stream is located using
     * {@link #getInput(String, String)} with "zip" passed as the extension.
     * This effectively means the zip should be alongside the current class in
     * the classpath with name &lt;simple classname&gt;.name.zip.
     *
     * @param name  name of the zip archive, appended to the class name
     * @param toDir directory to extract the archive to, should already exist
     * @throws IOException if there is a problem locating or extracting the
     *         archive
     */
    public  void unzipInput(String name, File toDir) throws IOException
    {
        ZipInputStream is = null;
        try
        {
            is = new ZipInputStream(getInput(name, "zip"));
            ZipUtils.extractZip(is, toDir);
        }
        finally
        {
            IOUtils.close(is);
        }
    }

    /**
     * Asserts that all items in an expected list are found, in any order, in
     * the actual list.  The lists must also be of the same size, implying that
     * (when there are no duplicate elements) the lists are permutations of
     * each other.  If there are duplicate elements in both lists the assertion
     * may pass despite the lists not containing the same duplications.  Items
     * are compared using == (i.e. by identity).
     *
     * @param expected the items that are expected
     * @param actual   the actual list obtained by testing
     * @param <T> the type of the list element
     *
     * @throws junit.framework.AssertionFailedError if the actual list does not
     *         have the same items as the expected one
     */
    protected <T> void assertItemsSame(List<T> expected, List<T> actual)
    {
        assertEquals(expected.size(), actual.size());

        for (final T expectedItem : expected)
        {
            assertNotNull(find(actual, new Predicate<T>()
            {
                public boolean apply(T actualItem)
                {
                    return expectedItem == actualItem;
                }
            }, null));
        }
    }
}
