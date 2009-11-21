package com.zutubi.pulse.master.tove.webwork;

import com.zutubi.pulse.master.tove.model.Field;
import com.zutubi.pulse.master.tove.model.Form;
import com.zutubi.pulse.master.tove.model.SubmitFieldDescriptor;
import com.zutubi.tove.annotations.FieldType;
import freemarker.template.Configuration;
import freemarker.template.TemplateException;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;

/**
 * Base class for Tove actions that manually create custom forms.  This is
 * especially targeted at built-in configuration actions such as refactoring.
 * <p/>
 * To implement a new action using this class:
 * <ul>
 *   <li>
 *     Extend with an action that passes the required details to the
 *     constructor and implements the abstract methods.  To understand the
 *     flow, refer to {@link #execute()}, which calls these abstract methods.
 *   </li>
 *   <li>
 *     Provide an implementation of {@link #doCancel()} which sets up an
 *     appropriate {@link ConfigurationResponse} in the response field.
 *   </li>
 *   <li>
 *     Add a new template to the aconfig/ directory named &lt;action&gt;.vm.
 *     This template should render a form in a panel, using $formSource.
 *   </li>
 *   <li>
 *     Add a new action mapping to the aconfig namespace in xwork.xml.
 *   </li>
 * </ul>
 */
public abstract class ToveFormActionSupport extends ToveActionSupport
{
    protected static final String SUBMIT_CANCEL = "cancel";
    private static final String FORM_NAME = "form";

    private String actionName;
    private String submitName;
    protected String formSource;
    protected ConfigurationPanel newPanel;

    private Configuration freemarkerConfiguration;

    /**
     * Create a new action to render a form.
     *
     * @param actionName the internal name of the action, used for xwork
     *                   mapping and locating the template to render
     * @param submitName text on the default submit button for the form
     */
    public ToveFormActionSupport(String actionName, String submitName)
    {
        this.actionName = actionName;
        newPanel = new ConfigurationPanel("aconfig/" + actionName + ".vm");
        this.submitName = submitName;
    }

    public String getFormSource()
    {
        return formSource;
    }

    public ConfigurationPanel getNewPanel()
    {
        return newPanel;
    }

    public String execute() throws Exception
    {
        validatePath();
        if(isInputSelected())
        {
            initialiseParameters();
            renderForm();
            return INPUT;
        }

        validateForm();

        if(hasErrors())
        {
            renderForm();
            return INPUT;
        }

        doAction();
        return SUCCESS;
    }

    private void renderForm() throws IOException, TemplateException
    {
        Form form = new Form(FORM_NAME, actionName, ToveUtils.getConfigURL(path, getFormAction(), null, "aconfig"), submitName);
        addFormFields(form);
        addSubmit(form, submitName, true);
        addSubmit(form, SUBMIT_CANCEL, false);

        StringWriter writer = new StringWriter();
        ToveUtils.renderForm(form, getClass(), writer, freemarkerConfiguration);
        formSource = writer.toString();
    }

    /**
     * Extracts the value of a form parameter submitted from the client.
     *
     * @param parameters map of parameters from the client
     * @param name       name of the parameter to retrieve
     * @return the value of the given parameter, may be null
     */
    protected String getParameterValue(Map parameters, String name)
    {
        Object rawValue = parameters.get(name);
        String value = null;
        if(rawValue instanceof String)
        {
            value = (String) rawValue;
        }
        else if(rawValue instanceof String[])
        {
            value = ((String[]) rawValue)[0];
        }
        return value;
    }

    private void addSubmit(Form form, String name, boolean isDefault)
    {
        Field field = new Field(FieldType.SUBMIT, name);
        field.setValue(name);
        if(isDefault)
        {
            field.addParameter(SubmitFieldDescriptor.PARAM_DEFAULT, true);
        }

        form.add(field);
    }

    /**
     * Gets the action the form should submit to.  By default this is the
     * action passed on construction, but for advanced cases (e.g. smart
     * clone vs normal clone) you may override this.
     *
     * @return the action the form should submit to
     */
    protected String getFormAction()
    {
        return actionName;
    }

    /**
     * May be overridden to provide initial values for parameters to be used
     * to populate a new form.
     */
    protected void initialiseParameters()
    {
        // Do nothing.
    }

    /**
     * Called to validate the path parameter.  This may also involve the
     * initialisation of other information based on the path.
     *
     * @throws IllegalArgumentException if the path is invalid
     */
    protected abstract void validatePath();

    /**
     * Adds all fields to the form.  Called during form rendering, so that
     * subclasses can supply the form fields.
     *
     * @param form form to add the fields to
     */
    protected abstract void addFormFields(Form form);

    /**
     * Called to validate submitted form parameters.  Any errors should be
     * recorded on the action itself using {@link #addActionError(String)} or
     * {@link #addFieldError(String, String)}.
     */
    protected abstract void validateForm();

    /**
     * Called to execute the requested action.  This stage is reached when the
     * form is submitted and validates.  The implementation should set up an
     * appropriate {@link com.zutubi.pulse.master.tove.webwork.ConfigurationResponse}
     * in the response field.
     */
    protected abstract void doAction();

    public void setFreemarkerConfiguration(Configuration freemarkerConfiguration)
    {
        this.freemarkerConfiguration = freemarkerConfiguration;
    }
}
