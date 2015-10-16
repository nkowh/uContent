Ext.define('admin.store.Groups', {
    extend: 'Ext.data.Store',
    alias : 'store.groups',
    model: 'admin.model.Group',
    pageSize: 10,
    autoSync : false,
    sorters  : [{
            property : "groupName",
            direction: "DESC"
        }],
    proxy: {
        type: 'rest',
        url: '/svc/groups',
        actionMethods : {
            update: 'PATCH'
        },
        reader: {
            type: 'json',
            rootProperty: 'groups',
            totalProperty: 'total'
        }
    },
    autoLoad: true
});