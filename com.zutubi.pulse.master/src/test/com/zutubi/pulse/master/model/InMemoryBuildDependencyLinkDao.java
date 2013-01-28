package com.zutubi.pulse.master.model;

import com.google.common.base.Predicate;
import static com.google.common.base.Predicates.or;
import com.zutubi.pulse.master.model.persistence.BuildDependencyLinkDao;
import com.zutubi.pulse.master.model.persistence.InMemoryEntityDao;

import java.util.List;

/**
 * Testing implementation of {@link BuildDependencyLinkDao} that keeps a set of links in memory.
 */
public class InMemoryBuildDependencyLinkDao extends InMemoryEntityDao<BuildDependencyLink> implements BuildDependencyLinkDao
{
    public List<BuildDependencyLink> findAllDependencies(long buildId)
    {
        return findByPredicate(hasId(buildId));
    }

    public List<BuildDependencyLink> findAllUpstreamDependencies(long buildId)
    {
        return findByPredicate(new BuildDependencyLink.HasDownstreamId(buildId));
    }

    public List<BuildDependencyLink> findAllDownstreamDependencies(long buildId)
    {
        return findByPredicate(new BuildDependencyLink.HasUpstreamId(buildId));
    }

    public int deleteDependenciesByBuild(long buildId)
    {
        return deleteByPredicate(hasId(buildId));
    }

    private Predicate<BuildDependencyLink> hasId(long buildId)
    {
        return or(new BuildDependencyLink.HasDownstreamId(buildId), new BuildDependencyLink.HasUpstreamId(buildId));
    }
}
