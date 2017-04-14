/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
