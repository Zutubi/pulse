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

            if (data)
            {
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

                that.actions = that._filteredActions(data.actions);
                if (that.actions.length > 0)
                {
                    that.actions.sort(Zutubi.admin.labelCompare);

                    element = $('<div class="context-content"></div>');
                    that._renderActions(element, that.actions, "action");
                    panels.push({
                        text: "actions",
                        expanded: true,
                        content: element[0].outerHTML
                    });
                }

                if (data.descendantActions && data.descendantActions.length > 0)
                {
                    data.descendantActions.sort(_actionCompare);

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
            }
            else
            {
                that.contentElement.html("");
            }
        },

        _filteredActions: function(actions)
        {
            if (!actions)
            {
                return [];
            }

            return jQuery.grep(actions, function(action)
            {
                return action.action !== "view" && action.action !== "write";
            });
        },

        beginNavigation: function()
        {
            this.setData(null);
            kendo.ui.progress(this.contentElement, true);
        },

        endNavigation: function(error)
        {
            // Note we ignore the error if present, assuming another panel will display it.
            kendo.ui.progress(this.contentElement, false);
        },

        applyDelta: function(delta)
        {
            if (delta.renamedPaths && delta.renamedPaths.hasOwnProperty(this.path))
            {
                this.path = delta.renamedPaths[this.path];
            }
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
                list.append(this.actionTemplate({
                    baseUrl: window.baseUrl,
                    id: prefix + "-" + action.action + "-" + i,
                    icon: this._getIcon(action.action, Zutubi.admin.ACTION_ICONS),
                    label: action.label
                }));
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
                index = parseInt(id.substring(id.lastIndexOf("-") + 1), 10);
                descendant = id.indexOf("descendant") === 0;
                action = descendant ? this.data.descendantActions[index] : this.actions[index];

                this.trigger(ACTION, {
                    path: this.path,
                    action: action,
                    descendant: descendant
                });
            }
        }
    });

    ui.plugin(Zutubi.admin.ContextPanel);
}(jQuery));
