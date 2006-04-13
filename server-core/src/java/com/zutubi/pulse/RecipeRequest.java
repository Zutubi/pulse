/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse;

import com.zutubi.pulse.core.PulseException;
import com.zutubi.pulse.core.Bootstrapper;

/**
 * A request to execute a specific recipe.  Includes details about how to
 * bootstrap this step of the build (e.g. by SCM checkout, or by using a
 * working directory left by a previous recipe).
 */
public class RecipeRequest
{
    /**
     * The unique identifier for the execution of this recipe.
     */
    private long id;
    /**
     * Used to bootstrap the working directory.
     */
    private Bootstrapper bootstrapper;
    /**
     * Source used to retrieve the pulse file.
     */
    private String pulseFileSource;
    /**
     * The name of the recipe to execute, or null to execute the default.
     */
    private String recipeName;


    public RecipeRequest(long id, String recipeName)
    {
        this.id = id;
        this.recipeName = recipeName;
    }

    public long getId()
    {
        return id;
    }

    public Bootstrapper getBootstrapper()
    {
        return bootstrapper;
    }

    public String getPulseFileSource()
    {
        return pulseFileSource;
    }

    public String getRecipeName()
    {
        return recipeName;
    }

    public void setBootstrapper(Bootstrapper bootstrapper)
    {
        this.bootstrapper = bootstrapper;
    }

    public void setPulseFileSource(String pulseFileSource)
    {
        this.pulseFileSource = pulseFileSource;
    }

    public void prepare() throws PulseException
    {
        bootstrapper.prepare();
    }
}
