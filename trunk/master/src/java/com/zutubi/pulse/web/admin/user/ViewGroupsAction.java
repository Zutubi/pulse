package com.zutubi.pulse.web.admin.user;

import com.zutubi.pulse.model.Group;
import com.zutubi.pulse.model.NamedEntityComparator;
import com.zutubi.pulse.web.PagingSupport;

import java.util.List;
import java.util.Collections;

/**
 */
public class ViewGroupsAction extends GroupsActionSupport
{
    public static final int GROUPS_PER_PAGE = 20;

    private List<Group> groups;
    private PagingSupport pagingSupport = new PagingSupport(GROUPS_PER_PAGE);

    public List<Group> getGroups()
    {
        return groups;
    }

    public PagingSupport getPagingSupport()
    {
        return pagingSupport;
    }

    public int getStartPage()
    {
        return pagingSupport.getStartPage();
    }

    public void setStartPage(int page)
    {
        pagingSupport.setStartPage(page);
    }

    public String execute()
    {
        groups = getUserManager().getAllGroups();
        Collections.sort(groups, new NamedEntityComparator());
        pagingSupport.setTotalItems(groups.size());
        pagingSupport.clampStartPage();
        groups = groups.subList(pagingSupport.getStartOffset(), pagingSupport.getEndOffset());
        return SUCCESS;
    }
}
