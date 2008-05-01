package com.zutubi.pulse.transfer;

import java.util.Map;

/**
 *
 *
 */
public interface TransferListener
{
    void start();

    void startTable(Table table);

    void row(Map<String, Object> row);

    void endTable();

    void end();
}
