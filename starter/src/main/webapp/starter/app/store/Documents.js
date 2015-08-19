Ext.define('starter.store.Documents', {
    extend: 'Ext.data.Store',
    alias: 'store.documents',
    autoLoad: true,
    proxy: {
        type: 'rest',
        url: '/dm/Users',
        reader: {
            type: 'json',
            rootProperty: 'value'
        }
    },
    model:'starter.model.Document'
});