// dependency: ./namespace.js
// dependency: ./InstallPluginsWindow.js

(function($)
{
    var ui = kendo.ui,
        TreeView = ui.TreeView,
        Observable = kendo.Observable,
        ACTION = "action",
        PLUGIN_SELECTED = "pluginSelected",
        ns = ".kendoZaPluginsPanel",
        CLICK = "click" + ns;

    Zutubi.admin.PluginsTree = TreeView.extend({
        init: function(element, options)
        {
            var that = this;

            options = jQuery.extend({}, options, {
                dataBound: function(e)
                {
                    var node;

                    if (!that.bound && !e.node && that.dataSource.data().length > 0)
                    {
                        that.bound = true;
                        // Note even if no lay id is set we make this call (and in that case the
                        // first plugin is selected).
                        node = that.selectPlugin(that.lazyId);
                        that.trigger("select", {node: node, initialDefault: typeof that.lazyId === "undefined"});
                        that._mask(false);
                    }
                }
            });

            TreeView.fn.init.call(that, element, options);

            this._load();
        },

        options: {
            name: "ZaPluginsTree",
            dataTextField: "name"
        },

        _mask: function(mask)
        {
            ui.progress(this.element, mask);
        },

        _load: function()
        {
            this.bound = false;
            this._mask(true);

            this.setDataSource({
                transport: {
                    read: function(options)
                    {
                        Zutubi.core.ajax({
                            url: "/api/plugins/",
                            success: options.success,
                            error: options.error
                        });
                    }
                },
                schema: {
                    model: {
                        id: "id",
                            hasChildren: false
                    },
                    parse: function(response)
                    {
                        return jQuery.map(response, function(item)
                        {
                            var cssClass = "fa fa-plug";
                            if (item.state === 'disabled' || item.state === 'disabling' || item.state === 'uninstalling')
                            {
                                cssClass += " k-plugin-fade";
                            }
                            else if (item.state === 'error')
                            {
                                cssClass = "fa fa-exclamation k-plugin-error";
                            }

                            item.spriteCssClass = cssClass;
                            return item;
                        });
                    }
                }
            });
        },

        selectPlugin: function(id)
        {
            var item, node = $();
            if (this.bound)
            {
                if (id)
                {
                    item = this.dataSource.get(id);
                    if (item)
                    {
                        node = this.findByUid(item.uid);
                    }
                }
                else
                {
                    node = this.findByUid(this.dataSource.data()[0].uid);
                }

                this.select(node);
            }
            else
            {
                this.lazyId = id;
            }

            return node;
        },

        reload: function()
        {
            var node = this.select();
            if (node.length > 0)
            {
                this.lazyId = this.dataItem(node).id;
            }

            this._load();
        }
    });

    ui.plugin(Zutubi.admin.PluginsTree);

    Zutubi.admin.PluginOverviewPanel = Observable.extend({
        init: function (container, showActions) {
            var that = this;

            that.showActions = showActions;
            Observable.fn.init.call(this);

            that.view = new kendo.View(
                '<div class="k-overview-panel k-plugin-overview">' +
                    '<h1></h1>' +
                    '<table></table>' +
                '</div>', {
                    wrap: false,
                    evalTemplate: false
                });

            that.view.render(container);
            that.rowTemplate = kendo.template('<tr><th>#: heading #</span></th><td></td>');
            that.titleEl = that.view.element.find("h1");
            that.tableEl = that.view.element.find("table");
        },

        events: [
            ACTION
        ],

        _detach: function ()
        {
            this.view.element.find(".k-plugin-handles a").off(ns);
        },

        destroy: function()
        {
            this._detach();
            this.view.destroy();
        },

        _addRow: function(heading, content, tableEl)
        {
            var rowEl = $(this.rowTemplate({heading: heading})).appendTo(tableEl || this.tableEl),
                cellEl = rowEl.children("td");
            if (typeof content === "function")
            {
                content(cellEl);
            }
            else
            {
                cellEl.html(content);
            }
        },

        _addSimpleRow: function(heading, key, defaultContent)
        {
            var content;

            key = key || heading;
            content = this.plugin[key] || defaultContent;

            if (content)
            {
                this._addRow(heading, kendo.htmlEncode(content));
            }
        },

        _addSubtable: function(cellEl)
        {
            var tableEl = $('<table></table>');
            cellEl.addClass("k-summary-wrapper");
            cellEl.append(tableEl);
            return tableEl;
        },

        renderPlugin: function(plugin)
        {
            this.plugin = plugin;
            this._detach();
            this.tableEl.empty();

            this.titleEl.html(kendo.htmlEncode(plugin.name));

            this._addSimpleRow("description");
            this._addRow("state", this._renderState());
            if (this.plugin.errorMessages && this.plugin.errorMessages.length > 0)
            {
                this._addRow("error messages", this._renderErrors());
            }

            this._addSimpleRow("id");
            this._addSimpleRow("vendor");
            this._addSimpleRow("version");
            if (this.plugin.requirements && this.plugin.requirements.length > 0)
            {
                this._addRow("depends on", jQuery.proxy(this._renderHandles, this, this.plugin.requirements));
            }
            if (this.plugin.dependents && this.plugin.dependents.length > 0)
            {
                this._addRow("required by", jQuery.proxy(this._renderHandles, this, this.plugin.dependents));
            }

            if (this.showActions && this.plugin.actions && this.plugin.actions.length > 0)
            {
                this._addRow("actions", jQuery.proxy(this._renderActions, this));
            }
        },

        _renderState: function()
        {
            var icons = {
                enabled: 'check-circle',
                disabled: 'stop-circle',
                error: 'exclamation-circle',
                installing: 'arrow-circle-down',
                uninstalling: 'minus-circle',
                disabling: 'pause-circle',
                upgrading: 'arrow-circle-up'
            };

            return '<span class="fa fa-' + icons[this.plugin.state] + '"></span> ' + this.plugin.state;
        },

        _renderActions: function(cellEl)
        {
            if (this._actionAvailable('enable'))
            {
                this._appendActionButton(cellEl, 'enable', 'fa fa-check-circle');
            }
            if (this._actionAvailable('disable'))
            {
                this._appendActionButton(cellEl, 'disable', 'fa fa-pause-circle');
            }
            if (this._actionAvailable('uninstall'))
            {
                this._appendActionButton(cellEl, 'uninstall', 'fa fa-minus-circle');
            }
        },

        _actionAvailable: function(action)
        {
            return this.showActions && this.plugin.actions && this.plugin.actions.indexOf(action) >= 0;
        },

        _appendActionButton: function(container, action, cssClass)
        {
            var button = $('<button type="button"> ' + action + '</button>').appendTo(container);
            button.kendoButton({
                spriteCssClass: cssClass,
                click: jQuery.proxy(this._actionClicked, this, action)
            });
            return button;
        },

        _actionClicked: function(action)
        {
            this.trigger(ACTION, {id: this.plugin.id, action: action});
        },

        _renderErrors: function()
        {
            var errors = this.plugin.errorMessages,
                i,
                list = $('<ul class="k-plugin-errors"></ul>');

            for (i = 0; i < errors.length; i++)
            {
                list.append('<li>' + kendo.htmlEncode(errors[i]) + '</li>');
            }

            return list[0].outerHTML;
        },

        _renderHandles: function(handles, cellEl)
        {
            var tableEl = this._addSubtable(cellEl),
                i,
                handle;

            tableEl.addClass("k-plugin-handles");

            for (i = 0; i < handles.length; i++)
            {
                handle = handles[i];
                if (handle.available && handle.name)
                {
                    tableEl.append('<tr><td><a>' + kendo.htmlEncode(handle.name) + '</a></td><td>' + handle.version + '</td></tr>');
                }
                else
                {
                    tableEl.append('<tr><td>' + kendo.htmlEncode(handle.id) + '</td><td>' + handle.version + '</td></tr>');
                }
            }

            tableEl.find("a").on(CLICK, jQuery.proxy(this._linkClicked, this, handles));
        },

        _linkClicked: function(handles, e)
        {
            var name = $(e.target).text(),
                i,
                id;

            for (i = 0; i < handles.length; i++)
            {
                if (handles[i].name === name)
                {
                    id = handles[i].id;
                    break;
                }
            }

            if (id)
            {
                this.trigger("click", {id: id});
            }
        }
    });

    Zutubi.admin.PluginsPanel = Observable.extend({
        init: function(container, showActions)
        {
            var that = this;

            that.showActions = showActions;

            Observable.fn.init.call(this);

            that.view = new kendo.View(
                '<div class="k-outer-split" style="height: 100%; width: 100%">' +
                    '<div>' +
                        '<div class="k-plugins-tree k-pane-content">' +
                        '</div>' +
                    '</div>' +
                    '<div>' +
                        '<div class="k-center-pane-content k-pane-content">' +
                        '</div>' +
                    '</div>' +
                '</div>', {wrap: false});

            that.view.render(container);

            that.view.element.kendoSplitter({
                panes: [
                    {collapsible: true, size: "350px"},
                    {collapsible: false}
                ]
            });

            that.tree = that.view.element.find(".k-plugins-tree").kendoZaPluginsTree().data("kendoZaPluginsTree");
            that.tree.bind("select", function(e)
            {
                var plugin = that._pluginNodeSelected(e.node);
                that.trigger(PLUGIN_SELECTED, {id: plugin ? plugin.id : "", initialDefault: e.initialDefault});
            });

            that.contentEl = that.view.element.find(".k-center-pane-content");
        },

        events: [
            PLUGIN_SELECTED
        ],

        destroy: function()
        {
            this.view.destroy();
        },

        setId: function(id)
        {
            this._pluginNodeSelected(this.tree.selectPlugin(id));
        },

        _mask: function(mask)
        {
            ui.progress(this.view.element, mask);
        },

        _pluginNodeSelected: function(node)
        {
            var that = this,
                plugin = null;

            if (node)
            {
                plugin = that.tree.dataItem(node);
            }

            if (plugin)
            {
                if (!that.contentPanel)
                {
                    that.contentPanel = new Zutubi.admin.PluginOverviewPanel(this.contentEl, this.showActions);
                    that.contentPanel.bind("action", jQuery.proxy(that._pluginAction, that));
                    that.contentPanel.bind("click", jQuery.proxy(that._pluginClick, that));
                }

                that.contentPanel.renderPlugin(plugin);
            }
            else
            {
                if (that.contentPanel)
                {
                    that.contentPanel.destroy();
                    delete that.contentPanel;
                }
                that.contentEl.empty();
            }

            return plugin;
        },

        _pluginClick: function(e)
        {
            var node = this.tree.selectPlugin(e.id),
                selectedId = node.length > 0 ? e.id : "";
            this._pluginNodeSelected(node);
            this.trigger(PLUGIN_SELECTED, {id: selectedId, initialDefault: false});
        },

        _pluginAction: function(e, confirmed)
        {
            var that = this,
                dialog,
                url = "/api/plugins/" + Zutubi.config.encodePath(e.id),
                method = "POST";

            if (e.action === "disable" || e.action === "uninstall")
            {
                if (typeof confirmed === "undefined")
                {
                    dialog = new Zutubi.core.PromptWindow({
                        title: "confirm action",
                        messageHTML: "Are you sure you wish to " + e.action + " this plugin?",
                        select: jQuery.proxy(that._pluginAction, that, e)
                    });
                    dialog.show();
                    return;
                }
                else if (!confirmed)
                {
                    return;
                }
            }

            if (e.action === "uninstall")
            {
                method = "DELETE";
            }
            else
            {
                url += "/" + e.action;
            }

            that._mask(true);
            Zutubi.core.ajax({
                url: url,
                method: method,
                success: function()
                {
                    that._mask(false);
                    that.tree.reload();
                },
                error: function(jqXHR)
                {
                    that._mask(false);
                    Zutubi.core.reportError("Could not " + e.action + " plugin: " + Zutubi.core.ajaxError(jqXHR));
                }
            });
        },

        showInstallWindow: function()
        {
            var that = this,
                piw = new Zutubi.admin.InstallPluginsWindow();

            piw.bind("installed", function(e)
            {
                e.sender.close();
                that.tree.reload();
                that.tree.selectPlugin(e.plugin.id);
                that.trigger(PLUGIN_SELECTED, {id: e.plugin.id, initialDefault: false});
            });
            piw.show();
        }
    });
}(jQuery));
