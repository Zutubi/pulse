// dependency: ./namespace.js

(function($) {
    var ui = kendo.ui,
        TreeView = ui.TreeView,
        PATHSELECT = "pathselect";

    Zutubi.admin.ConfigTree = TreeView.extend({
        init: function(element, options) {
            TreeView.fn.init.call(this, element, options);

            if (options && options.rootPath) {
                this.setRootPath(options.rootPath);
            }
        },

        events: [
            // FIXME kendo: to subscribe to our own select event we need to have this here, is there a better way?
            "select",
            PATHSELECT
        ],

        options: {
            name: "ZaConfigTree",
            dataTextField: "label",
            select: function(e) {
                var that = this;
                that.trigger(PATHSELECT, {path: that.configPathForNode(e.node)});
            }
        },

        configPathForNode: function(node) {
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

        setRootPath: function(rootPath) {
            var dataSource;

            if (this.rootPath && this.rootPath === rootPath)
            {
                return;
            }

            this.rootPath = rootPath;

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

        selectConfig: function(configPath) {
            //var rootNode = this.element.find("li");
            //console.dir(rootNode);
            if (configPath)
            {
                console.log('now need to select "' + configPath + '"');
            }
            else
            {
                console.log('now need to select root');
            }
        }
    });

    ui.plugin(Zutubi.admin.ConfigTree);
}(jQuery));
