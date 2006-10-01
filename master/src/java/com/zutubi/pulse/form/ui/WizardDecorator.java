package com.zutubi.pulse.form.ui;

import com.zutubi.pulse.form.descriptor.*;
import com.zutubi.pulse.form.FieldType;

import java.util.Arrays;
import java.util.List;
import java.util.LinkedList;

/**
 * <class-comment/>
 */
public class WizardDecorator
{
    private String state;

    private int currentStep;
    private int numberOfSteps;

    private boolean showPrevious;
    private boolean showFinish;

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

        List<ActionDescriptor> actionDescriptors = new LinkedList<ActionDescriptor>();
        if (showPrevious)
        {
            actionDescriptors.add(new DefaultActionDescriptor(ActionDescriptor.PREVIOUS));
        }
        if (showFinish)
        {
            actionDescriptors.add(new DefaultActionDescriptor(ActionDescriptor.FINISH));
        }
        else
        {
            actionDescriptors.add(new DefaultActionDescriptor(ActionDescriptor.NEXT));
        }
        actionDescriptors.add(new DefaultActionDescriptor(ActionDescriptor.CANCEL));

        descriptor.setActionDescriptors(actionDescriptors);

        // wizard has a different heading.
        // set heading...

        return descriptor;
    }

    public void setFirstState(boolean firstState)
    {
        showPrevious = !firstState;
    }

    public void setLastState(boolean lastState)
    {
        showFinish = lastState;
    }
}
