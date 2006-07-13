ZUTUBI.widget.Toolbar = function(id)
{
    this.initialize(id);
}

ZUTUBI.widget.Toolbar.prototype = {

    initialize: function(id)
    {
        this.id = id;
        this.tools = [];
    },

    getToolByIndex: function(index)
    {
        return this.tools[index];
    },

    add: function(tool)
    {
        this.tools[this.tools.length] = tool;
    },

    draw: function()
    {
        var html = this.getHtml();
        document.getElementById(this.id).innerHTML = html;
    },

    getElId: function()
    {
        return 'zttb';
    },

    getHtml: function()
    {
        var sb = [];

        sb[sb.length] = '<div id="'+this.getElId()+'">';

        // draw icons.
        $A(this.tools).each(function(toolbarItem)
        {
            sb[sb.length] = toolbarItem.getHtml();
        });

        sb[sb.length] = '</div>';

        return sb.join("");
    }
};

/**
 *
 *
 */
ZUTUBI.widget.ToolbarItem = function(id)
{
    this.initialize(id);
}

ZUTUBI.widget.ToolbarItem.itemCount = 0;
ZUTUBI.widget.ToolbarItem.items = [];

ZUTUBI.widget.ToolbarItem.prototype = {

    enabled:true,

    initialize: function(id)
    {
        this.id = id;
        this.index = ZUTUBI.widget.ToolbarItem.itemCount;
        ZUTUBI.widget.ToolbarItem.items[this.index] = this;
        ZUTUBI.widget.ToolbarItem.itemCount++;

        this.tooltip = null;
    },

    onClick: function(me)
    {
    },

    setEnabled: function(b)
    {
        this.enabled = (b == true);
    },

    isEnabled: function()
    {
        return this.enabled;
    },

    /**
     * Set the tooltip message.
     */
    setTooltip: function(tooltip)
    {
        this.tooltip = tooltip;
    },

    /**
     * Get the tooltip message.
     */
    getTooltip: function()
    {
        return this.tooltip;
    },

    getToolElId: function()
    {
        return "zttbi" + this.index;
    },

    getToolEl: function()
    {
        return document.getElementById(this.getToolElId());
    },

    getToolStyle: function()
    {
        var style = "zttbi" + (this.isEnabled() ? "" : "_d");
        if (this.id)
        {
            style = style + " " + this.id;
        }
        return style;
    },

    onMouseOver: function(event)
    {
        if (!this.isEnabled())
        {
            return;
        }

        Element.addClassName(this.getToolEl(), 'selected');

        // show the tooltip if one is configured.
        var tooltip = ZUTUBI.widget.TooltipFactory.getTooltip();
        if (this.tooltip)
        {
            tooltip.setTip(this.tooltip);
            tooltip.show();
        }
    },

    onMouseOut: function(event)
    {
        if (!this.isEnabled())
        {
            return;
        }

        Element.removeClassName(this.getToolEl(), 'selected');

        // hide the tooltip if one is visible.
        var tooltip = ZUTUBI.widget.TooltipFactory.getTooltip();
        tooltip.hide();
    },

    _onClick: function(node)
    {
        if (this.isEnabled())
        {
            this.onClick(this);
        }
    },

    getHtml: function()
    {
        var getTool = 'ZUTUBI.widget.ToolbarItem.items[\'' + this.index + '\']';

        var sb = [];
        sb[sb.length] = '<div';
        sb[sb.length] = ' id="' + this.getToolElId() + '"';
        sb[sb.length] = ' class="' + this.getToolStyle() + '"';
        sb[sb.length] = ' onclick="javascript:' + getTool + '.onClick();"';
        sb[sb.length] = ' onmouseover="javascript:' + getTool + '.onMouseOver('+getTool+');" ';
        sb[sb.length] = ' onmouseout="javascript:' + getTool + '.onMouseOut('+getTool+');" ';
        sb[sb.length] = '>';
        sb[sb.length] = '</div>';
        return sb.join("");
    }
};

/**
 * A toolbar separator is an inactive component in a toolbar used as a
 * separator between groups of related toolbar items.
 *
 * The toolbar separator does not reactive to mouse or lick events.
 */
ZUTUBI.widget.ToolbarSeparator = function()
{
    this.initialize();
}

ZUTUBI.widget.ToolbarSeparator.prototype = {

    initialize: function()
    {

    },

    getToolStyle: function()
    {
        return "zttbs";
    },

    getHtml: function()
    {
        var sb = [];
        sb[sb.length] = '<td';
        sb[sb.length] = ' class="' + this.getToolStyle() + '"';
        sb[sb.length] = '>';
        sb[sb.length] = '</td>';
        return sb.join("");
    }
};


/**
 * Tooltip for the toolbar items.
 *
 */
ZUTUBI.widget.TooltipFactory = {};

ZUTUBI.widget.TooltipFactory._tip = null;

ZUTUBI.widget.TooltipFactory.getTooltip = function()
{
    if (!ZUTUBI.widget.TooltipFactory._tip)
    {
        var tip = new ZUTUBI.widget.Tooltip('tooltip');
        ZUTUBI.widget.TooltipFactory._tip = tip;
        Event.observe(document, "mousemove", tip.monitorMouseMovement.bindAsEventListener(tip), false);
    }
    return ZUTUBI.widget.TooltipFactory._tip;
};

/**
 * Tooltip.
 *
 */
ZUTUBI.widget.Tooltip = function(id)
{
    this.initialize(id);
};

ZUTUBI.widget.Tooltip.prototype = {

    initialize: function(id)
    {
        this.id = id;
    },

    getTooltipEl: function()
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
    },

    setTip: function(msg)
    {
        this.getTooltipEl().innerHTML = msg;
    },

    show: function()
    {
        Element.show(this.getTooltipEl());
    },

    hide: function()
    {
        Element.hide(this.getTooltipEl());
    },

    monitorMouseMovement: function(event)
    {
        var mLoc = {"x":Event.pointerX(event), "y":Event.pointerY(event)};
        // todo: this should be the window offset so this works when the window scrollbars are out.
        var wLoc = {"x":0, "y":0};
        var offset = {"x":12, "y":8};

        var x = mLoc.x + wLoc.x + offset.x;
        var y = mLoc.y + wLoc.y + offset.y;

        Element.setStyle(this.getTooltipEl(), {"left":x + "px", "top":y + "px"})
    }
};