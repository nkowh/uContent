Ext.define('dm.view.system.Types', {
    extend: 'Ext.grid.Panel',
    selModel: 'rowmodel',

    initComponent: function () {

        Ext.apply(this, {
            tools: [
                {
                    type: 'refresh',
                    scope: this,
                    callback: this.reload
                },
                {
                    type: 'plus',
                    scope: this,
                    callback: this.createType
                }
            ],
            listeners: {
                afterrender: function () {
                    this.reload();
                }
            }
        });
        this.callParent();
    },


    reload: function () {
        var me = this;
        Ext.Ajax.request({
            url: Ext.util.Cookies.get('service') + '/documents/_mappings',
            callback: function (options, success, response) {
                if (!success) {
                    form.toast(response.responseText);
                    return;
                }
                var mappings = Ext.decode(response.responseText).documents.mappings;

                var columns = [
                    Ext.create('dm.grid.column.Action', {
                        sortable: false,
                        flex: 1,
                        scope: me,
                        items: [{
                            style: 'font-size:20px;',
                            iconCls: 'fa fa-cube',
                            handler: me.onMappingClick
                        }]
                    }),
                    {dataIndex: 'type', text: 'type', flex: 2},
                    Ext.create('dm.grid.column.Action', {
                        sortable: false,
                        flex: 1,
                        scope: me,
                        items: [{
                            style: 'font-size:20px;color:DarkRed;',
                            iconCls: 'fa fa-remove',
                            handler: me.onRemoveClick
                        }]
                    })];

                var store = Ext.create('Ext.data.ArrayStore', {
                    fields: ['type'],
                    data: Ext.Array.map(Ext.Object.getAllKeys(mappings), function (key) {
                        return [key];
                    })
                });
                me.reconfigure(store, columns);

            }
        });
    },

    createType: function (grid, tool, event) {
        Ext.create('Ext.window.Window', {
            title: 'Type', autoShow: true, closable: false, modal: true, items: [
                Ext.create('dm.view.system.NewTypeForm')
            ]
        });
    },


    onMappingClick: function (view, rowIndex, colIndex, item, e, record) {
        var me = this.up('grid');
        var type = record.get('type');
        Ext.Ajax.request({
            url: Ext.util.Cookies.get('service') + '/documents/_mapping/' + type,
            callback: function (options, success, response) {
                if (!success) return;
                var result = Ext.decode(response.responseText).documents.mappings;

                Ext.create('Ext.window.Window', {
                    title: type,
                    autoShow: true,
                    layout: 'fit',
                    height: 500,
                    width: 600,
                    scrollable: true,
                    layout: 'fit',
                    items: [Ext.create('dm.tree.CodeTree', {
                        rootVisible: false,
                        code: result[type].properties
                    })]
                });
            }
        });

    },

    onRemoveClick: function (grid, rowIndex) {
        var me = this;
        var selectionModel = me.getStore().getAt(rowIndex);
        me.setSelection(selectionModel);
        Ext.Msg.show({
            title: '确认删除',
            msg: '确认删除' + selectionModel.get('type') + "?",
            buttons: Ext.Msg.YESNO,
            icon: Ext.Msg.QUESTION,
            fn: function (buttonId) {
                if (buttonId !== 'yes') return;

                Ext.Ajax.request({
                    method: 'DELETE',
                    url: Ext.util.Cookies.get('service') + '/documents/' + selectionModel.get('type'),
                    callback: function (options, success, response) {
                        Ext.toast({
                            html: response.responseText,
                            closable: false,
                            align: 't'
                        });
                    }
                });
            }
        });
    }


});