package com.zutubi.pulse.acceptance.dependencies;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class Artifact
{
    @SuppressWarnings({"FieldCanBeLocal"})
    private final Pattern pattern = Pattern.compile("(.+)\\.(.+)");

    private String name;
    private String extension;
    private Recipe recipe;
    private String artifactPattern;

    public Artifact(String filename, Recipe recipe)
    {
        this.recipe = recipe;
        Matcher m = pattern.matcher(filename);
        if (m.matches())
        {
            name = m.group(1);
            extension = m.group(2);
        }
    }

    public String getName()
    {
        return name;
    }

    public String getExtension()
    {
        return extension;
    }

    public void setExtension(String extension)
    {
        this.extension = extension;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public Recipe getRecipe()
    {
        return recipe;
    }

    public String getArtifactPattern()
    {
        return artifactPattern;
    }

    public void setArtifactPattern(String artifactPattern)
    {
        this.artifactPattern = artifactPattern;
    }
}
