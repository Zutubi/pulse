package com.zutubi.pulse.master.model;

import com.zutubi.pulse.core.model.PersistentChangelist;
import com.zutubi.pulse.master.model.persistence.ChangelistDao;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 */
public class MockChangelistDao implements ChangelistDao
{
    public Set<Long> getAllAffectedProjectIds(PersistentChangelist changelist)
    {
        return new HashSet<Long>(Arrays.asList(changelist.getProjectId()));
    }

    public Set<Long> getAllAffectedResultIds(PersistentChangelist PersistentChangelist)
    {
        throw new RuntimeException("Method not yet implemented");
    }

    public List<PersistentChangelist> findLatestByUser(User user, int max)
    {
        throw new RuntimeException("Method not yet implemented");
    }

    public List<PersistentChangelist> findLatestByProject(Project project, int max)
    {
        throw new RuntimeException("Method not yet implemented");
    }

    public List<PersistentChangelist> findLatestByProjects(Project[] projects, int max)
    {
        throw new RuntimeException("Method not yet implemented");
    }

    public List<PersistentChangelist> findByResult(long id)
    {
        throw new RuntimeException("Method not yet implemented");
    }

    public List<PersistentChangelist> findAllEquivalent(PersistentChangelist PersistentChangelist)
    {
        throw new RuntimeException("Method not yet implemented");
    }

    public PersistentChangelist findById(long id)
    {
        throw new RuntimeException("Method not yet implemented");
    }

    public <U extends PersistentChangelist> U findByIdAndType(long id, Class<U> type)
    {
        throw new RuntimeException("Method not yet implemented");
    }

    public List<PersistentChangelist> findAll()
    {
        throw new RuntimeException("Method not yet implemented");
    }

    public void save(PersistentChangelist entity)
    {
        throw new RuntimeException("Method not yet implemented");
    }

    public void delete(PersistentChangelist entity)
    {
        throw new RuntimeException("Method not yet implemented");
    }

    public void refresh(PersistentChangelist entity)
    {
        throw new RuntimeException("Method not yet implemented");
    }

    public int count()
    {
        throw new RuntimeException("Method not yet implemented");
    }
}
