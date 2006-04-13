/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse;

import com.zutubi.pulse.core.model.Revision;
import com.zutubi.pulse.model.Scm;

/**
 * Simple value type carrying details required for SCM checkout.
 */
public class ScmCheckoutDetails
{
    public Scm scm;
    public Revision revision;

    public ScmCheckoutDetails(Scm scm, Revision revision)
    {
        this.scm = scm;
        this.revision = revision;
    }
}
