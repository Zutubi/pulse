package com.zutubi.pulse.committransformers;

import com.zutubi.pulse.core.ObjectFactory;
import com.zutubi.pulse.model.*;
import com.zutubi.pulse.wizard.Wizard;
import com.zutubi.pulse.wizard.WizardTransition;
import com.zutubi.pulse.notifications.NotificationHandler;
import com.zutubi.validation.Validateable;
import com.zutubi.validation.ValidationContext;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <class comment/>
 */
public class AddCommitMessageTransformerWizard implements Wizard, Validateable
{
    private CommitMessageTransformerManager transformerManager;
    private ProjectManager projectManager;
    private ObjectFactory objectFactory;

    private Object currentState;
    private SelectType selectState;

    private long projectId = -1;

    private Map<String, Object> handlers = new HashMap<String, Object>();

    public long getProjectId()
    {
        return projectId;
    }

    public void setProjectId(long projectId)
    {
        this.projectId = projectId;
    }

    public Project getProject()
    {
        return projectManager.getProject(projectId);
    }

    public Object getCurrentState()
    {
        return currentState;
    }

    public List<WizardTransition> getAvailableActions()
    {
        if (currentState == selectState)
        {
            return Arrays.asList(WizardTransition.NEXT, WizardTransition.CANCEL);
        }
        return Arrays.asList(WizardTransition.PREVIOUS, WizardTransition.FINISH, WizardTransition.CANCEL);
    }

    public void doFinish()
    {
        Object o = handlers.get(selectState.getType());
        if (o instanceof StandardHandler)
        {
            StandardHandler handler = (StandardHandler)o;
            StandardCommitMessageTransformer transformer = new StandardCommitMessageTransformer();
            transformer.setExpression(handler.getExpression());
            transformer.setLink(handler.getLink());
            transformer.setName(handler.getName());
            if (projectId != -1)
            {
                transformer.getProjects().add(projectId);
            }
            transformerManager.save(transformer);
        }
        else if (o instanceof JiraHandler)
        {
            JiraHandler handler = (JiraHandler) o;
            JiraCommitMessageTransformer transformer = new JiraCommitMessageTransformer();
            transformer.setName(handler.getName());
            transformer.setUrl(handler.getUrl());

            if (projectId != -1)
            {
                transformer.getProjects().add(projectId);
            }
            transformerManager.save(transformer);
        }
        else if (o instanceof CustomHandler)
        {
            CustomHandler handler = (CustomHandler) o;
            CustomCommitMessageTransformer transformer = new CustomCommitMessageTransformer();
            transformer.setName(handler.getName());
            transformer.setExpression(handler.getExpression());
            transformer.setReplacement(handler.getReplacement());

            if (projectId != -1)
            {
                transformer.getProjects().add(projectId);
            }
            transformerManager.save(transformer);
        }
    }

    public Object doNext()
    {
        if (currentState == selectState)
        {
            currentState = handlers.get(selectState.getType());
        }
        return currentState;
    }

    public Object doPrevious()
    {
        // only two stages, so always go back to the start.
        currentState = selectState;
        return currentState;
    }

    public void doCancel()
    {
    }

    public void initialise()
    {
        List<String> types = transformerManager.getTransformerTypes();
        selectState = new SelectType(types);

        currentState = selectState;

        for (String type : types)
        {
            try
            {
                Class handlerClass = transformerManager.getTransformerHandler(type);
                Object handler = objectFactory.buildBean(handlerClass);
                handlers.put(type, handler);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    public Object doRestart()
    {
        currentState = selectState;
        return currentState;
    }

    public void validate(ValidationContext context)
    {
        // handle some wizard level validation of the current state.
        if (currentState instanceof CommitMessageHandler)
        {
            CommitMessageTransformer transformer = transformerManager.getByName(((CommitMessageHandler)currentState).getName());
            if (transformer != null)
            {
                context.addFieldError("name", context.getText("name.invalid"));
            }
        }
    }

    public void setCommitMessageTransformerManager(CommitMessageTransformerManager commitMessageTransformerManager)
    {
        this.transformerManager = commitMessageTransformerManager;
    }

    public void setObjectFactory(ObjectFactory objectFactory)
    {
        this.objectFactory = objectFactory;
    }

    public void setProjectManager(ProjectManager projectManager)
    {
        this.projectManager = projectManager;
    }


}
