Ext.define('dm.store.ServiceMetadata', {
    extend: 'Ext.data.XmlStore',

    storeId: 'servicemetadata',
    proxy: {
        type: 'ajax',
        url : '/dm/$metadata'
    },
    record: 'Schema',
    //idPath: 'Namespace',
    fields: [
        {name: 'Author', mapping: 'ItemAttributes > Author'},
        'Namespace', 'Manufacturer', 'ProductGroup'
    ]
});