package com.zutubi.pulse.acceptance.dependencies;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class ArtifactHelper
{
    private static final Pattern pattern = Pattern.compile("(.+)\\.(.+)");

    private String name;
    private String extension;
    private RecipeHelper recipe;
    private String artifactPattern;

    public ArtifactHelper(String filename, RecipeHelper recipe)
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

    public RecipeHelper getRecipe()
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
