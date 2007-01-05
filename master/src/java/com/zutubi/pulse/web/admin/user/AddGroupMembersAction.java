package com.zutubi.pulse.web.admin.user;

import com.zutubi.pulse.model.Group;
import com.zutubi.pulse.model.User;

import java.util.*;
import java.text.Collator;

/**
 * An action to add new members (users) to a group.
 */
public class AddGroupMembersAction extends GroupActionSupport
{
    private LinkedHashMap<Long, String> nonMembers;
    private List<Long> members;
    private int startPage;

    public Map<Long, String> getNonMembers()
    {
        if(nonMembers == null)
        {
            nonMembers = new LinkedHashMap<Long, String>();
            Group group = getGroup();
            if(group != null)
            {
                List<User> nons = getUserManager().getUsersNotInGroup(group);

                // sort the nons list so that they appear in a predictable order in the UI.
                final Collator collator = Collator.getInstance();
                Collections.sort(nons, new Comparator<User>()
                {
                    public int compare(User user1, User user2)
                    {
                        return collator.compare(user1.getName(), user2.getName());
                    }
                });

                for(User u: nons)
                {
                    nonMembers.put(u.getId(), u.getLogin() + " (" + u.getName() + ")");
                }
            }
        }
        return nonMembers;
    }

    public List<Long> getMembers()
    {
        return members;
    }

    public void setMembers(List<Long> members)
    {
        this.members = members;
    }

    public String doInput() throws Exception
    {
        if(getGroup() == null)
        {
            addActionError("Unknown group [" + getGroupId() + "]");
            return ERROR;
        }

        return INPUT;
    }

    public String execute() throws Exception
    {
        if (members != null)
        {
            Group group = getGroup();
            if(group == null)
            {
                addActionError("Unknown group [" + getGroupId() + "]");
                return ERROR;
            }

            for(Long userId: members)
            {
                User user = getUserManager().getUser(userId);
                if(user != null)
                {
                    group.addUser(user);
                }
            }

            getUserManager().save(group);
        }
        return SUCCESS;
    }

    public int getStartPage()
    {
        return startPage;
    }

    public void setStartPage(int startPage)
    {
        this.startPage = startPage;
    }
}
