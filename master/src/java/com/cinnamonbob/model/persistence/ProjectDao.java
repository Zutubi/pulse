package com.cinnamonbob.model.persistence;

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
}
