// dependency: ./namespace.js
// dependency: zutubi/config/package.js

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

            Zutubi.admin.registerUnloadListener(this._beforeUnload, this);

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
            this._saveState();

            if (this.actionLists)
            {
                this.actionLists.off(ns);
            }

            if (this.panelBar)
            {
                this.panelBar.destroy();
            }
        },

        _beforeUnload: function()
        {
            this._saveState();
        },

        _stateKey: function()
        {
            return this.options.name;
        },

        _loadState: function()
        {
            var stateString = localStorage[this._stateKey()];
            return stateString ? JSON.parse(stateString) : {};
        },

        _saveState: function()
        {
            var that = this,
                collapsedPanels = this._loadState(),
                text;

            if (that.panelBar)
            {
                that.panelBar.element.find("li.k-item").each(function()
                {
                    text = $(this).find(".k-header").text();
                    if ($(this).attr("aria-expanded") === "true")
                    {
                        delete collapsedPanels[text];
                    }
                    else
                    {
                        collapsedPanels[text] = true;
                    }
                });

                localStorage[that._stateKey()] = JSON.stringify(collapsedPanels);
            }
        },

        _createContentElement: function ()
        {
            return $('<div class="k-context-content"></div>');
        },

        setData: function(path, data)
        {
            var that = this,
                collapsedPanels,
                panels = [],
                element,
                docs,
                content;

            if (that.panelBar)
            {
                that._saveState();
                that.panelBar.destroy();
                that.panelBar = null;
            }

            that.path = path;
            that.data = data;

            if (data)
            {
                collapsedPanels = that._loadState();

                if (data.links && data.links.length > 0)
                {
                    element = this._createContentElement();
                    that._renderLinks(element, data.links);
                    panels.push({
                        text: "links",
                        expanded: !collapsedPanels.links,
                        content: element[0].outerHTML
                    });
                }

                that.actions = that._filteredActions(data.actions);
                if (that.actions.length > 0)
                {
                    that.actions.sort(Zutubi.admin.labelCompare);

                    element = this._createContentElement();
                    that._renderActions(element, that.actions, "action");
                    panels.push({
                        text: "actions",
                        expanded: !collapsedPanels.actions,
                        content: element[0].outerHTML
                    });
                }

                if (data.descendantActions && data.descendantActions.length > 0)
                {
                    data.descendantActions.sort(Zutubi.admin.labelCompare);

                    element = this._createContentElement();
                    that._renderActions(element, data.descendantActions, "descendant");
                    panels.push({
                        text: "descendant actions",
                        expanded: !collapsedPanels["descendant actions"],
                        content: element[0].outerHTML
                    });
                }

                if (data.kind === "composite")
                {
                    docs = data.type.docs;
                }
                else if (data.kind === "collection")
                {
                    docs = data.type.targetType.docs;
                }

                if (docs)
                {
                    content = docs.verbose || docs.brief;
                    if (content)
                    {
                        element = this._createContentElement();
                        element.addClass("k-builtin-help");
                        element.append(content);
                        panels.push({
                            text: "documentation",
                            expanded: !collapsedPanels.documentation,
                            content: element[0].outerHTML
                        });
                    }
                }

                that.panelBar = that.contentElement.kendoPanelBar({dataSource: panels}).data("kendoPanelBar");

                that.actionLists = that.contentElement.find(".k-config-actions");
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
            var list = $('<ul class="k-config-links"></ul>'),
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
            var list = $('<ul class="k-config-actions"></ul>'),
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
