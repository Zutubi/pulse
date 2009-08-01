package com.zutubi.pulse.core;

import com.zutubi.pulse.core.engine.api.ExecutionContext;
import com.zutubi.pulse.core.model.CommandResult;
import com.zutubi.pulse.core.model.StoredArtifact;
import com.zutubi.util.FileSystemUtils;
import org.apache.tools.ant.DirectoryScanner;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

/**
 * Information about a directory artifact to be captured.
 */
public class DirectoryArtifact extends LocalArtifact
{
    private File base;
    private String index;
    private List<Pattern> inclusions;
    private List<Pattern> exclusions;
    private String type = null;
    private boolean followSymlinks;

    public DirectoryArtifact()
    {
    }

    public File getBase()
    {
        return base;
    }

    public void setBase(File base)
    {
        this.base = base;
    }

    public String getIndex()
    {
        return index;
    }

    public void setIndex(String index)
    {
        this.index = index;
    }

    public Pattern createInclude()
    {
        if (inclusions == null)
        {
            inclusions = new LinkedList<Pattern>();
        }

        Pattern result = new Pattern();
        inclusions.add(result);
        return result;
    }

    public Pattern createExclude()
    {
        if (exclusions == null)
        {
            exclusions = new LinkedList<Pattern>();
        }

        Pattern result = new Pattern();
        exclusions.add(result);
        return result;
    }

    public void capture(CommandResult result, ExecutionContext context)
    {
        if (base == null)
        {
            base = context.getWorkingDir();
        }
        else if (!isAbsolute(base))
        {
            base = new File(context.getWorkingDir(), base.getPath());
        }

        if (!base.exists())
        {
            checkFailIfNotPresent(result, "Capturing artifact '" + getName() + "': base directory '" + base.getAbsolutePath() + "' does not exist");

            // Don't attempt to capture.
            return;
        }

        if (!base.isDirectory())
        {
            result.error("Directory artifact '" + getName() + "': base '" + base.getAbsolutePath() + "' is not a directory");
            return;
        }

        DirectoryScanner scanner = new DirectoryScanner();
        scanner.setBasedir(base);
        if (inclusions != null)
        {
            scanner.setIncludes(flattenPatterns(inclusions));
        }

        if (exclusions != null)
        {
            scanner.setExcludes(flattenPatterns(exclusions));
        }

        scanner.setFollowSymlinks(followSymlinks);
        scanner.scan();

        StoredArtifact artifact = new StoredArtifact(getName());
        artifact.setIndex(index);
        for (String file : scanner.getIncludedFiles())
        {
            File source = new File(base, file);
            captureFile(artifact, source, FileSystemUtils.composeFilename(getName(), file), result, context, type);
        }
        result.addArtifact(artifact);
    }

    public String getType()
    {
        return type;
    }

    public void setType(String type)
    {
        this.type = type;
    }

    public void setFollowSymlinks(boolean followSymlinks)
    {
        this.followSymlinks = followSymlinks;
    }

    private String[] flattenPatterns(List<Pattern> patterns)
    {
        String[] result = new String[patterns.size()];
        int i = 0;

        for (Pattern p : patterns)
        {
            result[i++] = p.getPattern();
        }

        return result;
    }

    public class Pattern
    {
        private String pattern;

        public String getPattern()
        {
            return pattern;
        }

        public void setPattern(String pattern)
        {
            this.pattern = pattern;
        }
    }
}
