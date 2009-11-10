package com.zutubi.pulse.core.dependency.ivy;

import com.zutubi.util.RandomUtils;
import com.zutubi.util.junit.ZutubiTestCase;
import org.apache.ivy.core.module.descriptor.ModuleDescriptor;
import org.apache.ivy.plugins.parser.xml.XmlModuleDescriptorWriter;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class IvyModuleDescriptorTest extends ZutubiTestCase
{
    private File tmp;
    private IvyConfiguration configuration;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        tmp = createTempDirectory();
        configuration = new IvyConfiguration();
    }

    @Override
    protected void tearDown() throws Exception
    {
        removeDirectory(tmp);

        super.tearDown();
    }

    public void testCreateFromFile() throws Exception
    {
        IvyModuleDescriptor descriptor = new IvyModuleDescriptor("org", "module", "revision", configuration);
        descriptor.setBuildNumber(5);

        File file = writeToFile(descriptor.getDescriptor());

        IvyModuleDescriptor ivyFile = IvyModuleDescriptor.newInstance(file, configuration);

        assertEquals("revision", ivyFile.getRevision());
        assertEquals("integration", ivyFile.getStatus());
        assertEquals("org/module/ivy-revision.xml", ivyFile.getPath());

        // grr, why is the parser not picking up the extraInfo data?
        assertEquals(5L, ivyFile.getBuildNumber());
    }

    public void testArtifactPaths() throws IOException
    {
        IvyModuleDescriptor descriptor = new IvyModuleDescriptor("org", "module", "revision", configuration);
        descriptor.addArtifact(new File("artifact.jar"), "build");

        IvyModuleDescriptor ivyFile = new IvyModuleDescriptor(descriptor.getDescriptor(), configuration);
        
        List<String> paths = ivyFile.getArtifactPaths();
        assertEquals(1, paths.size());
        assertEquals("org/module/jars/artifact-revision.jar", paths.get(0));
    }

    private File writeToFile(ModuleDescriptor descriptor) throws IOException
    {
        File ivyFile = new File(tmp, "ivy" + RandomUtils.randomInt() + ".xml");
        XmlModuleDescriptorWriter.write(descriptor, ivyFile);
        return ivyFile;
    }
}
