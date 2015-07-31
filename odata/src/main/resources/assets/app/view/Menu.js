Ext.define('dm.view.Menu', {
    extend: 'Ext.tab.Panel',

    tabPosition: 'left',
    tabRotation: 0,
    activeTab: null,
    items: [
        {
            title: 'Search',
            class: 'dm.view.Search',
            glyph: 0xf002
        },
        {
            title: 'Create',
            class: 'dm.view.Create',
            glyph: 0xf044
        },
        {
            title: 'Dashboard',
            class: 'dm.view.Dashboard',
            glyph: 0xf0e4
        }, {
            title: 'Users',
            glyph: 0xf007
        }, {
            title: 'Groups',
            glyph: 0xf0c0
        }, {
            title: 'Settings',
            class: 'dm.view.Settings',
            glyph: 0xf085
        }, {
            title: 'FAQ',
            class: 'dm.view.FAQ',
            glyph: 0xf128
        }
    ],

    listeners: {

        afterrender: function (tabPanel, eOpts) {
            tabPanel.setActiveTab(0);
        },

        tabchange: function (tabPanel, newCard, oldCard, eOpts) {
            if (!newCard.class)return;
            var panel = Ext.getCmp('centerContent');
            panel.removeAll(true);
            panel.add(Ext.create(newCard.class, {}));
            console.log(tabPanel);
        }
    }
});