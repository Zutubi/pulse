package com.cinnamonbob.web.setup;

import com.cinnamonbob.web.wizard.BaseWizard;
import com.cinnamonbob.web.wizard.BaseWizardState;
import com.cinnamonbob.web.wizard.Wizard;
import com.cinnamonbob.web.wizard.WizardCompleteState;
import com.cinnamonbob.model.UserManager;
import com.cinnamonbob.model.User;
import com.cinnamonbob.model.GrantedAuthority;
import com.cinnamonbob.security.AcegiUtils;
import com.opensymphony.xwork.Validateable;

/**
 * <class-comment/>
 */
public class SetupWizard extends BaseWizard
{
    private CreateAdminState createAdminState;

    private UserManager userManager;

    public SetupWizard()
    {
        // create the admin user.
        createAdminState = new CreateAdminState(this, "admin");

        addInitialState("admin", createAdminState);
        addFinalState("success", new WizardCompleteState(this, "success"));
    }

    public void process()
    {
        super.process();

        // create the admin user.
        User admin = createAdminState.getAdmin();
        admin.setEnabled(true);
        admin.add(GrantedAuthority.USER);
        admin.add(GrantedAuthority.ADMINISTRATOR);
        userManager.save(admin);

        // login as the admin user.
        AcegiUtils.loginAs(admin);
    }

    public void setUserManager(UserManager userManager)
    {
        this.userManager = userManager;
    }

    public class CreateAdminState extends BaseWizardState implements Validateable
    {
        private User admin = new User();

        private String confirm;

        public CreateAdminState(Wizard wizard, String name)
        {
            super(wizard, name);
        }

        public void validate()
        {
            if (!confirm.equals(admin.getPassword()))
            {
                addFieldError("confirm", "confirmed password does not match password, please re-enter your password");
            }
        }

        public String getNextStateName()
        {
            return "success";
        }

        public User getAdmin()
        {
            return admin;
        }

        public String getConfirm()
        {
            return confirm;
        }

        public void setConfirm(String confirm)
        {
            this.confirm = confirm;
        }
    }
}
