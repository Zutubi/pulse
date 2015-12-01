package com.zutubi.pulse.master.model.persistence.hibernate;

import com.zutubi.pulse.core.model.StoredArtifact;
import com.zutubi.pulse.core.model.StoredFileArtifact;
import com.zutubi.pulse.core.spring.SpringComponentContext;
import com.zutubi.pulse.master.model.persistence.ArtifactDao;
import com.zutubi.pulse.master.model.persistence.FileArtifactDao;

/**
 */
public class HibernateFileArtifactDaoTest extends MasterPersistenceTestCase
{
    private ArtifactDao artifactDao;
    private FileArtifactDao fileArtifactDao;

    protected void setUp() throws Exception
    {
        super.setUp();
        artifactDao = SpringComponentContext.getBean("artifactDao");
        fileArtifactDao = SpringComponentContext.getBean("fileArtifactDao");
    }

    public void testSaveAndLoad()
    {
        StoredArtifact parent = new StoredArtifact();
        artifactDao.save(parent);

        StoredFileArtifact artifact = new StoredFileArtifact("some/path", "text/plain");
        parent.add(artifact);

        fileArtifactDao.save(artifact);
        commitAndRefreshTransaction();

        StoredFileArtifact otherArtifact = fileArtifactDao.findById(artifact.getId());
        assertPropertyEquals(artifact, otherArtifact);
    }
}
