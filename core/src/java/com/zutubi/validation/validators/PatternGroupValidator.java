package com.zutubi.validation.validators;

import com.zutubi.validation.ValidationException;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 *
 *
 */
public class PatternGroupValidator extends FieldValidatorSupport
{
    public PatternGroupValidator()
    {
        setDefaultMessageKey(".invalid");
        setMessageKey("${fieldName}.invalid");
    }

    public void validate(Object obj) throws ValidationException
    {
        Object fieldValue = getFieldValue(getFieldName(), obj);
        if (fieldValue == null)
        {
            addFieldError(getFieldName());
            return;
        }

        if (fieldValue instanceof String)
        {
            String str = ((String) fieldValue);

            Pattern pattern = Pattern.compile("(\\d)(\\d)(\\d)(\\d)(\\d)(\\d)(\\d)(\\d)(\\d)");
            Matcher matcher = pattern.matcher("123456789");

            // these calls are required to correctly set up the internal state of the matcher.
            matcher.matches();
            matcher.groupCount();
            
            try
            {
                processSubstitution(str, matcher);
            }
            catch (Exception e)
            {
                e.printStackTrace();
                setDefaultMessage(getDefaultMessage());
                validationContext.addFieldError(getFieldName(), e.getMessage());
            }
        }
    }

    /**
     * The following code was taken from Matcher.appendReplacement.
     *
     * @param replacement
     * @param matcher
     * @return
     * @see java.util.regex.Matcher#appendReplacement(StringBuffer,String)
     */
    private String processSubstitution(String replacement, Matcher matcher)
    {
        // Process substitution string to replace group references with groups
        int cursor = 0;

        StringBuilder result = new StringBuilder();

        while (cursor < replacement.length())
        {
            char nextChar = replacement.charAt(cursor);
            if (nextChar == '\\')
            {
                cursor++;
                nextChar = replacement.charAt(cursor);
                result.append(nextChar);
                cursor++;
            }
            else if (nextChar == '$')
            {
                // Skip past $
                cursor++;

                // The first number is always a group
                int refNum = (int) replacement.charAt(cursor) - '0';
                if ((refNum < 0) || (refNum > 9))
                {
                    throw new IllegalArgumentException("Illegal group reference");
                }

                cursor++;

                // Capture the largest legal group string
                boolean done = false;
                while (!done)
                {
                    if (cursor >= replacement.length())
                    {
                        break;
                    }
                    int nextDigit = replacement.charAt(cursor) - '0';
                    if ((nextDigit < 0) || (nextDigit > 9))
                    { // not a number
                        break;
                    }
                    int newRefNum = (refNum * 10) + nextDigit;
                    if (matcher.groupCount() < newRefNum)
                    {
                        done = true;
                    }
                    else
                    {
                        refNum = newRefNum;
                        cursor++;
                    }
                }

                // Append group
                if (matcher.group(refNum) != null)
                {
                    result.append(matcher.group(refNum));
                }
            }
            else
            {
                result.append(nextChar);
                cursor++;
            }
        }
        return result.toString();
    }
}
