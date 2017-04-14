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

package com.zutubi.pulse.core.scm.api;

import com.zutubi.pulse.core.test.api.PulseTestCase;

import java.util.Arrays;
import java.util.Collections;

/**
 * Note: we don't exhaustively test wildcards: we assume Ant works.  We do,
 * however, do a few trivial tests, then test for our special cases.
 */
public class FilterPathsPredicateTest extends PulseTestCase
{
    public void testIdentical()
    {
        assertExclusionAccepts("some/path/fragment", "some/path/fragment", false);
    }

    public void testDifferent()
    {
        assertExclusionAccepts("some/path/fragment", "some/other/fragment", true);
    }

    public void testBackslashes()
    {
        assertExclusionAccepts("some\\path\\fragment", "some\\path\\fragment", false);
    }

    public void testMixedSlashes()
    {
        assertExclusionAccepts("some\\path/fragment", "some/path\\fragment", false);
    }

    public void testFileWildcard()
    {
        assertExclusionAccepts("some/path/*", "some/path/fragment", false);
    }

    public void testFileWildcardNoMatch()
    {
        assertExclusionAccepts("some/path/*", "some/path/fragment/here", true);
    }

    public void testDirWildcard()
    {
        assertExclusionAccepts("some/**/path", "some/fragment/of/a/path", false);
    }

    public void testDirWildcardNOMatch()
    {
        assertExclusionAccepts("some/**/path", "fragment/of/a/path", true);
    }

    public void testLeadingSlash()
    {
        assertExclusionAccepts("**/path", "/some/abs/path", false);
    }

    public void testLeadingSlashNoWildcard()
    {
        assertExclusionAccepts("some/**/path", "/some/abs/path", true);
    }

    public void testBothLeadingSlash()
    {
        assertExclusionAccepts("/some/**/path", "/some/abs/path", false);
    }

    public void testPerforceStyle()
    {
        assertExclusionAccepts("**/path/**", "//depot/some/abs/path/etc", false);
    }

    public void testPerforceStyleOneSlash()
    {
        assertExclusionAccepts("/**/path/**", "//depot/some/abs/path/etc", false);
    }

    public void testPerforceStyleTwoSlashes()
    {
        assertExclusionAccepts("//**/path/**", "//depot/some/abs/path/etc", false);
    }

    public void testInclusionIdentical()
    {
        assertInclusionAccepts("some/path/fragment", "some/path/fragment", true);
    }

    public void testInclusionDifferent()
    {
        assertInclusionAccepts("some/path/fragment", "other/path/fragment", false);
    }

    public void testInclusionPattern()
    {
        assertInclusionAccepts("*/path/fragment", "other/path/fragment", true);
    }

    public void testInclusionExclusion()
    {
        FilterPathsPredicate predicate = new FilterPathsPredicate(Arrays.asList("*/bar/baz"), Arrays.asList("foo/*/baz"));
        assertEquals(false, predicate.apply("foo/bar/baz"));
        assertEquals(true, predicate.apply("quux/bar/baz"));
    }
    
    private void assertExclusionAccepts(String exclude, String path, boolean accept)
    {
        FilterPathsPredicate predicate = new FilterPathsPredicate(Collections.<String>emptyList(), Arrays.asList(exclude));
        assertEquals(accept, predicate.apply(path));
    }

    private void assertInclusionAccepts(String include, String path, boolean accept)
    {
        FilterPathsPredicate predicate = new FilterPathsPredicate(Arrays.asList(include), Collections.<String>emptyList());
        assertEquals(accept, predicate.apply(path));
    }
}
