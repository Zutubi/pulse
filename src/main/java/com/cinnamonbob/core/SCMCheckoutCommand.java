package com.cinnamonbob.core;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import com.cinnamonbob.core.scm.Change;
import com.cinnamonbob.core.scm.Changelist;
import com.cinnamonbob.core.scm.Revision;
import com.cinnamonbob.core.scm.SCMException;
import com.cinnamonbob.core.scm.SCMServer;

public abstract class SCMCheckoutCommand implements Command
{
    private static final String OUTPUT_FILENAME = "output.txt";

    protected CommandCommon common;
    
    //=======================================================================
    // Implementation
    //=======================================================================

    private Revision getPreviousRevision(BuildResult previousBuild)
    {
        Revision result = null;
        
        if(previousBuild != null)
        {
            CommandResultCommon previousResult = previousBuild.getCommandResult(common.getName());
            
            if(previousResult != null)
            {
                CommandResult commandResult = previousResult.getResult();
                
                if(commandResult instanceof SCMCheckoutCommandResult)
                {
                    SCMCheckoutCommandResult scmResult = (SCMCheckoutCommandResult)commandResult;
                    return scmResult.getRevision();
                }
            }
        }
        
        return result;
    }

    private void saveChanges(File outputDir, LinkedList<Change> changes) throws InternalBuildFailureException
    {
        File       output = new File(outputDir, OUTPUT_FILENAME);
        FileWriter writer = null;
        
        try
        {
            writer = new FileWriter(output);

            for(Change change: changes)
            {
                writer.write(change.getFilename() + "#" + change.getRevision() + "\n");
            }
        }
        catch(IOException e)
        {
            throw new InternalBuildFailureException("Could not create output file '" + output.getAbsolutePath() + "'", e);
        }
        finally
        {
            if(writer != null)
            {
                try
                {
                    writer.close();
                }
                catch(IOException e)
                {
                    // ignore
                }
            }
        }
    }

    private SCMCheckoutCommandResult doCheckout(File outputDir, Revision previousRevision, SCMServer server) throws SCMException, InternalBuildFailureException
    {
        LinkedList<Change> changes  = new LinkedList<Change>();
        Revision           revision = server.checkout(getPath(), null, changes);
        List<Changelist>   lists    = null;
        
        saveChanges(outputDir, changes);
        // It would be perverse to get two types of revisions, but not impossible...
        if(previousRevision != null && revision.getClass().equals(previousRevision.getClass()))
        {
            lists = server.getChanges(previousRevision, revision, "");
        }
        
        return new SCMCheckoutCommandResult(revision, lists);
    }

    //=======================================================================
    // Construction
    //=======================================================================

    protected SCMCheckoutCommand(CommandCommon common)
    {
        this.common = common;
    }
    
    //=======================================================================
    // Abstract interface to be filled in by subclasses
    //=======================================================================

    protected abstract File getPath();
    protected abstract SCMServer createServer() throws SCMException;
    protected abstract void destroyServer(SCMServer server);
    
    //=======================================================================
    // Command interface
    //=======================================================================

    public CommandResult execute(File outputDir, BuildResult previousBuild) throws InternalBuildFailureException
    {
        SCMServer                server = null;
        SCMCheckoutCommandResult result;
        
        try
        {
            server = createServer();
            result = doCheckout(outputDir, getPreviousRevision(previousBuild), server);
        }
        catch(SCMException e)
        {
            result = new SCMCheckoutCommandResult(e);
        }
        finally
        {
            if(server != null)
            {
                destroyServer(server);
            }
        }
        
        return result;
    }

    public List<ArtifactSpec> getArtifacts()
    {
        List<ArtifactSpec> result = new LinkedList<ArtifactSpec>();
        
        result.add(new ArtifactSpec("output", "Command Output", Artifact.TYPE_PLAIN, new File(OUTPUT_FILENAME)));
        return result;
    }

}
