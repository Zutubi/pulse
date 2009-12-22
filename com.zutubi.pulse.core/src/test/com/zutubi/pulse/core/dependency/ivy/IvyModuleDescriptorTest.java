package com.zutubi.pulse.core.dependency.ivy;

import com.zutubi.util.CollectionUtils;
import com.zutubi.util.RandomUtils;
import com.zutubi.util.junit.ZutubiTestCase;
import org.apache.ivy.core.module.descriptor.Artifact;
import org.apache.ivy.core.module.id.ModuleRevisionId;
import org.apache.ivy.plugins.parser.xml.XmlModuleDescriptorWriter;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
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

        File file = writeToFile(descriptor);

        IvyModuleDescriptor ivyFile = IvyModuleDescriptor.newInstance(file, configuration);

        assertEquals("revision", ivyFile.getRevision());
        assertEquals("integration", ivyFile.getStatus());
        assertEquals("org/module/ivy-revision.xml", ivyFile.getPath());

        assertEquals(5L, ivyFile.getBuildNumber());
    }

    public void testEncodingOfUnderlyingDescriptorsModuleRevisionId()
    {
        ModuleRevisionId original = IvyModuleRevisionId.newInstance("or*", "modul*", "rev");
        IvyModuleDescriptor descriptor = new IvyModuleDescriptor(original, configuration);
        ModuleRevisionId mrid = descriptor.getDescriptor().getModuleRevisionId();

        assertEquals(IvyEncoder.encode(original), mrid);
    }

    public void testGetIvyPath()
    {
        IvyModuleDescriptor descriptor = new IvyModuleDescriptor("org", "module", "revision", configuration);
        assertEquals("org/module/ivy-revision.xml", descriptor.getPath());        
    }

    public void testGetEncodedIvyPath()
    {
        IvyModuleDescriptor descriptor = new IvyModuleDescriptor("or*", "modul*", "revision", configuration);
        assertEquals("or$2a/modul$2a/ivy-revision.xml", descriptor.getPath());
    }

    public void testHasArtifacts()
    {
        IvyModuleDescriptor descriptor = new IvyModuleDescriptor("org", "module", "revision", configuration);
        assertFalse(descriptor.hasArtifacts());
        descriptor.addArtifact(new File("artifactA.jar"), "build");
        assertTrue(descriptor.hasArtifacts());
        descriptor.addArtifact(new File("artifactB.jar"), "build");
        assertTrue(descriptor.hasArtifacts());
    }

    public void testArtifactPaths()
    {
        IvyModuleDescriptor descriptor = new IvyModuleDescriptor("org", "module", "revision", configuration);
        descriptor.addArtifact(new File("artifact.jar"), "build");
        List<String> paths = descriptor.getArtifactPaths();
        assertEquals(Arrays.asList("org/module/build/jars/artifact-revision.jar"), paths);
    }

    public void testEncodedArtifactPaths()
    {
        IvyModuleDescriptor descriptor = new IvyModuleDescriptor("or*", "modul*", "revision", configuration);
        descriptor.addArtifact(new File("artifac*.ja*"), "buil*");
        List<String> paths = descriptor.getArtifactPaths();
        assertEquals(Arrays.asList("or$2a/modul$2a/buil$2a/ja$2as/artifac$2a-revision.ja$2a"), paths);
    }

    public void testGetArtifacts()
    {
        IvyModuleDescriptor descriptor = new IvyModuleDescriptor("org", "module", "revision", configuration);
        descriptor.addArtifact(new File("artifactA.jar"), "build");
        descriptor.addArtifact(new File("artifactB.jar"), "build");
        descriptor.addArtifact(new File("artifact*.jar"), "build");

        Artifact[] artifacts = descriptor.getAllArtifacts();
        assertEquals(3, artifacts.length);
        assertEquals("artifactA", artifacts[0].getName());
        assertEquals("artifactB", artifacts[1].getName());
        assertEquals("artifact*", artifacts[2].getName());
    }

    public void testGetArtifactsForEncodedStage()
    {
        IvyModuleDescriptor descriptor = new IvyModuleDescriptor("org", "module", "revision", configuration);
        descriptor.addArtifact(new File("artifactA.jar"), "build");
        descriptor.addArtifact(new File("artifactB.jar"), "buil*");

        assertEquals(1, descriptor.getArtifacts("build").length);
        assertEquals(1, descriptor.getArtifacts("buil*").length);
        assertEquals(0, descriptor.getArtifacts("builD").length);
    }

    public void testAddArtifact()
    {
        IvyModuleDescriptor descriptor = new IvyModuleDescriptor("org", "module", "revision", configuration);
        descriptor.addArtifact(new File("artifact.jar"), "a");
        descriptor.addArtifact(new File("art.ifact.jar"), "b");

        Artifact a = descriptor.getArtifacts("a")[0];
        assertEquals("artifact", a.getName());
        assertEquals("jar", a.getExt());

        Artifact b = descriptor.getArtifacts("b")[0];
        assertEquals("art.ifact", b.getName());
        assertEquals("jar", b.getExt());
    }

    public void testAddOptionalDependency()
    {
        final String MODULE_1 = "mod1";
        final String MODULE_2 = "mod2";

        IvyModuleDescriptor descriptor = new IvyModuleDescriptor("org", "module", "revision", configuration);
        assertEquals(0, descriptor.getOptionalDependencies().size());

        descriptor.addOptionalDependency(MODULE_1);
        assertEquals(CollectionUtils.asSet(MODULE_1), descriptor.getOptionalDependencies());

        descriptor.addOptionalDependency(MODULE_2);
        assertEquals(CollectionUtils.asSet(MODULE_1, MODULE_2), descriptor.getOptionalDependencies());

        descriptor.addOptionalDependency(MODULE_1);
        assertEquals(CollectionUtils.asSet(MODULE_1, MODULE_2), descriptor.getOptionalDependencies());
    }

    private File writeToFile(IvyModuleDescriptor descriptor) throws IOException
    {
        File ivyFile = new File(tmp, "ivy" + RandomUtils.randomInt() + ".xml");
        XmlModuleDescriptorWriter.write(descriptor.getDescriptor(), ivyFile);
        return ivyFile;
    }
}
