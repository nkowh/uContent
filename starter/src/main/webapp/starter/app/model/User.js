Ext.define('starter.model.User', {
    extend: 'Ext.data.Model',
    fields: [
        {name: '_id', type: 'string'},
        {name: 'userId', type: 'string'},
        {name: 'userName', type: 'string'},
        {name: 'email', type: 'string'},
        {name: 'password', type: 'string'},
        {name: 'createBy',  type: 'string'},
        {name: 'creationDate',   type: 'date'},
        {name: 'lastModifiedBy', type: 'string'},
        {name: 'lastModificationDate', type: 'date'}
    ],
    idProperty: '_id'
});