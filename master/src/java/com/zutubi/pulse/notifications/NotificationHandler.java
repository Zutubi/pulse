package com.zutubi.pulse.notifications;

import com.zutubi.validation.annotations.Required;
import com.zutubi.pulse.form.descriptor.annotation.Text;

/**
 * <class-comment/>
 */
public interface NotificationHandler
{
    @Required
    @Text(size= 60)
    String getName()
            ;

    void setName(String name)
            ;
}
