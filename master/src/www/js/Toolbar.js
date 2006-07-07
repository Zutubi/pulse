Toolbar = function(id)
{
    this.initialize(id);
}

Toolbar.prototype.initialize = function(id)
{
    this.id = id;
    this.tools = [];
};

Toolbar.prototype.getToolByIndex = function(index)
{
    return this.tools[index];
};

Toolbar.prototype.add = function(tool)
{
    this.tools[this.tools.length] = tool;
};

Toolbar.prototype.draw = function()
{
    var html = this.getHtml();
    document.getElementById(this.id).innerHTML = html;
};

Toolbar.prototype.getHtml = function()
{
    var sb = [];

    sb[sb.length] = '<table border="0" cellpadding="0" cellspacing="0">';
    sb[sb.length] = '<tr>';

    // draw icons.
    $A(this.tools).each(function(toolbarItem)
    {
        sb[sb.length] = toolbarItem.getHtml();
    });

    sb[sb.length] = '</tr>';
    sb[sb.length] = '</table>';

    return sb.join("");
};


ToolbarItem = function(id)
{
    this.initialize(id);
}

ToolbarItem.itemCount = 0;
ToolbarItem.items = [];

ToolbarItem.prototype.initialize = function(id)
{
    this.id = id;
    this.index = ToolbarItem.itemCount;
    ToolbarItem.items[this.index] = this;
    ToolbarItem.itemCount++;

    this.tooltip = null;
}

ToolbarItem.prototype.onClick = function(me)
{
};

/**
 * Set the tooltip message.
 */
ToolbarItem.prototype.setTooltip = function(tooltip)
{
    this.tooltip = tooltip;
};

/**
 * Get the tooltip message.
 */
ToolbarItem.prototype.getTooltip = function()
{
    return this.tooltip;
};

ToolbarItem.prototype.getToolElId = function()
{
    return "ztbt" + this.index;
};

ToolbarItem.prototype.getToolEl = function()
{
    return document.getElementById(this.getToolElId());
}

ToolbarItem.prototype.getToolStyle = function()
{
    return this.id + " toolbaritem";
};

ToolbarItem.prototype.onMouseOver = function(event)
{
    Element.addClassName(this.getToolEl(), 'selected');

    // show the tooltip if one is configured.
    var tooltip = TooltipFactory.getTooltip();
    tooltip.setTip(this.tooltip);
    tooltip.show();
}

ToolbarItem.prototype.onMouseOut = function(event)
{
    Element.removeClassName(this.getToolEl(), 'selected');

    // hide the tooltip if one is visible.
    var tooltip = TooltipFactory.getTooltip();
    tooltip.hide();
}

ToolbarItem.prototype.getHtml = function()
{
    var getTool = 'ToolbarItem.items[\'' + this.index + '\']';

    var sb = [];
    sb[sb.length] = '<td';
    sb[sb.length] = ' id="' + this.getToolElId() + '"';
    sb[sb.length] = ' class="' + this.getToolStyle() + '"';
    sb[sb.length] = ' onclick="javascript:' + getTool + '.onClick('+getTool+');"';
    sb[sb.length] = ' onmouseover="javascript:' + getTool + '.onMouseOver('+getTool+');" ';
    sb[sb.length] = ' onmouseout="javascript:' + getTool + '.onMouseOut('+getTool+');" ';
    sb[sb.length] = '>';
    sb[sb.length] = '</td>';
    return sb.join("");
};

/**
 * Tooltip for the toolbar items.
 *
 */
TooltipFactory = new Object();

TooltipFactory._tip = null;

TooltipFactory.getTooltip = function()
{
    if (!TooltipFactory._tip)
    {
        var tip = new Tooltip('tooltip');
        TooltipFactory._tip = tip;
        Event.observe(document, "mousemove", tip.monitorMouseMovement.bindAsEventListener(tip), false);
    }
    return TooltipFactory._tip;
};

Tooltip = function(id)
{
    this.initialize(id);
};

Tooltip.prototype.initialize = function(id)
{
    this.id = id;
};

Tooltip.prototype.getTooltipEl = function()
{
    var el = document.getElementById(this.id);
    if (!el)
    {
        // create it if it does not already exist.
        el = document.createElement('div');
        el.id = this.id;
        with(el.style)
        {
            display = 'none';
            position = 'absolute';
        }
        el.innerHTML = '&nbsp;';
        document.body.appendChild(el);
    }
    return el;
};

Tooltip.prototype.setTip = function(msg)
{
    this.getTooltipEl().innerHTML = msg;
};

Tooltip.prototype.show = function()
{
    Element.show(this.getTooltipEl());
};

Tooltip.prototype.hide = function()
{
    Element.hide(this.getTooltipEl());
};

Tooltip.prototype.monitorMouseMovement = function(event)
{
    var mLoc = {"x":Event.pointerX(event), "y":Event.pointerY(event)};
    // todo: this should be the window offset so this works when the window scrollbars are out.
    var wLoc = {"x":0, "y":0};
    var offset = {"x":12, "y":8};

    var x = mLoc.x + wLoc.x + offset.x;
    var y = mLoc.y + wLoc.y + offset.y;

    Element.setStyle(this.getTooltipEl(), {"left":x + "px", "top":y + "px"})
}



