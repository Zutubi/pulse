package com.zutubi.pulse.model.persistence;

import org.acegisecurity.acl.basic.BasicAclDao;
import com.zutubi.pulse.model.ProjectAclEntry;

import java.util.List;

/**
 */
public interface ProjectAclDao extends BasicAclDao
{
    List<ProjectAclEntry> findByRecipient(String recipient);
}
