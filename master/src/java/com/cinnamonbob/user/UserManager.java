package com.cinnamonbob.user;

import org.acegisecurity.userdetails.UserDetailsService;

import java.util.List;

import com.cinnamonbob.model.EntityManager;
import com.cinnamonbob.model.ContactPoint;

/**
 * 
 *
 */
public interface UserManager extends EntityManager<User>, UserDetailsService
{
    /**
     * Retrieve an instance of the user identified by the login name
     *
     * @param login
     * @return a user instance, or null if no matching user is found.
     */
    User getUser(String login);

    /**
     * Retrieve an instance of the user identified by the unique id.
     *
     * @param id
     * @return a user instance, or null if no matching user is found.
     */
    User getUser(long id);

    List<User> getUsersWithLoginLike(String name);

    List<User> getAllUsers();

    ContactPoint getContactPoint(long id);

    void delete(ContactPoint contact);
}
