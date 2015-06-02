Ext.define('dm.store.system.Users', {
    extend: 'Ext.data.Store',
    autoSync: true,
    autoLoad: true,
    model: 'dm.model.system.User',

    proxy: Ext.create('dm.data.proxy.ElasticDocument', {
        url: Ext.util.Cookies.get('service') + '/system/users',
        api: {
            read: Ext.util.Cookies.get('service') + '/system/users/_search'
        },
        reader: {
            type: 'json',
            rootProperty: 'hits.hits'
        }
    })

});