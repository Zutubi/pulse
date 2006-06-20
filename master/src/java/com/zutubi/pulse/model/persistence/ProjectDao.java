package com.zutubi.pulse.model.persistence;

import com.zutubi.pulse.model.*;

import java.util.List;

/**
 * 
 *
 */
public interface ProjectDao extends EntityDao<Project>
{
    Project findByName(String name);

    List<Project> findByLikeName(String name);

    Project findByScm(Scm scm);
    
    Project findByScmId(long id);

    void save(VersionedPulseFileDetails details);

    VersionedPulseFileDetails findVersionedPulseFileDetails(long id);

    void save(AntPulseFileDetails source);

    void save(MakePulseFileDetails source);

    void save(TagPostBuildAction action);

    AntPulseFileDetails findAntPulseFileSource(long id);

    MakePulseFileDetails findMakePulseFileSource(long id);

    TagPostBuildAction findTagPostBuildAction(long id);
}
