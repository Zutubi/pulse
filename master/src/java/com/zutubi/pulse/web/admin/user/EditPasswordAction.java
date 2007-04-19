package com.zutubi.pulse.web.admin.user;

import com.zutubi.pulse.model.User;
import com.zutubi.util.RandomUtils;
import com.zutubi.util.StringUtils;
import com.zutubi.pulse.web.user.UserActionSupport;

/**
 * <class-comment/>
 */
public class EditPasswordAction extends UserActionSupport
{
    /**
     * The new password.
     */
    private String password;
    /**
     * The new password confirmation.
     */
    private String confirm;
    private int startPage = 0;

    public String getPassword()
    {
        return password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    public String getConfirm()
    {
        return confirm;
    }

    public void setConfirm(String confirm)
    {
        this.confirm = confirm;
    }

    public int getStartPage()
    {
        return startPage;
    }

    public void validate()
    {
        if (hasErrors())
        {
            return;
        }
        if (getUser() == null)
        {
            addUnknownUserFieldError();
            return;
        }

        if (!StringUtils.equals(password, confirm))
        {
            addFieldError("confirm", getText("password.confirm.mismatch"));
        }
    }

    /**
     * Change the specified users password to some randomly generated password.
     *
     */
    public String doReset()
    {
        //TODO: There needs to be a way to send out some form of notification of this password.
        String rawPassword = RandomUtils.randomString(8);
        User user = getUser();
        getUserManager().setPassword(user, rawPassword);
        getUserManager().save(user);
        startPage = getUserStartPage(user);
        return SUCCESS;
    }

    /**
     * Edit the specified users password, setting it to the specified password.
     *
     */
    public String execute()
    {
        User user = getUser();
        getUserManager().setPassword(user, password);
        getUserManager().save(user);
        startPage = getUserStartPage(user);
        return SUCCESS;
    }
}
