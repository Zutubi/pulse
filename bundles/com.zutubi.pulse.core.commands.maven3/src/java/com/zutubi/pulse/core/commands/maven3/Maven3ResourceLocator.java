package com.zutubi.pulse.core.commands.maven3;

import com.zutubi.pulse.core.resources.api.FileSystemResourceLocator;
import com.zutubi.pulse.core.resources.api.ResourceConfiguration;
import com.zutubi.pulse.core.resources.api.ResourceLocator;
import com.zutubi.pulse.core.resources.api.StandardHomeDirectoryResourceBuilder;

import java.io.File;
import java.util.List;

/**
 * Locates a maven 3 installation.
 */
public class Maven3ResourceLocator implements ResourceLocator
{
    public List<ResourceConfiguration> locate()
    {
        final Maven3HomeDirectoryLocator homeDirectoryLocator = new Maven3HomeDirectoryLocator();
        StandardHomeDirectoryResourceBuilder builder = new StandardHomeDirectoryResourceBuilder("maven3", "mvn", true)
        {
            @Override
            protected String getVersionName(File homeDir, File binary)
            {
                String captured = homeDirectoryLocator.getCapturedVersion();
                if (captured != null)
                {
                    return captured;
                }
                
                return super.getVersionName(homeDir, binary);
            }
        };
        
        FileSystemResourceLocator delegate = new FileSystemResourceLocator(homeDirectoryLocator, builder);
        return delegate.locate();
    }
}
