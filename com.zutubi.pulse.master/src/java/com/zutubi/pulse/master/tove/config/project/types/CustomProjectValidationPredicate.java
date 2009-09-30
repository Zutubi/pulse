package com.zutubi.pulse.master.tove.config.project.types;

import com.zutubi.pulse.core.*;
import nu.xom.Element;

/**
 * A predicate used when validating the pulse file for a custom project.  We
 * don't resolve references and don't load resource references.
 */
public class CustomProjectValidationPredicate implements TypeLoadPredicate
{
    public boolean loadType(Object type, Element element)
    {
        return !(type instanceof ResourceReference);
    }

    public boolean resolveReferences(Object type, Element element)
    {
        return true;
    }

    public boolean allowUnresolved(Object type, Element element)
    {
        return true;
    }

    public boolean validate(Object type, Element element)
    {
        // Special case for commands.  This is hackish - but replaced by a more
        // sensible mechanism on later Pulse versions.
        if (type instanceof Command)
        {
            Command command = (Command) type;
            try
            {
                return !ReferenceResolver.containsReference(command.getName());
            }
            catch (ResolutionException e)
            {
                return true;
            }
        }

        return loadType(type, element);
    }
}
