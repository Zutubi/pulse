package com.zutubi.pulse.core.commands.maven3;

import com.zutubi.pulse.core.resources.api.ResourceConfiguration;
import com.zutubi.pulse.core.resources.api.ResourcePropertyConfiguration;
import com.zutubi.pulse.core.resources.api.ResourceVersionConfiguration;
import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.util.FileSystemUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static com.zutubi.pulse.core.test.api.Matchers.matchesRegex;
import static org.hamcrest.MatcherAssert.assertThat;

public class Maven3ResourceLocatorTest extends PulseTestCase
{
    public void testResourceFound() throws IOException
    {
        Maven3ResourceLocator locator = new Maven3ResourceLocator();
        List<ResourceConfiguration> configurations = locator.locate();
        assertEquals(1, configurations.size());
        
        ResourceConfiguration resource = configurations.get(0);
        assertEquals("maven3", resource.getName());
        
        Map<String,ResourceVersionConfiguration> versions = resource.getVersions();
        assertEquals(1, versions.size());
        
        ResourceVersionConfiguration version = versions.get(resource.getDefaultVersion());
        assertNotNull(version);
        assertThat(version.getValue(), matchesRegex("[.0-9]+"));
        
        assertTrue(version.hasProperty("maven3.bin"));
        
        ResourcePropertyConfiguration home = version.getProperty("MAVEN3_HOME");
        assertNotNull(home);
        assertEquals(getCanonicalHome(), home.getValue());
    }

    private String getCanonicalHome() throws IOException
    {
        String path = System.getenv("MAVEN3_HOME");
        File file = new File(path);
        return FileSystemUtils.normaliseSeparators(file.getCanonicalPath());
    }
}
