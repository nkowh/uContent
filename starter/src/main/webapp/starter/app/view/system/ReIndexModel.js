Ext.define('starter.view.system.ReIndexModel', {
    extend: 'Ext.app.ViewModel',

    alias: 'viewmodel.reIndex',
    data: {
        listTitle: '重建索引',
        operationId : '',
        srcIndex : '',
        targetIndex : ''
    },
    stores : {
        reIndexs: {type: 'reIndexs'}
    }

});
