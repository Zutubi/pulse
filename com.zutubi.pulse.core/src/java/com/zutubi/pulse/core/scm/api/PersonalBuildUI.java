package com.zutubi.pulse.core.scm.api;

/**
 * An interface for interaction with a user that is running personal build/
 * working copy operations.  Can be used to query for user input such as
 * passwords or confirmation, or to provide feedback during long-running
 * operations.
 */
public interface PersonalBuildUI
{
    /**
     * Used to represent a response from the user.
     */
    public enum Response
    {
        /**
         * An affirmative response.
         */
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
        /**
         * A negative response.
         */
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
        /**
         * A <strong>persistent</strong> positive response.  Only returned from
         * PersonalBuildUI#ynaPrompt.  Implementations that support the always
         * response are responsible for saving the preference to the user's
         * configuration.
         *
         * @see WorkingCopyContext#getConfig
         */
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

        /**
         * @return true if the response is affirmative, false if it is negative
         */
        public abstract boolean isAffirmative();

        /**
         * @return true if the preference should be persisted to the user's
         * configuration
         */
        public abstract boolean isPersistent();

        /**
         * Converts raw user input to a response, checking it falls withing the
         * set of valid responses.
         *
         * @param input            raw input from the user
         * @param defaultResponse  the default response, returned if input is
         *                         the empty string
         * @param allowedResponses the set of allowed responses
         * @return the user's response, or null if it did not conform to the
         *         allowed set
         */
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

    /**
     * Indicates if debug messages will be used by this interface.  If not, and
     * they are expensive to calculate, their calculation can be skipped.
     *
     * @return true iff debug messages will be used by this ui
     */
    boolean isDebugEnabled();

    /**
     * Reports a line of debugging-level feedback to the user.  The user will
     * not typically see this unless they request extra verbosity of feedback.
     *
     * @param message the message to report
     */
    void debug(String message);

    /**
     * Reports a line of informational feedback to the user.  The user will
     * typically see this unless they request supression of feedback.
     *
     * @param message the message to report
     */
    void status(String message);

    /**
     * Reports a line of warning-level feedback to the user.  The user will
     * typically see this unless they request supression of feedback.
     *
     * @param message the message to report
     */
    void warning(String message);

    /**
     * Reports a line of error-level feedback to the user.  The user will
     * always see this type of output.
     *
     * @param message the message to report
     */
    void error(String message);

    /**
     * Reports a line of error-level feedback to the user, optionally with a
     * full trace for the given throwable.  The user will always see the error
     * message, but will typically only see the full trace if they request
     * verbose feedback.
     *
     * @param message   the message to report
     * @param throwable the throwable that indicates the error, a trace of
     *                  which may be reported to the user
     */
    void error(String message, Throwable throwable);

    /**
     * Call to indicate that following messages fall within a common context.
     * This can be used to group related messages for the aid of the user.  For
     * example, if reporting on a directory, a caller may report a status
     * message with the directory name, then call this method before reporting
     * on each file within the directory, before finally calling
     * {@link #exitContext}. to return to the original level.  For the console
     * UI, indention is used to delineate contexts, so the output would appear
     * like:
     * <pre>
     * {@literal Processing directory foo...
     *     processing bar.txt
     *     processing baz.txt
     * Directory foo processed.}
     * </pre>
     */
    void enterContext();

    /**
     * Call to indicate that a context has completed, and the UI should return
     * to the previous context.
     *
     * @see #enterContext()
     */
    void exitContext();

    /**
     * Prompts the user for free-form input.
     *
     * @param prompt the prompt to present to the user
     * @return the raw string entered by the user, may be empty
     */
    String inputPrompt(String prompt);

    /**
     * Prompts the user for free-form input, providing them with a default
     * response which will be returned if they do not provide one.
     *
     * @param prompt          the prompt to present to the user, the UI itself
     *                        will provide details of the default response
     * @param defaultResponse the response to return if the user accepts the
     *                        default
     * @return the raw string entered by the user, which may be the default if
     *         they choose to accept it
     */
    String inputPrompt(String prompt, String defaultResponse);

    /**
     * Prompts the user for a password.  Their input will not be echoed in the
     * UI.
     *
     * @param prompt the prompt to present to the user
     * @return the raw string entered by the user
     */
    String passwordPrompt(String prompt);

    /**
     * Prompts the user for a yes or no response, with a default option
     * provided.
     *
     * @param question        the question to ask the user, the UI itself will
     *                        provide details of the default response
     * @param defaultResponse the response to return if the user accepts the
     *                        default
     * @return the user's response, one of {@link Response#YES} or
     *         {@link Response#NO}
     */
    Response ynPrompt(String question, Response defaultResponse);

    /**
     * Prompts the user for a yes, no or always response, with a default option
     * provided.  If the always response is given, the caller is responsible
     * for saving the preference to the user's configuration.
     *
     * @param question        the question to ask the user, the UI itself will
     *                        provide details of the default response
     * @param defaultResponse the response to return if the user accepts the
     *                        default
     * @return the user's response
     *
     * @see WorkingCopyContext#getConfig()
     */
    Response ynaPrompt(String question, Response defaultResponse);
}
