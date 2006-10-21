package com.zutubi.pulse.personal;

/**
 * Helper for personal build implementations that simplifies listener
 * handling.
 */
public abstract class PersonalBuildSupport implements PersonalBuildWorker
{
    private PersonalBuildUI ui;

    public void setUI(PersonalBuildUI ui)
    {
        this.ui = ui;
    }

    protected void debug(String message, Object... args)
    {
        if(ui != null)
        {
            ui.debug(String.format(message, args));
        }
    }
    
    protected void status(String message)
    {
        if(ui != null)
        {
            ui.status(message);
        }
    }

    protected void warning(String message)
    {
        if (ui != null)
        {
            ui.warning(message);
        }
    }

    protected void error(String message)
    {
        if (ui != null)
        {
            ui.error(message);
        }
    }

    protected void error(String message, Throwable throwable)
    {
        if (ui != null)
        {
            ui.error(message, throwable);
        }
    }

    protected PersonalBuildUI.Response ynaPrompt(String question, PersonalBuildUI.Response defaultResponse)
    {
        if(ui == null)
        {
            return defaultResponse;
        }
        else
        {
            return ui.ynaPrompt(question, defaultResponse);
        }
    }

    public PersonalBuildUI getUi()
    {
        return ui;
    }
}
