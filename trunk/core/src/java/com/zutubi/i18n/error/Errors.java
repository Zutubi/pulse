package com.zutubi.i18n.error;

import java.util.Set;
import java.util.HashSet;
import java.lang.reflect.Field;

/**
 * <class-comment/>
 */
public class Errors
{
    private static ErrorHandler handler;

    private static synchronized ErrorHandler handler()
    {
        if (handler == null)
        {
            handler = new ErrorHandler();
        }
        return handler;
    }

    public static String error(ErrorCode errorCode)
    {
        return handler().error(errorCode);
    }

    public static boolean validateErrorCodes(Class classToCheck)
    {
        return validateErrorCodes(classToCheck, new HashSet<Integer>());
    }

    public static boolean validateErrorCodes(Class[] classesToCheck)
    {
        boolean result = true;
        Set<Integer> codes = new HashSet<Integer>();
        for (Class toCheck : classesToCheck)
        {
            if (! validateErrorCodes(toCheck, codes))
            {
                result = false;
                break;
            }
        }
        return result;
    }

    private static boolean validateErrorCodes(Class toCheck, Set<Integer> codes)
    {
        boolean result = true;
        for (Field field : toCheck.getDeclaredFields())
        {
            if (field.getType().equals(ErrorCode.class))
            {
                try
                {
                    ErrorCode error = (ErrorCode) field.get(toCheck);
                    int code = error.getCode();
                    if (codes.contains(code))
                    {
                        result = false;
                        break;
                    }
                    codes.add(code);
                }
                catch (IllegalAccessException e)
                {
                    // noop.
                }
            }
        }
        return result;
    }
}
