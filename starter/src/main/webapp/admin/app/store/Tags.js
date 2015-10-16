Ext.define('admin.store.Tags', {
    extend: 'Ext.data.Store',
    alias : 'store.tags',
    model: 'admin.model.Tag',
    pageSize: 10,
    autoSync : false,
    proxy: {
        type: 'rest',
        url: '/svc/tags',
        actionMethods : {
            update: 'PATCH'
        },
        reader: {
            type: 'json',
            rootProperty: 'tags',
            totalProperty: 'total'
        }
    },
    autoLoad: true
});