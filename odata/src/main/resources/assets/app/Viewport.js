Ext.define('dm.Viewport', {
    extend: 'Ext.container.Viewport',
    layout: 'border',
    requires: [
        'Ext.layout.container.Border'
    ],

    bodyBorder: true,

    defaults: {
        collapsible: false,
        split: true
    },

    items: [
        {
            header: false,
            layout: 'fit',
            region: 'north',
            margin: '5 0 0 0',
            html: '<h1>aaa</h1>'
        },
        Ext.create('dm.view.Menu', {
            region: 'center',
            layout: 'fit',
            margin: '0 0 0 0'
        })
    ]

});