Ext.define('admin.model.User', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'userId', type: 'string'},
        {name: 'userName', type: 'string'},
        {name: 'email', type: 'string'},
        {name: 'password', type: 'string'},
        {name: 'createdBy',  type: 'string'},
        {name: 'createdOn',   type: 'date'},
        {name: 'lastUpdatedBy', type: 'string'},
        {name: 'lastUpdatedOn', type: 'date'}
    ],
    idProperty: 'userId'
});