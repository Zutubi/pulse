package com.zutubi.validation.validators;

import com.zutubi.validation.ShortCircuitableValidator;

/**
 * <class-comment/>
 */
public class EmailValidator extends RegexValidator
{
    public static final String emailAddressPattern =
            "\\b(^(\\S+@).+((\\.com)|(\\.net)|(\\.org)|(\\.info)|(\\.edu)|(\\.mil)|(\\.gov)|(\\.biz)|(\\.ws)|" +
                    "(\\.us)|(\\.tv)|(\\.cc)|(\\.aero)|(\\.arpa)|(\\.coop)|(\\.int)|(\\.jobs)|(\\.museum)|" +
                    "(\\.name)|(\\.pro)|(\\.travel)|(\\.nato)|(\\..{2,2}))$)\\b";

    public EmailValidator()
    {
        setCaseSensitive(false);
        setPattern(emailAddressPattern);
    }

/*
    public final static boolean verifyEmail(String email) {
        if (email == null) {
            return false;
        }

        if (email.indexOf('@') < 1) {
            return false;
        }

        try {
            new InternetAddress(email);

            return true;
        } catch (AddressException e) {
            return false;
        }
    }
*/
}