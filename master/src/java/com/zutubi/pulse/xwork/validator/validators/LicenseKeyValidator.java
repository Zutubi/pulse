package com.zutubi.pulse.xwork.validator.validators;

import com.opensymphony.xwork.validator.ValidationException;
import com.opensymphony.xwork.validator.validators.FieldValidatorSupport;
import com.zutubi.pulse.license.LicenseDecoder;
import com.zutubi.pulse.license.LicenseException;

/**
 * <class-comment/>
 */
public class LicenseKeyValidator extends FieldValidatorSupport
{
    // FIXME is this used?  there is validation elsewhere, and this may be
    // FIXME incomplete?
    public void validate(Object object) throws ValidationException
    {
        Object obj = getFieldValue(getFieldName(), object);
        if (obj == null || !(obj instanceof String))
        {
            addFieldError(getFieldName(), getValidatorContext().getText(getMessageKey()));
            return;
        }

        try
        {
            String licenseKey = (String) obj;

            // remove any '\n' characters from the license key.
            licenseKey = licenseKey.replaceAll("\n", "");

            // check that the decoder is able to decode the license.
            LicenseDecoder decoder = new LicenseDecoder();
            if (decoder.decode(licenseKey.getBytes()) == null)
            {
                // validation of the license key has failed.
                addFieldError(getFieldName(), getValidatorContext().getText(getMessageKey()));
            }
        }
        catch (LicenseException e)
        {
            // determining whether or not the license key is valid has failed.
            // the license key may in fact be valid.. lets ignore this problem
            // for now since we will deal with it as an action error later.
        }
    }
}
