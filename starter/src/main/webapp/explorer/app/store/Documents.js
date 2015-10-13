Ext.define('explorer.store.Documents', {
    extend: 'Ext.data.Store',
    alias: 'store.documents',
    autoLoad: false,
    proxy: {
        type: 'rest',
        url: '/svc',
        reader: {
            type: 'json',
            rootProperty: 'documents'
        }
    },
    model:'explorer.model.Document'
});