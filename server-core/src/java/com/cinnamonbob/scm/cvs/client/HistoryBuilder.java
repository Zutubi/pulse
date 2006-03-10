package com.cinnamonbob.scm.cvs.client;

import org.netbeans.lib.cvsclient.command.Builder;

import java.util.*;

import com.cinnamonbob.scm.SCMException;

/**
 * 
 *
 */
public class HistoryBuilder implements Builder
{
    private List<HistoryInfo> infos = new LinkedList<HistoryInfo>();

    static final String NO_RECORDS = "No records selected.";

    private String module;

    public void setModule(String module)
    {
        this.module = module;
    }

    public void parseLine(String line, boolean isErrorMessage)
    {
        if (!isErrorMessage && !line.equals(NO_RECORDS))
        {
            try
            {
                HistoryInfo info = parse(line);
                if (matchesRequestedModule(info))
                {
                    infos.add(info);
                }
            }
            catch (SCMException e)
            {
                e.printStackTrace();
            }
        }
    }

    public void parseEnhancedMessage(String key, Object value)
    {
        // noop.
    }

    public void outputDone()
    {
        // finalise 
    }

    public List<HistoryInfo> getHistoryInfo()
    {
        return Collections.unmodifiableList(infos);
    }

    private HistoryInfo parse(String data) throws SCMException
    {
        return new HistoryInfo(data);
    }

    /**
     * Unfortunately, when using the history command, there is no simple way to filter the history
     * entry by the requested module. For now, we assume that the module is a directory relative to the
     * root of the repository.
     *
     * @param info
     * @return
     */
    private boolean matchesRequestedModule(HistoryInfo info)
    {
        if (module == null)
        {
            return true;
        }
        String pathInRepository = info.getPathInRepository();
        if(pathInRepository.equals(module))
        {
            return true;
        }
        // ensure that if the path in repo and module are not exactly the same, then
        // the module represents a prefix AND that prefix is followed by directories.
        // This is to ensure that you match module/ and not modulename/
        if (pathInRepository.startsWith(module) && pathInRepository.length() > module.length())
        {
            String next = pathInRepository.substring(module.length(), module.length() + 1);
            if (next.equals("/") || next.equals("\\"))
            {
                return true;
            }
        }

        // if the module specifies a file directly, then we need some shenanigans, module == path + 1 + file
        if (module.startsWith(pathInRepository) &&
                module.endsWith(info.getFile()) &&
                (pathInRepository.length() + info.getFile().length() < module.length()))
        {
            String middle = module.substring(pathInRepository.length(), module.length() - info.getFile().length());
            if (middle.equals("/") || middle.equals("\\"))
            {
                return true;
            }
        }

        return false;
    }
}
