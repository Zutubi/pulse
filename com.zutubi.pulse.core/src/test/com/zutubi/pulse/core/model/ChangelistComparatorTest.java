package com.zutubi.pulse.core.model;

import com.google.common.base.Function;
import com.zutubi.pulse.core.scm.api.Revision;
import com.zutubi.pulse.core.test.api.PulseTestCase;

import java.util.Collections;
import java.util.List;

import static com.google.common.collect.Lists.transform;
import static java.util.Arrays.asList;

public class ChangelistComparatorTest extends PulseTestCase
{
    public void testSimpleDateOrdering()
    {
        List<PersistentChangelist> changelists = asList(createChangelist(1, 10), createChangelist(2, 4), createChangelist(3, 22), createChangelist(4, 1));
        Collections.sort(changelists, new ChangelistComparator());
        assertIds(changelists, 3L, 1L, 2L, 4L);
    }

    public void testSameDateOrderedById()
    {
        List<PersistentChangelist> changelists = asList(createChangelist(1, 10), createChangelist(2, 4), createChangelist(3, 10), createChangelist(4, 10), createChangelist(5, 4), createChangelist(6, 4));
        Collections.sort(changelists, new ChangelistComparator());
        assertIds(changelists, 4L, 3L, 1L, 6L, 5L, 2L);
    }

    private void assertIds(List<PersistentChangelist> changelists, Long... expected)
    {
        List<Long> got = transform(changelists, new Function<PersistentChangelist, Long>()
        {
            public Long apply(PersistentChangelist persistentChangelist)
            {
                return persistentChangelist.getId();
            }
        });

        assertEquals(asList(expected), got);
    }

    private PersistentChangelist createChangelist(long id, long timestamp)
    {
        PersistentChangelist changelist = new PersistentChangelist(new Revision("anything"), timestamp, "anyone", "any comment", Collections.<PersistentFileChange>emptyList());
        changelist.setId(id);
        return changelist;
    }
}
