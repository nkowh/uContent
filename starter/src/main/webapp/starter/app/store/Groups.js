Ext.define('starter.store.Groups', {
    extend: 'Ext.data.Store',
    alias : 'store.groups',
    model: 'starter.model.Group',
    pageSize: 10,
    autoSync : true,
    proxy: {
        type: 'rest',
        url: '/svc/groups',
        actionMethods : {
            update: 'PATCH'
        },
        reader: {
            type: 'json',
            rootProperty: 'value',
            totalProperty: 'total'
        }
    },
    autoLoad: true
});