package com.cinnamonbob.web.setup;

import com.cinnamonbob.web.wizard.BaseWizard;
import com.cinnamonbob.web.wizard.BaseWizardState;
import com.cinnamonbob.web.wizard.Wizard;
import com.cinnamonbob.web.wizard.WizardCompleteState;
import com.cinnamonbob.model.UserManager;
import com.cinnamonbob.model.User;

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
        userManager.save(createAdminState.getAdmin());
    }

    public void setUserManager(UserManager userManager)
    {
        this.userManager = userManager;
    }

    public class CreateAdminState extends BaseWizardState
    {
        private User admin = new User();

        public CreateAdminState(Wizard wizard, String name)
        {
            super(wizard, name);
        }

        public String getNextStateName()
        {
            return "success";
        }

        public User getAdmin()
        {
            return admin;
        }
    }
}
