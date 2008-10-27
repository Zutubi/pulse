package com.zutubi.pulse.core.scm.config.api;

import com.zutubi.config.annotations.SymbolicName;
import com.zutubi.config.annotations.Transient;
import com.zutubi.tove.config.AbstractConfiguration;
import com.zutubi.validation.annotations.Required;

/**
 * Base for SCM configuration types.  All SCM plugins must support at least
 * this configuration.
 */
@SymbolicName("zutubi.scmConfig")
public abstract class ScmConfiguration extends AbstractConfiguration
{
    @Required
    private CheckoutScheme checkoutScheme = CheckoutScheme.CLEAN_CHECKOUT;

    public CheckoutScheme getCheckoutScheme()
    {
        return checkoutScheme;
    }

    public void setCheckoutScheme(CheckoutScheme checkoutScheme)
    {
        this.checkoutScheme = checkoutScheme;
    }

    /**
     * Returns a short type string used to identify the SCM type (e.g.
     * "svn"). This type may be used by other parts of the system to
     * determine which SCM they are dealing with.  For example change viewers
     * may use different strategies to deal with different SCMs.
     *
     * @return the SCM type
     */
    @Transient
    public abstract String getType();

    /**
     * Calculates the previous revision for a given revision.  May return
     * null if there is no previous revision or the SCM does not have
     * capability {@link com.zutubi.pulse.core.scm.api.ScmCapability#CHANGESETS}.
     *
     * @param revision revision to get the previous revision for (in the form
     *                 returned by {@link com.zutubi.pulse.core.scm.api.Revision#getRevisionString()}).
     * @return the previous revision in the same form, or null if it does not
     *         exist or is not supported
     */
    public abstract String getPreviousRevision(String revision);
}
