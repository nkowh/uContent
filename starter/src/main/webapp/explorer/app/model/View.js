Ext.define('explorer.model.View', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'groups',  type: 'string'},
        {name: 'users',  type: 'string'},
        {name: 'viewName',  type: 'string'},
        {name: 'queryContext',   type: 'string'}
    ],
    idProperty: '_id'
});