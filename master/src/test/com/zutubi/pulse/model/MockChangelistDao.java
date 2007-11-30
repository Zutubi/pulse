package com.zutubi.pulse.model;

import com.zutubi.pulse.model.persistence.ChangelistDao;
import com.zutubi.pulse.model.User;
import com.zutubi.pulse.model.Project;
import com.zutubi.pulse.core.model.Changelist;
import com.zutubi.pulse.core.model.Revision;

import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;
import java.util.List;

/**
 */
public class MockChangelistDao implements ChangelistDao
{
    public Set<Long> getAllAffectedProjectIds(Changelist changelist)
    {
        return new HashSet<Long>(Arrays.asList(changelist.getProjectId()));
    }

    public Set<Long> getAllAffectedResultIds(Changelist changelist)
    {
        throw new RuntimeException("Method not yet implemented");
    }

    public List<Changelist> findLatestByUser(User user, int max)
    {
        throw new RuntimeException("Method not yet implemented");
    }

    public List<Changelist> findLatestByProject(Project project, int max)
    {
        throw new RuntimeException("Method not yet implemented");
    }

    public List<Changelist> findLatestByProjects(Project[] projects, int max)
    {
        throw new RuntimeException("Method not yet implemented");
    }

    public List<Changelist> findByResult(long id)
    {
        throw new RuntimeException("Method not yet implemented");
    }

    public List<Changelist> findByRevision(String serverUid, Revision revision)
    {
        throw new RuntimeException("Method not yet implemented");
    }

    public Changelist findById(long id)
    {
        throw new RuntimeException("Method not yet implemented");
    }

    public <U extends Changelist> U findByIdAndType(long id, Class<U> type)
    {
        throw new RuntimeException("Method not yet implemented");
    }

    public List<Changelist> findAll()
    {
        throw new RuntimeException("Method not yet implemented");
    }

    public void save(Changelist entity)
    {
        throw new RuntimeException("Method not yet implemented");
    }

    public void delete(Changelist entity)
    {
        throw new RuntimeException("Method not yet implemented");
    }

    public void refresh(Changelist entity)
    {
        throw new RuntimeException("Method not yet implemented");
    }

    public int count()
    {
        throw new RuntimeException("Method not yet implemented");
    }
}
