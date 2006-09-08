package com.zutubi.pulse.form.ui;

import com.opensymphony.xwork.ActionContext;
import com.opensymphony.xwork.ActionSupport;
import com.zutubi.pulse.form.bean.BeanException;
import com.zutubi.pulse.form.bean.BeanSupport;
import com.zutubi.pulse.form.descriptor.FieldDescriptor;
import com.zutubi.pulse.form.descriptor.FormDescriptor;
import com.zutubi.pulse.form.descriptor.DescriptorFactory;
import com.zutubi.pulse.form.descriptor.ActionDescriptor;
import com.zutubi.pulse.form.persistence.Copyable;
import com.zutubi.pulse.form.persistence.ObjectStore;
import com.zutubi.pulse.form.squeezer.SqueezeException;
import com.zutubi.pulse.form.squeezer.TypeSqueezer;
import com.zutubi.pulse.form.ui.components.FormComponent;
import com.zutubi.pulse.form.ui.components.Component;
import com.zutubi.pulse.form.validator.*;
import com.zutubi.pulse.form.FieldTypeRegistry;

import java.util.Map;

/**
 * <class-comment/>
 */
public class FormAction extends ActionSupport
{
    /**
     *
     */
    private static final String CANCEL = "cancel";

    /**
     *
     */
    private static final String SAVE = "save";

    /**
     *
     */
    private static final String RESET = "reset";

    /**
     *
     */
    private String objectKey;

    private ValidatorContext validatorContext = new ValidatorContextSupport();

    /**
     *
     */
    private ObjectStore objectStore;

    private DescriptorFactory descriptorFactory;

    private FieldTypeRegistry fieldTypeRegistry;

    private Renderer renderer;

    public void setObjectKey(String objectKey)
    {
        this.objectKey = objectKey;
    }

    public String getRenderedForm()
    {
        return renderer.getRenderedContent();
    }

    public String execute() throws Exception
    {

        Map parameters = ActionContext.getContext().getParameters();
        if (parameters.containsKey(ActionDescriptor.CANCEL))
        {
            return doCancel();
        }
        else if (parameters.containsKey(ActionDescriptor.RESET))
        {
            return doReset();
        }
        else if (parameters.containsKey(ActionDescriptor.SAVE))
        {
            return doSave();
        }
        else
        {
            return doInput();
        }
    }

    public String doSave()
    {
        Copyable obj = objectStore.load(objectKey);



        // copy the form values into the object, checking for conversion errors.
        populateObject(obj, validatorContext);

        // validate the form input
        validateObject(obj, validatorContext);

        if (validatorContext.hasErrors())
        {
            // prepare for rendering.
            doRender(obj);
            return INPUT;
        }

        // persist the changes.
        objectStore.save(objectKey, obj);

        // do rendering...
        doRender(obj);

        return SAVE;
    }

    public String doReset()
    {
        Copyable obj = objectStore.reset(objectKey);

        doRender(obj);

        return RESET;
    }

    public String doCancel()
    {
        Object obj = objectStore.load(objectKey);

        doRender(obj);

        return CANCEL;
    }

    public String doInput()
    {
        Object obj = objectStore.load(objectKey);

        doRender(obj);

        return INPUT;
    }

    private void doRender(Object obj)
    {
        FormDescriptor descriptor = descriptorFactory.createFormDescriptor(obj.getClass());

        // build the form.
        FormComponent form = descriptor.createForm();
        populateForm(form, obj);

        // render it.
        form.render(renderer);
    }

    private void populateObject(Object obj, ValidatorContext validatorContext)
    {
        FormDescriptor formDescriptor = descriptorFactory.createFormDescriptor(obj.getClass());
        for (FieldDescriptor fieldDescriptor : formDescriptor.getFieldDescriptors())
        {
            String name = fieldDescriptor.getName();

            TypeSqueezer squeezer = fieldTypeRegistry.getSqueezer(fieldDescriptor.getFieldType());

            String[] paramValue = getParameterValue(name);
            if (paramValue != null)
            {
                try
                {
                    Object value = squeezer.unsqueeze(paramValue);
                    BeanSupport.setProperty(name, value, obj);
                }
                catch (SqueezeException e)
                {
                    validatorContext.addFieldError(name, name + ".conversionerror");
                }
                catch (BeanException e)
                {
                    validatorContext.addFieldError(name, name + ".beanerror");
                }
            }
        }
    }

    private String[] getParameterValue(String parameterName)
    {
        Map parameters = ActionContext.getContext().getParameters();
        if (!parameters.containsKey(parameterName))
        {
            return null;
        }
        Object parameterValue = parameters.get(parameterName);
        if (parameterValue instanceof String)
        {
            return new String[]{(String)parameterValue};
        }
        else if (parameterValue instanceof String[])
        {
            return (String[]) parameterValue;
        }

        // unexpected non string type...
        return null;
    }

    private void validateObject(Object obj, ValidatorContext validatorContext)
    {
        FormDescriptor formDescriptor = descriptorFactory.createFormDescriptor(obj.getClass());
        for (FieldDescriptor fieldDescriptor : formDescriptor.getFieldDescriptors())
        {
            for (Validator validator : fieldTypeRegistry.getValidators(fieldDescriptor.getFieldType()))
            {
                validator.setValidatorContext(validatorContext);
                if (validator instanceof FieldValidator)
                {
                    ((FieldValidator)validator).setFieldName(fieldDescriptor.getName());
                }
                try
                {
                    validator.validate(obj);
                }
                catch (ValidationException e)
                {
                    validatorContext.addActionError(e.getMessage());
                }
            }
        }

        if (!validatorContext.hasErrors())
        {
            if (obj instanceof Validateable)
            {
                ((Validateable)obj).validate(validatorContext);
            }
        }
    }

    private void populateForm(FormComponent form, Object obj)
    {
        FormDescriptor formDescriptor = descriptorFactory.createFormDescriptor(obj.getClass());
        for (FieldDescriptor fieldDescriptor : formDescriptor.getFieldDescriptors())
        {
            try
            {
                String propertyName = fieldDescriptor.getName();
                Object propertyValue = BeanSupport.getProperty(propertyName, obj);

                Component component = form.getNestedComponent(propertyName);

                TypeSqueezer squeezer = fieldTypeRegistry.getSqueezer(fieldDescriptor.getFieldType());
                component.setValue(squeezer.squeeze(propertyValue));
            }
            catch (BeanException e)
            {
                e.printStackTrace();
            }
            catch (SqueezeException e)
            {
                e.printStackTrace();
            }
        }
    }

    /**
     * Required resource.
     *
     * @param objectStore
     */
    public void setObjectStore(ObjectStore objectStore)
    {
        this.objectStore = objectStore;
    }

    /**
     * Required resource.
     *
     * @param descriptorFactory
     */
    public void setDescriptorFactory(DescriptorFactory descriptorFactory)
    {
        this.descriptorFactory = descriptorFactory;
    }

    /**
     * Required resource.
     *
     * @param renderer
     */
    public void setRenderer(Renderer renderer)
    {
        this.renderer = renderer;
    }

    /**
     * Required resource.
     *
     * @param fieldTypeRegistry
     */
    public void setFieldTypeRegistry(FieldTypeRegistry fieldTypeRegistry)
    {
        this.fieldTypeRegistry = fieldTypeRegistry;
    }
}
