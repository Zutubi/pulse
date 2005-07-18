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

    List getUsersWithLoginLike(String name);

    User getUser(long id);
}
