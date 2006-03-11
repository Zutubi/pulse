package com.cinnamonbob.core;

import com.cinnamonbob.core.model.CommandResult;
import com.cinnamonbob.core.model.StoredArtifact;
import com.cinnamonbob.core.model.StoredFileArtifact;
import com.cinnamonbob.core.util.FileSystemUtils;
import org.apache.tools.ant.DirectoryScanner;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

/**
 * Information about a directory artifact to be captured.
 */
public class DirectoryArtifact extends Artifact
{
    private File base;
    private String index;
    private List<Pattern> inclusions;
    private List<Pattern> exclusions;
    private String type = null;

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

    public void capture(CommandResult result, File baseDir, File outputDir)
    {
        if (base == null)
        {
            base = baseDir;
        }
        else if (!base.isAbsolute())
        {
            base = new File(baseDir, base.getPath());
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

        scanner.scan();

        StoredArtifact artifact = new StoredArtifact(getName());
        artifact.setIndex(index);
        for (String file : scanner.getIncludedFiles())
        {
            File source = new File(base, file);
            captureFile(artifact, source, FileSystemUtils.composeFilename(getName(), file), outputDir, result, type);
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
