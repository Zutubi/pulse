package com.zutubi.pulse.plugins.update;

import com.zutubi.pulse.core.test.PulseTestCase;
import com.zutubi.util.CollectionUtils;
import com.zutubi.pulse.plugins.PluginVersion;

/**
 */
public class VersionMatchTest extends PulseTestCase
{
    private static final PluginVersion INSTALLED = new PluginVersion("2.4.8.v20070111");
    private static final PluginVersion IDENTICAL = new PluginVersion("2.4.8.v20070111");
    private static final PluginVersion WITHOUT_QUALIFIER = new PluginVersion("2.4.8");
    private static final PluginVersion HIGHER_QUALIFIER = new PluginVersion("2.4.8.v20070122");
    private static final PluginVersion LOWER_QUALIFIER = new PluginVersion("2.4.8.v20070101");
    private static final PluginVersion HIGHER_SERVICE = new PluginVersion("2.4.9");
    private static final PluginVersion LOWER_SERVICE = new PluginVersion("2.4.7");
    private static final PluginVersion HIGHER_MINOR = new PluginVersion("2.18.0");
    private static final PluginVersion LOWER_MINOR = new PluginVersion("2.3.100");
    private static final PluginVersion HIGHER_MAJOR = new PluginVersion("10.0.0");
    private static final PluginVersion LOWER_MAJOR = new PluginVersion("1.0.0");

    public static final PluginVersion[] ALL = { IDENTICAL, WITHOUT_QUALIFIER, HIGHER_QUALIFIER, LOWER_QUALIFIER, HIGHER_SERVICE, LOWER_SERVICE, HIGHER_MINOR, LOWER_MINOR, HIGHER_MAJOR, LOWER_MAJOR };
    public static final PluginVersion[] PERFECT = { IDENTICAL, WITHOUT_QUALIFIER };
    public static final PluginVersion[] EQUIVALENT = { IDENTICAL, WITHOUT_QUALIFIER, LOWER_QUALIFIER, LOWER_SERVICE };
    public static final PluginVersion[] COMPATIBLE = { IDENTICAL, WITHOUT_QUALIFIER, LOWER_QUALIFIER, LOWER_SERVICE, LOWER_MINOR };
    public static final PluginVersion[] GREATOR_OR_EQUAL = { IDENTICAL, WITHOUT_QUALIFIER, LOWER_QUALIFIER, LOWER_SERVICE, LOWER_MINOR, LOWER_MAJOR };

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

    private void helper(VersionMatch match, PluginVersion[] matchingRequiredVersions)
    {
        System.out.println("Testing '" + match + "'");
        for(PluginVersion v: ALL)
        {
            System.out.println("  trying '" + v + "'");
            assertEquals(CollectionUtils.containsIdentity(matchingRequiredVersions, v), match.versionsMatch(INSTALLED, v));
        }
    }
}
