package com.zutubi.pulse.core.model;

import com.zutubi.util.TextUtils;

import java.util.*;

public enum ResultState
{
    INITIAL
            {
                public boolean isCompleted()
                {
                    return false;
                }

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
                public boolean isCompleted()
                {
                    return false;
                }

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
                public boolean isCompleted()
                {
                    return false;
                }

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
                public boolean isCompleted()
                {
                    return true;
                }

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
                public boolean isCompleted()
                {
                    return true;
                }

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
                public boolean isCompleted()
                {
                    return true;
                }

                public String getString()
                {
                    return "error";
                }

                public String getPrettyString()
                {
                    return "error";
                }
            };

    public abstract boolean isCompleted();
    public abstract String getPrettyString();
    public abstract String getString();


    public static final String SEPARATOR = ",";
    private static final ResultState[] COMPLETED_STATES;
    private static final ResultState[] INCOMPLETE_STATES;
    static
    {
        int complete = 0;
        int incomplete = 0;
        for(ResultState state: values())
        {
            if(state.isCompleted())
            {
                complete++;
            }
            else
            {
                incomplete++;
            }
        }

        COMPLETED_STATES = new ResultState[complete];
        INCOMPLETE_STATES = new ResultState[incomplete];

        complete = 0;
        incomplete = 0;
        for(ResultState state: values())
        {
            if(state.isCompleted())
            {
                COMPLETED_STATES[complete++] = state;
            }
            else
            {
                INCOMPLETE_STATES[incomplete++] = state;
            }
        }
    }

    public static List<String> getNames(List<ResultState> states)
    {
        List<String> result = new ArrayList<String>(states.size());
        for(ResultState state: states)
        {
            result.add(state.toString());
        }
        return result;
    }

    public static String getStatesString(ResultState... states)
    {
        return getStateNamesString(getNames(Arrays.asList(states)));
    }

    public static String getStatesString(List<ResultState> states)
    {
        return getStateNamesString(getNames(states));
    }

    public static String getStateNamesString(List<String> stateNames)
    {
        StringBuilder result = new StringBuilder();
        boolean first = true;

        for(String s: stateNames)
        {
            if(first)
            {
                first = false;
            }
            else
            {
                result.append(SEPARATOR);
            }

            result.append(s);
        }

        return result.toString();
    }

    public static List<String> getNamesList(String value)
    {
        return getNames(getStatesList(value));
    }

    public static List<ResultState> getStatesList(String value)
    {
        return Arrays.asList(getStates(value));
    }

    public static List<ResultState> getStatesList(List<String> names)
    {
        List<ResultState> result = new LinkedList<ResultState>();
        if (names != null)
        {
            for(String name: names)
            {
                result.add(ResultState.valueOf(name));
            }
        }
        return result;
    }

    public static ResultState[] getStates(String value)
    {
        if(!TextUtils.stringSet(value))
        {
            return new ResultState[0];
        }

        String[] parts = value.split(SEPARATOR);
        ResultState[] states = new ResultState[parts.length];

        for(int i = 0; i < parts.length; i++)
        {
            states[i] = ResultState.valueOf(parts[i]);
        }

        return states;
    }

    public static ResultState[] getCompletedStates()
    {
        return COMPLETED_STATES;
    }

    public static Map<String, String> getCompletedStatesMap()
    {
        Map<String, String> states = new LinkedHashMap<String, String>();
        for(ResultState state: getCompletedStates())
        {
            states.put(state.toString(), state.toString().toLowerCase());
        }

        return states;
    }

    public static String[] getCompletedStateNames()
    {
        ResultState[] states = getCompletedStates();
        String[] result = new String[states.length];
        for(int i = 0; i < states.length; i++)
        {
            result[i] = states[i].toString();
        }

        return result;
    }

    public static ResultState[] getIncompleteStates()
    {
        return INCOMPLETE_STATES;
    }

    public static ResultState getWorseState(ResultState s1, ResultState s2)
    {
        if(s1 == ERROR || s2 == ERROR)
        {
            return ERROR;
        }
        else if(s1 == FAILURE || s2 == FAILURE)
        {
            return FAILURE;
        }
        else
        {
            return SUCCESS;
        }
    }

    public static ResultState fromPrettyString(String prettyString)
    {
        for(ResultState state: values())
        {
            if(state.getPrettyString().equals(prettyString))
            {
                return state;
            }
        }

        throw new IllegalArgumentException("No such result state '" + prettyString + "'");
    }
}
