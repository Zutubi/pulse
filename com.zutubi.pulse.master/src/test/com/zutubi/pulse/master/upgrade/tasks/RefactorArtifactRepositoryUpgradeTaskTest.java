package com.zutubi.pulse.master.upgrade.tasks;

import com.zutubi.pulse.core.dependency.ivy.IvyClient;
import com.zutubi.pulse.core.dependency.ivy.IvyConfiguration;
import com.zutubi.pulse.core.dependency.ivy.IvyModuleDescriptor;
import com.zutubi.pulse.master.util.monitor.TaskException;
import com.zutubi.util.junit.ZutubiTestCase;

import java.io.File;
import java.io.IOException;

public class RefactorArtifactRepositoryUpgradeTaskTest extends ZutubiTestCase
{
    private File tmpDir;
    private File repositoryBase;
    private File workBase;
    private RefactorArtifactRepositoryUpgradeTask task;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        tmpDir = createTempDirectory();
        repositoryBase = new File(tmpDir, "repository");
        workBase = new File(tmpDir, "work");

        task = new TestRefactorArtifactRepositoryUpgradeTask();
        task.setRepositoryBase(repositoryBase);
    }

    @Override
    protected void tearDown() throws Exception
    {
        removeDirectory(tmpDir);

        super.tearDown();
    }

    public void testArtifactPatternUpdate() throws Exception
    {
        // setup the artifact repository to test.
        String existingArtifactPattern = "([organisation]/)[module]/([stage]/)[type]s/[artifact](-[revision])(.[ext])";
        String newArtifactPattern = "([organisation]/)[module]/([stage]/)[artifact](-[revision])(.[ext])";

        setupRepository(existingArtifactPattern);
        assertRepositoryPath("org/moduleA/ivy-revision.xml");
        assertRepositoryPath("org/moduleA/stage/jars/artifactA-revision.jar");
        assertRepositoryPath("org/moduleA/stage/jars/artifactB-revision.jar");
        assertRepositoryPath("moduleB/ivy-revision.xml");
        assertRepositoryPath("moduleB/stage/jars/artifactC-revision.jar");
        assertRepositoryPath("moduleB/stage/txts/artifactD-revision.txt");

        task.execute(existingArtifactPattern, newArtifactPattern);

        assertRepositoryPath("org/moduleA/ivy-revision.xml");
        assertRepositoryPath("org/moduleA/stage/artifactA-revision.jar");
        assertRepositoryPath("org/moduleA/stage/artifactB-revision.jar");
        assertRepositoryPath("moduleB/ivy-revision.xml");
        assertRepositoryPath("moduleB/stage/artifactC-revision.jar");
        assertRepositoryPath("moduleB/stage/artifactD-revision.txt");
    }

    private void assertRepositoryPath(String path)
    {
        assertTrue("Path " + path + " not found in repository.", new File(repositoryBase, path).exists());
    }

    private void setupRepository(String artifactPattern) throws Exception
    {
        IvyConfiguration config = new IvyConfiguration(repositoryBase, "([organisation]/)[module]", artifactPattern, "([organisation]/)[module]/ivy(-[revision]).xml");
        IvyClient ivyClient = new IvyClient(config);

        IvyModuleDescriptor descriptorA = new IvyModuleDescriptor("org", "moduleA", "revision", config);
        descriptorA.addArtifact(createFile("artifactA.jar"), "stage");
        descriptorA.addArtifact(createFile("artifactB.jar"), "stage");
        ivyClient.publishDescriptor(descriptorA);
        ivyClient.publishArtifacts(descriptorA);

        IvyModuleDescriptor descriptorB = new IvyModuleDescriptor("", "moduleB", "revision", config);
        descriptorB.addArtifact(createFile("artifactC.jar"), "stage");
        descriptorB.addArtifact(createFile("artifactD.txt"), "stage");
        ivyClient.publishDescriptor(descriptorB);
        ivyClient.publishArtifacts(descriptorB);
    }

    private File createFile(String path) throws IOException
    {
        File fileToCreate = new File(workBase, path);
        fileToCreate.getParentFile().mkdirs();
        assertTrue(fileToCreate.createNewFile());
        return fileToCreate;
    }

    private class TestRefactorArtifactRepositoryUpgradeTask extends RefactorArtifactRepositoryUpgradeTask
    {
        public void execute() throws TaskException
        {
            // noop.  We call the base classes execute directly for this testing.
        }
    }
}
