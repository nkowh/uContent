Ext.define('starter.store.Properties', {
    extend: 'Ext.data.Store',
    alias : 'store.properties',
    model: 'starter.model.Property',
    pageSize: 10,
    autoSync : true,
    proxy: {
        type: 'rest',
        startParam : '$skip',
        pageParam : '',
        limitParam : '$top',
        url: '/dm/Users',
        reader: {
            type: 'json',
            rootProperty: 'value',
            totalProperty: 'total'
        }
    },
    autoLoad: true
});