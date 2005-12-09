package com.cinnamonbob.scheduling.persistence.mock;

import com.cinnamonbob.scheduling.Task;
import com.cinnamonbob.scheduling.persistence.TaskDao;

import java.util.List;

/**
 * <class-comment/>
 */
public class MockTaskDao extends MockEntityDao<Task> implements TaskDao
{
    public List<Task> findByGroup(final String group)
    {
        return findByFilter(new Filter()
        {
            public boolean include(Object o)
            {
                Task task = (Task) o;
                return group.compareTo(task.getGroup()) == 0;
            }
        });
    }

    public Task findByNameAndGroup(final String name, final String group)
    {
        return findUniqueByFilter(new Filter()
        {
            public boolean include(Object o)
            {
                Task task = (Task) o;
                return group.compareTo(task.getGroup()) == 0 &&
                        name.compareTo(task.getName()) == 0;
            }
        });
    }
}
