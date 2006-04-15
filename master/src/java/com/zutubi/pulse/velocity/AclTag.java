package com.zutubi.pulse.velocity;

import org.springframework.context.ApplicationContext;

import javax.servlet.jsp.PageContext;

import com.zutubi.pulse.bootstrap.ComponentContext;

/**
 */
public class AclTag extends org.acegisecurity.taglibs.authz.AclTag
{
    protected ApplicationContext getContext(PageContext pageContext)
    {
        return ComponentContext.getContext();
    }
}
