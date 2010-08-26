package com.zutubi.pulse.core.ui.api;

/**
 * Represents a single option in a menu shown to the user.
 *
 * @see com.zutubi.pulse.core.ui.api.UserInterface
 */
public class MenuOption<T>
{
    private T value;
    private String text;
    private boolean defaultOption;

    /**
     * Creates a new menu option.
     *
     * @param value         value of the option: returned in the {@link MenuChoice}
     *                      when the user chooses this option
     * @param text          text to display to the user for this option
     * @param defaultOption if true, this option shoud be the default if the
     *                      user makes no selection
     */
    public MenuOption(T value, String text, boolean defaultOption)
    {
        this.value = value;
        this.text = text;
        this.defaultOption = defaultOption;
    }

    /**
     * The value this option represents, returned in the {@link MenuChoice}
     * when the user chooses this option.
     *
     * @return the value represented by this option
     */
    public T getValue()
    {
        return value;
    }

    /**
     * The text displayed to the user for this option.
     *
     * @return a human-readable description of this option
     */
    public String getText()
    {
        return text;
    }

    /**
     * Indicates if this option is the default.
     *
     * @return true if this is the default option, false otherwise
     */
    public boolean isDefaultOption()
    {
        return defaultOption;
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

        MenuOption that = (MenuOption) o;

        if (defaultOption != that.defaultOption)
        {
            return false;
        }
        if (!text.equals(that.text))
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
        result = 31 * result + text.hashCode();
        result = 31 * result + (defaultOption ? 1 : 0);
        return result;
    }
}
