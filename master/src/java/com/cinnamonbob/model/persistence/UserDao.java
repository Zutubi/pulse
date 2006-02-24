package com.cinnamonbob.model.persistence;

import com.cinnamonbob.model.User;

import java.util.List;

/**
 * 
 *
 */
public interface UserDao extends EntityDao<User>
{
    User findByLogin(String login);

    List<User> findByLikeLogin(String login);

    int getUserCount();
}
