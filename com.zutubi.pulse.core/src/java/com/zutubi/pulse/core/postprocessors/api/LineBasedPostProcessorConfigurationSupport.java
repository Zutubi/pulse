package com.zutubi.pulse.core.postprocessors.api;

import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.validation.Validateable;
import com.zutubi.validation.ValidationContext;

/**
 * A support base class for post processors that process text files
 * line-by-line.  Supports capturing of leading and trailing context lines
 * and optional joining of overlapping features.
 */
@SymbolicName("zutubi.lineBasedPostProcessorConfigSupport")
public abstract class LineBasedPostProcessorConfigurationSupport extends TextFilePostProcessorConfigurationSupport implements Validateable
{
    /** @see #setLeadingContext(int) */
    private int leadingContext = 0;
    /** @see #setTrailingContext(int) */
    private int trailingContext = 0;
    /** @see #setJoinOverlapping(boolean) */
    private boolean joinOverlapping = true;

    protected LineBasedPostProcessorConfigurationSupport(Class<? extends LineBasedPostProcessorSupport> postProcessorType)
    {
        super(postProcessorType);
    }

    /**
     * @see #setLeadingContext(int)
     * @return the number of lines of leading context to capture
     */
    public int getLeadingContext()
    {
        return leadingContext;
    }

    /**
     * Sets the number of lines of leading context to capture with any
     * discovered feature (zero by default).
     *
     * @param leadingContext the number of lines to capture (may be zero)
     */
    public void setLeadingContext(int leadingContext)
    {
        this.leadingContext = leadingContext;
    }

    /**
     * @see #setTrailingContext(int)
     * @return the number of lines of trailing context to capture
     */
    public int getTrailingContext()
    {
        return trailingContext;
    }

    /**
     * Sets the number of lines of trailing context to capture with any
     * discovered feature (zero by default).
     *
     * @param trailingContext the number of lines to capture (may be zero)
     */
    public void setTrailingContext(int trailingContext)
    {
        this.trailingContext = trailingContext;
    }

    /**
     * @see #setJoinOverlapping(boolean)
     * @return true if overlapping features will be joined
     */
    public boolean isJoinOverlapping()
    {
        return joinOverlapping;
    }

    /**
     * If set to true, overlapping features (as determined by the first and
     * last lines captured - context included) will be joined into a single
     * feature.
     *
     * @param joinOverlapping true to join overlapping features
     */
    public void setJoinOverlapping(boolean joinOverlapping)
    {
        this.joinOverlapping = joinOverlapping;
    }

    public void validate(ValidationContext context)
    {
        if (leadingContext < 0)
        {
            context.addFieldError("leadingContext", "Leading context count must be non-negative (got " + leadingContext + ")");
        }

        if (trailingContext < 0)
        {
            context.addFieldError("trailingContext", "Trailing context count must be non-negative (got " + trailingContext + ")");
        }
    }
}