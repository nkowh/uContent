Ext.define('dm.view.main.Main', {
    extend: 'Ext.tab.Panel',
    xtype: 'app-main',

    requires: [
        'Ext.plugin.Viewport',
        'Ext.window.MessageBox',

        'dm.view.main.MainController',
        'dm.view.main.MainModel'
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
        iconCls: 'fa-database'
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
        title: '配置',
        iconCls: 'fa-cog',
        bind: {
            html: '{loremIpsum}'
        }
    }]
});
