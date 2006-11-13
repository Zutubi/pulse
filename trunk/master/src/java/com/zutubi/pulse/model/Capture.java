package com.zutubi.pulse.model;

import com.zutubi.pulse.core.model.Entity;

import java.util.LinkedList;
import java.util.List;

/**
 * A capture defines an artifact to be captured and post-processed in some well-defined
 * way.  It is used for built-in projects only.
 */
public abstract class Capture extends Entity implements NamedEntity
{
    /**
     * The capture's name, which is also the name of the artifact.
     */
    private String name;
    /**
     * A list of names of post-processors to apply to this artifact.  These
     * post-processors are pre-canned using templates in:
     *
     *   $TEMPLATE_ROOT/pulse-file/post-processors
     */
    private List<String> processors;

    protected Capture(String name)
    {
        this.name = name;
        this.processors = new LinkedList<String>();
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public List<String> getProcessors()
    {
        return processors;
    }

    public void setProcessors(List<String> processors)
    {
        this.processors = processors;
    }

    public void addProcessor(String name)
    {
        processors.add(name);
    }

    public abstract String getType();

    public abstract void clearFields();
}
