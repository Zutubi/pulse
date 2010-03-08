package com.zutubi.pulse.core.dependency.ivy;

import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.util.junit.ZutubiTestCase;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;

import static com.zutubi.pulse.core.dependency.ivy.IvyModuleDescriptor.UNKNOWN;

public class IvyClientTest extends ZutubiTestCase
{
    private static final String TEST_CONF = "someconf";

    private IvyClient client;

    private File tmp;
    private File repositoryBase;
    private File workBase;

    private IvyModuleDescriptor descriptor;
    private IvyConfiguration configuration;
    private String standardRetrievalPattern;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        tmp = createTempDirectory();
        repositoryBase = new File(tmp, "repository");
        workBase = new File(tmp, "work");
        standardRetrievalPattern = workBase.getCanonicalPath() + "/[artifact](-[revision])(.[ext])";
        File cacheBase = new File(tmp, "cache");

        configuration = new IvyConfiguration(repositoryBase.toURI().toString());
        configuration.setCacheBase(cacheBase);
        client = new IvyClient(configuration);
//        client.pushMessageLogger(new DefaultMessageLogger(Message.MSG_VERBOSE));

        descriptor = new IvyModuleDescriptor("org", "modu!le", "revision", configuration);
    }

    @Override
    protected void tearDown() throws Exception
    {
        removeDirectory(tmp);

        super.tearDown();
    }

    public void testPublishNoArtifacts() throws IOException
    {
        client.publishArtifacts(descriptor);

        assertEquals(null, repositoryBase.list());
    }

    public void testPublishSingleArtifact() throws IOException
    {
        descriptor.addArtifact(createArtifact("artifact.jar"), "build");

        client.publishArtifacts(descriptor);

        assertExists(repositoryBase, "org/modu$21le/build/artifact-revision.jar");
    }

    public void testPublishMultipleArtifacts() throws IOException
    {
        descriptor.addArtifact(createArtifact("artifactA.jar"), "build");
        descriptor.addArtifact(createArtifact("artifactB.jar"), "build");

        client.publishArtifacts(descriptor);

        assertExists(repositoryBase, "org/modu$21le/build/artifactA-revision.jar");
        assertExists(repositoryBase, "org/modu$21le/build/artifactB-revision.jar");
    }

    public void testPublishArtifactByConf() throws IOException
    {
        descriptor.addArtifact(createArtifact("artifactA.jar"), "buildA");
        descriptor.addArtifact(createArtifact("artifactB.jar"), "buildB");

        client.publishArtifacts(descriptor, "buildA");

        assertExists(repositoryBase, "org/modu$21le/buildA/artifactA-revision.jar");
        assertNotExists(repositoryBase, "org/modu$21le/buildB/artifactB-revision.jar");
    }

    public void testPublishDescriptor() throws IOException, ParseException
    {
        client.publishDescriptor(descriptor);
        
        assertExists(repositoryBase, "org/modu$21le/ivy-revision.xml");
    }

    public void testRetrieveArtifact() throws IOException, ParseException
    {
        publishArtifactsAndDescriptor("artifact.jar");

        IvyModuleDescriptor retrievalDescriptor = new IvyModuleDescriptor("org", "moduleB", "revision", configuration);
        retrievalDescriptor.addDependency(descriptor.getModuleRevisionId(), TEST_CONF);

        IvyRetrievalReport report = client.retrieveArtifacts(retrievalDescriptor.getDescriptor(), TEST_CONF, standardRetrievalPattern, true);

        assertExists(workBase, "artifact-revision.jar");
        assertEquals(1, report.getRetrievedArtifacts().size());
    }

    public void testRetrieveMultipleArtifacts() throws IOException, ParseException
    {
        publishArtifactsAndDescriptor("artifactA.jar", "artifactB.jar");

        IvyModuleDescriptor retrievalDescriptor = new IvyModuleDescriptor("org", "moduleB", "revision", configuration);
        retrievalDescriptor.addDependency(descriptor.getModuleRevisionId(), TEST_CONF);

        IvyRetrievalReport report = client.retrieveArtifacts(retrievalDescriptor.getDescriptor(), TEST_CONF, standardRetrievalPattern, true);

        assertExists(workBase, "artifactA-revision.jar");
        assertExists(workBase, "artifactB-revision.jar");
        assertEquals(2, report.getRetrievedArtifacts().size());
    }

    public void testRetrieveSyncDestination() throws IOException, ParseException
    {
        assertTrue(workBase.mkdirs());
        File innocentBystanderFile = new File(workBase, "foo");
        assertTrue(innocentBystanderFile.createNewFile());
        
        publishArtifactsAndDescriptor("artifact.jar");

        IvyModuleDescriptor retrievalDescriptor = new IvyModuleDescriptor("org", "moduleB", "revision", configuration);
        retrievalDescriptor.addDependency(descriptor.getModuleRevisionId(), TEST_CONF);

        client.retrieveArtifacts(retrievalDescriptor.getDescriptor(), TEST_CONF, standardRetrievalPattern, true);

        assertExists(workBase, "artifact-revision.jar");
        assertFalse(innocentBystanderFile.exists());        
    }
    
    public void testRetrieveDoNotSyncDestination() throws IOException, ParseException
    {
        assertTrue(workBase.mkdirs());
        File innocentBystanderFile = new File(workBase, "foo");
        assertTrue(innocentBystanderFile.createNewFile());
        publishArtifactsAndDescriptor("artifact.jar");

        IvyModuleDescriptor retrievalDescriptor = new IvyModuleDescriptor("org", "moduleB", "revision", configuration);
        retrievalDescriptor.addDependency(descriptor.getModuleRevisionId(), TEST_CONF);
        
        client.retrieveArtifacts(retrievalDescriptor.getDescriptor(), TEST_CONF, standardRetrievalPattern, false);

        assertExists(workBase, "artifact-revision.jar");
        assertTrue(innocentBystanderFile.exists());        
    }
    
    public void testTransitiveDependency() throws IOException, ParseException
    {
        publishArtifactsAndDescriptor("artifact.jar");

        IvyModuleDescriptor descriptorB = new IvyModuleDescriptor("org", "moduleB", "revision", configuration);
        descriptorB.addDependency(descriptor.getModuleRevisionId(), TEST_CONF, true);
        client.publishDescriptor(descriptorB);

        IvyModuleDescriptor descriptorC = new IvyModuleDescriptor("org", "moduleC", "revision", configuration);
        descriptorC.addDependency(descriptor.getModuleRevisionId(), TEST_CONF, true);

        IvyRetrievalReport report = client.retrieveArtifacts(descriptorC.getDescriptor(), TEST_CONF, standardRetrievalPattern, true);

        assertExists(workBase, "artifact-revision.jar");
        assertEquals(1, report.getRetrievedArtifacts().size());
    }

    public void testRetrievalFailure() throws IOException, ParseException
    {
        descriptor.addArtifact(createArtifact("artifact.jar"), "build");
        client.publishDescriptor(descriptor);

        IvyModuleDescriptor retrievalDescriptor = new IvyModuleDescriptor("org", "moduleB", "revision", configuration);
        retrievalDescriptor.addDependency(descriptor.getModuleRevisionId(), TEST_CONF);

        IvyRetrievalReport report = client.retrieveArtifacts(retrievalDescriptor.getDescriptor(), TEST_CONF, standardRetrievalPattern, true);
        assertTrue(report.hasFailures());
    }

    public void testResolveFailure() throws IOException, ParseException
    {
        descriptor.addArtifact(createArtifact("artifact.jar"), "build");

        IvyModuleDescriptor retrievalDescriptor = new IvyModuleDescriptor("org", "moduleB", "revision", configuration);
        retrievalDescriptor.addDependency(descriptor.getModuleRevisionId(), TEST_CONF);

        IvyRetrievalReport report = client.retrieveArtifacts(retrievalDescriptor.getDescriptor(), TEST_CONF, standardRetrievalPattern, true);
        assertTrue(report.hasFailures());
    }

    public void testResolveOptionalDependencyConfMissing() throws IOException, ParseException
    {
        client.publishDescriptor(descriptor);

        IvyModuleDescriptor retrievalDescriptor = new IvyModuleDescriptor("org", "moduleB", "revision", configuration);
        retrievalDescriptor.addOptionalDependency(descriptor.getModuleRevisionId().getName());
        retrievalDescriptor.addDependency(descriptor.getModuleRevisionId(), TEST_CONF, "nosuchconf");

        IvyRetrievalReport report = client.retrieveArtifacts(retrievalDescriptor.getDescriptor(), TEST_CONF, standardRetrievalPattern, true);
        assertFalse(report.hasFailures());
    }

    public void testResolveRequiredDependencyConfMissing() throws IOException, ParseException
    {
        client.publishDescriptor(descriptor);

        IvyModuleDescriptor retrievalDescriptor = new IvyModuleDescriptor("org", "moduleB", "revision", configuration);
        retrievalDescriptor.addDependency(descriptor.getModuleRevisionId(), TEST_CONF, "nosuchconf");

        IvyRetrievalReport report = client.retrieveArtifacts(retrievalDescriptor.getDescriptor(), TEST_CONF, standardRetrievalPattern, true);
        assertTrue(report.hasFailures());
    }
    
    public void testResolveOptionalAndRequiredDependencyConfMissing() throws IOException, ParseException
    {
        IvyModuleDescriptor secondUpstreamDescriptor = new IvyModuleDescriptor("org", "another module", "revision", configuration);
        client.publishDescriptor(descriptor);
        client.publishDescriptor(secondUpstreamDescriptor);

        IvyModuleDescriptor retrievalDescriptor = new IvyModuleDescriptor("org", "moduleB", "revision", configuration);
        retrievalDescriptor.addOptionalDependency(descriptor.getModuleRevisionId().getName());
        retrievalDescriptor.addDependency(descriptor.getModuleRevisionId(), TEST_CONF, "nosuchconf");
        retrievalDescriptor.addDependency(secondUpstreamDescriptor.getModuleRevisionId(), TEST_CONF, "nosuchconf");

        IvyRetrievalReport report = client.retrieveArtifacts(retrievalDescriptor.getDescriptor(), TEST_CONF, standardRetrievalPattern, true);
        assertEquals(1, report.getFailures().size());
    }

    public void testUnresolvedTransiantDependency() throws IOException, ParseException
    {
        descriptor.addArtifact(createArtifact("artifact.jar"), "build");

        IvyModuleDescriptor descriptorB = new IvyModuleDescriptor("org", "moduleB", "revision", configuration);
        descriptorB.addDependency(descriptor.getModuleRevisionId(), TEST_CONF, true);
        client.publishDescriptor(descriptorB);

        IvyModuleDescriptor descriptorC = new IvyModuleDescriptor("org", "moduleC", "revision", configuration);
        descriptorC.addArtifact(createArtifact("artifactC.jar"), "build");
        descriptorC.addDependency(descriptorB.getModuleRevisionId(), TEST_CONF, true);
        client.publishArtifacts(descriptorC);
        client.publishDescriptor(descriptorC);

        IvyModuleDescriptor descriptorD = new IvyModuleDescriptor("org", "moduleD", "revision", configuration);
        descriptorD.addDependency(descriptorC.getModuleRevisionId(), TEST_CONF, true);

        IvyRetrievalReport report = client.retrieveArtifacts(descriptorD.getDescriptor(), TEST_CONF, standardRetrievalPattern, true);
        assertTrue(report.hasFailures());
    }

    public void testNonTransitiveDependencyIsNotResolved() throws IOException, ParseException
    {
        descriptor.addArtifact(createArtifact("artifactA.jar"), "build");

        IvyModuleDescriptor descriptorB = new IvyModuleDescriptor("org", "moduleB", "revision", configuration);
        descriptorB.addDependency(descriptor.getModuleRevisionId(), TEST_CONF, false);
        descriptorB.addArtifact(createArtifact("artifactB.jar"), "build");
        client.publishArtifacts(descriptorB);
        client.publishDescriptor(descriptorB);

        IvyModuleDescriptor descriptorC = new IvyModuleDescriptor("org", "moduleC", "revision", configuration);
        descriptorC.addDependency(descriptorB.getModuleRevisionId(), TEST_CONF, false);

        IvyRetrievalReport report = client.retrieveArtifacts(descriptorC.getDescriptor(), TEST_CONF, standardRetrievalPattern, true);
        assertFalse(report.hasFailures());
    }

    public void testRetrieveWhereNoDownloadIsRequired() throws IOException, ParseException
    {
        publishArtifactsAndDescriptor("artifactB.jar");

        IvyModuleDescriptor retrievalDescriptor = new IvyModuleDescriptor("org", "moduleB", "revision", configuration);
        retrievalDescriptor.addDependency(descriptor.getModuleRevisionId(), TEST_CONF);

        IvyRetrievalReport report = client.retrieveArtifacts(retrievalDescriptor.getDescriptor(), TEST_CONF, standardRetrievalPattern, true);

        assertExists(workBase, "artifactB-revision.jar");
        assertEquals(1, report.getRetrievedArtifacts().size());

        report = client.retrieveArtifacts(retrievalDescriptor.getDescriptor(), TEST_CONF, standardRetrievalPattern, true);
        assertEquals(1, report.getRetrievedArtifacts().size());
    }

    public void testPublishAndRetrieveArtifactWithoutExtension() throws IOException, ParseException
    {
        descriptor.addArtifact("artifact", UNKNOWN, UNKNOWN, createArtifact("artifact"), "build");
        client.publishArtifacts(descriptor);
        client.publishDescriptor(descriptor);

        assertExists(repositoryBase, "org/modu$21le/build/artifact-revision");

        IvyModuleDescriptor retrievalDescriptor = new IvyModuleDescriptor("org", "moduleB", "revision", configuration);
        retrievalDescriptor.addDependency(descriptor.getModuleRevisionId(), TEST_CONF);

        IvyRetrievalReport report = client.retrieveArtifacts(retrievalDescriptor.getDescriptor(), TEST_CONF, standardRetrievalPattern, true);

        assertExists(workBase, "artifact-revision");
        assertEquals(1, report.getRetrievedArtifacts().size());
    }

    private void publishArtifactsAndDescriptor(String... artifacts) throws IOException
    {
        for (String artifact: artifacts)
        {
            descriptor.addArtifact(createArtifact(artifact), "build");
        }
        
        client.publishArtifacts(descriptor);
        client.publishDescriptor(descriptor);
    }

    private File createArtifact(String path) throws IOException
    {
        String parentPath = PathUtils.getParentPath(path);
        File parentDir = (parentPath != null) ? new File(workBase, parentPath) : workBase;
        if (!parentDir.isDirectory() && !parentDir.mkdirs())
        {
            throw new RuntimeException();
        }

        String name = PathUtils.getBaseName(path);
        File artifact = new File(parentDir, name);
        if (!artifact.createNewFile())
        {
            throw new RuntimeException();
        }
        return artifact;
    }

    private void assertExists(File base, String path)
    {
        assertTrue(new File(base, path).isFile());
    }

    private void assertNotExists(File base, String path)
    {
        assertFalse(new File(base, path).isFile());
    }
}
