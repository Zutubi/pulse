package com.cinnamonbob.model.persistence;

import com.cinnamonbob.model.User;

/**
 * 
 *
 */
public interface UserDao extends EntityDao
{
    User findByLogin(String login);
    
}
