package com.zutubi.pulse.core.commands.core;

import com.zutubi.pulse.core.postprocessors.api.XMLTestReportPostProcessorConfigurationSupport;
import com.zutubi.tove.annotations.Form;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.annotations.Wizard;
import com.zutubi.validation.annotations.Required;

/**
 * Configuration for instances of {@link com.zutubi.pulse.core.commands.core.JUnitReportPostProcessor}.
 */
@SymbolicName("zutubi.junitReportPostProcessorConfig")
@Form(fieldOrder = {"name", "failOnFailure", "suite", "resolveConflicts", "expectedFailureFile", "suiteElement", "caseElement", "errorElement", "failureElement", "skippedElement", "classAttribute", "messageAttribute", "nameAttribute", "packageAttribute", "timeAttribute"})
public class JUnitReportPostProcessorConfiguration extends XMLTestReportPostProcessorConfigurationSupport
{
    private static final String ELEMENT_SUITE   = "testsuite";
    private static final String ELEMENT_CASE    = "testcase";
    private static final String ELEMENT_ERROR   = "error";
    private static final String ELEMENT_FAILURE = "failure";
    private static final String ELEMENT_SKIPPED = "skipped";

    private static final String ATTRIBUTE_CLASS   = "classname";
    private static final String ATTRIBUTE_MESSAGE = "message";
    private static final String ATTRIBUTE_NAME    = "name";
    private static final String ATTRIBUTE_PACKAGE = "package";
    private static final String ATTRIBUTE_TIME    = "time";

    @Wizard.Ignore @Required
    private String suiteElement     = ELEMENT_SUITE;
    @Wizard.Ignore @Required
    private String caseElement      = ELEMENT_CASE;
    @Wizard.Ignore @Required
    private String errorElement     = ELEMENT_ERROR;
    @Wizard.Ignore @Required
    private String failureElement   = ELEMENT_FAILURE;
    @Wizard.Ignore @Required
    private String skippedElement   = ELEMENT_SKIPPED;
    @Wizard.Ignore @Required
    private String classAttribute   = ATTRIBUTE_CLASS;
    @Wizard.Ignore @Required
    private String messageAttribute = ATTRIBUTE_MESSAGE;
    @Wizard.Ignore @Required
    private String nameAttribute    = ATTRIBUTE_NAME;
    @Wizard.Ignore @Required
    private String packageAttribute = ATTRIBUTE_PACKAGE;
    @Wizard.Ignore @Required
    private String timeAttribute    = ATTRIBUTE_TIME;

    public JUnitReportPostProcessorConfiguration()
    {
        this(JUnitReportPostProcessor.class);
    }

    public JUnitReportPostProcessorConfiguration(Class<? extends JUnitReportPostProcessor> postProcessorType)
    {
        this(postProcessorType, "JUnit");
    }

    public JUnitReportPostProcessorConfiguration(Class<? extends JUnitReportPostProcessor> postProcessorType, String reportType)
    {
        super(postProcessorType, reportType);
    }

    public String getSuiteElement()
    {
        return suiteElement;
    }

    public void setSuiteElement(String suiteElement)
    {
        this.suiteElement = suiteElement;
    }

    public String getCaseElement()
    {
        return caseElement;
    }

    public void setCaseElement(String caseElement)
    {
        this.caseElement = caseElement;
    }

    public String getErrorElement()
    {
        return errorElement;
    }

    public void setErrorElement(String errorElement)
    {
        this.errorElement = errorElement;
    }

    public String getFailureElement()
    {
        return failureElement;
    }

    public void setFailureElement(String failureElement)
    {
        this.failureElement = failureElement;
    }

    public String getSkippedElement()
    {
        return skippedElement;
    }

    public void setSkippedElement(String skippedElement)
    {
        this.skippedElement = skippedElement;
    }

    public String getClassAttribute()
    {
        return classAttribute;
    }

    public void setClassAttribute(String classAttribute)
    {
        this.classAttribute = classAttribute;
    }

    public String getMessageAttribute()
    {
        return messageAttribute;
    }

    public void setMessageAttribute(String messageAttribute)
    {
        this.messageAttribute = messageAttribute;
    }

    public String getNameAttribute()
    {
        return nameAttribute;
    }

    public void setNameAttribute(String nameAttribute)
    {
        this.nameAttribute = nameAttribute;
    }

    public String getPackageAttribute()
    {
        return packageAttribute;
    }

    public void setPackageAttribute(String packageAttribute)
    {
        this.packageAttribute = packageAttribute;
    }

    public String getTimeAttribute()
    {
        return timeAttribute;
    }

    public void setTimeAttribute(String timeAttribute)
    {
        this.timeAttribute = timeAttribute;
    }
}