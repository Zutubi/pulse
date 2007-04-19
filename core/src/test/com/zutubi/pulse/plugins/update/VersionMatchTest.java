package com.zutubi.pulse.plugins.update;

import com.zutubi.pulse.test.PulseTestCase;
import com.zutubi.util.CollectionUtils;
import com.zutubi.pulse.plugins.Version;

/**
 */
public class VersionMatchTest extends PulseTestCase
{
    private static final Version INSTALLED = new Version("2.4.8.v20070111");
    private static final Version IDENTICAL = new Version("2.4.8.v20070111");
    private static final Version WITHOUT_QUALIFIER = new Version("2.4.8");
    private static final Version HIGHER_QUALIFIER = new Version("2.4.8.v20070122");
    private static final Version LOWER_QUALIFIER = new Version("2.4.8.v20070101");
    private static final Version HIGHER_SERVICE = new Version("2.4.9");
    private static final Version LOWER_SERVICE = new Version("2.4.7");
    private static final Version HIGHER_MINOR = new Version("2.18.0");
    private static final Version LOWER_MINOR = new Version("2.3.100");
    private static final Version HIGHER_MAJOR = new Version("10.0.0");
    private static final Version LOWER_MAJOR = new Version("1.0.0");

    public static final Version[] ALL = { IDENTICAL, WITHOUT_QUALIFIER, HIGHER_QUALIFIER, LOWER_QUALIFIER, HIGHER_SERVICE, LOWER_SERVICE, HIGHER_MINOR, LOWER_MINOR, HIGHER_MAJOR, LOWER_MAJOR };
    public static final Version[] PERFECT = { IDENTICAL, WITHOUT_QUALIFIER };
    public static final Version[] EQUIVALENT = { IDENTICAL, WITHOUT_QUALIFIER, LOWER_QUALIFIER, LOWER_SERVICE };
    public static final Version[] COMPATIBLE = { IDENTICAL, WITHOUT_QUALIFIER, LOWER_QUALIFIER, LOWER_SERVICE, LOWER_MINOR };
    public static final Version[] GREATOR_OR_EQUAL = { IDENTICAL, WITHOUT_QUALIFIER, LOWER_QUALIFIER, LOWER_SERVICE, LOWER_MINOR, LOWER_MAJOR };

    public void testPerfect()
    {
        helper(VersionMatch.PERFECT, PERFECT);
    }

    public void testEquivalent()
    {
        helper(VersionMatch.EQUIVALENT, EQUIVALENT);
    }

    public void testCompatible()
    {
        helper(VersionMatch.COMPATIBLE, COMPATIBLE);
    }

    public void testGreaterOrEquals()
    {
        helper(VersionMatch.GREATER_OR_EQUAL, GREATOR_OR_EQUAL);
    }

    private void helper(VersionMatch match, Version[] matchingRequiredVersions)
    {
        System.out.println("Testing '" + match + "'");
        for(Version v: ALL)
        {
            System.out.println("  trying '" + v + "'");
            assertEquals(CollectionUtils.containsIdentity(matchingRequiredVersions, v), match.versionsMatch(INSTALLED, v));
        }
    }
}
