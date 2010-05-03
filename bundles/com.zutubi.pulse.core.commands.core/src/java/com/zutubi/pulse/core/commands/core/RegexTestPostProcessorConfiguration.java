package com.zutubi.pulse.core.commands.core;

import com.zutubi.pulse.core.engine.api.Addable;
import com.zutubi.pulse.core.engine.api.AttributeBinding;
import com.zutubi.pulse.core.engine.api.Content;
import com.zutubi.pulse.core.postprocessors.api.TestReportPostProcessorConfigurationSupport;
import com.zutubi.pulse.core.postprocessors.api.TestStatus;
import com.zutubi.tove.annotations.Form;
import com.zutubi.tove.annotations.StringList;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.annotations.Transient;
import com.zutubi.validation.annotations.Required;
import com.zutubi.validation.annotations.ValidRegex;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Configuration for instances of {@link RegexTestPostProcessor}.
 */
@SymbolicName("zutubi.regexTestPostProcessorConfig")
@Form(fieldOrder = {"name", "regex", "nameGroup", "suiteGroup", "statusGroup", "detailsGroup", "durationGroup", "passStatus", "failureStatus", "errorStatus", "skippedStatus", "autoFail", "trim", "suite", "failOnFailure", "resolveConflicts"})
public class RegexTestPostProcessorConfiguration extends TestReportPostProcessorConfigurationSupport
{
    private static final int UNDEFINED_GROUP = -1;

    @Required @ValidRegex @Content
    private String regex;
    @Required
    private int statusGroup;
    @Required
    private int nameGroup;
    private int detailsGroup = UNDEFINED_GROUP;
    private int suiteGroup = UNDEFINED_GROUP;
    private int durationGroup = UNDEFINED_GROUP;

    private boolean autoFail = false;
    private boolean trim = true;

    @Transient
    private Map<String, TestStatus> statusMap = new HashMap<String, TestStatus>();

    public RegexTestPostProcessorConfiguration()
    {
        this(RegexTestPostProcessor.class);
    }

    public RegexTestPostProcessorConfiguration(String name)
    {
        this();
        setName(name);
    }

    public RegexTestPostProcessorConfiguration(Class<? extends RegexTestPostProcessor> postProcessorType)
    {
        super(postProcessorType);

        // provide some defaults.
        this.statusMap.put("PASS", TestStatus.PASS);
        this.statusMap.put("FAILURE", TestStatus.FAILURE);
        this.statusMap.put("ERROR", TestStatus.ERROR);
        this.statusMap.put("SKIPPED", TestStatus.SKIPPED);
    }

    public RegexTestPostProcessorConfiguration(Class<? extends RegexTestPostProcessor> postProcessorType, String name)
    {
        this(postProcessorType);
        setName(name);
    }

    Map<String, TestStatus> getStatusMap()
    {
        return statusMap;
    }

    public String getRegex()
    {
        return regex;
    }

    public void setRegex(String regex)
    {
        this.regex = regex;
    }

    public void setStatusGroup(int i)
    {
        this.statusGroup = i;
    }

    public int getStatusGroup()
    {
        return statusGroup;
    }

    public void setNameGroup(int i)
    {
        this.nameGroup = i;
    }

    public int getNameGroup()
    {
        return nameGroup;
    }

    public int getDetailsGroup()
    {
        return detailsGroup;
    }

    public void setDetailsGroup(int detailsGroup)
    {
        this.detailsGroup = detailsGroup;
    }

    public boolean hasDetailsGroup()
    {
        return getDetailsGroup() != UNDEFINED_GROUP;
    }

    public int getSuiteGroup()
    {
        return suiteGroup;
    }

    public void setSuiteGroup(int suiteGroup)
    {
        this.suiteGroup = suiteGroup;
    }

    public boolean hasSuiteGroup()
    {
        return getSuiteGroup() != UNDEFINED_GROUP;
    }

    public int getDurationGroup()
    {
        return durationGroup;
    }

    public void setDurationGroup(int durationGroup)
    {
        this.durationGroup = durationGroup;
    }

    public boolean hasDurationGroup()
    {
        return getDurationGroup() != UNDEFINED_GROUP;
    }

    @StringList @Addable(value = "pass-status", attribute = "") @AttributeBinding(split = false)
    public List<String> getPassStatus()
    {
        return getStatuses(TestStatus.PASS);
    }

    public void setPassStatus(List<String> strings)
    {
        setStatuses(strings, TestStatus.PASS);
    }

    @StringList @Addable(value = "failure-status", attribute = "") @AttributeBinding(split = false)
    public List<String> getFailureStatus()
    {
        return getStatuses(TestStatus.FAILURE);
    }

    public void setFailureStatus(List<String> strings)
    {
        setStatuses(strings, TestStatus.FAILURE);
    }

    @StringList @Addable(value = "error-status", attribute = "") @AttributeBinding(split = false)
    public List<String> getErrorStatus()
    {
        return getStatuses(TestStatus.ERROR);
    }

    public void setErrorStatus(List<String> strings)
    {
        setStatuses(strings, TestStatus.ERROR);
    }

    @StringList @Addable(value = "skipped-status", attribute = "") @AttributeBinding(split = false)
    public List<String> getSkippedStatus()
    {
        return getStatuses(TestStatus.SKIPPED);
    }

    public void setSkippedStatus(List<String> strings)
    {
        setStatuses(strings, TestStatus.SKIPPED);
    }

    private List<String> getStatuses(TestStatus status)
    {
        List<String> strings = new LinkedList<String>();
        for (Map.Entry<String, TestStatus> entry: statusMap.entrySet())
        {
            if (entry.getValue().equals(status))
            {
                strings.add(entry.getKey());
            }
        }

        return strings;
    }

    private void setStatuses(List<String> strings, TestStatus status)
    {
        for (String s: strings)
        {
            this.statusMap.put(s, status);
        }
    }

    public boolean isAutoFail()
    {
        return autoFail;
    }

    public void setAutoFail(boolean autoFail)
    {
        this.autoFail = autoFail;
    }

    public boolean isTrim()
    {
        return trim;
    }

    public void setTrim(boolean trim)
    {
        this.trim = trim;
    }
}
