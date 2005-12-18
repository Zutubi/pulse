package com.cinnamonbob;

import com.cinnamonbob.core.model.Revision;
import com.cinnamonbob.model.Scm;

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
