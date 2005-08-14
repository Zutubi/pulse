package com.cinnamonbob.core.validation;

import com.opensymphony.xwork.*;
import com.opensymphony.xwork.validator.ActionValidatorManager;
import com.opensymphony.xwork.validator.DelegatingValidatorContext;
import com.opensymphony.xwork.validator.ValidationException;
import com.opensymphony.xwork.validator.ValidatorContext;

/**
 * This class provides a wrapper around the xwork validation framework allowing it to
 * be used by all of the components that make up a bob file.
 *
 */
public class CommandValidationManager
{
    public static void validate(Object obj, String name) throws ValidationException
    {
        if (obj == null)
        {
            return;
        }

        LocaleProvider localeProvider = makeLocaleProvider(obj);
        TextProvider textProvider = makeTextProvider(obj, localeProvider);
        ValidationAware validationAware = makeValidationAware(obj);

        ValidatorContext validatorContext = new DelegatingValidatorContext(validationAware, textProvider, localeProvider);

        ActionValidatorManager.validate(obj, name, validatorContext);

        if (validatorContext.hasErrors())
        {
            throw new ValidationException();
        }
    }

    private static LocaleProvider makeLocaleProvider(Object obj)
    {
        if (obj instanceof LocaleProvider)
        {
            return (LocaleProvider) obj;
        }
        else
        {
            return new LocaleProviderSupport();
        }
    }

    private static TextProvider makeTextProvider(Object obj, LocaleProvider localeProvider)
    {
        if (obj instanceof TextProvider)
        {
            return (TextProvider) obj;
        }
        else
        {
            return new TextProviderSupport(obj.getClass(), localeProvider);
        }
    }

    private static ValidationAware makeValidationAware(Object obj)
    {
        if (obj instanceof ValidationAware)
        {
            return (ValidationAware) obj;
        }
        else
        {
            return new ValidationAwareSupport();
        }
    }
}
