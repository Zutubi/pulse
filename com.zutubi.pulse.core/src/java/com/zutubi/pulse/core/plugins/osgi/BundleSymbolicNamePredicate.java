package com.zutubi.pulse.core.plugins.osgi;

import com.google.common.base.Predicate;
import org.osgi.framework.Bundle;

/**
 * A predicate that tests if a bundle has a symbolic name.
 */
class BundleSymbolicNamePredicate implements Predicate<Bundle>
{
    private final String symbolicName;

    public BundleSymbolicNamePredicate(String symbolicName)
    {
        this.symbolicName = symbolicName;
    }

    public boolean apply(Bundle bundle)
    {
        return bundle.getSymbolicName().equals(symbolicName);
    }
}
