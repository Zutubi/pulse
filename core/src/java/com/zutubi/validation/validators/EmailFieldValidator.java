package com.zutubi.validation.validators;

import com.zutubi.validation.ValidationException;
import com.zutubi.validation.Shortcircuitable;

/**
 * <class-comment/>
 */
public class EmailFieldValidator extends RegexValidator implements Shortcircuitable
{
    public static final String emailAddressPattern =
            "\\b(^(\\S+@).+((\\.com)|(\\.net)|(\\.org)|(\\.info)|(\\.edu)|(\\.mil)|(\\.gov)|(\\.biz)|(\\.ws)|(\\.us)|(\\.tv)|(\\.cc)|(\\.aero)|(\\.arpa)|(\\.coop)|(\\.int)|(\\.jobs)|(\\.museum)|(\\.name)|(\\.pro)|(\\.travel)|(\\.nato)|(\\..{2,2}))$)\\b";

    public EmailFieldValidator()
    {
        setCaseSensitive(false);
        setPattern(emailAddressPattern);
    }
}