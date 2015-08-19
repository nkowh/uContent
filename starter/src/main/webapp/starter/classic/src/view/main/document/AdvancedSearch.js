Ext.define('starter.view.main.document.AdvancedSearch', {
    extend: 'Ext.grid.Panel',
    xtype: 'advancedsearch',

    requires: [
        //'dm.store.Personnel'
    ],

    title: 'AdvancedSearch',

    store: {
        type: 'personnel'
    },

    columns: [
        { text: 'Name',  dataIndex: 'name' },
        { text: 'Phone', dataIndex: 'phone', flex: 1 }
    ],

    listeners: {
        select: 'onItemSelected'
    }
});
