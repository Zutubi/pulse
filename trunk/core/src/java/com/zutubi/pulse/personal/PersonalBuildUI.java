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

        public static Response fromInput(String input, Response defaultResponse, Response... allowedResponses)
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
                    if(in(r, allowedResponses) && r.toString().charAt(0) == inputChar)
                    {
                        return r;
                    }
                }
            }
            else
            {
                try
                {
                    Response r = valueOf(input);
                    if(in(r, allowedResponses))
                    {
                        return r;
                    }
                }
                catch(IllegalArgumentException e)
                {
                    // Fall through
                }
            }

            return null;
        }

        private static boolean in(Response r, Response... a)
        {
            for(Response c: a)
            {
                if(c == r)
                {
                    return true;
                }
            }

            return false;
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

    String inputPrompt(String question);
    String inputPrompt(String prompt, String defaultResponse);
    String passwordPrompt(String question);

    Response ynPrompt(String question, Response defaultResponse);
    Response ynaPrompt(String question, Response defaultResponse);
}
