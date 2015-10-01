// dependency: ./namespace.js

(function($)
{
    var ui = kendo.ui,
        Widget = ui.Widget,
        ns = ".kendoContextPanel",
        CLICK = "click" + ns,
        ACTION = "action";

    Zutubi.admin.ContextPanel = Widget.extend({
        init: function (options)
        {
            Widget.fn.init.call(this, options);

            this.element.append("<div></div>");
            this.contentElement = this.element.children("div");
            this.linkTemplate = kendo.template(this.options.linkTemplate);
            this.actionTemplate = kendo.template(this.options.actionTemplate);

            if (options.data)
            {
                this.setData(options.path, options.data);
            }
        },

        options: {
            name: "ZaContextPanel",
            linkTemplate: '<li><a href="#= baseUrl #/#= url #"><span class="fa fa-#= icon #"></span> #: label #</a></li>',
            actionTemplate: '<li id="#= id #"><span class="fa fa-#= icon #"></span> #: label #</li>'
        },

        events: [
            ACTION
        ],

        destroy: function()
        {
            if (this.actionLists)
            {
                this.actionLists.off(ns);
            }

            if (this.panelBar)
            {
                this.panelBar.destroy();
            }
        },

        setData: function(path, data)
        {
            var that = this,
                panels = [],
                element;

            if (that.panelBar)
            {
                that.panelBar.destroy();
            }

            that.path = path;
            that.data = data;

            if (data.links && data.links.length > 0)
            {
                element = $('<div class="context-content"></div>');
                that._renderLinks(element, data.links);
                panels.push({
                    text: "links",
                    expanded: true,
                    content: element[0].outerHTML
                });
            }

            if (data.actions && (data.actions.length > 1 || data.actions[0].action !== "view"))
            {
                element = $('<div class="context-content"></div>');
                that._renderActions(element, data.actions, "action");
                panels.push({
                    text: "actions",
                    expanded: true,
                    content: element[0].outerHTML
                });
            }

            if (data.descendantActions && data.descendantActions.length > 0)
            {
                element = $('<div class="context-content"></div>');
                that._renderActions(element, data.descendantActions, "descendant");
                panels.push({
                    text: "descendant actions",
                    expanded: true,
                    content: element[0].outerHTML
                });
            }

            that.panelBar = that.contentElement.kendoPanelBar({dataSource: panels}).data("kendoPanelBar");

            that.actionLists = that.contentElement.find(".config-actions");
            that.actionLists.on(CLICK, jQuery.proxy(that._actionClicked, that));
        },

        _renderLinks: function(element, links)
        {
            var list = $('<ul class="config-links"></ul>'),
                i,
                link;

            for (i = 0; i < links.length; i++)
            {
                link = links[i];
                list.append(this.linkTemplate({
                    baseUrl: window.baseUrl,
                    icon: this._getIcon(link.name, Zutubi.admin.LINK_ICONS),
                    label: link.label,
                    url: link.url
                }));
            }

            element.append(list);
        },

        _renderActions: function(element, actions, prefix)
        {
            var list = $('<ul class="config-actions"></ul>'),
                i,
                action;

            for (i = 0; i < actions.length; i++)
            {
                action = actions[i];
                if (action.action !== "view")
                {
                    list.append(this.actionTemplate({
                        baseUrl: window.baseUrl,
                        id: prefix + "-" + action.action + "-" + i,
                        icon: this._getIcon(action.action, Zutubi.admin.ACTION_ICONS),
                        label: action.label
                    }));
                }
            }

            element.append(list);
        },

        _getIcon: function(name, icons)
        {
            if (icons.hasOwnProperty(name))
            {
                return icons[name];
            }
            else
            {
                return "circle-o";
            }
        },

        _actionClicked: function(e)
        {
            var item = $(e.target).closest("li"),
                id,
                index,
                descendant,
                action;

            if (item.length > 0)
            {
                id = item.attr("id");
                index = parseInt(id.substring(id.lastIndexOf("-") + 1));
                descendant = id.indexOf("descendant") === 0;
                action = descendant ? this.data.descendantActions[index] : this.data.actions[index];

                this.trigger(ACTION, {
                    path: this.path,
                    action: action.action,
                    argument: action.argument,
                    descendant: descendant
                });
            }
        }
    });

    ui.plugin(Zutubi.admin.ContextPanel);
}(jQuery));
