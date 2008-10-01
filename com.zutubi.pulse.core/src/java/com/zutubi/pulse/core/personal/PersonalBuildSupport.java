package com.zutubi.pulse.core.personal;

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

    protected void enterContext()
    {
        if (ui != null)
        {
            ui.enterContext();
        }
    }

    protected void exitContext()
    {
        if (ui != null)
        {
            ui.exitContext();
        }
    }

    protected String inputPrompt(String prompt)
    {
        if(ui != null)
        {
            return ui.inputPrompt(prompt);
        }
        else
        {
            return "";
        }
    }

    protected String inputPrompt(String prompt, String defaultResponse)
    {
        if(ui != null)
        {
            return ui.inputPrompt(prompt, defaultResponse);
        }
        else
        {
            return defaultResponse;
        }
    }

    protected String passwordPrompt(String prompt)
    {
        if(ui != null)
        {
            return ui.passwordPrompt(prompt);
        }
        else
        {
            return "";
        }
    }

    protected String readConfirmedPassword(String prompt)
    {
        if(ui != null)
        {
            while(true)
            {
                String password = ui.passwordPrompt(prompt);
                String confirm = ui.passwordPrompt("Confirm");

                if(password.equals(confirm))
                {
                    return password;
                }
                else
                {
                    status("Passwords do not match");
                }
            }
        }
        else
        {
            return "";
        }
    }

    protected PersonalBuildUI.Response ynPrompt(String question, PersonalBuildUI.Response defaultResponse)
    {
        if(ui == null)
        {
            return defaultResponse;
        }
        else
        {
            return ui.ynPrompt(question, defaultResponse);
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

    public boolean hasUI()
    {
        return ui != null;
    }
}
