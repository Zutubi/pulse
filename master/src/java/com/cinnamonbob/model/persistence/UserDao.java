package com.cinnamonbob.model.persistence;

import com.cinnamonbob.user.User;
import com.cinnamonbob.model.persistence.EntityDao;

import java.util.List;

/**
 * 
 *
 */
public interface UserDao extends EntityDao<User>
{
    User findByLogin(String login);

    List<User> findByLikeLogin(String login);
}
