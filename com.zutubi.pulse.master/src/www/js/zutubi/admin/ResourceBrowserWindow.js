// dependency: ./namespace.js

(function($)
{
    var ui = kendo.ui,
        TreeView = ui.TreeView,
        Observable = kendo.Observable;

    Zutubi.admin.ResourceTree = TreeView.extend({
        init: function(element)
        {
            TreeView.fn.init.call(this, element, {
                dataTextField: "name",
                loadOnDemand: false,
                scrollable: false,
                dataSource: {
                    transport: {
                        read: {
                            url: window.baseUrl + "/api/resources/",
                            dataType: "json",
                            headers: {
                                Accept: "application/json; charset=utf-8",
                                "Content-Type": "application/json; charset=utf-8"
                            }
                        }
                    },
                    schema: {
                        model: {
                            children: "versions",
                            id: "id"
                        },
                        parse: function(response) {
                            return jQuery.map(response, function(item)
                            {
                                var i, version;
                                if (item.hasOwnProperty("versions"))
                                {
                                    item.id = item.name;
                                    for (i = 0; i < item.versions.length; i++)
                                    {
                                        version = item.versions[i];
                                        version.name = version.versionId || "[default]";
                                        version.id = item.name + "/" + version.versionId;
                                    }
                                }
                                return item;
                            });
                        }
                    }
                }
            });
        },

        options: {
            name: "ZaResourceTree"
        }
    });

    ui.plugin(Zutubi.admin.ResourceTree);


    Zutubi.admin.ResourceBrowserWindow = Observable.extend({
        // options: {
        //    resourceField: target text field for the resource name
        //    defaultVersionField: target checkbox for resource default version
        //    versionField: target text field for the resource version
        // }
        init: function (options)
        {
            var that = this;

            that.options = jQuery.extend({}, that.options, options);

            Observable.fn.init.call(that);

            that.view = new kendo.View(
                '<div>' +
                    '<div class="k-resource-window-content">' +
                    '<div class="k-resource-splitter" style="width: 100%; height: 100%">' +
                        '<div class="k-resource-window-pane">' +
                            '<div class="k-resource-tree">' +
                            '</div>' +
                        '</div>' +
                        '<div class="k-resource-window-pane">' +
                            '<div class="k-resource-window-agents"></div>' +
                        '</div>' +
                    '</div>' +
                    '</div>' +
                    '<div class="k-resource-window-actions"></div>' +
                '</div>',
                {
                    wrap: false,
                    evalTemplate: false
                });

            that.element = that.view.render("body");
            that.tree = that.element.find(".k-resource-tree").kendoZaResourceTree().data("kendoZaResourceTree");
            that.tree.bind("dataBound", function(e)
            {
                // This callback is invoked for every level, but only once with a null node.
                if (!!e.node)
                {
                    that._selectExistingVersion();
                }
            });
            that.tree.bind("select", jQuery.proxy(that._nodeSelected, that));

            that.agentsElement = that.element.find(".k-resource-window-agents");

            that.buttonTemplate = kendo.template(that.options.buttonTemplate);
            that._renderButtons();
        },

        options: {
            title: "select resource",
            buttonTemplate: '<button><span class="k-sprite"></span> #: label #</button>',
            width: 500,
            height: 400
        },

        _renderButtons: function()
        {
            var that = this,
                parentElement = that.element.find(".k-resource-window-actions"),
                buttonElement;

            buttonElement = $(that.buttonTemplate({label: "ok"}));
            parentElement.append(buttonElement);
            that.okButton = buttonElement.kendoButton({
                enable: false,
                spriteCssClass: "fa fa-check-circle",
                click: jQuery.proxy(that._buttonClicked, that, true)
            }).data("kendoButton");

            buttonElement = $(that.buttonTemplate({label: "cancel"}));
            parentElement.append(buttonElement);
            buttonElement.kendoButton({
                spriteCssClass: "fa fa-times-circle",
                click: jQuery.proxy(that._buttonClicked, that, false)
            });
        },

        _selectExistingVersion: function()
        {
            var resource = this.options.resourceField.getValue(),
                version,
                dataSource,
                resourceItem,
                versionItem,
                node;

            if (resource)
            {
                version = this.options.defaultVersionField.getValue() ? "" : this.options.versionField.getValue();
                dataSource = this.tree.dataSource;
                resourceItem = dataSource.get(resource);
                if (resourceItem)
                {
                    resourceItem.set("expanded", true);

                    versionItem = dataSource.get(resource + "/" + version);
                    if (versionItem)
                    {
                        node = this.tree.findByUid(versionItem.uid);
                    }
                    else
                    {
                        node = this.tree.findByUid(resourceItem.uid);
                    }
                    this.tree.select(node);
                    this.tree.trigger("select", {node: node});
                }
            }
        },

        _nodeSelected: function(e)
        {
            var item;

            this.okButton.enable(e.node);
            this.agentsElement.empty();

            if (e.node)
            {
                item = this.tree.dataItem(e.node);
                if (item)
                {
                    this.agentsElement.html(this._renderAgents(item));
                }
            }
        },

        _renderAgents: function(item)
        {
            var name, html, i, plural = "";

            if (!item.hasOwnProperty("versionId"))
            {
                item = item.children.data()[0];
            }

            name = kendo.htmlEncode(item.name);
            if (item.agents && item.agents.length > 0)
            {
                if (item.agents.length > 1)
                {
                    plural = "s";
                }

                html = '<p>Version <strong>' + name + '</strong> is available on ' + item.agents.length + ' agent' + plural + ':</p><ul class="k-simple-value-list">';
                for (i = 0; i < item.agents.length; i++)
                {
                    html += '<li>' + kendo.htmlEncode(item.agents[i]) + '</li>';
                }
                html += '</ul>';
            }
            else
            {
                html = "Version " + name + " is not available on any agent.";
            }

            return html;
        },

        _mask: function(mask)
        {
            kendo.ui.progress(this.element, mask);
        },

        _buttonClicked: function(ok)
        {
            var item,
                resource,
                version;

            this.window.close();
            if (ok)
            {
                item = this.tree.dataItem(this.tree.select());
                if (item.hasOwnProperty("versionId"))
                {
                    resource = item.parentNode().name;
                    version = item.versionId;
                }
                else
                {
                    resource = item.name;
                    version = "";
                }

                this.options.resourceField.bindValue(resource);
                if (version)
                {
                    this.options.defaultVersionField.bindValue(false);
                    this.options.versionField.bindValue(version);
                }
                else
                {
                    this.options.defaultVersionField.bindValue(true);
                }

                this.options.resourceField.options.parentForm.updateButtons();
            }
        },

        show: function()
        {
            var that = this,
                windowWidth = $(window).width(),
                maxWidth = windowWidth - 80,
                width = Math.min(that.options.width, maxWidth),
                maxHeight = $(window).height() - 80;

            that.window = $(that.element).kendoWindow({
                position: {
                    top: 40,
                    left: (windowWidth - width) / 2
                },
                width: width,
                height: Math.min(that.options.height, maxHeight),
                minWidth: 300,
                maxWidth: maxWidth,
                minHeight: 200,
                maxHeight: maxHeight,
                modal: true,
                title: that.options.title,
                activate: function()
                {
                    that.element.find(".k-resource-splitter").kendoSplitter({
                        orientation: "horizontal",
                        panes: [
                            { collapsible: false, size: 200 },
                            { collapsible: false }
                        ]
                    });
                },
                deactivate: function()
                {
                    that.window.destroy();
                }
            }).data("kendoWindow");

            that.window.open();
        },

        close: function()
        {
            this.window.close();
        }
    });
}(jQuery));
