Ext.define('starter.store.Types', {
    extend: 'Ext.data.Store',
    alias : 'store.types',
    model: 'starter.model.Type',
    pageSize: 10,
    autoSync : true,
    proxy: {
        type: 'rest',
        url: '/svc/types',
        reader: {
            type: 'json',
            root :'documentTypes'
        }
    },
    autoLoad: true
});