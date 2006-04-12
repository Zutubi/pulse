package com.zutubi.pulse.model.persistence;

import com.zutubi.pulse.model.AntPulseFileDetails;
import com.zutubi.pulse.model.CustomPulseFileDetails;
import com.zutubi.pulse.model.MakePulseFileDetails;
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

    void save(CustomPulseFileDetails details);

    CustomPulseFileDetails findCustomPulseFileSource(long id);

    void save(AntPulseFileDetails source);

    void save(MakePulseFileDetails source);

    AntPulseFileDetails findAntPulseFileSource(long id);

    MakePulseFileDetails findMakePulseFileSource(long id);
}
