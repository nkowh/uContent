Ext.define('dm.view.main.FullTextSearch', {
    extend: 'Ext.grid.Panel',
    xtype: 'fulltextsearch',

    requires: [
        'dm.store.Personnel'
    ],

    controller: 'fulltextsearch',
    viewModel: 'fulltextsearch',

    title: {
        bind: {
            text: '{title}'
        }
    },
        store: {
        type: 'personnel'
    },

    columns: [
        {text: 'Name', dataIndex: 'name'},
        {text: 'Phone', dataIndex: 'phone', flex: 1}
    ],

    listeners: {
        select: 'onItemSelected'
    }
});
