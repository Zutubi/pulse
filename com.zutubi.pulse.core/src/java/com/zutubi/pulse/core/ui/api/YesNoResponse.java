package com.zutubi.pulse.core.ui.api;

/**
 * Used to represent a simple positive or negative response from the user.
 */
public enum YesNoResponse
{
    /**
     * An affirmative response.
     */
    YES('Y', true, false, "Yes"),
    /**
     * A negative response.
     */
    NO('N', false, false, "No"),
    /**
     * A <strong>persistent</strong> positive response.  Implementations
     * that support the always response are responsible for saving the
     * preference to the user's configuration.
     *
     * @see com.zutubi.pulse.core.scm.api.WorkingCopyContext#getConfig
     */
    ALWAYS('A', true, true, "Always"),
    /**
     * A <strong>persistent</strong> negative response.  Implementations
     * that support the always response are responsible for saving the
     * preference to the user's configuration.
     *
     * @see com.zutubi.pulse.core.scm.api.WorkingCopyContext#getConfig
     */
    NEVER('E', false, true, "nEver");

    private char keyChar;
    private boolean affirmative;
    private boolean persistent;
    private String prompt;

    YesNoResponse(char keyChar, boolean affirmative, boolean persistent, String prompt)
    {
        this.keyChar = keyChar;
        this.affirmative = affirmative;
        this.persistent = persistent;
        this.prompt = prompt;
    }

    /**
     * @return true if the response is affirmative, false if it is negative
     */
    public boolean isAffirmative()
    {
        return affirmative;
    }

    /**
     * @return true if the preference should be persisted to the user's
     * configuration
     */
    public boolean isPersistent()
    {
        return persistent;
    }

    /**
     * @return a string representation of this responses to display to the user
     * in a prompt
     */
    public String getPrompt()
    {
        return prompt;
    }

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
    public static YesNoResponse fromInput(String input, YesNoResponse defaultResponse, YesNoResponse... allowedResponses)
    {
        input = input.toUpperCase().trim();
        int length = input.length();

        if(length == 0)
        {
            return defaultResponse;
        }
        else if(length == 1)
        {
            char inputChar = input.charAt(0);
            for(YesNoResponse r: allowedResponses)
            {
                if(r.keyChar == inputChar)
                {
                    return r;
                }
            }
        }
        else
        {
            try
            {
                YesNoResponse r = valueOf(input);
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

    private static boolean in(YesNoResponse r, YesNoResponse... a)
    {
        for(YesNoResponse c: a)
        {
            if(c == r)
            {
                return true;
            }
        }

        return false;
    }
}
