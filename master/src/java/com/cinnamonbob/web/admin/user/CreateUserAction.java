package com.cinnamonbob.web.admin.user;

import com.cinnamonbob.model.User;
import com.cinnamonbob.model.GrantedAuthority;
import com.cinnamonbob.web.user.UserActionSupport;

import java.util.Arrays;

/**
 * <class-comment/>
 */
public class CreateUserAction extends UserActionSupport
{
    private User user = new User();
    private String confirm;

    public Object getUser()
    {
        return user;
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
            // do not attempt to validate unless all other validation rules have
            // completed successfully.
            return;
        }
        // check the password confirmation.
        if (!confirm.equals(user.getPassword()))
        {
            addFieldError("confirm", getText("user.confirm.mismatch"));
        }

        // check that the user does not already exist.
        if (getUserManager().getUser(user.getLogin()) != null)
        {
            // login name already in use.
            addFieldError("user.login", getText("user.login.exists", Arrays.asList(user.getLogin())));
        }
    }

    public String execute() throws Exception
    {
        // ensure that the user has the correct authorities to login.
        user.add(GrantedAuthority.USER);
        user.setEnabled(true);
        getUserManager().save(user);
        doReset();
        return SUCCESS;
    }

    public void doReset()
    {
        // reset the user details.
        user = new User();
    }
}
