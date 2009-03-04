package com.zutubi.pulse.core.marshal.types;

import com.zutubi.pulse.core.engine.api.Referenceable;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.config.api.AbstractNamedConfiguration;

/**
 * A type that has a broken @Referenceable annotation.
 */
@SymbolicName("badReferenceName")
@Referenceable(nameProperty = "nosuchproperty")
public class BadReferenceNameConfiguration extends AbstractNamedConfiguration
{
}