Ext.define('dm.view.document.FullTextSearchController', {
    extend: 'Ext.app.ViewController',

    alias: 'controller.fulltextsearch',

    onItemSelected: function (sender, record) {
        var store = Ext.data.StoreManager.lookup('servicemetadata');
        store.setListeners({
            load:function( store, records, successful, eOpts ){
                console.log(successful);
            }
        });
        store.load(function(){

        });

        Ext.Msg.confirm('aaaaa', 'Are you sure?', 'onConfirm', this);
    },

    onConfirm: function (choice) {
        if (choice === 'yes') {
            //
        }
    }
});
