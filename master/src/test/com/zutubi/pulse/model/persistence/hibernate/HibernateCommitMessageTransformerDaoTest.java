package com.zutubi.pulse.model.persistence.hibernate;

import com.zutubi.pulse.committransformers.LinkCommitMessageTransformer;
import com.zutubi.pulse.model.CommitMessageTransformer;
import com.zutubi.pulse.model.Project;
import com.zutubi.pulse.model.persistence.CommitMessageTransformerDao;

import java.util.Arrays;
import java.util.List;

/**
 * @noinspection FieldCanBeLocal
 */
public class HibernateCommitMessageTransformerDaoTest extends MasterPersistenceTestCase
{
    private CommitMessageTransformerDao commitMessageTransformerDao;

    public void setUp() throws Exception
    {
        super.setUp();
        commitMessageTransformerDao = (CommitMessageTransformerDao) context.getBean("commitMessageTransformerDao");
    }

    public void tearDown() throws Exception
    {
        commitMessageTransformerDao = null;
        super.tearDown();
    }

    public void testSaveAndLoad()
    {
        CommitMessageTransformer transformer = new LinkCommitMessageTransformer("name");
        commitMessageTransformerDao.save(transformer);
        commitAndRefreshTransaction();

        CommitMessageTransformer anotherTransformer = commitMessageTransformerDao.findById(transformer.getId());

        // want to ensure that a new object has been created by hibernate and
        // that the old one is not just returned to us.
        assertFalse(transformer == anotherTransformer);
        assertPropertyEquals(transformer, anotherTransformer);
    }

    public void testFindByProject()
    {
        CommitMessageTransformer transformer1 = new LinkCommitMessageTransformer("1");
        CommitMessageTransformer transformer2 = new LinkCommitMessageTransformer("2");
        CommitMessageTransformer transformer3 = new LinkCommitMessageTransformer("3");
        transformer1.setProjects(Arrays.asList(new Long[] {Long.valueOf(1), Long.valueOf(2)}));
        transformer2.setProjects(Arrays.asList(new Long[] {Long.valueOf(2), Long.valueOf(3)}));
        transformer3.setProjects(Arrays.asList(new Long[] {Long.valueOf(1), Long.valueOf(3)}));

        commitMessageTransformerDao.save(transformer1);
        commitMessageTransformerDao.save(transformer2);
        commitMessageTransformerDao.save(transformer3);

        commitAndRefreshTransaction();

        Project p = new Project();
        p.setId(1);
        List<CommitMessageTransformer> trans = commitMessageTransformerDao.findByProject(p);
        assertEquals(2, trans.size());
        assertPropertyEquals(transformer1, trans.get(0));
        assertPropertyEquals(transformer3, trans.get(1));
    }

    public void testFindByName()
    {
        CommitMessageTransformer transformer1 = new LinkCommitMessageTransformer("1");
        CommitMessageTransformer transformer2 = new LinkCommitMessageTransformer("2");

        commitMessageTransformerDao.save(transformer1);
        commitMessageTransformerDao.save(transformer2);

        commitAndRefreshTransaction();

        CommitMessageTransformer found = commitMessageTransformerDao.findByName("1");
        assertPropertyEquals(transformer1, found);
    }
}
