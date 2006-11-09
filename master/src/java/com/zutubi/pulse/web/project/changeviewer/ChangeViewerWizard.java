package com.zutubi.pulse.web.project.changeviewer;

import com.zutubi.pulse.wizard.Wizard;
import com.zutubi.pulse.wizard.WizardTransition;
import com.zutubi.pulse.model.*;
import com.zutubi.pulse.core.ObjectFactory;
import com.zutubi.validation.Validateable;
import com.zutubi.validation.ValidationContext;

import java.util.*;

/**
 * <class comment/>
 */
public class ChangeViewerWizard implements Wizard, Validateable
{
    private static final String TYPE_CUSTOM = "custom";
    private static final String TYPE_FISHEYE = "Fisheye";
    private static final String TYPE_TRAC = "Trac";
    private static final String TYPE_VIEW_VC = "ViewVC";

    private ProjectManager projectManager;
    private ObjectFactory objectFactory = null;

    private long projectId;

    private SelectType selectState;
    private Object currentState;
    private Map<String, ChangeViewerForm> forms;


    public long getProjectId()
    {
        return projectId;
    }

    public void setProjectId(long projectId)
    {
        this.projectId = projectId;
    }

    public void initialise()
    {
        forms = new LinkedHashMap<String, ChangeViewerForm>();
        CustomChangeViewerForm custom = new CustomChangeViewerForm();
        forms.put(TYPE_CUSTOM, custom);
        FisheyeChangeViewerForm fisheye = new FisheyeChangeViewerForm();
        forms.put(TYPE_FISHEYE, fisheye);
        TracChangeViewerForm trac = new TracChangeViewerForm();
        forms.put(TYPE_TRAC, trac);
        ViewVCChangeViewerForm viewVc = new ViewVCChangeViewerForm();
        forms.put(TYPE_VIEW_VC, viewVc);

        selectState = new SelectType(forms.keySet());
        currentState = selectState;

        Project project = projectManager.getProject(projectId);
        if(project != null)
        {
            ChangeViewer currentViewer = project.getChangeViewer();
            if(currentViewer != null)
            {
                if(currentViewer instanceof CustomChangeViewer)
                {
                    custom.initialise((CustomChangeViewer)currentViewer);
                    selectState.setType(TYPE_CUSTOM);
                    currentState = custom;
                }
                else if(currentViewer instanceof FisheyeChangeViewer)
                {
                    fisheye.initialise((FisheyeChangeViewer)currentViewer);
                    selectState.setType(TYPE_FISHEYE);
                    currentState = fisheye;
                }
                else if(currentViewer instanceof TracChangeViewer)
                {
                    trac.initialise((TracChangeViewer) currentViewer);
                    selectState.setType(TYPE_TRAC);
                    currentState = trac;
                }
                else if(currentViewer instanceof ViewVCChangeViewer)
                {
                    viewVc.initialise((ViewVCChangeViewer)currentViewer);
                    selectState.setType(TYPE_VIEW_VC);
                    currentState = viewVc;
                }
            }
        }
    }

    public Object getCurrentState()
    {
        return currentState;
    }

    public boolean isInCustomState()
    {
        return currentState == forms.get(TYPE_CUSTOM);
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
        Project project = projectManager.getProject(projectId);

        ChangeViewerForm form = forms.get(selectState.getType());
        project.setChangeViewer(form.constructChangeViewer());
        projectManager.save(project);
    }

    public Object doNext()
    {
        if (currentState == selectState)
        {
            currentState = forms.get(selectState.getType());
        }
        return currentState;
    }

    public Object doPrevious()
    {
        currentState = selectState;
        return currentState;
    }

    public void doCancel()
    {

    }

    public Object doRestart()
    {
        currentState = selectState;
        return currentState;
    }

    public void validate(ValidationContext context)
    {
        if(projectManager.getProject(projectId) == null)
        {
            context.addActionError("Unknown project [" + projectId + "]");
        }
    }

    public class SelectType
    {
        private String type;

        private List<String> options;

        public SelectType(Collection<String> options)
        {
            this.options = new ArrayList<String>(options);
        }

        public String getType()
        {
            return type;
        }

        public void setType(String type)
        {
            this.type = type;
        }

        public List<String> getTypeOptions()
        {
            return options;
        }
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
