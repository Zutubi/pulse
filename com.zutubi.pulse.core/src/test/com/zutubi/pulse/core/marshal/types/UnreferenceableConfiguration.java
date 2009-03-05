package com.zutubi.pulse.core.marshal.types;

import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.config.api.AbstractNamedConfiguration;

/**
 * Nothing special here - the lack of @Referenceable is the important bit.
 */
@SymbolicName("unreferenceable")
public class UnreferenceableConfiguration extends AbstractNamedConfiguration
{
}
