package com.zutubi.pulse.core.postprocessors.api;

import com.zutubi.util.CircularBuffer;
import com.zutubi.validation.Validateable;
import com.zutubi.validation.ValidationContext;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.LinkedList;
import java.util.List;

/**
 * A support base class for post processors that process text files
 * line-by-line.  Supports capturing of leading and trailing context lines
 * and optional joining of overlapping features.
 */
public abstract class LineBasedPostProcessorSupport extends TextFilePostProcessorSupport implements Validateable
{
    /** @see #setLeadingContext(int) */
    private int leadingContext = 0;
    /** @see #setTrailingContext(int) */
    private int trailingContext = 0;
    /** @see #setJoinOverlapping(boolean) */
    private boolean joinOverlapping = true;

    protected final void process(BufferedReader reader, PostProcessorContext ppContext) throws IOException
    {
        List<Feature> allFeatures = new LinkedList<Feature>();
        if (leadingContext == 0 && trailingContext == 0)
        {
            // Optimise this common case
            simpleProcess(reader, allFeatures);
        }
        else
        {
            String line;
            long lineNumber = 0;
            CircularBuffer<String> trailingBuffer = new CircularBuffer<String>(trailingContext + 1);

            // First fill with the trailing context plus a line to be processed
            for (int i = 0; i <= trailingContext; i++)
            {
                line = reader.readLine();
                if (line == null)
                {
                    break;
                }

                trailingBuffer.append(line);
            }

            // Now read in the bulk of the lines, processing them as we go.
            CircularBuffer<String> leadingBuffer = new CircularBuffer<String>(leadingContext);
            String next;

            while ((next = reader.readLine()) != null)
            {
                lineNumber++;
                line = trailingBuffer.getElement(0);
                processLine(line, lineNumber, leadingBuffer, trailingBuffer, 1, allFeatures);
                leadingBuffer.append(line);
                trailingBuffer.append(next);
            }

            // Finally, exhaust the trailing context
            for (int i = 0; i < trailingBuffer.getCount(); i++)
            {
                lineNumber++;
                line = trailingBuffer.getElement(i);
                processLine(line, lineNumber, leadingBuffer, trailingBuffer, i + 1, allFeatures);
                leadingBuffer.append(line);
            }
        }

        for (Feature f: allFeatures)
        {
            ppContext.addFeature(f);
        }
    }

    private void simpleProcess(BufferedReader reader, List<Feature> allFeatures) throws IOException
    {
        String line;
        long lineNumber = 0;

        while ((line = reader.readLine()) != null)
        {
            lineNumber++;
            processLine(line, lineNumber, allFeatures);
        }
    }

    private void processLine(String line, long lineNumber, List<Feature> allFeatures)
    {
        processLine(line, lineNumber, null, null, 0, allFeatures);
    }

    private void processLine(String line, long lineNumber, CircularBuffer<String> leadingContext, CircularBuffer<String> trailingContext, int trailingIndex, List<Feature> allFeatures)
    {
        for (Feature f: findFeatures(line))
        {
            if (leadingContext == null)
            {
                addFeature(new Feature(f.getLevel(), f.getSummary(), lineNumber), allFeatures);
            }
            else
            {
                // Add the context lines to the summary
                StringBuilder summaryBuilder = new StringBuilder();
                append(summaryBuilder, leadingContext, 0, true);
                summaryBuilder.append(f.getSummary());
                append(summaryBuilder, trailingContext, trailingIndex, false);
                addFeature(new Feature(f.getLevel(), summaryBuilder.toString(), lineNumber, lineNumber - leadingContext.getCount(), lineNumber + trailingContext.getCount() - trailingIndex), allFeatures);
            }
        }
    }

    private void addFeature(Feature feature, List<Feature> allFeatures)
    {
        if (canJoin(feature, allFeatures))
        {
            // Join with previous
            Feature previous = allFeatures.remove(allFeatures.size() - 1);
            long overlappingLines = previous.getLastLine() - feature.getFirstLine() + 1;
            String remainingSummary = getRemainingSummary(feature.getSummary(), overlappingLines);
            allFeatures.add(new Feature(previous.getLevel(), previous.getSummary() + remainingSummary, previous.getLineNumber(), previous.getFirstLine(), feature.getLastLine()));
        }
        else
        {
            allFeatures.add(feature);
        }
    }

    private boolean canJoin(Feature feature, List<Feature> allFeatures)
    {
        if (joinOverlapping && allFeatures.size() > 0)
        {
            Feature previous = allFeatures.get(allFeatures.size() - 1);
            return previous.getLevel() == feature.getLevel() && previous.getLastLine() >= feature.getFirstLine();
        }

        return false;
    }

    private String getRemainingSummary(String summary, long overlappingLines)
    {
        StringBuilder result = new StringBuilder(summary.length());
        BufferedReader reader = new BufferedReader(new StringReader(summary));
        String line;
        int lineNumber = 1;

        try
        {
            while((line = reader.readLine()) != null)
            {
                if(lineNumber++ > overlappingLines)
                {
                    result.append('\n');
                    result.append(line);
                }
            }
        }
        catch (IOException e)
        {
            return summary;
        }

        return result.toString();
    }

    private void append(StringBuilder builder, CircularBuffer<String> context, int i, boolean leading)
    {
        for (; i < context.getCount(); i++)
        {
            if (!leading)
            {
                builder.append('\n');
            }

            builder.append(context.getElement(i));

            if (leading)
            {
                builder.append('\n');
            }
        }
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

    /**
     * Called once for each line to post-process.  This method should return
     * the features found in the line.  This implementation will then take
     * care of additional logic before adding features to the build.
     *
     * @param line the line to process
     * @return any features found in the line
     */
    protected abstract List<Feature> findFeatures(String line);
}
