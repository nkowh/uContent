Ext.define('admin.view.main.ConfigModel', {
    extend: 'Ext.app.ViewModel',

    alias: 'viewmodel.config',
    stores: {
        users: {type: 'users', pageSize: 1},
        groups: {type: 'groups'}
    },
    data: {
        title: '配置',
        analyzerText: '沙莎王蕾是好基友',
        analyzer: 'standard'
    }

});
