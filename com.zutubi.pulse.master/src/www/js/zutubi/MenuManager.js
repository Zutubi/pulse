//dependency: ext/package.js
//dependency: ./namespace.js
//dependency: ./FloatManager.js

/**
 * Provides central management of pop-down menus.  Implements lazy, cached rendering, and allows
 * menus to be toggled from simple onclick handlers.  To place a menu button in the dom, you must:
 * 
 * - create a link with id <menu id>-link
 * - set the link's onclick handler to "Zutubi.MenuManager.toggleMenu(this); return false"
 * - wrap that link around an empty image with id <menu id>-button and class "popdown floating-widget"
 *
 * This allows the menu manager to find/render the corresponding menu and place it on the page.
 */
Zutubi.MenuManager = function() {
    var menusById = {};
    
    var renderMenu = function(id)
    {
        var menu = menusById[id];
        if (menu)
        {
            var menuEl = menu.el;
            if (!menuEl)
            {
                menuEl = Ext.getBody().createChild({tag: 'div',  id: id, style: 'display: none'});
                var listEl = menuEl.createChild({tag: 'ul', cls: 'actions'});
                var items = menu.itemCallback();
                for (var i = 0; i < items.length; i++)
                {
                    appendMenuItem(listEl, id, items[i]);
                }
            }
        
            menu.el = menuEl;            
        }
    
        return menu;
    };
    
    var appendMenuItem = function(el, menuId, item) {
        if (!item.title)
        {
            item.title = item.id;
        }
    
        var child = {
            tag: 'a',
            id: item.id + '-' + menuId,
            cls: 'unadorned',
            href: '#',
            title: item.title,
            children: [{
                tag: 'img',
                src: window.baseUrl + '/images/' + item.image
            }, ' ' + item.title]
        };
    
        if (item.url !== undefined)
        {
            child.href = window.baseUrl + '/' + item.url;
        }
        if (item.onclick !== undefined)
        {
            child.onclick = item.onclick;
        }
        el.createChild({tag: 'li', children: [child]});
    };

    return {
        /**
         * Registers a menu with this manager.
         *
         * @param id           a unique identifier for the menu
         * @param itemCallback a function that when called will return an array of item configs for
         *                     the menu (configs must contain an id, image and link, and may also
         *                     contain a title, href, and/or onclick)
         * @param imageClass   if provided, the class of the button image (defaults to popdown)
         */
        registerMenu: function(id, itemCallback, imageClass)
        {
            menusById[id] = {itemCallback: itemCallback, imageClass: imageClass};
        },
        
        /**
         * Toggles the display of a menu.  This should be called from an anchor's onclick handler,
         * with the Link object passed, e.g.:
         *
         * <a id="my-menu-link" onclick="Zutubi.MenuManager.toggleMenu(this); return false">
         *
         * @param link the anchor item clicked on, the id of which is used to derive the menu id
         */
        toggleMenu: function(link)
        {
            var id = link.id.replace(/-link$/, '');
            var buttonId = id + '-button';
            var button = Ext.get(buttonId);
            if (button && !button.hasClass('x-item-disabled'))
            {
                var menu = renderMenu(id);
                Zutubi.FloatManager.showHideFloat('menus', id, 'tl-bl?', menu.imageClass);
            }
        }
    }
}();
