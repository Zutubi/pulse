package com.zutubi.pulse.core.commands.maven2;

import com.zutubi.pulse.core.resources.api.ResourceConfiguration;
import com.zutubi.pulse.core.resources.api.ResourcePropertyConfiguration;
import com.zutubi.pulse.core.resources.api.ResourceVersionConfiguration;
import static com.zutubi.pulse.core.test.api.Matchers.matchesRegex;
import com.zutubi.pulse.core.test.api.PulseTestCase;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class Maven2ResourceLocatorTest extends PulseTestCase
{
    public void testResourceFound() throws IOException
    {
        Maven2ResourceLocator locator = new Maven2ResourceLocator();
        List<ResourceConfiguration> configurations = locator.locate();
        assertEquals(1, configurations.size());
        
        ResourceConfiguration resource = configurations.get(0);
        assertEquals("maven2", resource.getName());
        
        Map<String,ResourceVersionConfiguration> versions = resource.getVersions();
        assertEquals(1, versions.size());
        
        ResourceVersionConfiguration version = versions.get(resource.getDefaultVersion());
        assertNotNull(version);
        assertThat(version.getValue(), matchesRegex("[.0-9]+"));
        
        assertTrue(version.hasProperty("maven2.bin"));
        
        ResourcePropertyConfiguration home = version.getProperty("MAVEN2_HOME");
        assertNotNull(home);
        assertEquals(getCanonicalHome(), home.getValue());
    }

    private String getCanonicalHome() throws IOException
    {
        String path = System.getenv("MAVEN2_HOME");
        File file = new File(path);
        return file.getCanonicalPath();
    }
}
