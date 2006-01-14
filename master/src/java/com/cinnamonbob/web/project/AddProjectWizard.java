package com.cinnamonbob.web.project;

import com.cinnamonbob.model.*;
import com.cinnamonbob.web.wizard.BaseWizard;
import com.cinnamonbob.web.wizard.BaseWizardState;
import com.cinnamonbob.web.wizard.Wizard;
import com.cinnamonbob.web.wizard.WizardCompleteState;

import java.util.Map;
import java.util.TreeMap;

/**
 */
public class AddProjectWizard extends BaseWizard
{
    protected ProjectDetails projectDetails;
    private CvsDetails cvsDetails;
    private SvnDetails svnDetails;
    private P4Details p4Details;

    private CustomDetails customDetails;
    private AntDetails antDetails;
    private WizardCompleteState completeState;

    private ProjectManager projectManager;

    private long projectId;

    public AddProjectWizard()
    {
        projectDetails = new ProjectDetails(this, "project");
        cvsDetails = new CvsDetails(this, "cvs");
        svnDetails = new SvnDetails(this, "svn");
        p4Details = new P4Details(this, "p4");

        antDetails = new AntDetails(this, "ant");
        customDetails = new CustomDetails(this, "custom");
        completeState = new WizardCompleteState(this, "success");

        initialState = projectDetails;

        addState(projectDetails);
        addState(cvsDetails);
        addState(svnDetails);
        addState(p4Details);
        addState(antDetails);
        addState(customDetails);
        addState(completeState);
    }

    public void process()
    {
        super.process();

        Project project = new Project();
        project.setName(projectDetails.getName());
        project.setDescription(projectDetails.getDescription());

        Scm scm = null;
        String scmType = projectDetails.getScm();
        if ("cvs".equals(scmType))
        {
            scm = cvsDetails.getCvs();
        }
        else if ("svn".equals(scmType))
        {
            scm = svnDetails.getSvn();
        }
        else if ("p4".equals(scmType))
        {
            scm = p4Details.getP4();
        }
        project.setScm(scm);

        BobFileDetails details = null;
        String projectType = projectDetails.getType();
        if ("ant".equals(projectType))
        {
            details = antDetails.getDetails();
        }
        else if ("custom".equals(projectType))
        {
            details = customDetails.getDetails();
        }
        project.setBobFileDetails(details);

        projectManager.save(project);
        projectId = project.getId();
    }


    public void setProjectManager(ProjectManager projectManager)
    {
        this.projectManager = projectManager;
    }

    public long getProjectId()
    {
        return projectId;
    }

    private class ProjectDetails extends BaseWizardState
    {
        private Map<String, String> scms = null;
        private Map<String, String> types = null;

        private String scm;
        private String type;

        private String name;
        private String description;

        public ProjectDetails(Wizard wizard, String name)
        {
            super(wizard, name);
        }

        public Map<String, String> getScms()
        {
            if (scms == null)
            {
                scms = new TreeMap<String, String>();
                scms.put("cvs", "cvs");
                scms.put("p4", "perforce");
                scms.put("svn", "subversion");
            }
            return scms;
        }

        public Map<String, String> getTypes()
        {
            if (types == null)
            {
                types = new TreeMap<String, String>();
                types.put("ant", "jakarta ant project");
                types.put("custom", "custom project");
            }
            return types;
        }

        public void validate()
        {
            if (!types.containsKey(type))
            {
                addActionError("An invalid type has been requested. Please select one of the options " +
                        "from the drop down list provided.");
            }

            if (!scms.containsKey(scm))
            {
                addActionError("An invalid scm has been requested. Please select one of the options " +
                        "from the drop down list provided.");
            }
        }

        public String getNextState()
        {
            return scm;
        }

        public String getType()
        {
            return type;
        }

        public void setType(String type)
        {
            this.type = type;
        }

        public String getScm()
        {
            return scm;
        }

        public void setScm(String scm)
        {
            this.scm = scm;
        }

        public String getDescription()
        {
            return description;
        }

        public void setDescription(String description)
        {
            this.description = description;
        }

        public String getName()
        {
            return name;
        }

        public void setName(String name)
        {
            this.name = name;
        }
    }

    private class CvsDetails extends BaseWizardState
    {
        private Cvs cvs = new Cvs();

        public CvsDetails(Wizard wizard, String name)
        {
            super(wizard, name);
        }

        public Cvs getCvs()
        {
            return cvs;
        }

        public String getNextState()
        {
            return ((AddProjectWizard) getWizard()).projectDetails.getType();
        }
    }

    private class SvnDetails extends BaseWizardState
    {
        private Svn svn = new Svn();

        public SvnDetails(Wizard wizard, String name)
        {
            super(wizard, name);
        }

        public Svn getSvn()
        {
            return svn;
        }

        public String getNextState()
        {
            return ((AddProjectWizard) getWizard()).projectDetails.getType();
        }
    }

    private class P4Details extends BaseWizardState
    {
        private P4 p4 = new P4();

        public P4Details(Wizard wizard, String name)
        {
            super(wizard, name);
        }

        public P4 getP4()
        {
            return p4;
        }

        public String getNextState()
        {
            return ((AddProjectWizard) getWizard()).projectDetails.getType();
        }
    }

    private class AntDetails extends BaseWizardState
    {
        private AntBobFileDetails details = new AntBobFileDetails("build.xml", "", null);

        public AntDetails(Wizard wizard, String name)
        {
            super(wizard, name);
        }

        public String getNextState()
        {
            return ((AddProjectWizard) getWizard()).completeState.getWizardStateName();
        }

        public BobFileDetails getDetails()
        {
            return details;
        }
    }

    private class CustomDetails extends BaseWizardState
    {
        private BobFileDetails details = new CustomBobFileDetails("bob.xml");

        public CustomDetails(Wizard wizard, String name)
        {
            super(wizard, name);
        }

        public String getNextState()
        {
            return ((AddProjectWizard) getWizard()).completeState.getWizardStateName();
        }

        public BobFileDetails getDetails()
        {
            return details;
        }
    }
}
