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

package com.zutubi.i18n.bundle;

import com.zutubi.util.junit.ZutubiTestCase;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class ExpanderTest extends ZutubiTestCase
{
    private Expander expander;

    protected void setUp() throws Exception
    {
        super.setUp();

        expander = new Expander();
    }

    protected void tearDown() throws Exception
    {
        expander = null;

        super.tearDown();
    }

    public void testExpandWithVariant()
    {
        List<String> names = expander.expand("base", new Locale("de", "de", "ch"), ".x");
        assertEquals(4, names.size());
        assertEquals(Arrays.asList("base.x", "base_de.x", "base_de_DE.x", "base_de_DE_ch.x"), names);
    }

    private void assertEquals(List<String> is, List<String> should)
    {
        assertEquals(is.size(),should.size());
        for (String toCheck : should)
        {
            assertTrue(is.contains(toCheck));
        }
    }
}
