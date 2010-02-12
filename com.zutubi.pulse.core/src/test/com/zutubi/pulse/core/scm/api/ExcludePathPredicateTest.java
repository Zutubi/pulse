package com.zutubi.pulse.core.scm.api;

import com.zutubi.pulse.core.test.api.PulseTestCase;

import java.util.Arrays;

/**
 * Note: we don't exhaustively test wildcards: we assume Ant works.  We do,
 * however, do a few trivial tests, then test for our special cases.
 */
public class ExcludePathPredicateTest extends PulseTestCase
{
    public void testIdentical()
    {
        assertAccepts("some/path/fragment", "some/path/fragment", false);
    }

    public void testDifferent()
    {
        assertAccepts("some/path/fragment", "some/other/fragment", true);
    }

    public void testBackslashes()
    {
        assertAccepts("some\\path\\fragment", "some\\path\\fragment", false);
    }

    public void testMixedSlashes()
    {
        assertAccepts("some\\path/fragment", "some/path\\fragment", false);        
    }

    public void testFileWildcard()
    {
        assertAccepts("some/path/*", "some/path/fragment", false);
    }

    public void testFileWildcardNoMatch()
    {
        assertAccepts("some/path/*", "some/path/fragment/here", true);
    }

    public void testDirWildcard()
    {
        assertAccepts("some/**/path", "some/fragment/of/a/path", false);
    }

    public void testDirWildcardNOMatch()
    {
        assertAccepts("some/**/path", "fragment/of/a/path", true);
    }

    public void testLeadingSlash()
    {
        assertAccepts("**/path", "/some/abs/path", false);
    }

    public void testLeadingSlashNoWildcard()
    {
        assertAccepts("some/**/path", "/some/abs/path", true);
    }

    public void testBothLeadingSlash()
    {
        assertAccepts("/some/**/path", "/some/abs/path", false);
    }

    public void testPerforceStyle()
    {
        assertAccepts("**/path/**", "//depot/some/abs/path/etc", false);
    }

    public void testPerforceStyleOneSlash()
    {
        assertAccepts("/**/path/**", "//depot/some/abs/path/etc", false);
    }

    public void testPerforceStyleTwoSlashes()
    {
        assertAccepts("//**/path/**", "//depot/some/abs/path/etc", false);
    }

    private void assertAccepts(String exclude, String path, boolean accept)
    {
        ExcludePathPredicate predicate = new ExcludePathPredicate(Arrays.asList(exclude));
        assertEquals(accept, predicate.satisfied(path));
    }
}
