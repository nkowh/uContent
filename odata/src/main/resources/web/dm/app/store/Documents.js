Ext.define('dm.store.Documents', {
    extend: 'Ext.data.Store',
    alias: 'store.documents',
    autoLoad: true,
    proxy: {
        type: 'odata',
        url: '/dm/Users',
        reader: {
            type: 'json',
            rootProperty: 'value'
        }
    },
    model:'dm.model.Document'
});