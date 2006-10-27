package com.zutubi.pulse.web.wizard.auto;

import com.opensymphony.xwork.ActionInvocation;
import com.opensymphony.xwork.ActionContext;
import com.opensymphony.xwork.interceptor.Interceptor;
import com.opensymphony.xwork.interceptor.PreResultListener;
import com.opensymphony.xwork.util.OgnlValueStack;
import com.zutubi.pulse.form.descriptor.DescriptorFactory;
import com.zutubi.pulse.form.ui.FormSupport;
import com.zutubi.pulse.wizard.Wizard;
import com.zutubi.pulse.validation.MessagesTextProvider;
import com.zutubi.validation.ValidationManager;
import com.zutubi.validation.ValidationContext;
import com.zutubi.validation.DelegatingValidationContext;
import com.zutubi.validation.XWorkValidationAdapter;
import freemarker.template.Configuration;

import java.util.Map;

/**
 * <class comment/>
 */
public class WizardInterceptor implements Interceptor
{
    private ValidationManager validationManager;

    private Configuration configuration;

    private DescriptorFactory descriptorFactory;

    public void destroy()
    {

    }

    public void init()
    {
    }

    public String intercept(ActionInvocation invocation) throws Exception
    {
        String result = before(invocation);
        if (result != null)
        {
            return result;
        }
        return invocation.invoke();
    }

    protected String before(ActionInvocation invocation) throws Exception
    {
        Object action = invocation.getAction();

        final OgnlValueStack stack = invocation.getStack();

        // short circuit the wizard if circumstances require it.
        String shortCircuit = null;

        if (action instanceof WizardAction)
        {
            WizardAction wizardAction = (WizardAction) action;

            // if an action has been requested, then we need to ensure that the state for which the action was
            // requests matches the state of the wizard. If the user has used the browser back button for example,
            // the wizard state will be out of sync.
            // NOTE: we need to go directly to the parameters here since the wizard will not have been through
            //       the params interceptor.
            final Map parameters = ActionContext.getContext().getParameters();
            boolean actionRequested = parameters.containsKey("next") ||
                    parameters.containsKey("previous") ||
                    parameters.containsKey("finish") ||
                    parameters.containsKey("cancel") ||
                    parameters.containsKey("submit");

            if (actionRequested)
            {
                // ensure state is in sync.
                if(wizardAction.isInitialised())
                {
                    String[] actualStates = (String[]) parameters.get("state");
                    String actualState = (actualStates.length > 0) ? actualStates[0] : null;
                    String expectedState = wizardAction.getState().getClass().getName();
                    if (!expectedState.equals(actualState))
                    {
                        Wizard wizard = wizardAction.getWizardInstance();
                        while (!expectedState.equals(actualState))
                        {
                            wizard.doPrevious();
                            expectedState = wizard.getCurrentState().getClass().getName();
                        }
                    }
                }
                else
                {
                    wizardAction.addActionError(wizardAction.getText("wizard.state.lost"));
                    wizardAction.getWizardInstance().doRestart();
                    shortCircuit = "step";
                }
            }

            // ensure that the wizard is on the stack so that it receives any necessary parameters.
            stack.push(wizardAction.getWizardInstance());

            invocation.addPreResultListener(new PreResultListener()
            {
                public void beforeResult(ActionInvocation invocation, String resultCode)
                {
                    if (resultCode.equals("step"))
                    {
                        // TODO: The rendering should be moved into the template, allowing it to be
                        // configured on a per view basis as required.
                        WizardAction wizardAction = (WizardAction) invocation.getAction();
                        stack.getContext().put("renderedForm", renderState(wizardAction));
                    }
                }
            });
        }
        return shortCircuit;
    }

    private String renderState(WizardAction action)
    {
        //Render the current state.
        Wizard wizard = action.getWizardInstance();
        Object subject = action.getState();

        // Setting up this form support is ugly.  There must be a better way to handle the initialisation
        // of the required objects.
        FormSupport support = createFormSupport(subject);

        ValidationContext validatorContext = createValidationContext(subject, action);
        try
        {
            // rendering should be much simpler once the state, first and last variables are removed.
            return support.renderWizard(wizard, subject, validatorContext);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    private ValidationContext createValidationContext(Object subject, WizardAction wizardAction)
    {
        MessagesTextProvider textProvider = new MessagesTextProvider(subject);
        return new DelegatingValidationContext(new XWorkValidationAdapter(wizardAction), textProvider);
    }

    private FormSupport createFormSupport(Object subject)
    {
        FormSupport support = new FormSupport();
        support.setValidationManager(validationManager);
        support.setConfiguration(configuration);
        support.setDescriptorFactory(descriptorFactory);
        support.setTextProvider(new com.zutubi.pulse.form.MessagesTextProvider(subject));
        return support;
    }

    public void setValidationManager(ValidationManager validationManager)
    {
        this.validationManager = validationManager;
    }

    public void setFreemarkerConfiguration(Configuration configuration)
    {
        this.configuration = configuration;
    }

    public void setDescriptorFactory(DescriptorFactory descriptorFactory)
    {
        this.descriptorFactory = descriptorFactory;
    }

}
