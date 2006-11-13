package com.zutubi.pulse.model.persistence;

import com.zutubi.pulse.model.BuildSpecificationNode;

import java.util.List;

/**
 */
public interface BuildSpecificationNodeDao extends EntityDao<BuildSpecificationNode>
{
    List<BuildSpecificationNode> findByResourceRequirement(String resource);    
}
