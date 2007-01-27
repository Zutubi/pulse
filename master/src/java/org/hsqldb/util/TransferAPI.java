package org.hsqldb.util;

import com.zutubi.pulse.util.JDBCUtils;

import javax.sql.DataSource;
import java.io.File;
import java.sql.Connection;
import java.util.Vector;

/**
 * Simple programmatic interface to the HSQL transfer tools.  In the HSQL
 * package to access package-private code.
 */
public class TransferAPI
{
    public void dump(DataSource dataSource, File out) throws Exception
    {
        Traceable traceable = new PrintingTraceable();

        Connection connection = null;
        DataAccessPoint source = null;
        DataAccessPoint target = null;

        try
        {
            connection = dataSource.getConnection();
            source = new TransferDb(connection, traceable);
            target = new TransferSQLText(out.getAbsolutePath(), traceable);

            doTransfer(source, target);
        }
        finally
        {
            if (source != null)
            {
                source.close();
            }
            else
            {
                JDBCUtils.close(connection);
            }

            if (target != null)
            {
                target.close();
            }
        }
    }

    public void restore(File in, DataSource dataSource) throws Exception
    {
        Traceable traceable = new PrintingTraceable();

        Connection connection = null;
        DataAccessPoint source = null;
        DataAccessPoint target = null;

        try
        {
            connection = dataSource.getConnection();
            source = new TransferSQLText(in.getAbsolutePath(), traceable);
            target = new TransferDb(connection, traceable);

            doTransfer(source, target);
        }
        finally
        {
            if (source != null)
            {
                source.close();
            }
            else
            {
                JDBCUtils.close(connection);
            }

            if (target != null)
            {
                target.close();
            }
        }
    }

    private void doTransfer(DataAccessPoint source, DataAccessPoint target) throws Exception
    {
        Vector tables = source.getTables(null, new String[] { "PUBLIC" });
        for (int i = 0; i < tables.size(); i++)
        {
            TransferTable t = (TransferTable) tables.elementAt(i);
            t.setDest(null, target);
            t.extractTableStructure(source, target);

            // Needs to be done after changing flags in the ttable
            source.getTableStructure(t, target);
        }

        for (int i = 0; i < tables.size(); i++)
        {
            TransferTable t = (TransferTable) tables.elementAt(i);

            t.transferStructure();
            t.transferData(1000000);
        }

        for (int i = 0; i < tables.size(); i++)
        {
            TransferTable t = (TransferTable) tables.elementAt(i);
            t.transferAlter();
        }
    }

    private static class PrintingTraceable implements Traceable
    {
        public void trace(String s)
        {
            System.out.println(s);
        }
    }
}
