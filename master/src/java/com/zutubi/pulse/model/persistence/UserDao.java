package com.zutubi.pulse.model.persistence;

import com.zutubi.pulse.model.Group;
import com.zutubi.pulse.model.User;

import java.util.List;

/**
 * 
 *
 */
public interface UserDao extends EntityDao<User>
{
    User findByLogin(String login);
    List<User> findByNotInGroup(Group group);
}
