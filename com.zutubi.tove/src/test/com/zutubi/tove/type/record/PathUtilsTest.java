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

package com.zutubi.tove.type.record;

import com.zutubi.util.junit.ZutubiTestCase;

public class PathUtilsTest extends ZutubiTestCase
{
    public void testIsUnderSamePath()
    {
        assertTrue(PathUtils.isUnder("same", "same"));
    }

    public void testIsUnderSameMultiElementPath()
    {
        assertTrue(PathUtils.isUnder("same/path", "same/path"));
    }

    public void testIsUnderSameTrailingSlash()
    {
        assertTrue(PathUtils.isUnder("same/path/", "same/path/"));
    }

    public void testIsUnderSameTrailingSlashOnPath()
    {
        assertTrue(PathUtils.isUnder("same/path/", "same/path"));
    }

    public void testIsUnderSameTrailingSlashOnPrefix()
    {
        assertTrue(PathUtils.isUnder("same/path", "same/path/"));
    }

    public void testIsUnderPrefixMatches()
    {
        assertTrue(PathUtils.isUnder("a/full/path", "a/full"));
    }

    public void testIsUnderPrefixMatchesTrailingSlash()
    {
        assertTrue(PathUtils.isUnder("a/full/path/", "a/full/"));
    }

    public void testIsUnderPrefixMatchesTrailingSlashOnPath()
    {
        assertTrue(PathUtils.isUnder("a/full/path/", "a/full"));
    }

    public void testIsUnderPrefixMatchesTrailingSlashOnPrefix()
    {
        assertTrue(PathUtils.isUnder("a/full/path", "a/full/"));
    }

    public void testIsUnderPrefixDoesntMatch()
    {
        assertFalse(PathUtils.isUnder("a/full/path", "another/full"));
    }

    public void testIsUnderPrefixDoesntMatchTrailingSlash()
    {
        assertFalse(PathUtils.isUnder("a/full/path/", "another/full/"));
    }

    public void testIsUnderStartsWithPrefixButNotFullPathElement()
    {
        assertFalse(PathUtils.isUnder("a/full/path", "a/fu"));
    }

    public void testIsUnderStartsWithPrefixButNotFullPathElementTrailingSlash()
    {
        assertFalse(PathUtils.isUnder("a/full/path/", "a/fu/"));
    }
    
    public void testPrefixMatchesSimpleMatch()
    {
        assertTrue(PathUtils.prefixMatchesPathPattern("simple", "simple"));
    }

    public void testPrefixMatchesSimpleMismatch()
    {
        assertFalse(PathUtils.prefixMatchesPathPattern("simple", "notsimple"));
    }

    public void testPrefixMatchesMultipleMatch()
    {
        assertTrue(PathUtils.prefixMatchesPathPattern("simple/stuff", "simple"));
        assertTrue(PathUtils.prefixMatchesPathPattern("simple/stuff", "simple/stuff"));
    }

    public void testPrefixMatchesMultipleMismatch()
    {
        assertFalse(PathUtils.prefixMatchesPathPattern("simple/stuff", "stuff"));
    }

    public void testPrefixMatchesWildcardMatch()
    {
        assertTrue(PathUtils.prefixMatchesPathPattern("*/stuff", "some"));
        assertTrue(PathUtils.prefixMatchesPathPattern("*/stuff", "some/stuff"));
    }

    public void testPrefixMatchesWildcardMismatch()
    {
        assertFalse(PathUtils.prefixMatchesPathPattern("*/stuff", "some/thingelse"));
    }

    public void testPrefixMatchesLongerPrefix()
    {
        assertFalse(PathUtils.prefixMatchesPathPattern("simple", "simple/simple/simple"));
        assertFalse(PathUtils.prefixMatchesPathPattern("simple", "complex/complex/complex"));
    }

    public void testGetSuffixEmpty()
    {
        assertEquals("", PathUtils.getSuffix("", 0));
        assertEquals("", PathUtils.getSuffix("", 1));
        assertEquals("", PathUtils.getSuffix("", 2));
    }

    public void testGetSuffixSingleElement()
    {
        assertEquals("el", PathUtils.getSuffix("el", 0));
        assertEquals("", PathUtils.getSuffix("el", 1));
        assertEquals("", PathUtils.getSuffix("el", 2));
    }

    public void testGetSuffixTwoElements()
    {
        assertEquals("el1/el2", PathUtils.getSuffix("el1/el2", 0));
        assertEquals("el2", PathUtils.getSuffix("el1/el2", 1));
        assertEquals("", PathUtils.getSuffix("el1/el2", 2));
    }

    public void testGetSuffixManyElements()
    {
        assertEquals("el1/el2/el3/el4", PathUtils.getSuffix("el1/el2/el3/el4", 0));
        assertEquals("el2/el3/el4", PathUtils.getSuffix("el1/el2/el3/el4", 1));
        assertEquals("el3/el4", PathUtils.getSuffix("el1/el2/el3/el4", 2));
    }

    public void testGetElementEmpty()
    {
        assertEquals("", PathUtils.getElement("", 0));
        assertEquals("", PathUtils.getElement("", 1));
        assertEquals("", PathUtils.getElement("", 2));
    }

    public void testGetElementSingleElement()
    {
        assertEquals("one", PathUtils.getElement("one", 0));
        assertEquals("", PathUtils.getElement("one", 1));
        assertEquals("", PathUtils.getElement("one", 2));
    }

    public void testGetElementTwoElements()
    {
        assertEquals("one", PathUtils.getElement("one/two", 0));
        assertEquals("two", PathUtils.getElement("one/two", 1));
        assertEquals("", PathUtils.getElement("one/two", 2));
    }

    public void testGetElementManyElements()
    {
        assertEquals("one", PathUtils.getElement("one/two/three/four/five", 0));
        assertEquals("two", PathUtils.getElement("one/two/three/four/five", 1));
        assertEquals("three", PathUtils.getElement("one/two/three/four/five", 2));
    }
}
