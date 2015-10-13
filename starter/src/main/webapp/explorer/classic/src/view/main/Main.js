/**
 * This class is the main view for the application. It is specified in app.js as the
 * "mainView" property. That setting automatically applies the "viewport"
 * plugin causing this view to become the body element (i.e., the viewport).
 *
 * TODO - Replace this content of this view to suite the needs of your application.
 */
Ext.define('explorer.view.main.Main', {
    extend: 'Ext.panel.Panel',
    xtype: 'app-main',

    requires: [
        'Ext.plugin.Viewport',
        'Ext.window.MessageBox',
        'explorer.view.main.MainController',
        'explorer.view.main.MainModel'
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
            region:'center',
            xtype: 'tabpanel',
            ui: 'navigation',
            tabPosition: 'left',
            tabRotation: 0,
            tabBar: {
                border: false
            },
            tabWidth : 150,
            minTabWidth : 150,
            defaults: {
                textAlign: 'left',
                closable: true,
                //scrollable: true,
                margin: 5
            },
            listeners :{
                beforerender : 'loadMenu',
                tabchange : 'changeMenu'
            }
        }
    ]
});
