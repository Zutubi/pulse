package com.zutubi.pulse.transfer;


import java.sql.Types;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * This transfer target is used to handle the data conversion from 1.2.x to 2.0.x.  As
 * the 1.2.x is exported, this object will filter out and adjust as necessary to map to the 2.0.x
 * schema.
 */
public class OneDotXToTwoDotZeroExportTransferTarget implements TransferTarget
{
    private TransferTarget delegate;
    private boolean transferringTable;

    private String currentTable;

    private static final Set<String> transferredTables = new HashSet<String>();
    {
        transferredTables.add("AGENT_STATE"); // rename first
        transferredTables.add("ARTIFACT");
        transferredTables.add("BUILD_REASON");
        transferredTables.add("BUILD_RESULT");
        transferredTables.add("CHANGELIST");
        transferredTables.add("CHANGELIST_PROJECTS");
        transferredTables.add("CHANGELIST_RESULTS");
        transferredTables.add("COMMAND_RESULT");
        transferredTables.add("COMMIT_MESSAGE_TRANSFORMER");
        transferredTables.add("COMMIT_MESSAGE_TRANSFORMER_PROJECTS");
        transferredTables.add("FEATURE");
        transferredTables.add("FILE_ARTIFACT");
        transferredTables.add("FILE_CHANGE");
        transferredTables.add("FORCE_CLEAN_AGENTS"); // rename
        transferredTables.add("LOCAL_TRIGGER");
        transferredTables.add("LOCAL_USER");
        transferredTables.add("PROJECT");
        transferredTables.add("RECIPE_RESULT");
        transferredTables.add("RECIPE_RESULT_NODE");
        transferredTables.add("REVISION");
        transferredTables.add("TEST_CASE_INDEX");
        transferredTables.add("TEST_RESULT_SUMMARY");
    }

    private static final Map<String, String> renamedTables = new HashMap<String, String>();
    {
        renamedTables.put("SLAVE", "AGENT_STATE");
        renamedTables.put("FORCE_CLEAN_SLAVES", "FORCE_CLEAN_AGENTS");
    }

    public OneDotXToTwoDotZeroExportTransferTarget(TransferTarget delegate)
    {
        this.delegate = delegate;
    }

    public void start() throws TransferException
    {
        delegate.start();
    }

    public void startTable(Table table) throws TransferException
    {
        if (renamedTables.containsKey(table.getName()))
        {
            table.setName(renamedTables.get(table.getName()));
        }

        if (transferredTables.contains(table.getName()))
        {
            transferringTable = true;

            // make the necessary adjustments to the table.
            table = updateTableIfRequired(table);
        }

        if (transferringTable)
        {
            delegate.startTable(table);
        }
    }

    private Table updateTableIfRequired(Table table)
    {
        TransferTable mutableTable = (TransferTable) table;
        if (table.getName().equals("AGENT_STATE"))
        {
            mutableTable.remove(table.getColumn("NAME"));
            mutableTable.remove(table.getColumn("HOST"));
            mutableTable.remove(table.getColumn("PORT"));
        }
        else if (table.getName().equals("FILE_CHANGE"))
        {
            TransferColumn revisionIdColumn = (TransferColumn) mutableTable.getColumn("REVISION_ID");
            revisionIdColumn.setName("REVISION_STRING");
            revisionIdColumn.setSqlTypeCode(Types.VARCHAR);
        }
        else if (table.getName().equals("LOCAL_USER"))
        {
            mutableTable.remove(table.getColumn("LOGIN"));
            mutableTable.remove(table.getColumn("NAME"));
            mutableTable.remove(table.getColumn("PASSWORD"));
            mutableTable.remove(table.getColumn("defaultAction"));
            mutableTable.remove(table.getColumn("refreshInterval"));
        }
        else if (table.getName().equals("RECIPE_RESULT_NODE"))
        {
            TransferColumn stageNameColumn = (TransferColumn) mutableTable.getColumn("STAGE_NAME");
            stageNameColumn.setName("stageName");
            stageNameColumn.setSqlTypeCode(Types.VARCHAR);
        }
        else if (table.getName().equals("PROJECT"))
        {
            mutableTable.remove(table.getColumn("NAME"));
            mutableTable.remove(table.getColumn("DESCRIPTION"));
            mutableTable.remove(table.getColumn("URL"));
            mutableTable.remove(table.getColumn("BOB_FILE_DETAILS"));
            mutableTable.remove(table.getColumn("DEFAULT_SPECIFICATION"));
            mutableTable.remove(table.getColumn("SCM"));
            mutableTable.remove(table.getColumn("CHANGE_VIEWER"));
        }
        else if (currentTable.equals("TEST_CASE_INDEX"))
        {
            mutableTable.remove(table.getColumn("SPEC_ID"));
        }

        return null;
    }

    public void row(Map<String, Object> row) throws TransferException
    {
        if (transferringTable)
        {
            row = updateRowIfRequired(row);

            delegate.row(row);
        }
    }

    private Map<String, Object> updateRowIfRequired(Map<String, Object> row)
    {
        // modify the row based on the table changes.
        if (currentTable.equals("AGENT_STATE"))
        {
            row.remove("NAME");
            row.remove("HOST");
            row.remove("PORT");
        }
        else if (currentTable.equals("FILE_CHANGE"))
        {
            row.put("REVISION_STRING", row.remove("REVISION_ID"));
        }
        else if (currentTable.equals("LOCAL_USER"))
        {
            row.remove("LOGIN");
            row.remove("NAME");
            row.remove("PASSWORD");
            row.remove("defaultAction");
            row.remove("refreshInterval");
        }
        else if (currentTable.equals("PROJECT"))
        {
            row.remove("NAME");
            row.remove("DESCRIPTION");
            row.remove("URL");
            row.remove("BOB_FILE_DETAILS");
            row.remove("DEFAULT_SPECIFICATION");
            row.remove("SCM");
            row.remove("CHANGE_VIEWER");
        }
        else if (currentTable.equals("RECIPE_RESULT_NODE"))
        {
            row.put("stageName", row.remove("STAGE_NAME"));
        }
        else if (currentTable.equals("TEST_CASE_INDEX"))
        {
            row.remove("SPEC_ID");
        }
        return row;
    }

    public void endTable() throws TransferException
    {
        if (transferringTable)
        {
            delegate.endTable();
            currentTable = null;
            transferringTable = false;
        }
    }

    public void end() throws TransferException
    {
        delegate.end();
    }

    public void close()
    {
        delegate.close();
    }
}
