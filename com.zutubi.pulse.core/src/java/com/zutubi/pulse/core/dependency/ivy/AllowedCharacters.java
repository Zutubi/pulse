package com.zutubi.pulse.core.dependency.ivy;

import com.google.common.base.Predicate;
import com.zutubi.util.StringUtils;

/**
 * A central location for the predicates that are used to determine what characters
 * need encoding in what ivy fields.
 */
public class AllowedCharacters
{
    /**
     * Defines the allowed characters in the organisation, module, artifact and conf fields.
     */
    public static final Predicate<Character> NAMES = new Predicate<Character>()
    {
        public boolean apply(Character character)
        {
            if (StringUtils.isAsciiAlphaNumeric(character))
            {
                return true;
            }
            else
            {
                // A few more likely-used characters
                switch (character)
                {
                    case '-':
                    case '_':
                    case '.':
                        return true;
                }
            }

            return false;
        }
    };
}
