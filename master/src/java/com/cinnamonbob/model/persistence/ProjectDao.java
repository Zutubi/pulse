package com.zutubi.pulse.model.persistence;

import com.zutubi.pulse.model.AntBobFileDetails;
import com.zutubi.pulse.model.CustomBobFileDetails;
import com.zutubi.pulse.model.MakeBobFileDetails;
import com.zutubi.pulse.model.Project;

import java.util.List;

/**
 * 
 *
 */
public interface ProjectDao extends EntityDao<Project>
{
    Project findByName(String name);

    List<Project> findByLikeName(String name);

    void save(CustomBobFileDetails details);

    CustomBobFileDetails findCustomBobFileSource(long id);

    void save(AntBobFileDetails source);

    void save(MakeBobFileDetails source);

    AntBobFileDetails findAntBobFileSource(long id);

    MakeBobFileDetails findMakeBobFileSource(long id);
}
