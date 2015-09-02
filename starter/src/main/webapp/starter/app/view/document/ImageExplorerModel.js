Ext.define('starter.view.document.ImageExplorerModel', {
    extend: 'Ext.app.ViewModel',

    alias: 'viewmodel.imageexplorer',

    data: {},
    stores: {
        images: {
            fields:['streamId','size','contentType']
        }
    }

});