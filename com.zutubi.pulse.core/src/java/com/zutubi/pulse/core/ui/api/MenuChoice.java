package com.zutubi.pulse.core.ui.api;

/**
 * Represents the user's choice when presented with a menu.
 *
 * @see com.zutubi.pulse.core.ui.api.UserInterface
 */
public class MenuChoice<T>
{
    private T value;
    private boolean persistent;

    /**
     * Create a new choice with the given value.
     *
     * @param value      the value of the option chosen by the user
     * @param persistent if true, the user has requested that this choice is
     *                   persisted (i.e. made automatically from now on)
     */
    public MenuChoice(T value, boolean persistent)
    {
        this.value = value;
        this.persistent = persistent;
    }

    /**
     * Gets the value of the option chosen by the user.
     *
     * @return the value for this choice
     */
    public T getValue()
    {
        return value;
    }

    /**
     * Indicates if the user wants this choice to persist.
     *
     * @return true if the user indicated they want this choice to persist,
     *         false otherwise
     */
    public boolean isPersistent()
    {
        return persistent;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        MenuChoice that = (MenuChoice) o;

        if (persistent != that.persistent)
        {
            return false;
        }
        if (!value.equals(that.value))
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = value.hashCode();
        result = 31 * result + (persistent ? 1 : 0);
        return result;
    }

    @Override
    public String toString()
    {
        return value.toString() + (persistent ? "!" : "");
    }
}
