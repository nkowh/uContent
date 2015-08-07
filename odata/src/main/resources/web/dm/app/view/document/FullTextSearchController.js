Ext.define('dm.view.document.FullTextSearchController', {
    extend: 'Ext.app.ViewController',

    alias: 'controller.fulltextsearch',

    save:function(){
        this.getView().getStore().sync();
    },

    'delete':function(sender, index){
        this.getView().getStore().removeAt(index);
    },

    onDocumentSelected: function (sender, record) {

        record.set('Name',Ext.Number.randomInt(1,998));
        Ext.Msg.confirm('aaaaa', 'Are you sure?', 'onConfirm', this);
    }


});
