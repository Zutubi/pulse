package com.cinnamonbob.model;

/**
 * 
 *
 */
public interface UserManager
{
    void createNewUser(User user);
    
    User getUser(String login);
}
