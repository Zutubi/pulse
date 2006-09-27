package com.zutubi.pulse.form.ui;

import com.zutubi.pulse.form.descriptor.*;
import com.zutubi.pulse.form.FieldType;

import java.util.Arrays;

/**
 * <class-comment/>
 */
public class WizardDecorator
{
    private String state;

    private int currentStep;
    private int numberOfSteps;

    public void setStep(int step, int totalNumberOfSteps)
    {
        currentStep = step;
        numberOfSteps = totalNumberOfSteps;
    }

    public void setState(String state)
    {
        this.state = state;
    }

    public FormDescriptor decorate(FormDescriptor descriptor)
    {
        // each field has onkeypress handler: onkeypress=return submitenter(this,event)
        for (FieldDescriptor fieldDescriptor : descriptor.getFieldDescriptors())
        {
            fieldDescriptor.getParameters().put("onkeypress", "return submitenter(this, event)");
        }

        // wizard has extra fields.
        DefaultFieldDescriptor submitField = new DefaultFieldDescriptor();
        submitField.setFieldType(FieldType.HIDDEN);
        submitField.setName("submit");
        submitField.setType(String.class);
        descriptor.addFieldDescriptor(submitField);

        DefaultFieldDescriptor stateField = new DefaultFieldDescriptor();
        stateField.setFieldType(FieldType.HIDDEN);
        stateField.setName("state");
        stateField.getParameters().put("value", state);
        stateField.setType(String.class);
        descriptor.addFieldDescriptor(stateField);

        // wizard has different actions depending upon the stage of the action.
        descriptor.setActionDescriptors(Arrays.asList((ActionDescriptor)
                new DefaultActionDescriptor(ActionDescriptor.PREVIOUS),
                new DefaultActionDescriptor(ActionDescriptor.FINISH),
                new DefaultActionDescriptor(ActionDescriptor.CANCEL)
        ));

        // wizard has a different heading.
        // set heading...

        return descriptor;
    }
}
