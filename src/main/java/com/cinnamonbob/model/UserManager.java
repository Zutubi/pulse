package com.cinnamonbob.model;

import java.util.List;

/**
 * 
 *
 */
public interface UserManager extends EntityManager<User>
{
    User getUser(String login);
    User getUser(long id);
    List<User> getUsersWithLoginLike(String name);
    List<User> getAllUsers();
}
