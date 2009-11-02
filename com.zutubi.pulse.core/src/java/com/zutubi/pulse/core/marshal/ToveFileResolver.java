package com.zutubi.pulse.core.marshal;

import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.util.FileSystemUtils;

import java.io.InputStream;
import java.util.Stack;

/**
 * Wrapper around file resolvers used by the {@link ToveFileLoader} to keep
 * track of which file it is loading.
 */
public class ToveFileResolver
{
    private Stack<String> paths = new Stack<String>();
    private Stack<FileResolver> delegateStack = new Stack<FileResolver>();

    /**
     * Creates a resolver wrapping the given initial delegate resolver.
     *
     * @param delegate initial resolver to load imports from
     */
    public ToveFileResolver(FileResolver delegate)
    {
        delegateStack.push(delegate);
    }

    /**
     * Resolves the given file path to an input stream and adjusts the delegate
     * resolver to load subsequent files relative to it.  When the file load is
     * complete you should call {@link #popImport()}.
     *
     * @param file path of the file being imported
     * @return input stream containing the contents of the requested file
     * @throws Exception if the requested file path cannot be resolved
     */
    public InputStream pushImport(String file) throws Exception
    {
        // Attempt to resolve first so if there is an error our stack is
        // unaffected.
        FileResolver resolver = delegateStack.peek();
        InputStream result = resolver.resolve(file);

        paths.push(file);
        delegateStack.push(new RelativeFileResolver(file, resolver));

        return result;
    }

    /**
     * Restore the delegate resolver to be relative to the previous file.
     * These calls should be matched with calls to {@link #pushImport(String)}.
     */
    public void popImport()
    {
        paths.pop();
        delegateStack.pop();
    }

    /**
     * Returns the path of the current file, relative to the first file to
     * participate in loading.
     *
     * @return path of the file currently being loaded
     */
    public String getCurrentPath()
    {
        String path = null;
        for (String current: paths)
        {
            String parent = path == null ? null : PathUtils.getParentPath(path);
            path = FileSystemUtils.appendAndCanonicalise(parent, current);
        }

        return path;
    }
}
