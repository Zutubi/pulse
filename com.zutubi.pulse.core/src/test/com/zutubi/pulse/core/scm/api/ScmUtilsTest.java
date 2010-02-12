package com.zutubi.pulse.core.scm.api;

import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.util.Predicate;

import java.util.List;
import java.util.LinkedList;
import java.util.Arrays;

public class ScmUtilsTest extends PulseTestCase
{
    public void testFiltering()
    {
        Changelist c = getChangelist("a.txt", "b.txt", "c.xml");
        List<Changelist> changelists = ScmUtils.filter(Arrays.asList(c), newPathFilter(".xml"));
        assertEquals(1, changelists.size());
        assertEquals(1, changelists.get(0).getChanges().size());

        changelists = ScmUtils.filter(Arrays.asList(c), newPathFilter(".txt"));
        assertEquals(1, changelists.size());
        assertEquals(2, changelists.get(0).getChanges().size());

        changelists = ScmUtils.filter(Arrays.asList(c), newPathFilter(".ftl"));
        assertEquals(0, changelists.size());
    }

    private Predicate<String> newPathFilter(final String endsWith)
    {
        return new Predicate<String>()
        {
            public boolean satisfied(String path)
            {
                return path.endsWith(endsWith);
            }
        };
    }

    private Changelist getChangelist(String... filePaths)
    {
        return new Changelist(new Revision(), 0, null, null, getFiles(filePaths));
    }

    private List<FileChange> getFiles(String... paths)
    {
        List<FileChange> files = new LinkedList<FileChange>();
        for (String path : paths)
        {
            files.add(new FileChange(path, null, null));
        }
        return files;
    }
}
