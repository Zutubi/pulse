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
}

ToolbarItem.prototype.onClick = function(me)
{
};

ToolbarItem.prototype.getToolElId = function()
{
    return "ztbt" + this.index;
};

ToolbarItem.prototype.getToolStyle = function()
{
    return this.id + " tool";
};

ToolbarItem.prototype.getHtml = function()
{
    var getTool = 'ToolbarItem.items[\'' + this.index + '\']';
    var getToolEl = 'document.getElementById(\'' + this.getToolElId() + '\')';

    var sb = [];
    sb[sb.length] = '<td';
    sb[sb.length] = ' id="' + this.getToolElId() + '"';
    sb[sb.length] = ' class="' + this.getToolStyle() + '"';
    sb[sb.length] = ' onclick="javascript:' + getTool + '.onClick('+getTool+')"';

    sb[sb.length] = ' onmouseover="javascript:Element.addClassName(' + getToolEl + ', \'selected\');" ';
    sb[sb.length] = ' onmouseout="javascript:Element.removeClassName(' + getToolEl + ', \'selected\');" ';
    sb[sb.length] = '>';
    sb[sb.length] = '</td>';
    return sb.join("");
};
