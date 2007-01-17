var keyToText = {
        'actions': 'actions',
        'elapsed': 'elapsed',
        'id': 'build id',
        'owner': 'owner',
        'project': 'project',
        'reason': 'reason',
        'rev': 'revision',
        'spec': 'spec',
        'status': 'status',
        'tests': 'tests',
        'version': 'version',
        'when': 'when',
        'warnings': 'warnings',
        'errors': 'errors'
};

/**
 * @class a YAHOO.util.DDProxy implementation for dragging column
 * headers about for sorting purposes.
 *
 * @extends YAHOO.util.DDProxy
 * @constructor
 * @param {String} id the id of the linked element
 * @param {String} sGroup the group of related DragDrop objects
 */
var DraggableHeader = function(id, popup, parentRow)
{
    if (id)
    {
        this.init(id, popup.suffix);
        this.initFrame();
    }

    this.popup = popup;
    this.parentRow = parentRow;
    this.arrow = $("column-arrow-" + popup.suffix);

    var s = this.getDragEl().style;
    s.borderColor = "transparent";
    s.backgroundColor = "#eee";
    s.opacity = 0.70;
    s.filter = "alpha(opacity=70)";
};

YAHOO.extend(DraggableHeader, YAHOO.util.DDProxy);

DraggableHeader.prototype.startDrag = function(x, y)
{
    var dragEl = this.getDragEl();
    var clickEl = this.getEl();

    dragEl.innerHTML = clickEl.innerHTML;
    dragEl.style.padding = "2px";
    dragEl.style.border = "1px solid #eee";
    dragEl.style.fontWeight = "bold";

    this.arrow.style.display = "";
    YAHOO.util.Dom.setY(this.arrow, YAHOO.util.Dom.getY(this.parentRow) - this.arrow.offsetHeight);
};

DraggableHeader.prototype.endDrag = function(e)
{
    this.arrow.style.display = "none";

    var e1 = this.getEl();
    var e2 = null;

    if(this.over)
    {
        e2 = this.over;
    }

    this.parentRow.insertBefore(e1, e2);
    this.popup.setBuildColumnArray();
};

DraggableHeader.prototype.overColumn = function(e)
{
    var posX = YAHOO.util.Event.getPageX(e);

    return this.popup.buildColumnArray.find(function(columnHeader) {
        // Are we to the left of the middle of this column
        var mid = YAHOO.util.Dom.getX(columnHeader) + ( Math.floor(columnHeader.offsetWidth / 2));
        return posX < mid;
    });
}

DraggableHeader.prototype.onDrag = function(e, id)
{
    this.over = this.overColumn(e);
    var arrowX;

    if(this.over)
    {
        arrowX = YAHOO.util.Dom.getX(this.over);
    }
    else
    {
        var last = this.popup.buildColumnArray.last()
        arrowX = YAHOO.util.Dom.getX(last) + last.offsetWidth;
    }

    YAHOO.util.Dom.setX(this.arrow, arrowX - 8);
};

function BuildColumnPopup(suffix, base, actionUrl, panel)
{
    this.suffix = suffix;
    this.base = base;
    this.actionUrl = actionUrl;
    this.panel = panel;
}

BuildColumnPopup.prototype.popup = function()
{
    this.createConfigureRow();

    var popup = this.getColumnsPopup()
    popup.style.display = "";
    var link = $("columns-popup-link-" + this.suffix);
    YAHOO.util.Dom.setX(popup, Math.max(0, YAHOO.util.Dom.getX(link) - popup.offsetWidth));
    YAHOO.util.Dom.setY(popup, YAHOO.util.Dom.getY(link) + 16);
    var dd = new YAHOO.util.DD(popup);
    dd.setHandleElId("columns-popup-handle-" + this.suffix);
}

BuildColumnPopup.prototype.createConfigureRow = function()
{
    var currentHeaderRow = $("build-table-" + this.suffix);
    var currentHeaders = $A(currentHeaderRow.getElementsByTagName("th"));
    var oldConfigureHeaderRow = this.getBuildConfigureRow();

    // Clear current state
    var configureHeaderRow = oldConfigureHeaderRow.cloneNode(false);
    oldConfigureHeaderRow.parentNode.replaceChild(configureHeaderRow, oldConfigureHeaderRow);
    var checkboxes = $A(this.getColumnsPopup().getElementsByTagName("input"));
    checkboxes.each(function(checkbox) { checkbox.checked = false; });

    // Initialise configure headers based on current headers
    for(i = 0; i < currentHeaders.length; i++)
    {
        // Extract id information
        var parts = currentHeaders[i].id.split('-');
        var key = parts[2];

        // Add the configure header
        var id = this.addBuildColumn(key);

        // Check the corresponding checkbox
        var checkbox = this.getBuildColumnCheckbox(key);
        if(checkbox)
        {
            checkbox.checked = true;
        }
    }
}

BuildColumnPopup.prototype.getBuildConfigureRow = function()
{
    return $("build-header-row-" + this.suffix);
}

BuildColumnPopup.prototype.getBuildColumnId = function(key)
{
    return "configure-" + key + "-" + this.suffix;
}

BuildColumnPopup.prototype.setBuildColumnArray = function()
{
    this.buildColumnArray = $A(this.getBuildConfigureRow().getElementsByTagName("th"));
}

BuildColumnPopup.prototype.addBuildColumn = function(key)
{
    var configureHeaderRow = this.getBuildConfigureRow();

    var id = this.getBuildColumnId(key);
    var configureHeader = document.createElement("th");

    configureHeader.id = id;
    configureHeader.appendChild(document.createTextNode(keyToText[key]));
    configureHeader.className = "content";
    configureHeaderRow.appendChild(configureHeader);

    // Enable drag'n'drop
    new DraggableHeader(id, this, configureHeaderRow);
    new YAHOO.util.DDTarget(id);

    this.setBuildColumnArray();
    return id;
}

BuildColumnPopup.prototype.removeBuildColumn = function(key)
{
    var configureHeaderRow = this.getBuildConfigureRow();
    var configureHeader = $(this.getBuildColumnId(key));
    configureHeaderRow.removeChild(configureHeader);
    this.setBuildColumnArray();
}

BuildColumnPopup.prototype.getBuildColumnCheckbox = function(key)
{
    return $("build-column-" + key + "-" + this.suffix);
}

BuildColumnPopup.prototype.addRemoveBuildColumn = function(key)
{
    var add = this.getBuildColumnCheckbox(key).checked;
    if(add)
    {
        this.addBuildColumn(key);
    }
    else
    {
        this.removeBuildColumn(key);
    }
}

BuildColumnPopup.prototype.getColumnsPopup = function()
{
    return $("columns-popup-" + this.suffix);
}

BuildColumnPopup.prototype.getBuildColumns = function()
{
    var configureHeaderRow = this.getBuildConfigureRow();
    var headers = $A(configureHeaderRow.getElementsByTagName("th"));
    var columns = "";

    headers.each(function(header)
    {
        if(columns.length > 0)
        {
            columns += ",";
        }

        var parts = header.id.split('-');
        columns += parts[1];
    });

    return columns;
}

BuildColumnPopup.prototype.getErrorEl = function()
{
    return $("columns-popup-error-" + this.suffix);
}

BuildColumnPopup.prototype.reportError = function(message)
{
    var errorEl = this.getErrorEl();
    errorEl.style.display = "";
    errorEl.innerHTML = "<p>" + message + "</p>";
}

BuildColumnPopup.prototype.clearError = function()
{
    var errorEl = this.getErrorEl();
    errorEl.style.display = "none";
}

BuildColumnPopup.prototype.applyBuildColumns = function()
{
    this.clearError();

    var columns = this.getBuildColumns();
    if(columns.length == 0)
    {
        this.reportError("You must select at least one column.");
        return;
    }

    var req = new Ajax.Request(this.base + "/ajax/customiseBuildColumns.action",
                               {
                                   method: "get",
                                   parameters: "suffix=" + this.suffix + "&columns=" + columns,
                                   asynchronous: false
                               });

    if(req.responseIsSuccess())
    {
        var updater = new Ajax.Updater({ success: this.panel },
                                       this.actionUrl,
                                       { method: "get" });
        this.getColumnsPopup().style.display = "none";
    }
    else
    {
        this.reportError("Unable to apply changes.");
    }
}

BuildColumnPopup.prototype.cancelBuildColumns = function()
{
    this.clearError();
    this.getColumnsPopup().style.display = "none";
}
