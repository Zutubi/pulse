package com.zutubi.pulse.core.model;

public enum ResultState
{
    INITIAL
            {
                public String getString()
                {
                    return "pending";
                }

                public String getPrettyString()
                {
                    return "pending";
                }
            },
    IN_PROGRESS
            {
                public String getString()
                {
                    return "inprogress";
                }

                public String getPrettyString()
                {
                    return "in progress";
                }
            },
    TERMINATING
            {
                public String getString()
                {
                    return "terminating";
                }

                public String getPrettyString()
                {
                    return "terminating";
                }
            },
    SUCCESS
            {
                public String getString()
                {
                    return "success";
                }

                public String getPrettyString()
                {
                    return "success";
                }
            },
    FAILURE
            {
                public String getString()
                {
                    return "failure";
                }

                public String getPrettyString()
                {
                    return "failure";
                }
            },
    ERROR
            {
                public String getString()
                {
                    return "error";
                }

                public String getPrettyString()
                {
                    return "error";
                }
            };

    public abstract String getPrettyString();

    public abstract String getString();

    public static ResultState[] getCompletedStates()
    {
        return new ResultState[] { SUCCESS, FAILURE, ERROR };
    }
}
