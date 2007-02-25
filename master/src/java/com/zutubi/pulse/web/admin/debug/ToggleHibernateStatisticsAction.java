package com.zutubi.pulse.web.admin.debug;

import com.zutubi.pulse.web.ActionSupport;
import org.hibernate.SessionFactory;

/**
 */
public class ToggleHibernateStatisticsAction extends ActionSupport
{
    private SessionFactory sessionFactory;
    private boolean on = true;

    public boolean isOn()
    {
        return on;
    }

    public void setOn(boolean on)
    {
        this.on = on;
    }

    public String execute() throws Exception
    {
        sessionFactory.getStatistics().setStatisticsEnabled(on);
        return SUCCESS;
    }

    public void setSessionFactory(SessionFactory sessionFactory)
    {
        this.sessionFactory = sessionFactory;
    }
}
