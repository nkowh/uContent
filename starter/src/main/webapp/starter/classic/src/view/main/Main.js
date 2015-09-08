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
            iconCls: 'fa-th-list',
            bind: {
                text: '{name}'
            },
            listeners: {
                afterrender: function (cmp, eOpts) {

                    var icon = cmp.getEl().down('.fa-th-list');
                    icon.addListener('click', 'onTitleClick');
                    icon.addListener('mouseenter', function () {
                        icon.setStyle({color: 'black'})
                    });
                    icon.addListener('mouseleave', function () {
                        icon.setStyle({color: undefined})
                    });
                }
            },
            flex: 0
        },

        listeners: {
            //click: 'onTitleClick'
        }
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
        layout: 'fit',
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
    }, {
        title: '高级搜索',
        iconCls: 'fa-search',
        items: [{
            xtype: 'advancedsearch'
        }]
    }, {
        title: '新建文档',
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
    }, {
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
