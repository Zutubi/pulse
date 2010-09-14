// dependency: ext/package.js
// dependency: ext/StatusBar.js
// dependency: zutubi.js

ZUTUBI.PROJECT_CELLS = '<td class="health-{health}" rowspan="{rowspan}">&nbsp;</td>' +
                       '<td class="fit-width" rowspan="{rowspan}">' +
                           '<img alt="-" src="{base}/images/default/s.gif" class="project-name" style="width: {indent}px; height: 16px;"/> ' +
                           '<span class="nowrap"><a href="{homeLink}">{name:htmlEncode}</a></span>' +
                           '<tpl if="responsibleMessage">' +
                               ' <img alt="fixing" id="{id}_fixing" src="{base}/images/config/actions/takeResponsibility.gif" ext:qtip="{responsibleMessage:htmlEncode}' +
                               '<tpl if="responsibleComment">' +
                                   ': {responsibleComment:htmlEncode}' +
                               '</tpl>' +
                               '"/>' +
                           '</tpl>' +
                       '</td>' +
                       '<td class="actions-menu project-actions" rowspan="{rowspan}">' +
                           '<span id="{id}_actions_link">' +
                               '<img src="{base}/images/default/s.gif" class="popdown floating-widget" id="{id}_actions_button" alt="project menu"/>' +
                           '</span>' +
                       '</td>';

ZUTUBI.BUILD_CELLS = '<td class="build-id fit-width">' +
                         '<a href="{buildLink}">build {buildNumber}</a>' +
                     '</td>' +
                     '<td class="actions-menu">' +
                         '<span id="{buildId}_bactions_link">' +
                             '<img src="{base}/images/default/s.gif" class="popdown floating-widget" id="{buildId}_bactions_button" alt="build menu"/>' +
                         '</span>' +
                     '</td>' +
                     '<td class="build-status fit-width">' +
                         ':: <span id="{buildId}.status"><img alt="status" src="{base}/images/{statusIcon}"/> {status}</span>' +
                     '</td>' +
                     '<tpl for="columns"><td class="build-column">{.} <tpl if="xindex &lt; xcount"></tpl></td></tpl>';

ZUTUBI.NO_BUILD_CELLS = '<td colspan="{noBuildSpan}" class="understated">this project has never been built</td>';

ZUTUBI.ConcreteProject = function(data, columnCount, showHideLinks) {
    this.data = data;
    this.columnCount = columnCount;
    this.showHideLinks = showHideLinks;
    this.hidden = false;
    this.rows = [];
    this.renderedMenus = {};
};

ZUTUBI.ConcreteProject.prototype = {
    buildTemplate: new Ext.XTemplate('<tr class="project-row" id="{id}">' + ZUTUBI.PROJECT_CELLS + ZUTUBI.BUILD_CELLS + '</tr>').compile(),

    noBuildTemplate: new Ext.XTemplate('<tr class="project-row" id="{id}">' + ZUTUBI.PROJECT_CELLS + ZUTUBI.NO_BUILD_CELLS + '</tr>').compile(),

    noProjectTemplate: new Ext.XTemplate('<tr class="project-row" id="{buildId}">' + ZUTUBI.BUILD_CELLS + '</tr>').compile(),

    addBuildData: function(templateData, build, index) {
        templateData.buildId = 'b' + (index + 1) + '.' + templateData.id;
        templateData.buildNumber = build.number;
        templateData.buildLink = window.baseUrl + '/browse/projects/' + encodeURIComponent(templateData.name) + '/builds/' + build.number + '/';
        templateData.status = build.status;
        templateData.statusIcon = build.statusIcon;
        templateData.columns = build.columns;
    },

    render: function(previousRowEl) {
        var templateData = {
            id: this.data.id,
            health: this.data.health,
            responsibleMessage: this.data.responsibleMessage,
            responsibleComment: this.data.responsibleComment,
            rowspan: this.data.built ? this.data.buildRows.length : 1,
            base: window.baseUrl,
            indent: this.data.depth * 12,
            homeLink: window.baseUrl + '/' + this.getProjectTabLink('home'),
            name: this.data.name,
            noBuildSpan: this.columnCount + 3
        };

        // This is slightly more complex than adding the project row and then
        // selectively choosing to add the build cells to it only if there is
        // a build available - but adding cells fails in IE7.
        if (this.data.built)
        {
            var builds = this.data.buildRows;

            this.addBuildData(templateData, builds[0], 0);
            this.el = this.buildTemplate.insertAfter(previousRowEl, templateData, true);
            this.rows.push(this.el);

            for (var i = 0; i < builds.length; i++)
            {
                var build = builds[i];
                this.addBuildData(templateData, build, i);

                // First build is already rendered in the project row above.
                if (i > 0)
                {
                    this.rows.push(this.noProjectTemplate.insertAfter(this.rows[this.rows.length - 1], templateData, true));
                }

                var buildButton = Ext.get(templateData.buildId + '_bactions_link');
                buildButton.on('click', this.toggleMenu.createDelegate(this, ['bactions', templateData.buildNumber, templateData.buildId + '_bactions']));
            }
        }
        else
        {
            this.el = this.noBuildTemplate.insertAfter(previousRowEl, templateData, true);
            this.rows.push(this.el);
        }

        var projectButton = Ext.get(this.data.id + '_actions_link');
        projectButton.on('click', this.toggleMenu.createDelegate(this, ['actions', '', this.data.id + '_actions']));

        return this.rows[this.rows.length - 1];
    },

    toggleMenu: function(menuType, menuArg, id) {
        renderMenu(this, this.getMenuItems(menuType, menuArg, id), id);
        showHideFloat('actions', id, 'tl-bl?');
    },

    setRowDisplay: function(display) {
        for (var i = 0; i < this.rows.length; i++)
        {
            this.rows[i].setStyle('display', display);
        }
    },

    hide: function() {
        if (!this.hidden)
        {
            this.hidden = true;
            this.setRowDisplay('none');
        }
    },

    show: function() {
        if (this.hidden)
        {
            this.hidden = false;
            this.setRowDisplay('');
        }
    },

    getProjectTabLink: function(tab) {
        return 'browse/projects/' + encodeURIComponent(this.data.name) + '/' + tab + '/';
    },

    getBuildMenuItem: function(build, id, image, title) {
        return {
            id: id,
            image: image,
            title: title,
            url: 'browse/projects/' + encodeURIComponent(this.data.name) + '/builds/' + build + '/' + id + '/'
        };
    },

    getMenuItems: function(menuType, menuArg, id) {
        var items;

        if (menuType == 'actions')
        {
            var encodedName = encodeURIComponent(this.data.name);
            items = [{
                id: 'home',
                url: this.getProjectTabLink('home'),
                image: 'house.gif'
            }, {
                id: 'reports',
                url: this.getProjectTabLink('reports'),
                image: 'chart_bar.gif'
            }, {
                id: 'history',
                url: this.getProjectTabLink('history'),
                image: 'time.gif'
            }, {
                id: 'dependencies',
                url: this.getProjectTabLink('dependencies'),
                image: 'dependencies.gif'
            }, {
                id: 'log',
                url: this.getProjectTabLink('log'),
                image: 'script.gif'
            }, {
                id: 'configuration',
                url: 'admin/projects/' + encodedName + '/',
                image: 'pencil.gif'
            }];

            if (this.data.canTrigger)
            {
                var item = {
                    id: 'trigger',
                    image: 'lightning.gif'
                };

                if (this.data.prompt)
                {
                    item.url = 'triggerBuild.action?projectName=' + encodedName;
                }
                else
                {
                    item.onclick = 'actionPath(\'projects/' + encodeString(this.data.name) + '\', \'trigger\', false, false); showHideFloat(\'actions\', \'' + id + '\', \'tl-bl?\'); return false;';
                }

                items.push(item);
            }

            if (this.data.canRebuild)
            {
                items.push({
                    id: 'with dependencies',
                    image: 'lightning.gif',
                    onclick: 'actionPath(\'projects/' + encodeString(this.data.name) + '\', \'rebuild\', false, false); showHideFloat(\'actions\', \'' + id + '\', \'tl-bl?\'); return false;'
                });
            }

            if (this.showHideLinks)
            {
                items.push({
                    id: 'hide',
                    url: 'user/hideDashboardProject.action?projectName=' + encodedName,
                    image: 'close.gif'
                });
            }
        }
        else
        {
            items = [
                this.getBuildMenuItem(menuArg, 'summary', 'information.gif'),
                this.getBuildMenuItem(menuArg, 'logs', 'script.gif'),
                this.getBuildMenuItem(menuArg, 'details', 'magnifier.gif', 'details'),
                this.getBuildMenuItem(menuArg, 'changes', 'page_code.gif'),
                this.getBuildMenuItem(menuArg, 'artifacts', 'folder_page.gif')
            ];
        }

        return items;
    },

    destroy: function() {
        for (var i = 0; i < this.rows.length; i++)
        {
            this.rows[i].select('a').removeAllListeners();
            this.rows[i].select('span').removeAllListeners();
        }

        for (var key in this.renderedMenus)
        {
            this.renderedMenus[key].remove();
        }
    }
};

ZUTUBI.ProjectContainer = function(model, columnCount, showHideLinks) {
    this.model = model;
    this.children = [];
    this.hidden = false;
    this.childrenRendered = false;
    this.columnCount = columnCount;
    this.showHideLinks = showHideLinks;
};

ZUTUBI.ProjectContainer.prototype = {
    renderChildren: function() {
        this.childrenRendered = true;

        var children = this.model.children;
        if (children)
        {
            var previousRowEl = this.el;
            for (var i = 0; i < children.length; i++)
            {
                var childData = children[i];
                var child;
                if (childData.concrete)
                {
                    child = new ZUTUBI.ConcreteProject(childData, this.columnCount, this.showHideLinks);
                }
                else
                {
                    child = new ZUTUBI.TemplateProject(childData, this.columnCount, this.showHideLinks);
                }

                previousRowEl = child.render(previousRowEl);
                this.children.push(child);
            }

            return previousRowEl;
        }
        else
        {
            return this.el;
        }
    },

    onClick: function() {
        if(this.model.collapsed)
        {
            this.expand();
        }
        else
        {
            this.collapse();
        }
    },

    collapse: function() {
        if (!this.model.collapsed)
        {
            this.hideDescendants();
            this.model.collapsed = true;
            this.el.addClass('project-collapsed');
        }
    },

    collapseAll: function() {
        this.collapse();
        for (var i = 0; i < this.children.length; i++)
        {
            var child = this.children[i];
            if (!child.data.concrete)
            {
                child.collapseAll();
            }
        }
    },

    hide: function() {
        if (!this.hidden)
        {
            this.el.setStyle('display', 'none');
            this.hidden = true;
        }

        if (!this.model.collapsed)
        {
            this.hideDescendants();
        }
    },

    hideDescendants: function() {
        for (var i = 0; i < this.children.length; i++)
        {
            var child = this.children[i];
            child.hide();
        }
    },

    expand: function() {
        if (this.model.collapsed)
        {
            if (!this.childrenRendered)
            {
                this.renderChildren();
            }
            
            this.showDescendants();
            this.model.collapsed = false;
            this.el.removeClass('project-collapsed');
        }
    },

    expandAll: function() {
        this.expand();
        for (var i = 0; i < this.children.length; i++)
        {
            var child = this.children[i];
            if (!child.data.concrete)
            {
                child.expand();
            }
        }
    },

    show: function() {
        if (this.hidden)
        {
            this.el.setStyle('display', '');
            this.hidden = false;
        }

        if (!this.model.collapsed)
        {
            this.showDescendants();
        }
    },

    showDescendants: function() {
        for(var i = 0; i < this.children.length; i++)
        {
            var child = this.children[i];
            child.show();
        }
    },

    destroy: function() {
        this.el.removeAllListeners();
        for (var i = 0; i < this.children.length; i++)
        {
            var child = this.children[i];
            child.destroy();
        }
    }
};

ZUTUBI.TemplateProject = function(data, columnCount, showHideLinks) {
    ZUTUBI.TemplateProject.superclass.constructor.call(this, data, columnCount, showHideLinks);
    this.data = data;
};

Ext.extend(ZUTUBI.TemplateProject, ZUTUBI.ProjectContainer, {
    template: new Ext.Template(
        '<tr class="project-row {expandable}" id="{id}">' +
            '<td class="health-{health}">&nbsp;</td>' +
            '<td class="fit-width">' +
                '<img alt="-" src="{base}/images/default/s.gif" class="project-name" style="width: {indent}px; height: 16px;"/> ' +
                '<span class="nowrap">{name:htmlEncode}</span>' +
            '</td>' +
            '<td class="project-template-actions">&nbsp;</td>' +
            '<td class="fit-width" colspan="3" id="{id}.building">{building}</td>' +
            '<td class="build-details" colspan="{columnCount}">' +
                'summary :: ' +
                '<img alt="health: ok" src="{base}/images/health/ok.gif"> ok: {okCount} ' +
                '<img alt="health: warnings" src="{base}/images/health/warnings.gif"> warnings: {warningCount} ' +
                '<img alt="health: broken" src="{base}/images/health/broken.gif"> broken: {brokenCount}' +
            '</td>' +
        '</tr>').compile(),

    render: function(previousRowEl) {
        var children = this.data.children;
        var canExpand = children && children.length > 0;
        var expandable = canExpand ? 'project-expandable' : '';
        if (canExpand && this.data.collapsed)
        {
            expandable += ' project-collapsed';
        }

        this.el = this.template.insertAfter(previousRowEl, {
            expandable: expandable,
            id: this.data.id,
            health: this.data.health,
            base: window.baseUrl,
            indent: this.data.depth * 12,
            name: this.data.name,
            building: this.data.building,
            okCount: this.data.okCount,
            warningCount: this.data.warningCount,
            brokenCount: this.data.brokenCount,
            columnCount: this.columnCount
        }, true);

        if (canExpand)
        {
            this.el.on('click', this.onClick, this);
            this.el.addClassOnOver('project-highlighted');
        }

        if (this.data.collapsed)
        {
            return this.el;
        }
        else
        {
            return this.renderChildren();
        }
    }
});

ZUTUBI.ProjectGroup = function(data, columnCount, rssEnabled, showHideLinks, last) {
    ZUTUBI.ProjectGroup.superclass.constructor.call(this, data.root, columnCount, showHideLinks);
    this.data = data;
    this.rssEnabled = rssEnabled;
    this.last = last;
};

Ext.extend(ZUTUBI.ProjectGroup, ZUTUBI.ProjectContainer, {
    template: new Ext.XTemplate('<tr class="project-row project-expandable {collapsed}" id="{id}">' +
            '<td class="group-header health-{health}">&nbsp;</td>' +
            '<td class="group-header fit-width" colspan="5"><img src="{base}/images/default/s.gif" alt="-" class="group-name"/> {name:htmlEncode}</td>' +
            '<td class="group-header build-details" colspan="{columnCount}">' +
                '<img alt="health: ok" src="{base}/images/health/ok.gif"> ok: {okCount} ' +
                '<img alt="health: warnings" src="{base}/images/health/warnings.gif"> warnings: {warningCount} ' +
                '<img alt="health: broken" src="{base}/images/health/broken.gif"> broken: {brokenCount}&nbsp;' +
                '<tpl if="rssEnabled"><a class="unadorned" id="rss.builds.{id}" href="{base}/rss.action?groupName={encodedName}" onclick="stopEventPropagation(event);"><img alt="rss" src="{base}/images/feed-icon-16x16.gif"></a>&nbsp;</tpl>' +
                '<tpl if="showHideLinks &amp;&amp; labelled"><a class="unadorned" id="hide.{id}" href="{base}/user/hideDashboardGroup.action?groupName={encodedName}" onclick="stopEventPropagation(event);"><img alt="hide group" src="{base}/images/close.gif"></a></tpl>' +
            '</td>').compile(),

    render: function(parentEl) {
        this.el = this.template.append(parentEl, {
            id: this.data.id,
            health: this.data.root.health,
            base: window.baseUrl,
            name: this.data.groupName,
            okCount: this.data.root.okCount,
            warningCount: this.data.root.warningCount,
            brokenCount: this.data.root.brokenCount,
            encodedName: encodeURIComponent(this.data.groupName),
            collapsed: this.model.collapsed ? 'project-collapsed' : '',
            columnCount: this.columnCount,
            rssEnabled: this.rssEnabled,
            showHideLinks: this.showHideLinks,
            labelled: this.data.labelled
        }, true);

        this.el.on('click', this.onClick, this);
        this.el.addClassOnOver('project-highlighted');
        if (!this.model.collapsed)
        {
            this.renderChildren();
        }

        var spacerClass = 'project-group-spacer';
        if (this.last)
        {
            spacerClass += ' project-group-last';
        }
        return parentEl.createChild({tag: 'tr', cls: spacerClass, children: [{ tag: 'td', colspan: '6', html: '&nbsp;'}]});
    },

    cleanupHandler: function(prefix)
    {
        var el = Ext.get(prefix + this.data.id);
        if (el)
        {
            el.dom.onclick = null;
        }
    },

    destroy: function()
    {
        ZUTUBI.ProjectGroup.superclass.destroy.call(this);
        this.cleanupHandler('rss.builds.');
        this.cleanupHandler('hide.');
    }
});

ZUTUBI.ProjectsTable = function(containerEl, toolbarEl, columnCount, rssEnabled, isDashboard) {
    this.toolbarEl = toolbarEl;
    this.containerEl = containerEl;
    this.columnCount = columnCount;
    this.rssEnabled = rssEnabled;
    this.isDashboard = isDashboard;
    this.groups = {};
    this.toolbarRendered = false;
};

ZUTUBI.ProjectsTable.prototype = {
    renderToolbar: function() {
        this.toolbar = new Ext.ux.StatusBar({
            defaultText: 'Layout Controls:',
            renderTo: this.toolbarEl,
            style: { 'background': '#F2F2FF', 'border-width': '1px' },
            width: 176,
            items:  [ {
                         icon: window.baseUrl + '/images/expand.gif',
                         cls: 'x-btn-icon',
                         tooltip: 'expand all',
                         onClick: this.expandAll.createDelegate(this)
                     }, {
                         icon: window.baseUrl + '/images/collapse.gif',
                         cls: 'x-btn-icon',
                         tooltip: 'collapse all',
                         onClick: this.collapseAll.createDelegate(this)
                     }, {
                         icon: window.baseUrl + '/images/save.gif',
                         cls: 'x-btn-icon',
                         tooltip: 'save current layout',
                         onClick: this.saveLayout.createDelegate(this)
                     }]
        });
        this.toolbarRendered = true;
    },

    destroyToolbar: function() {
        this.toolbar.destroy();
        this.toolbarRendered = false;
    },
    
    expandAll: function() {
        var tb = this.toolbar;
        tb.showBusy('Expanding...');
        var groups = this.groups;
        (function() {
            for(var group in groups)
            {
                groups[group].expandAll();
            }
            tb.clearStatus({useDefaults: true});
        }).defer(1);
    },

    collapseAll: function() {
        var tb = this.toolbar;
        tb.showBusy('Collapsing...');
        var groups = this.groups;
        (function() {
            for(var group in groups)
            {
                groups[group].collapseAll();
            }
            tb.clearStatus({useDefaults: true});
        }).defer(1);
    },

    getCurrentLayout: function() {
        var layout = [];
        for(var groupName in this.groups)
        {
            var group = this.groups[groupName];
            layout.push({
                group: this.getGroupName(group.data),
                collapsed: group.model.collapsed,
                layout: this.getGroupLayout(group.data)
            });
        }
        return layout;
    },

    saveLayout: function() {
        var tb = this.toolbar;
        tb.showBusy('Saving...');
        Ext.Ajax.request({
            url: window.baseUrl + '/ajax/saveProjectsLayout.action',
            params: { layout: Ext.util.JSON.encode(this.getCurrentLayout()), dashboard: this.isDashboard },
            callback: function() { tb.clearStatus({useDefaults: true}); }
        });
    },

    update: function(groupsData) {
        var el = this.containerEl;
        var previousGroups = this.groups;

        for (var key in previousGroups)
        {
            previousGroups[key].destroy();
        }

        el.update('<table class="project-group"><tbody></tbody></table>');
        var tableEl = el.first().first();

        this.groups = {};
        if (groupsData.length == 0)
        {
            if (this.toolbarRendered)
            {
                this.destroyToolbar();
            }

            el.update('No projects found.');
        }
        else
        {
            if (!this.toolbarRendered)
            {
                this.renderToolbar();
            }

            for (var i = 0; i < groupsData.length; i++)
            {
                var groupData = groupsData[i];
                var groupName = this.getGroupName(groupData);
                this.applyPreviousState(groupData, previousGroups[groupName]);

                var group = new ZUTUBI.ProjectGroup(groupData, this.columnCount, this.rssEnabled, this.isDashboard, i == groupsData.length - 1);
                group.render(tableEl);
                this.groups[groupName] = group;
            }
        }
    },

    getGroupName: function(groupData) {
        if (groupData.labelled)
        {
            return groupData.groupName;
        }
        else
        {
            return '';
        }
    },

    forEachTemplate: function(groupData, f) {
        this.forEachChildTemplate(groupData.root, f);
    },

    forEachChildTemplate: function(templateData, f) {
        var children = templateData.children;
        if (children)
        {
            for (var i = 0; i < children.length; i++)
            {
                var childData = children[i];
                if (!childData.concrete)
                {
                    f.call(this, childData);
                    this.forEachChildTemplate(childData, f);
                }
            }
        }
    },

    applyPreviousState: function(groupData, previousGroup)
    {
        if (previousGroup)
        {
            groupData.root.collapsed = previousGroup.model.collapsed;

            var groupLayout = this.getGroupLayout(previousGroup.data);
            this.forEachTemplate(groupData, function(templateData) {
                var previous = groupLayout[templateData.name];
                if (previous != null)
                {
                    templateData.collapsed = previous;
                }
            });
        }
    },

    getGroupLayout: function(groupData)
    {
        var groupLayout = {};
        this.forEachTemplate(groupData, function(templateData) {
            groupLayout[templateData.name] = templateData.collapsed;
        });
        return groupLayout;
    }
};

ZUTUBI.ChangesTable = function(containerEl, idSuffix, title, showWho) {
    this.containerEl = containerEl;
    this.idSuffix = idSuffix;
    this.title = title;
    this.showWho = showWho;
    this.renderedComments = {};
    this.renderedBuilds = {};
};

ZUTUBI.ChangesTable.prototype = {
    tableTemplate: new Ext.XTemplate('<table id="{idSuffix}-changes-table" class="two-content"><tbody>' +
                                         '<tr><th class="two-heading" colspan="7">{title}</th></tr>' +
                                         '<tr>' +
                                             '<th class="content leftmost">revision</th>' +
                                             '<tpl if="showWho"><th class="content">who</th></tpl>' +
                                             '<th class="content">when</th>' +
                                             '<th class="content">comment</th>' +
                                             '<th class="content">status</th>' +
                                             '<th class="content">affected builds</th>' +
                                             '<th class="content rightmost">actions</th>' +
                                         '</tr>' +
                                     '</tbody></table>').compile(),

    changeTemplate: new Ext.XTemplate('<tr>' +
                                          '<td class="content leftmost">' +
                                              '<tpl if="url"><a href="{url}"></tpl>' +
                                                  '<tpl if="shortRevision">' +
                                                      '<span title="' +
                                                  '</tpl>' +
                                                  '{revision:htmlEncode}' +
                                              '<tpl if="shortRevision">' +
                                                  '">{shortRevision:htmlEncode}</span>' +
                                              '</tpl>' +
                                              '<tpl if="url"></a></tpl>' +
                                          '</td>' +
                                          '<tpl if="showWho"><td class="content">{who}</td></tpl>' +
                                          '<td class="content">' +
                                              '<a href="#" class="unadorned" title="{date}" onclick="toggleDisplay(\'{id}_{idSuffix}_time\'); toggleDisplay(\'{id}_{idSuffix}_date\'); return false;">' +
                                                  '<img alt="toggle format" src="{base}/images/calendar.gif"/>' +
                                              '</a> ' +
                                              '<span id="{id}_{idSuffix}_time">{time}</span>' +
                                              '<span id="{id}_{idSuffix}_date" style="display: none">{date}</span>' +
                                          '</td>' +
                                          '<td class="content" id="${id}_{idSuffix}_cell">' +
                                              '<tpl if="shortComment">' +
                                                  '{shortComment} ' +
                                                  '<span id="{id}_{idSuffix}_comment_link">' +
                                                      '<img src="{base}/images/default/s.gif" class="popdown floating-widget" id="{id}_{idSuffix}_comment_button" alt="show full comment"/>' +
                                                  '</span>' +
                                              '</tpl>' +
                                              '<tpl if="!shortComment">{comment}</tpl>' +
                                          '</td>' +
                                          '<td class="content">' +
                                              '<img alt="{aggregateStatus}" src="{base}/images/{aggregateStatusIcon}"/> {aggregateStatus}' +
                                          '</td>' +
                                          '<td class="content">' +
                                              '<tpl if="buildCount == 0">' +
                                                  'no builds' +
                                              '</tpl>' +
                                              '<tpl if="buildCount == 1">' +
                                                  '<a href="{base}/browse/projects/{[values.builds[0].encodedProject]}/home/">{[Ext.util.Format.htmlEncode(values.builds[0].project)]}</a> :: ' +
                                                  '<a href="{base}/browse/projects/{[values.builds[0].encodedProject]}/builds/{[values.builds[0].number]}/">build {[values.builds[0].number]}</a>' +
                                              '</tpl>' +
                                              '<tpl if="buildCount &gt; 1">' +
                                                  '{buildCount} builds ' +
                                                  '<span id="{id}_{idSuffix}_builds_link">' +
                                                      '<img src="{base}/images/default/s.gif" class="popdown floating-widget" id="{id}_{idSuffix}_builds_button" alt="show all builds"/>' +
                                                  '</span>' +
                                              '</tpl>' +
                                          '</td>' +
                                          '<td class="content rightmost"><a href="{base}/dashboard/changes/{id}/">view</a></td>' +
                                      '</tr>').compile(),

    commentTemplate: new Ext.Template('<div id="{id}_{idSuffix}_comment" style="display: none">' +
                                          '<table class="content" style="margin: 0">' +
                                              '<tr>' +
                                                  '<th class="heading" colspan="5">' +
                                                       '<span class="action">' +
                                                           '<a href="#" onclick="showHideFloat(\'comments\', \'{id}_{idSuffix}_comment\'); return false;">' +
                                                               '<img alt="close" src="{base}/images/delete.gif"/> close' +
                                                           '</a>' +
                                                       '</span>' +
                                                       'comment' +
                                                   '</th>' +
                                               '</tr>' +
                                               '<tr><td><pre>{comment}</pre></td></tr>' +
                                           '</table>' +
                                       '</div>').compile(),

    buildsTemplate: new Ext.XTemplate('<div id="{id}_{idSuffix}_builds" style="display: none">' +
                                          '<table class="content" style="margin: 0">' +
                                              '<tr>' +
                                                  '<th class="heading" colspan="4">' +
                                                      '<span class="action">' +
                                                          '<a href="#" onclick="showHideFloat(\'builds\', \'{id}_{idSuffix}_builds\'); return false;">' +
                                                              '<img alt="close" src="{base}/images/delete.gif"/> close' +
                                                          '</a>' +
                                                      '</span>' +
                                                      'build results' +
                                                  '</th>' +
                                              '</tr>' +
                                              '<tr><th class="content">project</th><th class="content">build id</th><th class="content">status</th></tr>' +
                                              '<tpl for="builds">' +
                                                  '<tr>' +
                                                      '<td class="content"><a href="{base}/browse/projects/{encodedProject}/home/">{project:htmlEncode}</a></td>' +
                                                      '<td class="content-right"><a href="{base}/browse/projects/{encodedProject}/builds/{number}/">{number}</a></td>' +
                                                      '<td class="{statusClass}"><img alt="{status}" src="{base}/images/{statusIcon}"/> {status}</td>' +
                                                  '</tr>' +
                                              '</tpl>' +
                                          '</table>' +
                                      '</div>').compile(),

    update: function(changes) {
        this.destroy();
        this.containerEl.update('');

        if (changes && changes.length > 0)
        {
            var tableEl = this.tableTemplate.append(this.containerEl, this, true);
            var tbodyEl = tableEl.first();

            for (var i = 0; i < changes.length; i++)
            {
                var change = changes[i];
                change.base = window.baseUrl;
                change.idSuffix = this.idSuffix;
                change.showWho = this.showWho;
                if (!change.who)
                {
                    change.who = 'anonymous';
                }

                this.changeTemplate.append(tbodyEl, change);
                if (change.shortComment)
                {
                    var commentButton = Ext.get(change.id + '_' + this.idSuffix + '_comment_link');
                    commentButton.on('click', this.toggleComment.createDelegate(this, [change]));
                }

                var buildsButton = Ext.get(change.id + '_' + this.idSuffix + '_builds_link');
                if (buildsButton)
                {
                    buildsButton.on('click', this.toggleBuilds.createDelegate(this, [change]));
                }
            }
        }
        else
        {
            this.containerEl.update('No changes to show.');
        }
    },

    toggleComment: function(change) {
        this.renderComment(change);
        showHideFloat('comments', change.id + '_' + this.idSuffix + '_comment');
    },

    renderComment: function(change) {
        if (this.renderedComments[change.id])
        {
            return;
        }

        this.renderedComments[change.id] = this.commentTemplate.append(Ext.getBody(), change, true);
    },

    toggleBuilds: function(change) {
        this.renderBuilds(change);
        showHideFloat('builds', change.id + '_' + this.idSuffix + '_builds');
    },

    renderBuilds: function(change) {
        if (this.renderedBuilds[change.id]) {
            return;
        }

        this.renderedBuilds[change.id] = this.buildsTemplate.append(Ext.getBody(), change, true);
    },

    destroy: function() {
        this.containerEl.select('a').removeAllListeners();
        this.containerEl.select('span').removeAllListeners();

        var key;
        for (key in this.renderedBuilds)
        {
            this.renderedBuilds[key].remove();
        }

        for (key in this.renderedComments)
        {
            this.renderedComments[key].remove();
        }
    }
};


ZUTUBI.ActiveView = function(url, refreshInterval, updateFn, updateScope, el, failureMessage) {
    this.url = url;
    this.refreshInterval = refreshInterval;
    this.updateFn = updateFn;
    this.updateScope = updateScope;
    this.el = el;
    this.failureMessage = failureMessage;
    this.initialised = false;
};

ZUTUBI.ActiveView.prototype = {
    init: function() {
        if (this.refreshInterval > 0)
        {
            this.runner = new Ext.util.TaskRunner();
            this.runner.start({
                run: this.load,
                scope: this,
                interval: 1000 * this.refreshInterval
            });
        }
        else
        {
            this.load();
        }
    },

    load: function() {
        var view = this;

        Ext.Ajax.request({
            url: view.url,
            timeout: 120000,
            
            success: function(transport/*, options*/)
            {
                view.updateFn.call(view.updateScope, eval('(' + transport.responseText + ')'));
                view.initialised = true;
            },

            failure: function(/*transport, options*/)
            {
                // Stop trying to refresh.
                if (view.runner)
                {
                    view.runner.stopAll();
                }

                // If we never initialised the view, show an error message.
                if (!view.initialised)
                {
                    view.el.update(view.failureMessage);
                }
            }
        });
    }
};
