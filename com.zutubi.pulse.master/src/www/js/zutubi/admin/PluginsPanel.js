// dependency: ./namespace.js
// dependency: ./OverviewPanel.js

(function($)
{
    var ui = kendo.ui,
        TreeView = ui.TreeView,
        Observable = kendo.Observable,
        PLUGIN_SELECTED = "pluginSelected",
        ns = ".kendoZaPluginsPanel",
        CLICK = "click" + ns;

    Zutubi.admin.PluginsTree = TreeView.extend({
        init: function(element, options)
        {
            var that = this;

            that.bound = false;

            options = jQuery.extend({}, options, {
                dataBound: function(e)
                {
                    var node;

                    if (!that.bound && !e.node)
                    {
                        that.bound = true;
                        // Note even if no lay id is set we make this call (and in that case the
                        // first plugin is selected).
                        node = that.selectPlugin(that.lazyId);
                        that.trigger("select", {node: node, initialDefault: typeof that.lazyId === "undefined"});
                    }
                },
                dataSource: {
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
                        }
                    }
                }
            });

            TreeView.fn.init.call(that, element, options);
        },

        options: {
            name: "ZaPluginsTree",
            dataTextField: "name"
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
        }
    });

    ui.plugin(Zutubi.admin.PluginsTree);

    Zutubi.admin.PluginOverviewPanel = Observable.extend({
        init: function (container) {
            var that = this;

            Observable.fn.init.call(this);

            that.view = new kendo.View(
                '<div class="k-overview-panel">' +
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
        init: function(container)
        {
            var that = this;

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
                    that.contentPanel = new Zutubi.admin.PluginOverviewPanel(this.contentEl);
                    that.contentPanel.bind("click", function(e)
                    {
                        var node = that.tree.selectPlugin(e.id),
                            selectedId = node.length > 0 ? e.id : "";
                        that._pluginNodeSelected(node);
                        that.trigger(PLUGIN_SELECTED, {id: selectedId, initialDefault: false});
                    });
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
        }
    });
}(jQuery));
