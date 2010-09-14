package com.zutubi.pulse.core.plugins.osgi;

import com.zutubi.util.Predicate;
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

    public boolean satisfied(Bundle bundle)
    {
        return bundle.getSymbolicName().equals(symbolicName);
    }
}
