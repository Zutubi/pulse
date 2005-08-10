package com.cinnamonbob.model;

import java.util.List;

/**
 * 
 *
 */
public interface UserManager
{
    void createNewUser(User user);

    User getUser(String login);
    User getUser(long id);
    
    List<User> getAllUsers();
    List<User> getUsersWithLoginLike(String name);
}
