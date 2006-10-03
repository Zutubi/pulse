package com.zutubi.validation.validators;

import com.zutubi.validation.ValidationException;
import com.zutubi.validation.ShortCircuitableValidator;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * <class-comment/>
 */
public class RegexValidator extends FieldValidatorSupport
{
    private static final String INVALID = ".invalid";

    private String pattern = ".";

    private boolean caseSensitive = true;

    public RegexValidator()
    {
        setDefaultMessageKey(".invalid");
        setMessageKey("${fieldName}.invalid");
    }

    public void validate(Object obj) throws ValidationException
    {
        String fieldName = getFieldName();
        Object fieldValue = getFieldValue(fieldName, obj);

        if (fieldValue == null)
        {
            return;
        }

        if (!(fieldValue instanceof String))
        {
            throw new ValidationException();
        }

        String value = (String) fieldValue;
        value = value.trim();

        if (value.length() == 0)
        {
            return;
        }

        Pattern pattern = null;
        if (isCaseSensitive())
        {
            pattern = Pattern.compile(getPattern());
        }
        else
        {
            pattern = Pattern.compile(getPattern(), Pattern.CASE_INSENSITIVE);
        }

        Matcher matcher = pattern.matcher(value);
        if (!matcher.matches())
        {
            validationContext.addFieldError(getFieldName(), getMessage());
        }
    }

    public void setPattern(String pattern)
    {
        this.pattern = pattern;
    }

    public String getPattern()
    {
        return this.pattern;
    }

    /**
     * @return Returns whether the expression should be matched against in a case-sensitive way.
     *         Default is <code>true</code>.
     */
    public boolean isCaseSensitive()
    {
        return caseSensitive;
    }

    /**
     * Sets whether the expression should be matched against in a case-sensitive way.
     * Default is <code>true</code>.
     */
    public void setCaseSensitive(boolean caseSensitive)
    {
        this.caseSensitive = caseSensitive;
    }
}
