package com.zutubi.pulse.model.persistence.hibernate;

import com.zutubi.pulse.bootstrap.ComponentContext;
import com.zutubi.pulse.core.model.Feature;
import com.zutubi.pulse.core.model.PlainFeature;
import com.zutubi.pulse.core.model.StoredArtifact;
import com.zutubi.pulse.core.model.StoredFileArtifact;
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
        artifactDao = null;
        super.tearDown();
    }

    public void testSaveAndLoad()
    {
        StoredArtifact parent = new StoredArtifact();
        artifactDao.save(parent);

        StoredFileArtifact artifact = new StoredFileArtifact("some/path", "text/plain");
        parent.add(artifact);

        artifact.addFeature(new PlainFeature(Feature.Level.ERROR, "summary", 2));

        fileArtifactDao.save(artifact);
        commitAndRefreshTransaction();

        StoredFileArtifact otherArtifact = fileArtifactDao.findById(artifact.getId());
        assertEquals(1, otherArtifact.getFeatures().size());
        assertPropertyEquals(artifact, otherArtifact);
    }
}
