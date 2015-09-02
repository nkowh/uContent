/**
 * This class is the main view for the application. It is specified in app.js as the
 * "mainView" property. That setting automatically applies the "viewport"
 * plugin causing this view to become the body element (i.e., the viewport).
 *
 * TODO - Replace this content of this view to suite the needs of your application.
 */
Ext.define('starter.view.main.Main', {
    extend: 'Ext.tab.Panel',
    xtype: 'app-main',

    requires: [
        'Ext.plugin.Viewport',
        'Ext.window.MessageBox',

        'starter.view.main.MainController',
        'starter.view.main.MainModel'
    ],

    controller: 'main',
    viewModel: 'main',

    ui: 'navigation',

    tabBarHeaderPosition: 1,
    titleRotation: 0,
    tabRotation: 0,

    header: {
        layout: {
            align: 'stretchmax'
        },
        title: {
            bind: {
                text: '{name}'
            },
            flex: 0
        },
        iconCls: 'fa-th-list'
    },

    tabBar: {
        flex: 1,
        layout: {
            align: 'stretch',
            overflowHandler: 'none'
        }
    },

    responsiveConfig: {
        tall: {
            headerPosition: 'top'
        },
        wide: {
            headerPosition: 'left'
        }
    },

    defaults: {
        layout:'fit',
        bodyPadding: 20,
        tabConfig: {
            plugins: 'responsive',
            responsiveConfig: {
                wide: {
                    iconAlign: 'left',
                    textAlign: 'left'
                },
                tall: {
                    iconAlign: 'top',
                    textAlign: 'center',
                    width: 120
                }
            }
        }
    },

    items: [{
        title: '全文搜索',
        iconCls: 'fa-search',
        items: [{
            xtype: 'fulltextsearch'
        }]
    },{
        title: '高级搜索',
        iconCls: 'fa-search',
        items: [{
            xtype: 'advancedsearch'
        }]
    },{
        title: '导入文档',
        iconCls: 'fa-file-o',
        items: [{
            xtype: 'indexdocument'
        }]
    }, {
        title: '用户',
        iconCls: 'fa-user',
        items: [{
            xtype: 'users'
        }]
    }, {
        title: '组',
        iconCls: 'fa-users',
        items: [{
            xtype: 'groups'
        }]
    }, {
        title: '监控',
        iconCls: 'fa-cog',
        items: [{
            xtype: 'monitorOs'
        }]
    },{
        title: '类型',
        iconCls: 'fa-cubes',
        items: [{
            xtype: 'types'
        }]
    }, {
        title: '配置',
        iconCls: 'fa-cog',
        items: []
    }]
});
