package com.zutubi.pulse.web.project;

import com.opensymphony.util.TextUtils;
import com.opensymphony.xwork.Validateable;
import com.zutubi.pulse.core.*;
import com.zutubi.pulse.model.*;
import com.zutubi.pulse.model.persistence.BuildSpecificationNodeDao;
import com.zutubi.pulse.scheduling.EventTrigger;
import com.zutubi.pulse.scheduling.SCMChangeEventFilter;
import com.zutubi.pulse.scheduling.Scheduler;
import com.zutubi.pulse.scheduling.SchedulingException;
import com.zutubi.pulse.scheduling.tasks.BuildProjectTask;
import com.zutubi.pulse.scm.SCMChangeEvent;
import com.zutubi.pulse.security.AcegiUtils;
import com.zutubi.pulse.util.StringUtils;
import com.zutubi.pulse.util.logging.Logger;
import com.zutubi.pulse.web.wizard.BaseWizard;
import com.zutubi.pulse.web.wizard.BaseWizardState;
import com.zutubi.pulse.web.wizard.Wizard;
import com.zutubi.pulse.web.wizard.WizardCompleteState;
import org.acegisecurity.AccessDeniedException;

import java.io.ByteArrayInputStream;
import java.util.Map;
import java.util.TreeMap;

/**
 */
public class AddProjectWizard extends BaseWizard
{
    private static final Logger LOG = Logger.getLogger(AddProjectWizard.class);

    protected ProjectDetails projectDetails;
    private CvsDetails cvsDetails;
    private SvnDetails svnDetails;
    private P4Details p4Details;

    private CustomDetails customDetails;
    private VersionedDetails versionedDetails;
    private AntDetails antDetails;
    private MakeDetails makeDetails;
    private MavenDetails mavenDetails;
    private Maven2Details maven2Details;

    private WizardCompleteState completeState;

    private ProjectManager projectManager;
    private BuildSpecificationNodeDao buildSpecificationNodeDao;
    private Scheduler scheduler;

    private long projectId;

    public AddProjectWizard()
    {
        // step 1.
        projectDetails = new ProjectDetails(this, "project");

        // step 2, scms.
        cvsDetails = new CvsDetails(this, "cvs");
        svnDetails = new SvnDetails(this, "svn");
        p4Details = new P4Details(this, "p4");

        // step 3, project.
        antDetails = new AntDetails(this, "ant");
        makeDetails = new MakeDetails(this, "make");
        mavenDetails = new MavenDetails(this, "maven");
        maven2Details = new Maven2Details(this, "maven2");
        customDetails = new CustomDetails(this, "custom");
        versionedDetails = new VersionedDetails(this, "versioned");

        // finished.
        completeState = new WizardCompleteState(this, "success");

        addInitialState(projectDetails.getStateName(), projectDetails);
        addState(cvsDetails);
        addState(svnDetails);
        addState(p4Details);
        addState(antDetails);
        addState(makeDetails);
        addState(mavenDetails);
        addState(maven2Details);
        addState(customDetails);
        addState(versionedDetails);
        addFinalState(completeState.getStateName(), completeState);
    }

    public void initialise()
    {
        super.initialise();
        if(!AcegiUtils.userHasRole(GrantedAuthority.ADMINISTRATOR))
        {
            throw new AccessDeniedException("Access denied");
        }
    }

    public void process()
    {
        super.process();

        // initialise new project.
        Project project = new Project(projectDetails.getName(), projectDetails.getDescription());
        project.setUrl(projectDetails.getUrl());

        // setup scm details.
        Scm scm = getScm();
        scm.setMonitor(true);
        project.setScm(scm);

        // configure pulse file.
        PulseFileDetails details = null;
        String projectType = projectDetails.getType();
        if ("ant".equals(projectType))
        {
            details = antDetails.getDetails();
        }
        else if ("custom".equals(projectType))
        {
            details = customDetails.getDetails();
        }
        else if ("make".equals(projectType))
        {
            details = makeDetails.getDetails();
        }
        else if ("maven".equals(projectType))
        {
            details = mavenDetails.getDetails();
        }
        else if ("maven2".equals(projectType))
        {
            details = maven2Details.getDetails();
        }
        else if ("versioned".equals(projectType))
        {
            details = versionedDetails.getDetails();
        }
        project.setPulseFileDetails(details);

        BuildSpecification buildSpec = new BuildSpecification("default");
        project.addBuildSpecification(buildSpec);

        projectManager.save(project);
        projectId = project.getId();

        // TODO: All of this should be done within a manager.

        // create a simple build specification that executes the default recipe.
        BuildSpecificationNode parent = buildSpecificationNodeDao.findById(buildSpec.getRoot().getId());
        BuildStage stage = new BuildStage("default", new AnyCapableBuildHostRequirements(), null);
        BuildSpecificationNode node = new BuildSpecificationNode(stage);
        parent.addChild(node);
        buildSpecificationNodeDao.save(parent);

        // schedule the event trigger - unique to this project.
        EventTrigger trigger = new EventTrigger(SCMChangeEvent.class, project.getName() + " scm trigger", "scm event triggers", SCMChangeEventFilter.class);
        trigger.setProject(project.getId());
        trigger.setTaskClass(BuildProjectTask.class);
        trigger.getDataMap().put(BuildProjectTask.PARAM_SPEC, "default");
        trigger.getDataMap().put(BuildProjectTask.PARAM_PROJECT, project.getId());

        try
        {
            scheduler.schedule(trigger);
        }
        catch (SchedulingException e)
        {
            //CIB-169: need to display this error to the user...
            addActionError(e.getMessage());
            LOG.severe(e.getMessage(), e);
        }
    }

    public Scm getScm()
    {
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
        return scm;
    }

    public void setScheduler(Scheduler scheduler)
    {
        this.scheduler = scheduler;
    }

    public void setProjectManager(ProjectManager projectManager)
    {
        this.projectManager = projectManager;
    }

    public void setBuildSpecificationNodeDao(BuildSpecificationNodeDao buildSpecificationNodeDao)
    {
        this.buildSpecificationNodeDao = buildSpecificationNodeDao;
    }

    public long getProjectId()
    {
        return projectId;
    }

    private class ProjectDetails extends BaseWizardState implements Validateable
    {
        private Map<String, String> scms = null;
        private Map<String, String> types = null;

        private String scm;
        private String type;

        private String name;
        private String description;
        private String url;

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
                types.put("ant", "ant project");
                types.put("custom", "custom project");
                types.put("make", "make project");
                types.put("maven", "maven project");
                types.put("maven2", "maven 2 project");
                types.put("versioned", "versioned project");
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

            // is the name being used by another project??
            if (projectManager.getProject(name) != null)
            {
                addFieldError("name", "The name " + name + " is already being used by another project. " +
                        "Please select a different name.");
            }
        }

        public String getNextStateName()
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

        public String getUrl()
        {
            return url;
        }

        public void setUrl(String url)
        {
            this.url = url;
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

    private class CvsDetails extends BaseWizardState implements Validateable
    {
        private Cvs cvs = new Cvs();

        private String minutes;
        private String seconds;

        public CvsDetails(Wizard wizard, String name)
        {
            super(wizard, name);
        }

        public Cvs getCvs()
        {
            return cvs;
        }

        public String getMinutes()
        {
            return minutes;
        }

        public void setMinutes(String minutes)
        {
            this.minutes = minutes;
        }

        public String getSeconds()
        {
            return seconds;
        }

        public void setSeconds(String seconds)
        {
            this.seconds = seconds;
        }

        public String getNextStateName()
        {
            // record the specified minutes / seconds.
            cvs.setQuietPeriod(minutes, seconds);

            return ((AddProjectWizard) getWizard()).projectDetails.getType();
        }

        public void validate()
        {
            try
            {
                // check the minutes field.
                if (TextUtils.stringSet(minutes))
                {
                    if (Integer.parseInt(minutes) < 0)
                    {
                        addFieldError("quiet", getTextProvider().getText("unit.invalid.negative"));
                        return;
                    }
                }

                // check the seconds field.
                if (TextUtils.stringSet(seconds))
                {
                    if (Integer.parseInt(seconds) < 0)
                    {
                        addFieldError("quiet", getTextProvider().getText("unit.invalid.negative"));
                    }
                }
            }
            catch (NumberFormatException nfe)
            {
                addFieldError("quiet", getTextProvider().getText("unit.invalid.nan"));
            }
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

        public String getNextStateName()
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

            String value = System.getenv("P4PORT");
            if(value == null)
            {
                p4.setPort("perforce:1666");
            }
            else
            {
                p4.setPort(value);
            }

            value = System.getenv("P4USER");
            if(value != null)
            {
                p4.setUser(value);
            }

            value = System.getenv("P4CLIENT");
            if(value != null)
            {
                p4.setClient(value);
            }
        }

        public P4 getP4()
        {
            return p4;
        }

        public String getNextStateName()
        {
            return ((AddProjectWizard) getWizard()).projectDetails.getType();
        }
    }

    private class AntDetails extends BaseWizardState
    {
        private AntPulseFileDetails details = new AntPulseFileDetails("build.xml", null, null, null, null);

        public AntDetails(Wizard wizard, String name)
        {
            super(wizard, name);
        }

        public String getNextStateName()
        {
            return ((AddProjectWizard) getWizard()).completeState.getStateName();
        }

        public AntPulseFileDetails getDetails()
        {
            return details;
        }

        public void execute()
        {
            if (!TextUtils.stringSet(details.getBuildFile()))
            {
                details.setBuildFile(null);
            }

            if (!TextUtils.stringSet(details.getTargets()))
            {
                details.setTargets(null);
            }

            if (!TextUtils.stringSet(details.getArguments()))
            {
                details.setArguments(null);
            }

            if (!TextUtils.stringSet(details.getWorkingDir()))
            {
                details.setWorkingDir(null);
            }
        }
    }

    private class MakeDetails extends BaseWizardState
    {
        private MakePulseFileDetails details = new MakePulseFileDetails("Makefile", null, null, null, null);

        public MakeDetails(Wizard wizard, String name)
        {
            super(wizard, name);
        }

        public String getNextStateName()
        {
            return ((AddProjectWizard) getWizard()).completeState.getStateName();
        }

        public MakePulseFileDetails getDetails()
        {
            return details;
        }

        public void execute()
        {
            if (!TextUtils.stringSet(details.getMakefile()))
            {
                details.setMakefile(null);
            }

            if (!TextUtils.stringSet(details.getTargets()))
            {
                details.setTargets(null);
            }

            if (!TextUtils.stringSet(details.getArguments()))
            {
                details.setArguments(null);
            }

            if (!TextUtils.stringSet(details.getWorkingDir()))
            {
                details.setWorkingDir(null);
            }
        }
    }

    private class MavenDetails extends BaseWizardState
    {
        private MavenPulseFileDetails details = new MavenPulseFileDetails();

        public MavenDetails(Wizard wizard, String name)
        {
            super(wizard, name);
        }

        public String getNextStateName()
        {
            return ((AddProjectWizard) getWizard()).completeState.getStateName();
        }

        public PulseFileDetails getDetails()
        {
            return details;
        }

        public void execute()
        {
            if (!TextUtils.stringSet(details.getTargets()))
            {
                details.setTargets(null);
            }

            if (!TextUtils.stringSet(details.getWorkingDir()))
            {
                details.setWorkingDir(null);
            }

            DirectoryCapture testCapture = new DirectoryCapture("test reports");
            testCapture.setIncludes("**/target/test-reports/TEST-*.xml");
            testCapture.addProcessor("junit");
            details.addCapture(testCapture);
        }
    }

    private class Maven2Details extends BaseWizardState
    {
        private Maven2PulseFileDetails details = new Maven2PulseFileDetails();

        public Maven2Details(Wizard wizard, String name)
        {
            super(wizard, name);
        }

        public String getNextStateName()
        {
            return ((AddProjectWizard) getWizard()).completeState.getStateName();
        }

        public PulseFileDetails getDetails()
        {
            return details;
        }

        public void execute()
        {
            if (!TextUtils.stringSet(details.getGoals()))
            {
                details.setGoals(null);
            }

            if (!TextUtils.stringSet(details.getWorkingDir()))
            {
                details.setWorkingDir(null);
            }

            DirectoryCapture testCapture = new DirectoryCapture("test reports");
            testCapture.setIncludes("**/target/surefire-reports/TEST-*.xml");
            testCapture.addProcessor("junit");
            details.addCapture(testCapture);
        }
    }

    private class CustomDetails extends BaseWizardState implements Validateable
    {
        private CustomPulseFileDetails details = new CustomPulseFileDetails("<?xml version=\"1.0\"?>\n<project>\n</project>\n");
        private ResourceRepository resourceRepository;

        public CustomDetails(Wizard wizard, String name)
        {
            super(wizard, name);
        }

        public String getNextStateName()
        {
            return ((AddProjectWizard) getWizard()).completeState.getStateName();
        }

        public PulseFileDetails getDetails()
        {
            return details;
        }


        public void validate()
        {
            if(!TextUtils.stringSet(details.getPulseFile()))
            {
                addFieldError("details.pulseFile", "pulse file is required");
                return;
            }

            try
            {
                PulseFileLoader loader = new PulseFileLoader(new ObjectFactory());
                loader.load(new ByteArrayInputStream(details.getPulseFile().getBytes()), new PulseFile(), new Scope(), resourceRepository, new CustomProjectValidationPredicate());
            }
            catch(ParseException pe)
            {
                addActionError(pe.getMessage());
                if(pe.getLine() > 0)
                {
                    String line = StringUtils.getLine(details.getPulseFile(), pe.getLine());
                    if(line != null)
                    {
                        addActionError("First line of offending element: " + line);
                    }
                }
            }
            catch(Exception e)
            {
                addActionError(e.getMessage());
            }
        }

        public void setResourceRepository(ResourceRepository resourceRepository)
        {
            this.resourceRepository = resourceRepository;
        }
    }

    private class VersionedDetails extends BaseWizardState
    {
        private PulseFileDetails details = new VersionedPulseFileDetails("pulse.xml");

        public VersionedDetails(Wizard wizard, String name)
        {
            super(wizard, name);
        }

        public String getNextStateName()
        {
            return ((AddProjectWizard) getWizard()).completeState.getStateName();
        }

        public PulseFileDetails getDetails()
        {
            return details;
        }
    }
}
