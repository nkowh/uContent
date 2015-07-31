Ext.define('dm.Viewport', {
    extend: 'Ext.container.Viewport',
    layout: 'border',
    requires: [
        'Ext.layout.container.Border'
    ],

    bodyBorder: true,

    defaults: {
        collapsible: false,
        split: false
    },

    items: [

        Ext.create('dm.view.Menu', {
            region: 'west',
            layout: 'fit',
            margin: '0 0 0 0'
        }), {
            header: false,
            region: 'center',
            layout: 'border',
            margin: '0 0 0 0',
            items:[
                Ext.create('dm.view.Header', {
                    header: false,
                    layout: 'fit',
                    region: 'north'
                    //padding:'0 0 0 5'
                }),
                {
                    header: false,
                    region: 'center',
                    layout: 'fit',
                    id:'centerContent'
                    //padding: '0 0 0 5',

                }
            ]
        }
    ]

});