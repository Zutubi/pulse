// dependency: zutubi/pulse/namespace.js

window.Zutubi.pulse.project = window.Zutubi.pulse.project || {
    imageLabel: function(type, value) {
        return '<img alt="' + value + '" src="' + window.baseUrl + '/images/' + type + '/' + value.replace(' ', '').toLowerCase() + '.gif"/> ' + value;    
    },
        
    renderers: {
        link: function(value, data) {
            return '<a href="' + data.link + '">' + Ext.util.Format.htmlEncode(value) + '</a>';
        },

        DATE_TEMPLATE: new Ext.XTemplate(
            '<a href="#" class="unadorned" title="{date}" onclick="toggleDisplay(\'time-{id}\'); toggleDisplay(\'date-{id}\'); return false;">' +
                '<img alt="toggle format" src="{[window.baseUrl]}/images/calendar.gif"/>' +
            '</a> ' +
            '<span id="time-{id}">{time}</span>' +
            '<span id="date-{id}" style="display: none">{date}</span>'
        ),
        
        date: function(date) {
            // e.g. {time: '3 hours ago', date: '12/03/2010 10:01:33pm' }
            return Zutubi.pulse.project.renderers.DATE_TEMPLATE.apply({
                id: Ext.id(),
                date: date.date,
                time: date.time
            });
        },
        
        health: function(health) {
            return Zutubi.pulse.project.imageLabel('health', health);
        },
                
        PAUSE_TEMPLATE: new Ext.XTemplate(
            '<a class="unadorned" href="{[window.baseUrl]}/projectState.action?projectName={encodedProjectName}&amp;pause={pause}">' +
                '<img src="{[window.baseUrl]}/images/{icon}.gif"/> ' +
                '{label}' +
            '</a>'
        ),

        projectState: function(state, project) {
            // e.g. { pretty: 'idle', canPause: true, canResume: false }
            var result = state.pretty;
            if (state.canPause)
            {
                result += '&nbsp;&nbsp;' + Zutubi.pulse.project.renderers.PAUSE_TEMPLATE.apply({
                    encodedProjectName: encodeURIComponent(project.name),
                    pause: true,
                    label: 'pause',
                    icon: 'control_pause_blue'
                });
            }
            else if (state.canResume)
            {
                result += '&nbsp;&nbsp;' + Zutubi.pulse.project.renderers.PAUSE_TEMPLATE.apply({
                    encodedProjectName: encodeURIComponent(project.name),
                    pause: false,
                    label: 'resume',
                    icon: 'control_play_blue'
                });
            }
            return result;
        },

        STATISTICS_TEMPLATE: new Ext.XTemplate(
            '<img class="centre" title="{ok} ({percentOk}%) ok" src="{[window.baseUrl]}/images/box-success.gif" height="10" width="{percentOk}"/>' +
            '<img class="centre" title="{failed} ({percentFailed}%) failed" src="{[window.baseUrl]}/images/box-failure.gif" height="10" width="{percentFailed}"/>' +
            '<img class="centre" title="{error} ({percentError}%) errors" src="{[window.baseUrl]}/images/box-error.gif" height="10" width="{percentError}"/>' +
            '<br/>{total} builds (ok: {ok}, f: {failed}, e: {error})'
        ),
        
        projectStatistics: function(statistics) {
            // e.g. { ok: 8, failed: 2, error: 1 }
            var total = statistics.ok + statistics.failed + statistics.error;
            if (total == 0)
            {
                return '<span class="understated">no builds</span>';
            }

            var getPercent = function(n) {
                return (n * 100 / total).toFixed(0);
            };
            
            return Zutubi.pulse.project.cells.STATISTICS_TEMPLATE.apply({
                baseUrl: window.baseUrl,
                ok: statistics.ok,
                percentOk: getPercent(statistics.ok),
                failed: statistics.failed,
                percentFailed: getPercent(statistics.failed),
                error: statistics.error,
                percentError: getPercent(statistics.error),
                total: total
            });
        },

        ID_TEMPLATE: new Ext.XTemplate(
            '<a href="{link}">build {id}</a> ' +
            '<span id="build-{id}-bactions-link">' +
                '<img src="{[window.baseUrl]}/images/default/s.gif" class="popdown floating-widget" id="build-{id}-bactions-button" alt="build menu"/>' +
            '</span>'
        ),
        
        buildId: function(id, build) {
            return Zutubi.pulse.project.renderers.ID_TEMPLATE.apply({
                id: id,
                link: build.link
            });
        },
        
        buildStatus: function(status, build) {
            if (status == 'in progress' && build.elapsed && build.elapsed.prettyEstimatedTimeRemaining)
            {
                return '<img alt="in progress" src="' + window.baseUrl + '/images/status/inprogress.gif"/> ' +
                       Zutubi.pulse.project.renderers.buildElapsed(build.elapsed, build);
            }
            else
            {
                return Zutubi.pulse.project.imageLabel('status', status);
            }
        },
        
        buildRevision: function(revision, build) {
            // e.g. { revisionString: '1234' [, link: 'link to change viewer'] }
            if (build.personal)
            {
                return 'personal';
            }
            else if (revision)
            {
                var result = '';
                var abbreviate = revision.revisionString.length > 9;
                if (abbreviate)
                {
                    result += '<span title="' + Ext.util.Format.htmlEncode(revision.revisionString) + '">'
                }
                
                if (revision.link)
                {
                    result += '<a href="' + revision.link + '">'; 
                }
                
                result += Ext.util.Format.htmlEncode(abbreviate ? revision.revisionString.substring(0, 6) + '...' : revision.revisionString);
                
                if (revision.link)
                {
                    result += '</a>';
                }
                
                if (abbreviate)
                {
                    result += '</span>';
                }
                
                return result;
            }
            else
            {
                return 'none';
            }
        },
        
        buildTests: function(testSummary, build)
        {
            return Zutubi.pulse.project.renderers.link(testSummary, {link: build.link + 'tests/'});
        },
        
        ELAPSED_TEMPLATE: new Ext.XTemplate(
            '<tpl if="percentComplete &gt; 0"><img class="centre" title="{prettyElapsed} ({percentComplete}%) elapsed" src="{[window.baseUrl]}/images/box-elapsed.gif" height="10" width="{percentComplete}"/></tpl>' +
            '<tpl if="percentRemaining &gt; 0"><img class="centre" title="{prettyEstimatedTimeRemaining} ({percentRemaining}%) remaining" src="{[window.baseUrl]}/images/box-remaining.gif" height="10" width="{percentRemaining}"/></tpl>'
        ),
        
        buildElapsed: function(stamps)
        {
            // e.g. { prettyElapsed: '3 mins 32 secs' [, estimatedPercentComplete: 90, prettyEstimatedTimeRemaining: '21 secs']}
            if (stamps.prettyEstimatedTimeRemaining)
            {
                return Zutubi.pulse.project.renderers.ELAPSED_TEMPLATE.apply({
                    prettyElapsed: stamps.prettyElapsed,
                    prettyEstimatedTimeRemaining: stamps.prettyEstimatedTimeRemaining,
                    percentComplete: stamps.estimatedPercentComplete,
                    percentRemaining: 100 - stamps.estimatedPercentComplete
                });
            }
            else
            {
                return stamps.prettyElapsed;
            }
        },
        
        buildFeatureCount: function(count)
        {
            if (count < -0)
            {
                return 'n/a';
            }
            else
            {
                return '' + count;
            }
        }
    }
};

Ext.apply(Zutubi.pulse.project, {
    configs: {
        build: {
            id: {
                name: 'id',
                cls: 'right',
                renderer: Zutubi.pulse.project.renderers.buildId
            },
            
            status: {
                name: 'status',
                renderer: Zutubi.pulse.project.renderers.buildStatus
            },
            
            rev: {
                name: 'revision',
                renderer: Zutubi.pulse.project.renderers.buildRevision
            },
            
            tests: {
                name: 'tests',
                renderer: Zutubi.pulse.project.renderers.buildTests
            },

            errors: {
                name: 'errors',
                renderer: Zutubi.pulse.project.renderers.buildFeatureCount
            },

            warnings: {
                name: 'warnings',
                renderer: Zutubi.pulse.project.renderers.buildFeatureCount
            },

            when: {
                name: 'when',
                renderer: Zutubi.pulse.project.renderers.date
            },

            completed: {
                name: 'completed',
                renderer: Zutubi.pulse.project.renderers.date
            },

            elapsed: {
                name: 'elapsed',
                renderer: Zutubi.pulse.project.renderers.buildElapsed
            }
        }
    }
});
