package com.cinnamonbob.model.persistence;

import com.cinnamonbob.core.model.AntBobFileDetails;
import com.cinnamonbob.core.model.CustomBobFileDetails;
import com.cinnamonbob.model.Project;

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

    AntBobFileDetails findAntBobFileSource(long id);

}
