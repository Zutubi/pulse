package com.zutubi.prototype.model;

import com.zutubi.prototype.AbstractParameterised;
import com.zutubi.prototype.type.record.PathUtils;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Predicate;

import java.util.LinkedList;
import java.util.List;

/**
 * Models a row of a configuration table.
 */
public class Row extends AbstractParameterised
{
    private static final String PARAM_PATH   = "path";
    private static final String PARAM_HIDDEN = "hidden";

    private List<Cell> cells = new LinkedList<Cell>();
    private List<ActionLink> actions = null;

    public Row()
    {
    }

    public Row(String path, boolean hidden, List<ActionLink> actions)
    {
        addParameter(PARAM_PATH, path);
        addParameter(PARAM_HIDDEN, hidden);
        this.actions = actions;
    }

    public String getPath()
    {
        return getParameter(PARAM_PATH, "");
    }

    public boolean isHidden()
    {
        return getParameter(PARAM_HIDDEN, false);
    }

    public String getBaseName()
    {
        return PathUtils.getBaseName(getParameter(PARAM_PATH, ""));
    }

    public List<Cell> getCells()
    {
        return cells;
    }

    public void addCell(Cell cell)
    {
        cells.add(cell);
    }

    public List<ActionLink> getActions()
    {
        return actions;
    }

    public ActionLink getAction(final String actionName)
    {
        return CollectionUtils.find(actions, new Predicate<ActionLink>()
        {
            public boolean satisfied(ActionLink actionLink)
            {
                return actionLink.getAction().equals(actionName);
            }
        });
    }

    public void addAction(ActionLink action)
    {
        actions.add(action);
    }
}
