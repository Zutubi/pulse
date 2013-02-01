package com.zutubi.util.junit;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.zutubi.util.io.IOUtils;
import junit.framework.Assert;
import junit.framework.AssertionFailedError;

import java.io.*;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Assertions for checking streams of data and the file system.
 */
public class IOAssertions
{
    /**
     * Compares the content of the two given directories recursively,
     * asserting that they have identical contents.  That is, all the
     * contained files and directories are the same, as is the
     * contents of the files.
     *
     * @param dir1 the first directory in the comparison
     * @param dir2 the second directory in the comparison
     * @throws junit.framework.AssertionFailedError
     *          if the given directories
     *          differ
     * @throws java.io.IOException if there is an error comparing two files
     */
    public static void assertDirectoriesEqual(File dir1, File dir2) throws IOException
    {
        if (!dir1.isDirectory())
        {
            throw new AssertionFailedError("Directory '" + dir1.getAbsolutePath() + "' does not exist or is not a directory");
        }

        if (!dir2.isDirectory())
        {
            throw new AssertionFailedError("Directory '" + dir2.getAbsolutePath() + "' does not exist or is not a directory");
        }

        String[] files1 = dir1.list();
        String[] files2 = dir2.list();

        // File.list does not guarantee ordering, so we do
        Arrays.sort(files1);
        Arrays.sort(files2);

        List<String> fileList1 = new LinkedList<String>(Arrays.asList(files1));
        List<String> fileList2 = new LinkedList<String>(Arrays.asList(files2));

        // Ignore .svn directories
        fileList1.remove(".svn");
        fileList2.remove(".svn");

        if (!fileList1.equals(fileList2))
        {
            throw new AssertionFailedError("Directory contents differ: " +
                    dir1.getAbsolutePath() + " = " + fileList1 + ", " +
                    dir2.getAbsolutePath() + " = " + fileList2);
        }

        for (String file : fileList1)
        {
            File file1 = new File(dir1, file);
            File file2 = new File(dir2, file);

            if (file1.isDirectory())
            {
                assertDirectoriesEqual(file1, file2);
            }
            else
            {
                assertFilesEqual(file1, file2);
            }
        }
    }

    /**
     * Asserts that the contents of the two given files is identical.
     *
     * @param file1 the first file to compare
     * @param file2 the second file to compare
     * @throws junit.framework.AssertionFailedError if the contents of the files differ
     * @throws java.io.IOException if there is an error reading the files
     */
    public static void assertFilesEqual(File file1, File file2) throws IOException
    {
        assertFilesEqual(file1, file2, Functions.<String>identity());
    }

    /**
     * Asserts that the contents of the two given files is identical.
     *
     * @param file1                  the first file to compare
     * @param file2                  the second file to compare
     * @param lineProcessingFunction function used to process each line as it
     *                               is read, before lines are compared
     * @throws junit.framework.AssertionFailedError if the contents of the files differ
     * @throws java.io.IOException if there is an error reading the files
     */
    public static void assertFilesEqual(File file1, File file2, Function<String, String> lineProcessingFunction) throws IOException
    {
        if (!file1.isFile())
        {
            throw new AssertionFailedError("File '" + file1.getAbsolutePath() + "' does not exist or is not a regular file");
        }

        if (!file2.isFile())
        {
            throw new AssertionFailedError("File '" + file2.getAbsolutePath() + "' does not exist or is not a regular file");
        }

        BufferedReader rs1 = null;
        BufferedReader rs2 = null;
        try
        {
            rs1 = new BufferedReader(new InputStreamReader(new FileInputStream(file1)));
            rs2 = new BufferedReader(new InputStreamReader(new FileInputStream(file2)));
            while (true)
            {
                String line1 = lineProcessingFunction.apply(rs1.readLine());
                String line2 = lineProcessingFunction.apply(rs2.readLine());

                if (line1 == null)
                {
                    if (line2 == null)
                    {
                        return;
                    }
                    throw new AssertionFailedError("Contents of '" + file1.getAbsolutePath() + " differs from contents of '" + file2.getAbsolutePath() + "'");
                }
                else
                {
                    if (line2 == null)
                    {
                        throw new AssertionFailedError("Contents of '" + file1.getAbsolutePath() + " differs from contents of '" + file2.getAbsolutePath() + "'");
                    }
                    Assert.assertEquals(line1, line2);
                }
            }
        }
        finally
        {
            IOUtils.close(rs1);
            IOUtils.close(rs2);
        }
    }
}
