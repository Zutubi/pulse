(function() {
    var kendo = window.kendo,
        ui = kendo.ui,
        TreeView = ui.TreeView;

    var NimdaTreeView = TreeView.extend({
        init: function(element, options) {
            var that = this;

            TreeView.prototype.init.call(that, element, options);
        },

        options: {
            name: "NimdaTreeView"
        },

        setRootPath: function(path) {
            this.setDataSource(new kendo.data.HierarchicalDataSource({
                transport: {
                    read: {
                        url: window.baseUrl + "/ajax/nimda/ls?path=" + path,
                        dataType: "json"
                    }
                },
                schema: {
                    model: {
                        id: 'path'
                    }
                }
            }));
        }
    });

    ui.plugin(NimdaTreeView);
})(jQuery);