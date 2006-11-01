package com.zutubi.pulse.personal;

/**
 * Implementations of this interface listen for details of personal build
 * operations.
 */
public interface PersonalBuildUI
{
    public enum Response
    {
        YES
        {
            public boolean isAffirmative()
            {
                return true;
            }

            public boolean isPersistent()
            {
                return false;
            }
        },
        NO
        {
            public boolean isAffirmative()
            {
                return false;
            }

            public boolean isPersistent()
            {
                return false;
            }
        },
        ALWAYS
        {
            public boolean isAffirmative()
            {
                return true;
            }

            public boolean isPersistent()
            {
                return true;
            }
        };

        public abstract boolean isAffirmative();
        public abstract boolean isPersistent();

        public static Response fromInput(String input, Response defaultResponse)
        {
            input = input.toUpperCase();
            int length = input.length();

            if(length == 0)
            {
                return defaultResponse;
            }
            else if(length == 1)
            {
                char inputChar = input.charAt(0);

                for(Response r: values())
                {
                    if(r.toString().charAt(0) == inputChar)
                    {
                        return r;
                    }
                }
            }
            else
            {
                try
                {
                    return valueOf(input);
                }
                catch(IllegalArgumentException e)
                {
                    // Fall through
                }
            }

            return null;
        }
    }

    public enum Verbosity
    {
        QUIET,
        NORMAL,
        VERBOSE
    }

    void setVerbosity(Verbosity verbosity);

    void debug(String message);
    void status(String message);
    void warning(String message);
    void error(String message);
    void error(String message, Throwable throwable);

    void enterContext();
    void exitContext();

    Response ynaPrompt(String question, Response defaultResponse);
}
