package com.cinnamonbob.model.persistence;

import com.cinnamonbob.model.User;

import java.util.List;

/**
 * 
 *
 */
public interface UserDao extends EntityDao
{
    User findByLogin(String login);

    List findByLikeLogin(String login);
}
