Ext.define('dm.store.Documents', {
    extend: 'Ext.data.Store',
    alias: 'store.documents',
    autoLoad: true,
    proxy: {
        type: 'rest',
        url: '/dm/Documents',
        reader: {
            type: 'json',
            rootProperty: 'value'
        }
    },
    fields: [
        'Id', 'Name', 'CreateBy', 'LastUpdatedBy', 'CreatedOn', 'LastUpdatedOn'
    ]
});