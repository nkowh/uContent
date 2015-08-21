Ext.define('starter.model.Group', {
    extend: 'Ext.data.Model',
    fields: [
        {name: '_id', type: 'string'},
        {name: 'groupName', type: 'string'},
        {name: 'createBy',  type: 'string'},
        {name: 'creationDate',   type: 'date'},
        {name: 'lastModifiedBy', type: 'string'},
        {name: 'lastModificationDate', type: 'date'},
        {name: 'users', type: 'auto'}
    ],
    idProperty: '_id'
});