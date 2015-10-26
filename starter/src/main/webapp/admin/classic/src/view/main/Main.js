/**
 * This class is the main view for the application. It is specified in app.js as the
 * "mainView" property. That setting automatically applies the "viewport"
 * plugin causing this view to become the body element (i.e., the viewport).
 *
 * TODO - Replace this content of this view to suite the needs of your application.
 */
Ext.define('admin.view.main.Main', {
    extend: 'Ext.panel.Panel',
    xtype: 'app-main',

    requires: [
        'Ext.plugin.Viewport',
        'Ext.window.MessageBox',

        'admin.view.main.MainController',
        'admin.view.main.MainModel'
    ],

    controller: 'main',
    viewModel: 'main',


    layout: 'border',

    bodyBorder: false,

    defaults: {
        split: true,
        bodyPadding: 2
    },
    items: [ {
        region: 'north',
        height: 75,
        minHeight: 75,
        maxHeight: 150,
        xtype : 'app-header'
    },
        {
            region: 'center',
            xtype: 'tabpanel',
            ui: 'navigation',
            tabPosition: 'left',
            tabRotation: 0,
            tabBar: {
                border: false
            },
            tabWidth: 150,
            minTabWidth: 150,
            defaults: {
                textAlign: 'left',
                //scrollable: true,
                margin: 5
            },
            items: [
                {
                title: '用户',
                iconCls: 'fa-user',
                xtype: 'users'
            }, {
                title: '组',
                iconCls: 'fa-users',
                xtype: 'groups'
            }, {
                    title: '标签',
                    iconCls: 'fa-users',
                    xtype: 'tags'
            }, {
                    title: '视图',
                    iconCls: 'fa-users',
                    xtype: 'views'
                }, {
                title: '类型',
                iconCls: 'fa-cubes',
                xtype: 'types'
            }, {
                    title: '日志',
                    iconCls: 'fa fa-bars',
                    xtype: 'logs'
            },  {
                    title: '重建索引',
                    iconCls: 'fa-cog',
                    xtype: 'reIndex'
                }, {
                title: '配置',
                iconCls: 'fa-cog',
                xtype: 'config'
            }
            ]
        }]
     });