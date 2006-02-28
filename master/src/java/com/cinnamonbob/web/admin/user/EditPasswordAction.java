package com.cinnamonbob.web.admin.user;

import com.cinnamonbob.web.user.UserActionSupport;
import com.cinnamonbob.model.User;
import com.cinnamonbob.core.util.RandomUtils;

/**
 * <class-comment/>
 */
public class EditPasswordAction extends UserActionSupport
{
    /**
     * The id of the users whose password is being updated.
     */
    private long id;

    /**
     * The new password.
     */
    private String password;

    /**
     * The new password confirmation.
     */
    private String confirm;

    public long getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id = id;
    }

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

    public void validate()
    {
        if (hasErrors())
        {
            return;
        }
        if (getUserManager().getUser(id) == null)
        {
            addFieldError("id", getText("user.id.unknown"));
            return;
        }

        if (password != null && confirm != null)
        {
            if (password.equals(confirm))
            {
                addFieldError("confirm", getText("user.confirm.mismatch"));
            }
        }
    }

    /**
     * Change the specified users password to some randomly generated password.
     *
     */
    public String doReset()
    {
        User user = getUserManager().getUser(id);

        // generate random password.
        user.setPassword(RandomUtils.randomString(8));
        getUserManager().save(user);

        return SUCCESS;
    }

    /**
     * Edit the specified users password, setting it to the specified password.
     *
     */
    public String execute()
    {
        User user = getUserManager().getUser(id);
        user.setPassword(password);
        getUserManager().save(user);

        return SUCCESS;
    }
}
