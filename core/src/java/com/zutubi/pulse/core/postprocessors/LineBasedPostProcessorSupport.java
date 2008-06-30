package com.zutubi.pulse.core.postprocessors;

import com.zutubi.pulse.core.model.Feature;
import com.zutubi.pulse.core.model.PlainFeature;
import com.zutubi.util.CircularBuffer;
import com.zutubi.validation.Validateable;
import com.zutubi.validation.ValidationContext;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;

/**
 * A post processor that does line-by-line processing with option content capturing.
 */
public abstract class LineBasedPostProcessorSupport extends TextFilePostProcessorSupport implements Validateable
{
    /**
     * Number of lines of leading context to capture with the summary.
     */
    private int leadingContext = 0;
    /**
     * Number of lines of trailing context to capture with the summary.
     */
    private int trailingContext = 0;
    /**
     * If true, overlapping features (as determined by the first and last
     * lines) will be joined into a single feature.
     */
    private boolean joinOverlapping = true;


    public void process(BufferedReader reader, PostProcessorContext ppContext) throws IOException
    {
        LineBasedPostProcessorContext lineContext = new LineBasedPostProcessorContext(ppContext);
        if (leadingContext == 0 && trailingContext == 0)
        {
            // Optimise this common case
            simpleProcess(lineContext, reader);
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
                processLine(lineContext, line, lineNumber, leadingBuffer, trailingBuffer, 1);
                leadingBuffer.append(line);
                trailingBuffer.append(next);
            }

            // Finally, exhaust the trailing context
            for (int i = 0; i < trailingBuffer.getCount(); i++)
            {
                lineNumber++;
                line = trailingBuffer.getElement(i);
                processLine(lineContext, line, lineNumber, leadingBuffer, trailingBuffer, i + 1);
                leadingBuffer.append(line);
            }
        }
    }

    private void simpleProcess(LineBasedPostProcessorContext ppContext, BufferedReader reader) throws IOException
    {
        String line;
        long lineNumber = 0;

        while ((line = reader.readLine()) != null)
        {
            lineNumber++;
            processLine(ppContext, line, lineNumber);
        }
    }

    private void processLine(LineBasedPostProcessorContext ppContext, String line, long lineNumber)
    {
        processLine(ppContext, line, lineNumber, null, null, 0);
    }

    private void processLine(LineBasedPostProcessorContext ppContext, String line, long lineNumber, CircularBuffer<String> leadingContext, CircularBuffer<String> trailingContext, int trailingIndex)
    {
        for (Feature f: findFeatures(line))
        {
                if (leadingContext == null)
                {
                    addFeature(ppContext, new PlainFeature(f.getLevel(), f.getSummary(), lineNumber));
                }
                else
                {
                    // Add the context lines to the summary
                    StringBuilder summaryBuilder = new StringBuilder();
                    append(summaryBuilder, leadingContext, 0, true);
                    summaryBuilder.append(f.getSummary());
                    append(summaryBuilder, trailingContext, trailingIndex, false);
                    addFeature(ppContext, new PlainFeature(f.getLevel(), summaryBuilder.toString(), lineNumber - leadingContext.getCount(), lineNumber + trailingContext.getCount() - trailingIndex, lineNumber));
            }
        }
    }

    private void addFeature(LineBasedPostProcessorContext ppContext, PlainFeature feature)
    {
        if(canJoin(ppContext, feature))
        {
            // Join with previous
            PlainFeature previous = (PlainFeature) ppContext.getPreviousFeature();
            long overlappingLines = previous.getLastLine() - feature.getFirstLine() + 1;
            String remainingSummary = getRemainingSummary(feature.getSummary(), overlappingLines);
            previous.appendToSummary(remainingSummary);
            previous.setLastLine(feature.getLastLine());
        }
        else
        {
            ppContext.addFeature(feature);
        }
    }

    private boolean canJoin(LineBasedPostProcessorContext ppContext, PlainFeature feature)
    {
        if(joinOverlapping && ppContext.getPreviousFeature() != null)
        {
            PlainFeature previous = (PlainFeature) ppContext.getPreviousFeature();
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

    public int getLeadingContext()
    {
        return leadingContext;
    }

    public void setLeadingContext(int leadingContext)
    {
        this.leadingContext = leadingContext;
    }

    public int getTrailingContext()
    {
        return trailingContext;
    }

    public void setTrailingContext(int trailingContext)
    {
        this.trailingContext = trailingContext;
    }

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

    protected abstract List<Feature> findFeatures(String line);
}
