/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.model.persistence.hibernate;

import com.zutubi.pulse.bootstrap.ComponentContext;
import com.zutubi.pulse.core.model.*;
import com.zutubi.pulse.model.persistence.ArtifactDao;
import com.zutubi.pulse.model.persistence.FileArtifactDao;

/**
 */
public class HibernateFileArtifactDaoTest extends MasterPersistenceTestCase
{
    private ArtifactDao artifactDao;
    private FileArtifactDao fileArtifactDao;

    protected void setUp() throws Exception
    {
        super.setUp();
        artifactDao = (ArtifactDao) ComponentContext.getBean("artifactDao");
        fileArtifactDao = (FileArtifactDao) ComponentContext.getBean("fileArtifactDao");
    }

    protected void tearDown() throws Exception
    {
        fileArtifactDao = null;
        super.tearDown();
    }

    public void testSaveAndLoad()
    {
        StoredArtifact parent = new StoredArtifact();
        artifactDao.save(parent);

        StoredFileArtifact artifact = new StoredFileArtifact("some/path", "text/plain");
        parent.add(artifact);

        artifact.addFeature(new PlainFeature(Feature.Level.ERROR, "summary", 2));
        TestSuiteResult suiteResult = new TestSuiteResult("suite result", 100);
        TestCaseResult testCase = new TestCaseResult("case result", 20, TestCaseResult.Status.FAILURE, "my failure message");
        suiteResult.add(testCase);
        artifact.addTest(suiteResult);

        fileArtifactDao.save(artifact);
        commitAndRefreshTransaction();

        StoredFileArtifact otherArtifact = fileArtifactDao.findById(artifact.getId());
        assertEquals(1, otherArtifact.getFeatures().size());
        assertEquals(1, otherArtifact.getTests().size());
        TestSuiteResult otherSuite = (TestSuiteResult) otherArtifact.getTests().get(0);
        assertEquals(1, otherSuite.getChildren().size());
        TestCaseResult otherCase = (TestCaseResult) otherSuite.getChildren().get(0);
        assertPropertyEquals(testCase, otherCase);
        assertPropertyEquals(artifact, otherArtifact);
    }
}
