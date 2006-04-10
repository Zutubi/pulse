package com.cinnamonbob;

import com.cinnamonbob.model.User;
import com.cinnamonbob.util.Sort;

import java.util.Comparator;

/**
 * A comparator that orders users lexically according to their names.
 */
public class UserLoginComparator implements Comparator<User>
{
    private Sort.StringComparator sc = new Sort.StringComparator();

    public int compare(User u1, User u2)
    {
        return sc.compare(u1.getLogin(), u2.getLogin());
    }
}
