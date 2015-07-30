Ext.define('dm.view.Menu', {
    extend: 'Ext.tab.Panel',

    tabPosition: 'left',
    items: [
        {
            title: 'Search',
            glyph: 0xf002,
            html: 'KitchenSink.DummyText.Search'
        },
        {
            title: 'Create',
            glyph: 0xf044,
            html: 'KitchenSink.DummyText.Create'
        },
        {
            title: 'Dashboard',
            glyph: 0xf0e4,
            html: 'KitchenSink.DummyText.longText'
        }, {
            title: 'Users',
            glyph: 0xf007,
            html: 'KitchenSink.DummyText.extraLongText'
        }, {
            title: 'Groups',
            glyph: 0xf0c0,
            html: 'KitchenSink.DummyText.longText'
        }, {
            title: 'Settings',
            glyph: 0xf085,
            items: [{
                html: '<h1>aaabbbbbb</h1>'
            }]
        }, {
            title: 'FAQ',
            glyph: 0xf128,
            html: 'KitchenSink.DummyText.FAQ'
        }

    ]
});