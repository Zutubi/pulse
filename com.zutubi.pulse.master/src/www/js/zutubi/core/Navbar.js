// dependency: ./namespace.js

(function($)
{
    var ui = kendo.ui,
        Widget = ui.Widget,
        ns = ".kendoZaNavbar",
        CLICK = "click" + ns,
        SELECT = "select";

    Zutubi.core.NavbarItem = Widget.extend({
        init: function(element, options)
        {
            var that = this;

            Widget.fn.init.call(this, element, options);

            that.template = kendo.template(that.options.template);
            that.create();
        },

        options: {
            name: "ZaNavbarItem",
            template: '<a href="#= url #">#= content #</a>'
        },

        create: function()
        {
            var model = this.options.model;

            this.innerElement = $(this.template(model));
            this.element.append(this.innerElement);
            if (typeof model.click !== "undefined")
            {
                this.innerElement.on(CLICK, jQuery.proxy(model.click, this));
            }
        },

        destroy: function()
        {
            this.innerElement.off(ns);
            Widget.fn.destroy.call(this);
        },

        hide: function()
        {
            this.element.hide();
        },

        show: function()
        {
            this.element.show();
        },

        applyDelta: function(delta)
        {
            // Default does nothing.
        }
    });

    Zutubi.core.MenuNavbarItem = Zutubi.core.NavbarItem.extend({
        options: {
            name: "ZaMenuNavbarItem",
            itemTemplate: '<li>#: text #</li>',
            urlItemTemplate: '<li><a href="#= url #">#: text #</a></li>'
        },

        events: [
            SELECT
        ],

        create: function()
        {
            var that = this;

            that.outer = $('<div class="k-split-container"></div>');
            that.mainButton = $('<a class="k-split-button"></a>');
            that.outer.append(that.mainButton);
            that.arrowButton = $('<a class="k-split-button-arrow"><span class="fa fa-caret-down"></span></a>');
            that.outer.append(that.arrowButton);

            that.element.append(that.outer);

            that.popup = that.createPopup();

            that.mainButton.on(CLICK, jQuery.proxy(that._clicked, that));
            that.arrowButton.on(CLICK, jQuery.proxy(that._clicked, that));
        },

        destroy: function()
        {
            this.mainButton.off(ns);
            this.arrowButton.off(ns);
            if (this.popupEl)
            {
                this.popupEl.off(ns);
            }
            Zutubi.core.NavbarItem.fn.destroy.call(this);
        },

        createPopup: function()
        {
            var that = this,
                i,
                items = that.options.items,
                urls = that.options.urls,
                template = kendo.template(urls ? this.options.urlItemTemplate : this.options.itemTemplate),
                popup;

            that.popupEl = $("<ul class='k-selector-popup'></ul>");
            for (i = 0; i < items.length; i++)
            {
                that.popupEl.append(template({
                    text: items[i],
                    url: urls ? (window.baseUrl + urls[i]) : ""
                }));
            }

            popup = that.popupEl.kendoPopup({
                anchor: that.element.closest(".k-navitem"),
                origin: "bottom left",
                position: "top left"
            }).data("kendoPopup");

            if (!urls)
            {
                that.popupEl.on(CLICK, jQuery.proxy(that._itemClicked, that));
            }

            return popup;
        },

        select: function(text)
        {
            this.selected = text;
            this.mainButton.text(text);
        },

        _clicked: function(e)
        {
            var that = this;

            e.preventDefault();

            if (that.popup.visible())
            {
                that.popup.close();
            }
            else
            {
                that.popup.open();
            }
        },

        _itemClicked: function(e)
        {
            var that = this,
                target = kendo.eventTarget(e),
                item = $(target).closest("li");

            e.preventDefault();

            if (item)
            {
                that.popup.close();
                that.select(item.text());
                that.trigger(SELECT, {scope: that.selected});
            }
        }
    });

    Zutubi.core.ButtonNavbarItem = Zutubi.core.NavbarItem.extend({
        options: {
            name: "ZaButtonNavbarItem",
            template: '<button class="k-primary"><span class="#= spriteCssClass #"></span> <span class="k-button-navitem-title">#: title #</span></button>'
        },

        events: [
            "click"
        ],

        create: function()
        {
            var that = this;

            Zutubi.core.NavbarItem.fn.create.call(that);

            that.button = that.innerElement.kendoButton().data("kendoButton");
            that.button.bind("click", function(e) { that.trigger("click", e); });
        },

        setTitle: function(title)
        {
            this.element.find(".k-button-navitem-title").html(kendo.htmlEncode(title));
        }
    });

    Zutubi.core.Navbar = Widget.extend({
        init: function(element, options)
        {
            var that = this;

            Widget.fn.init.call(this, element, options);

            that.create();

            if (options.section)
            {
                that.setSection(options.section);
            }
        },

        options: {
            name: "ZaNavbar",
            itemTemplate: '<span class="k-navitem #: cls #"></span>'
        },

        create: function()
        {
            var that = this;

            that.itemTemplate = kendo.template(that.options.itemTemplate);

            that.list = $('<div class="k-navlist"></div>');
            that.element.append(that.list);

            that.homeItem = that._addSimpleItem({
                url: window.baseUrl + '/',
                content: '<span class="fa fa-heartbeat"></span>'
            });

            that.sectionItem = that._addItemElement().kendoZaMenuNavbarItem({
                items: ["dashboard", "my builds", "browse", "server", "agents", "administration"],
                urls: ["/dashboard/", "/dashboard/my/", "/browse/", "/server/", "/agents/", "/admin/"]
            }).data("kendoZaMenuNavbarItem");

            that._createExtraItems();

            that.helpItem = that._addSimpleItem({
                url: "#",
                click: function(e)
                {
                    var popup;

                    e.preventDefault();

                    popup = window.open(window.baseUrl + '/popups/reference.action', '_pulsereference', 'status=yes,resizable=yes,top=100,left=100,width=900,height=600,scrollbars=yes');
                    popup.focus();
                },
                content: '<span class="fa fa-question-circle"></span>'
            }, 'k-navright');
        },

        _addItemElement: function(cls)
        {
            return $(this.itemTemplate({cls: cls || ''})).appendTo(this.list);
        },

        _addSimpleItem: function(model, cls)
        {
            var el = this._addItemElement(cls);
            el.kendoZaNavbarItem({model : model}).data("kendoZaNavbarItem");
        },

        _createExtraItems: function()
        {
            var that = this,
                items = this.options.extraItems,
                i;

            this.extraItems = [];
            if (items)
            {
                for (i = 0; i < items.length; i++)
                {
                    that.addExtraItem(items[i]);
                }
            }
        },

        addExtraItem: function(item)
        {
            var el,
                type,
                result;

            el = $(this.itemTemplate({cls: item.cls || ''}));
            el.insertBefore(this.list.find(".k-navright"));
            type = item.type || "kendoZaNavbarItem";
            result = el[type](item.options || {}).data(type);
            this.extraItems.push(result);

            return result;
        },

        setSection: function(section)
        {
            this.sectionItem.select(section);
        },

        applyDelta: function(delta)
        {
            var i;
            for (i = 0; i < this.extraItems.length; i++)
            {
                this.extraItems[i].applyDelta(delta);
            }
        }
    });

    ui.plugin(Zutubi.core.NavbarItem);
    ui.plugin(Zutubi.core.MenuNavbarItem);
    ui.plugin(Zutubi.core.ButtonNavbarItem);
    ui.plugin(Zutubi.core.Navbar);
}(jQuery));
