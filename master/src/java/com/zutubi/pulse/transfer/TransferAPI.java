package com.zutubi.pulse.transfer;

import com.zutubi.pulse.Version;
import com.zutubi.pulse.upgrade.tasks.MutableConfiguration;
import com.zutubi.pulse.util.IOUtils;

import javax.sql.DataSource;
import java.io.*;

/**
 *
 *
 */
public class TransferAPI
{
    public void dump(MutableConfiguration configuration, DataSource dataSource, File outFile) throws TransferException
    {
        FileOutputStream outputStream = null;
        try
        {
            outputStream = new FileOutputStream(outFile);

            dump(configuration, dataSource, outputStream);

        }
        catch (IOException e)
        {
            throw new TransferException(e);
        }
        finally
        {
            IOUtils.close(outputStream);
        }
    }

    public void dump(MutableConfiguration configuration, DataSource dataSource, OutputStream outputStream) throws TransferException
    {
        XMLTransferTarget target = null;
        try
        {
            target = new XMLTransferTarget();
            target.setOutput(outputStream);
            target.setVersion(Version.getVersion().getBuildNumber());

            JDBCTransferSource source = new JDBCTransferSource();
            source.setConfiguration(configuration);
            source.setDataSource(dataSource);

            source.transferTo(target);
        }
        finally
        {
            if (target != null)
            {
                target.close();
            }
        }
    }

    public void restore(MutableConfiguration configuration, File inFile, DataSource dataSource) throws TransferException
    {
        FileInputStream inputStream = null;
        try
        {
            inputStream = new FileInputStream(inFile);
            restore(configuration, dataSource, inputStream);
        }
        catch (IOException e)
        {
            throw new TransferException(e);
        }
        finally
        {
            IOUtils.close(inputStream);
        }
    }

    public void restore(MutableConfiguration configuration, DataSource dataSource, InputStream inputStream) throws TransferException
    {
        // configure the import.
        JDBCTransferTarget target = null;
        try
        {
            target = new JDBCTransferTarget();
            target.setDataSource(dataSource);
            target.setConfiguration(configuration);

            XMLTransferSource source = new XMLTransferSource();
            source.setConfiguration(configuration);
            source.setSource(inputStream);

            source.transferTo(target);
        }
        finally
        {
            if (target != null)
            {
                target.close();
            }
        }
    }
}
