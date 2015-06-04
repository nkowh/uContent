Ext.define('dm.store.system.Principals', {
    extend: 'Ext.data.Store',
    autoLoad: true,
    fields: [{name: '_id', type: 'string'}],

    proxy: Ext.create('dm.data.proxy.ElasticDocument', {
        url: Ext.util.Cookies.get('service') + '/system/groups',
        api: {
            read: Ext.util.Cookies.get('service') + '/system/users,groups/_search'
        },
        reader: {
            type: 'json',
            rootProperty: 'hits.hits'
        }
    })

});