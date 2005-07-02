package com.cinnamonbob.core2.config;

import com.cinnamonbob.util.IOHelper;
import com.cinnamonbob.core2.InternalBuildFailureException;
import com.cinnamonbob.scm.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public abstract class SCMCheckoutCommand implements Command
{
    private static final String OUTPUT_FILENAME = "output.txt";

    //=======================================================================
    // Implementation
    //=======================================================================

    private Revision getPreviousRevision()//BuildResult previousBuild)
    {
//        Revision result = null;
//        
//        if(previousBuild != null)
//        {
//            CommandResultCommon previousResult = previousBuild.getCommandResult(common.getName());
//            
//            if(previousResult != null)
//            {
//                CommandResult commandResult = previousResult.getResult();
//                
//                if(commandResult instanceof SCMCheckoutCommandResult)
//                {
//                    SCMCheckoutCommandResult scmResult = (SCMCheckoutCommandResult)commandResult;
//                    return scmResult.getRevision();
//                }
//            }
//        }
        
        // lookup scm manager that needs to remember revisions etc. 
        
        return null;
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
            IOHelper.close(writer);
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

    //=======================================================================
    // Abstract interface to be filled in by subclasses
    //=======================================================================

    protected abstract File getPath();
    protected abstract SCMServer createServer() throws SCMException;
    protected abstract void destroyServer(SCMServer server);
    
    //=======================================================================
    // Command interface
    //=======================================================================

    public CommandResult execute(File outputDir) throws InternalBuildFailureException
    {
        SCMServer                server = null;
        SCMCheckoutCommandResult result;
        
        try
        {
            server = createServer();
            result = doCheckout(outputDir, getPreviousRevision(), server);
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
}
