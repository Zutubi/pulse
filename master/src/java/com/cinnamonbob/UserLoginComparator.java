package com.cinnamonbob;

import com.cinnamonbob.model.User;

import java.util.Comparator;

/**
 * A comparator that orders users lexically according to their names.
 */
public class UserLoginComparator implements Comparator<User>
{
    public int compare(User u1, User u2)
    {
        return u1.getLogin().compareTo(u2.getLogin());
    }
}
