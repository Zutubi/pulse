package com.zutubi.pulse.core;

import com.zutubi.pulse.core.model.CommandResult;
import com.zutubi.pulse.core.model.Feature;
import com.zutubi.pulse.core.model.PlainFeature;
import com.zutubi.pulse.core.model.StoredFileArtifact;
import com.zutubi.util.CircularBuffer;
import com.zutubi.util.IOUtils;
import com.zutubi.util.logging.Logger;
import com.zutubi.validation.Validateable;
import com.zutubi.validation.ValidationContext;

import java.io.*;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;


/**
 * A post processor that does line-by-line searching with regular expressions
 * to detect features.
 */
public class RegexPostProcessor implements PostProcessor, Validateable
{
    private static final Logger LOG = Logger.getLogger(RegexPostProcessor.class.getName());

    private String name;
    private List<RegexPattern> patterns;
    /**
     * If true, any errors detected during post-processing will trigger
     * command failure.
     */
    private boolean failOnError = true;
    /**
     * If true, any warnings detected during post-processing will trigger
     * command failure.
     */
    private boolean failOnWarning = false;
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


    public RegexPostProcessor()
    {
        patterns = new LinkedList<RegexPattern>();
    }

    public RegexPostProcessor(String name)
    {
        this.name = name;
        patterns = new LinkedList<RegexPattern>();
    }

    public void process(StoredFileArtifact artifact, CommandResult result, CommandContext context)
    {
        List<PlainFeature> features = new LinkedList<PlainFeature>();

        if (leadingContext == 0 && trailingContext == 0)
        {
            // Optimise this common case
            simpleProcess(context.getOutputDir(), artifact, result, features);
        }
        else
        {
            BufferedReader reader = null;
            try
            {
                File file = new File(context.getOutputDir(), artifact.getPath());
                reader = new BufferedReader(new FileReader(file));
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
                    processLine(features, result, line, lineNumber, leadingBuffer, trailingBuffer, 1);
                    leadingBuffer.append(line);
                    trailingBuffer.append(next);
                }

                // Finally, exhaust the trailing context
                for (int i = 0; i < trailingBuffer.getCount(); i++)
                {
                    lineNumber++;
                    line = trailingBuffer.getElement(i);
                    processLine(features, result, line, lineNumber, leadingBuffer, trailingBuffer, i + 1);
                    leadingBuffer.append(line);
                }
            }
            catch (IOException e)
            {
                LOG.warning("I/O error post-processing artifact '" + artifact.getPath() + "': " + e.getMessage());
            }
            finally
            {
                IOUtils.close(reader);
            }
        }

        for(PlainFeature f: features)
        {
            artifact.addFeature(f);
        }
    }

    private void simpleProcess(File outputDir, StoredFileArtifact artifact, CommandResult result, List<PlainFeature> features)
    {
        BufferedReader reader = null;
        try
        {
            File file = new File(outputDir, artifact.getPath());
            reader = new BufferedReader(new FileReader(file));
            String line;
            long lineNumber = 0;

            while ((line = reader.readLine()) != null)
            {
                lineNumber++;
                processLine(features, result, line, lineNumber);
            }
        }
        catch (IOException e)
        {
            LOG.warning("I/O error post-processing artifact '" + artifact.getPath() + "': " + e.getMessage());
        }
        finally
        {
            IOUtils.close(reader);
        }
    }

    private void processLine(List<PlainFeature> features, CommandResult result, String line, long lineNumber)
    {
        processLine(features, result, line, lineNumber, null, null, 0);
    }

    private void processLine(List<PlainFeature> features, CommandResult result, String line, long lineNumber, CircularBuffer<String> leadingContext, CircularBuffer<String> trailingContext, int trailingIndex)
    {
        for (RegexPattern p : patterns)
        {
            String summary = p.match(line);
            if (summary != null)
            {
                if (p.getCategory() == Feature.Level.ERROR && failOnError)
                {
                    result.failure("Error features detected");
                }
                else if (p.getCategory() == Feature.Level.WARNING && failOnWarning)
                {
                    result.failure("Warning features detected");
                }

                if (leadingContext == null)
                {
                    addFeature(features, new PlainFeature(p.getCategory(), summary, lineNumber));
                }
                else
                {
                    // Add the context lines to the summary
                    StringBuilder summaryBuilder = new StringBuilder();
                    append(summaryBuilder, leadingContext, 0, true);
                    summaryBuilder.append(summary);
                    append(summaryBuilder, trailingContext, trailingIndex, false);
                    addFeature(features, new PlainFeature(p.getCategory(), summaryBuilder.toString(), lineNumber - leadingContext.getCount(), lineNumber + trailingContext.getCount() - trailingIndex, lineNumber));
                }
            }
        }
    }

    private void addFeature(List<PlainFeature> features, PlainFeature feature)
    {
        if(canJoin(features, feature))
        {
            // Join with previous
            PlainFeature previous = features.get(features.size() - 1);
            long overlappingLines = previous.getLastLine() - feature.getFirstLine() + 1;
            String remainingSummary = getRemainingSummary(feature.getSummary(), overlappingLines);
            previous.setSummary(previous.getSummary() + remainingSummary);
            previous.setLastLine(feature.getLastLine());
        }
        else
        {
            features.add(feature);
        }
    }

    private boolean canJoin(List<PlainFeature> features, PlainFeature feature)
    {
        if(joinOverlapping && features.size() > 0)
        {
            PlainFeature previous = features.get(features.size() - 1);
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

    public RegexPattern createPattern()
    {
        RegexPattern pattern = new RegexPattern();
        addRegexPattern(pattern);
        return pattern;
    }

    /* Hrm, if we call this addPattern it gets magically picked up by FileLoader */
    public void addRegexPattern(RegexPattern pattern)
    {
        patterns.add(pattern);
    }

    public String getName()
    {
        return name;
    }

    public Object getValue()
    {
        return this;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public List<RegexPattern> getPatterns()
    {
        return patterns;
    }

    public boolean getFailOnError()
    {
        return failOnError;
    }

    public void setFailOnError(boolean failOnError)
    {
        this.failOnError = failOnError;
    }

    public boolean getFailOnWarning()
    {
        return failOnWarning;
    }

    public void setFailOnWarning(boolean failOnWarning)
    {
        this.failOnWarning = failOnWarning;
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

    protected void addErrorRegexs(String[] errorRegexs)
    {
        for (String errorRegex : errorRegexs)
        {
            RegexPattern pattern = createPattern();
            pattern.setPattern(Pattern.compile(errorRegex));
            pattern.setCategory(Feature.Level.ERROR);
        }
    }

    protected void addWarningRegexs(String[] warningRegexs)
    {
        for (String warningRegex : warningRegexs)
        {
            RegexPattern pattern = createPattern();
            pattern.setPattern(Pattern.compile(warningRegex));
            pattern.setCategory(Feature.Level.WARNING);
        }
    }
}
