package com.zutubi.tove.links;

import com.zutubi.tove.config.api.AbstractConfiguration;
import com.zutubi.tove.config.api.Configuration;
import com.zutubi.util.junit.ZutubiTestCase;

import java.util.Arrays;
import java.util.List;

public class ConfigurationLinksTest extends ZutubiTestCase
{
    private static final String TEST_NAME = "linkname";
    private static final String TEST_URL = "linkurl";

    public void testLinks()
    {
        ConfigurationLinks configurationLinks = new ConfigurationLinks(SubjectConfiguration.class);
        List<ConfigurationLink> links = configurationLinks.getLinks(new SubjectConfiguration());
        assertEquals(1, links.size());
        assertEquals(TEST_NAME, links.get(0).getName());
        assertEquals(TEST_URL, links.get(0).getUrl());
    }

    public void testNoLinksClass()
    {
        noLinksHelper(NoLinksClassConfiguration.class);
    }

    public void testNoDefaultConstructor()
    {
        noLinksHelper(NoDefaultConstructorConfiguration.class);
    }

    public void testNoValidListingMethod()
    {
        noLinksHelper(NoValidListingMethodConfiguration.class);
    }

    private void noLinksHelper(Class<? extends Configuration> configurationClass)
    {
        ConfigurationLinks configurationLinks = new ConfigurationLinks(configurationClass);
        assertTrue(configurationLinks.getLinks(new SubjectConfiguration()).isEmpty());
    }

    public static class SubjectConfiguration extends AbstractConfiguration
    {
    }

    public static class SubjectConfigurationLinks
    {
        public List<ConfigurationLink> getLinks(SubjectConfiguration config)
        {
            return Arrays.asList(new ConfigurationLink(TEST_NAME, TEST_URL));
        }
    }

    public static class NoLinksClassConfiguration extends AbstractConfiguration
    {
    }

    public static class NoDefaultConstructorConfiguration extends AbstractConfiguration
    {
    }

    public static class NoDefaultConstructorConfigurationLinks
    {
        public NoDefaultConstructorConfigurationLinks(String anything)
        {
        }

        public List<ConfigurationLink> getLinks(NoDefaultConstructorConfiguration config)
        {
            return Arrays.asList(new ConfigurationLink(TEST_NAME, TEST_URL));
        }
    }

    public static class NoValidListingMethodConfiguration extends AbstractConfiguration
    {
    }

    public static class NoValidListingMethodConfigurationLinks
    {
        public List<ConfigurationLink> getLinks(String config)
        {
            return Arrays.asList(new ConfigurationLink("never", "seen"));
        }

        public List<ConfigurationLink> getLinks(SubjectConfiguration config)
        {
            return Arrays.asList(new ConfigurationLink("never", "seen"));
        }

        public List<String> getLinks(NoValidListingMethodConfiguration config)
        {
            return Arrays.asList("never seen");
        }
    }
}
