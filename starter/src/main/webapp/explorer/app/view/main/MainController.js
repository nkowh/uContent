/**
 * This class is the controller for the main view for the application. It is specified as
 * the "controller" of the Main view class.
 *
 * TODO - Replace this content of this view to suite the needs of your application.
 */
Ext.define('explorer.view.main.MainController', {
    extend: 'Ext.app.ViewController',

    alias: 'controller.main',
    loadMenu : function(){
        var me = this;
        var tabPanel = this.getView().down('tabpanel');
        this.getViewModel().getStore('views').load({
            callback: function(records, operation, success) {
                Ext.Array.each(records, function(record, index, countriesItSelf) {
                    tabPanel.add({
                        title:record.get('name'),
                            xtype: 'documents',
                            docQuery : record.get('query'),
                            qType : record.get('type'),
                            index : index
                    });
                });
               tabPanel.setActiveTab(0);

            }
        });

    },
    changeMenu : function(tabPanel, newCard, oldCard, eOpts){
        if (oldCard && oldCard.items.getAt(0))
            oldCard.items.getAt(0).fireEvent('deactivate', {})
        if (newCard && newCard.items.getAt(0))
            newCard.items.getAt(0).fireEvent('activate', {})

    }
});
