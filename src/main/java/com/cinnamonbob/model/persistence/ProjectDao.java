package com.cinnamonbob.model.persistence;

import com.cinnamonbob.model.Project;

/**
 * 
 *
 */
public interface ProjectDao extends EntityDao
{
    Project findByName(String name);
}
