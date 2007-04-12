package com.zutubi.pulse.notifications;

import com.zutubi.validation.annotations.Required;

/**
 * <class-comment/>
 */
public interface NotificationHandler
{
    @Required
    String getName() ;

    void setName(String name) ;
}
