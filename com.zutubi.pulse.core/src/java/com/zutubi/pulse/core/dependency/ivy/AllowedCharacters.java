package com.zutubi.pulse.core.dependency.ivy;

import com.zutubi.util.Predicate;
import com.zutubi.util.StringUtils;

/**
 * A central location for the predicates that are used to determine what characters
 * need encoding in what ivy fields.
 */
public class AllowedCharacters
{
    /**
     * Defines the allowed characters in the organisation, module and artifact fields.
     */
    public static final Predicate<Character> NAMES = new Predicate<Character>()
    {
        public boolean satisfied(Character character)
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
                    case ' ':
                    case '-':
                    case '_':
                    case '.':
                        return true;
                }
            }

            return false;
        }
    };

    /**
     * Defines the allowed characters in stage names.  This is further restricted
     * from the {@link #NAMES} predicate because stage names are used as ivy
     * configuration names which have special formatting requirements.
     */
    public static final Predicate<Character> STAGE_NAMES = new Predicate<Character>()
    {
        public boolean satisfied(Character character)
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
                    case ' ':
                    case '-':
                    case '.':
                        return true;
                }
            }

            return false;
        }
    };
}
