package com.cinnamonbob.model;

import java.util.List;

/**
 * 
 *
 */
public interface UserManager extends EntityManager<User>
{
    /**
     * Retrieve an instance of the user identified by the login name
     * @param login
     *
     * @return a user instance, or null if no matching user is found.
     */
    User getUser(String login);

    /**
     * Retrieve an instance of the user identified by the unique id.
     * @param id
     *
     * @return a user instance, or null if no matching user is found.
     */
    User getUser(long id);
    
    List<User> getUsersWithLoginLike(String name);
    List<User> getAllUsers();

    
    ContactPoint getContactPoint(long id);
    void delete(ContactPoint contact);
}
