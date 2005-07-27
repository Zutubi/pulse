package com.cinnamonbob.model.persistence;

import com.cinnamonbob.model.Project;

import java.util.List;

/**
 * 
 *
 */
public interface ProjectDao extends EntityDao
{
    Project findByName(String name);
    List findByLikeName(String name);
}
