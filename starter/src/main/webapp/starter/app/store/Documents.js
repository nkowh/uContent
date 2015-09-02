Ext.define('starter.store.Documents', {
    extend: 'Ext.data.Store',
    alias: 'store.documents',
    autoLoad: false,
    proxy: {
        type: 'rest',
        url: '/svc/types',
        reader: {
            type: 'json',
            rootProperty: 'value'
        }
    },
    model:'starter.model.Document'
});