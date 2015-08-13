// dependency: ./namespace.js

(function($)
{
    var ui = kendo.ui,
        TreeView = ui.TreeView,
        PATHSELECT = "pathselect";

    Zutubi.admin.ConfigTree = TreeView.extend({
        init: function(element, options)
        {
            TreeView.fn.init.call(this, element, options);

            this.bound = false;

            if (options && options.rootPath)
            {
                this.setRootPath(options.rootPath);
            }
        },

        events: [
            // FIXME kendo: to subscribe to our own select event we need to have this here, is there a better way?
            "dataBound",
            "select",
            PATHSELECT
        ],

        options: {
            name: "ZaConfigTree",
            dataTextField: "label",
            loadOnDemand: false,
            dataBound: function(e)
            {
                // This callback is invoked for every level, but only once with a null node.
                if (!e.node)
                {
                    this.bound = true;
                    this.selectConfigNode();
                }
            },
            select: function(e)
            {
                var that = this;
                that.trigger(PATHSELECT, {path: that.configPathForNode(e.node)});
            }
        },

        configPathForNode: function(node)
        {
            var nodeData = this.dataItem(node),
                path = nodeData.key,
                parent = this.parent(node);

            if (parent.length)
            {
                path = this.configPathForNode(parent) + '/' + path;
            }
            else
            {
                path = this.rootPath;
            }

            return path;
        },

        setRootPath: function(rootPath)
        {
            var dataSource;

            if (this.rootPath && this.rootPath === rootPath)
            {
                return;
            }

            this.rootPath = rootPath;
            this.bound = false;

            dataSource = new kendo.data.HierarchicalDataSource({
                transport: {
                    read: {
                        url: window.baseUrl + "/api/config/" + rootPath + "?depth=-1&filter=nested",
                            dataType: "json",
                            headers: {
                                Accept: "application/json; charset=utf-8",
                                    "Content-Type": "application/json; charset=utf-8"
                        }
                    }
                },
                schema: {
                    model: {
                        children: "nested"
                    }
                }
            });

            dataSource.bind('error', function(e) {
                zaReportError('Could not load configuration tree: ' + zaAjaxError(e.xhr));
            });

            this.setDataSource(dataSource);
        },

        selectConfigNode: function()
        {
            var that = this,
                root = that.wrapper.find(".k-item:first"),
                dataItem = that.dataItem(root),
                keys = [],
                i, j,
                key,
                children;

            if (this.configPath)
            {
                keys = this.configPath.split("/");
            }

            for (i = 0; i < keys.length; i++)
            {
                key = keys[i];

                children = dataItem.children.data();
                for (j = 0; j < children.length; j++)
                {
                    if (children[j].key === key)
                    {
                        break;
                    }
                }

                if (j === children.length)
                {
                    break;
                }

                dataItem = children[j];
            }

            if (dataItem)
            {
                that.expandTo(dataItem);
                that.select(that.findByUid(dataItem.uid));
            }
            else
            {
                that.select(root);
            }
        },

        selectConfig: function(configPath)
        {
            this.configPath = configPath;
            if (this.bound)
            {
                this.selectConfigNode();
            }
        }
    });

    ui.plugin(Zutubi.admin.ConfigTree);
}(jQuery));
