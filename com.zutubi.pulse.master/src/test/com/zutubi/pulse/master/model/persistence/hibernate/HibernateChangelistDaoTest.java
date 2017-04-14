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

import com.zutubi.pulse.core.model.PersistentChangelist;
import com.zutubi.pulse.core.model.PersistentFileChange;
import com.zutubi.pulse.core.scm.api.FileChange;
import com.zutubi.pulse.core.scm.api.Revision;
import com.zutubi.pulse.master.model.Project;
import com.zutubi.pulse.master.model.User;
import com.zutubi.pulse.master.model.persistence.ChangelistDao;
import com.zutubi.pulse.master.tove.config.user.UserConfiguration;
import com.zutubi.util.Constants;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class HibernateChangelistDaoTest extends MasterPersistenceTestCase
{
    private ChangelistDao changelistDao;

    public void setUp() throws Exception
    {
        super.setUp();
        changelistDao = (ChangelistDao) context.getBean("changelistDao");
    }

    public void testLoadSave() throws Exception
    {
        Revision revision = new Revision("wow");
        PersistentChangelist list = new PersistentChangelist(revision, System.currentTimeMillis() - Constants.YEAR, "pulse", "test changelist", Arrays.asList(new PersistentFileChange("some/random/file", "23", FileChange.Action.EDIT, false)));
        changelistDao.save(list);

        commitAndRefreshTransaction();

        PersistentChangelist otherList = changelistDao.findById(list.getId());
        assertPropertyEquals(list, otherList);

        Revision otherRevision = otherList.getRevision();
        assertPropertyEquals(revision, otherRevision);

        PersistentFileChange otherChange = otherList.getChanges().get(0);
        assertPropertyEquals(list.getChanges().get(0), otherChange);
    }

    public void testLatestForProject()
    {
        changelistDao.save(createChangelist(1, 1, "login1"));
        changelistDao.save(createChangelist(1, 2, "login2"));
        changelistDao.save(createChangelist(2, 3, "login1"));
        changelistDao.save(createChangelist(1, 4, "login1"));
        changelistDao.save(createChangelist(2, 5, "login2"));

        commitAndRefreshTransaction();
        Project p = new Project();
        p.setId(1);

        List<PersistentChangelist> changes = changelistDao.findLatestByProject(p, 10);
        assertEquals(3, changes.size());
        assertEquals("4", changes.get(0).getRevision().getRevisionString());
        assertEquals("2", changes.get(1).getRevision().getRevisionString());
        assertEquals("1", changes.get(2).getRevision().getRevisionString());
    }

    public void testLatestForProjectSameTimestamp()
    {
        // If the timestamps are identical, the latest saved revision should
        // come first (i.e. ordered by descending id).
        final int TIMESTAMP = 101000010;
        changelistDao.save(createChangelist(1, 1, TIMESTAMP, "login1"));
        changelistDao.save(createChangelist(1, 2, TIMESTAMP, "login1"));
        changelistDao.save(createChangelist(1, 3, TIMESTAMP, "login1"));
        changelistDao.save(createChangelist(1, 4, TIMESTAMP, "login1"));
        changelistDao.save(createChangelist(1, 5, TIMESTAMP, "login1"));

        commitAndRefreshTransaction();
        Project p = new Project();
        p.setId(1);

        List<PersistentChangelist> changes = changelistDao.findLatestByProject(p, 10);
        assertEquals(5, changes.size());
        assertEquals("5", changes.get(0).getRevision().getRevisionString());
        assertEquals("4", changes.get(1).getRevision().getRevisionString());
        assertEquals("3", changes.get(2).getRevision().getRevisionString());
        assertEquals("2", changes.get(3).getRevision().getRevisionString());
        assertEquals("1", changes.get(4).getRevision().getRevisionString());
    }

    public void testLatestForProjects()
    {
        changelistDao.save(createChangelist(1, 1, "login1"));
        changelistDao.save(createChangelist(1, 2, "login2"));
        changelistDao.save(createChangelist(2, 3, "login1"));
        changelistDao.save(createChangelist(1, 4, "login1"));
        changelistDao.save(createChangelist(2, 5, "login2"));
        changelistDao.save(createChangelist(3, 6, "login2"));
        changelistDao.save(createChangelist(3, 7, "login2"));
        changelistDao.save(createChangelist(2, 8, "login2"));

        commitAndRefreshTransaction();
        Project p1 = new Project();
        p1.setId(1);
        Project p3 = new Project();
        p3.setId(3);

        List<PersistentChangelist> changes = changelistDao.findLatestByProjects(new Project[] {p1, p3}, 4);
        assertEquals(4, changes.size());
        assertEquals("7", changes.get(0).getRevision().getRevisionString());
        assertEquals("6", changes.get(1).getRevision().getRevisionString());
        assertEquals("4", changes.get(2).getRevision().getRevisionString());
        assertEquals("2", changes.get(3).getRevision().getRevisionString());
    }

    public void testLatestForProjectsOverlapping()
    {
        changelistDao.save(createChangelist(1, 1, "login1"));
        changelistDao.save(createChangelist(3, 1, "login1"));
        commitAndRefreshTransaction();

        Project p1 = new Project();
        p1.setId(1);
        Project p3 = new Project();
        p3.setId(3);

        List<PersistentChangelist> changes = changelistDao.findLatestByProjects(new Project[] {p1, p3}, 10);
        assertEquals(1, changes.size());
        assertEquals("1", changes.get(0).getRevision().getRevisionString());
    }

    public void testLatestForProjectsOverlappingStillGetMax()
    {
        changelistDao.save(createChangelist(1, 1, "login1"));
        changelistDao.save(createChangelist(3, 1, "login1"));
        changelistDao.save(createChangelist(1, 2, "login1"));
        commitAndRefreshTransaction();

        Project p1 = new Project();
        p1.setId(1);
        Project p3 = new Project();
        p3.setId(3);

        List<PersistentChangelist> changes = changelistDao.findLatestByProjects(new Project[] {p1, p3}, 10);
        assertEquals(2, changes.size());
        assertEquals("2", changes.get(0).getRevision().getRevisionString());
        assertEquals("1", changes.get(1).getRevision().getRevisionString());
    }

    public void testLatestByUser()
    {
        changelistDao.save(createChangelist(1, "login1"));
        changelistDao.save(createChangelist(2, "login2"));
        changelistDao.save(createChangelist(3, "login1"));
        changelistDao.save(createChangelist(4, "login1"));
        changelistDao.save(createChangelist(5, "login2"));

        commitAndRefreshTransaction();

        List<PersistentChangelist> changes = changelistDao.findLatestByUser(createUser(), 10);
        assertEquals(3, changes.size());
        assertEquals("4", changes.get(0).getRevision().getRevisionString());
        assertEquals("3", changes.get(1).getRevision().getRevisionString());
        assertEquals("1", changes.get(2).getRevision().getRevisionString());
    }

    public void testLatestByUserAlias()
    {
        changelistDao.save(createChangelist(1, "login1"));
        changelistDao.save(createChangelist(2, "login2"));
        changelistDao.save(createChangelist(3, "login1"));
        changelistDao.save(createChangelist(4, "alias1"));
        changelistDao.save(createChangelist(5, "alias2"));
        changelistDao.save(createChangelist(6, "alias3"));

        commitAndRefreshTransaction();

        User user = createUser();
        user.getConfig().getPreferences().getAliases().add("alias1");
        user.getConfig().getPreferences().getAliases().add("alias3");

        List<PersistentChangelist> changes = changelistDao.findLatestByUser(user, 10);
        assertEquals(4, changes.size());
        assertEquals("6", changes.get(0).getRevision().getRevisionString());
        assertEquals("4", changes.get(1).getRevision().getRevisionString());
        assertEquals("3", changes.get(2).getRevision().getRevisionString());
        assertEquals("1", changes.get(3).getRevision().getRevisionString());
    }

    public void testLatestByUserOverlapping()
    {
        // Some heavy overlapping to test correctness of performance enhancements for CIB-3066.
        for (int change = 1; change <= 26; change++)
        {
            for (long project = 1; project <= 10; project++)
            {
                changelistDao.save(createChangelist(project, change, "login1"));
            }
        }

        commitAndRefreshTransaction();

        List<PersistentChangelist> changes = changelistDao.findLatestByUser(createUser(), 5);
        assertEquals(5, changes.size());
        assertEquals("26", changes.get(0).getRevision().getRevisionString());
        assertEquals("25", changes.get(1).getRevision().getRevisionString());
        assertEquals("24", changes.get(2).getRevision().getRevisionString());
        assertEquals("23", changes.get(3).getRevision().getRevisionString());
        assertEquals("22", changes.get(4).getRevision().getRevisionString());
    }


    private User createUser()
    {
        User user = new User();
        UserConfiguration config = new UserConfiguration("login1", "Login One");
        user.setConfig(config);
        return user;
    }

    public void testLookupEquivalent()
    {
        changelistDao.save(createChangelist(12, "jason"));

        commitAndRefreshTransaction();

        List<PersistentChangelist> changelists = changelistDao.findAllEquivalent(createChangelist(12, "jason"));
        assertNotNull(changelists);
        assertEquals(1, changelists.size());
        assertEquals("jason", changelists.get(0).getAuthor());
    }

    public void testLookupByCvsRevision()
    {
        PersistentChangelist list = new PersistentChangelist(new Revision("1"), 1234, "joe", "i made this", Collections.<PersistentFileChange>emptyList());

        changelistDao.save(list);

        commitAndRefreshTransaction();

        List<PersistentChangelist> otherList = changelistDao.findAllEquivalent(new PersistentChangelist(new Revision("1"), 1234, "joe", "i made this", Collections.<PersistentFileChange>emptyList()));
        assertNotNull(otherList);
        assertEquals(1, otherList.size());
        assertPropertyEquals(list, otherList.get(0));
    }

    public void testFindByResult()
    {
        PersistentChangelist list = new PersistentChangelist(createRevision(1), 1, "hmm", "yay", Collections.<PersistentFileChange>emptyList());
        list.setResultId(12);
        changelistDao.save(list);
        commitAndRefreshTransaction();

        List<PersistentChangelist> found = changelistDao.findByResult(1, true);
        assertEquals(0, found.size());
        found = changelistDao.findByResult(12, true);
        assertEquals(1, found.size());
        assertEquals(list, found.get(0));
    }

    public void testFindByResultDistinct()
    {
        Revision r1 = createRevision(1);
        long time1 = System.currentTimeMillis() - 1000;
        createChangelistForResult(r1, time1, 12);
        PersistentChangelist l1 = createChangelistForResult(r1, time1, 13);
        createChangelistForResult(r1, time1, 14);

        Revision r2 = createRevision(100);
        long time2 = System.currentTimeMillis();
        PersistentChangelist l2 = createChangelistForResult(r2, time2, 13);
        createChangelistForResult(r2, time2, 14);
        createChangelistForResult(r2, time2, 15);
        commitAndRefreshTransaction();

        List<PersistentChangelist> found = changelistDao.findByResult(13, true);
        assertEquals(2, found.size());
        assertEquals(l2, found.get(0));
        assertEquals(l1, found.get(1));
    }
    
    public void testFindByResultFilterEmpty()
    {
        final int RESULT_ID = 12;
        
        PersistentChangelist empty = new PersistentChangelist(createRevision(1), 1, "a1", "comment", Collections.<PersistentFileChange>emptyList());
        empty.setResultId(RESULT_ID);
        changelistDao.save(empty);

        PersistentChangelist nonEmpty = new PersistentChangelist(createRevision(2), 10, "a2", "comment", Arrays.asList(new PersistentFileChange("file", "rev", FileChange.Action.ADD, false)));
        nonEmpty.setResultId(RESULT_ID);
        changelistDao.save(nonEmpty);

        commitAndRefreshTransaction();

        List<PersistentChangelist> found = changelistDao.findByResult(RESULT_ID, true);
        assertEquals(2, found.size());

        found = changelistDao.findByResult(RESULT_ID, false);
        assertEquals(1, found.size());
        assertEquals(1, found.get(0).getChanges().size());
    }
    
    public void testGetSize()
    {
        PersistentChangelist changelist = createChangelist(1, 1, "login1");
        changelist.getChanges().add(new PersistentFileChange("f1", "rev", FileChange.Action.ADD, false));
        changelist.getChanges().add(new PersistentFileChange("f2", "rev", FileChange.Action.ADD, false));
        changelist.getChanges().add(new PersistentFileChange("f3", "rev", FileChange.Action.ADD, false));
        changelistDao.save(changelist);

        commitAndRefreshTransaction();
        
        assertEquals(3, changelistDao.getSize(changelist));
    }

    public void testGetFiles()
    {
        PersistentChangelist changelist = createChangelist(1, 1, "login1");
        changelist.getChanges().add(new PersistentFileChange("f1", "rev", FileChange.Action.ADD, false));
        changelist.getChanges().add(new PersistentFileChange("f2", "rev", FileChange.Action.ADD, false));
        changelist.getChanges().add(new PersistentFileChange("f3", "rev", FileChange.Action.ADD, false));
        changelist.getChanges().add(new PersistentFileChange("a1", "rev", FileChange.Action.ADD, false));
        changelist.getChanges().add(new PersistentFileChange("a2", "rev", FileChange.Action.ADD, false));
        changelistDao.save(changelist);

        commitAndRefreshTransaction();
        changelist = changelistDao.findById(changelist.getId());
        
        List<PersistentFileChange> files = changelistDao.getFiles(changelist, 1, 1);
        assertEquals(1, files.size());
        assertEquals("f2", files.get(0).getFilename());

        files = changelistDao.getFiles(changelist, 2, 5);
        assertEquals(3, files.size());
        assertEquals("f3", files.get(0).getFilename());
        assertEquals("a1", files.get(1).getFilename());
        assertEquals("a2", files.get(2).getFilename());
    }
    
    private PersistentChangelist createChangelistForResult(Revision r1, long time, int resultId)
    {
        PersistentChangelist l1 = new PersistentChangelist(r1, time, "author", "comment", Arrays.asList(
                new PersistentFileChange("file1", "1", FileChange.Action.ADD, false),
                new PersistentFileChange("file2", "23", FileChange.Action.ADD, false),
                new PersistentFileChange("file3", "4", FileChange.Action.ADD, false)
        ));
        l1.setResultId(resultId);
        changelistDao.save(l1);
        return l1;
    }

    private PersistentChangelist createChangelist(long project, long number, String login)
    {
        return createChangelist(project, number, number, login);
    }

    private PersistentChangelist createChangelist(long project, long number, long timestamp, String login)
    {
        PersistentChangelist changelist = new PersistentChangelist(createRevision(number), timestamp, login, null, Collections.<PersistentFileChange>emptyList());

        if(project != 0)
        {
            changelist.setProjectId(project);
        }

        return changelist;
    }

    private PersistentChangelist createChangelist(long number, String login)
    {
        return createChangelist(0, number, login);
    }

    private Revision createRevision(long rev)
    {
        return new Revision(Long.toString(rev));
    }
}
