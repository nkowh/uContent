Ext.define('dm.view.system.Groups', {
    extend: 'Ext.grid.Panel',
    store: Ext.create('dm.store.system.Groups'),
    selModel: 'rowmodel',
    plugins: {
        ptype: 'rowediting',
        clicksToEdit: 2
    },


    initComponent: function () {
        var me = this;
        Ext.apply(me, {
            columns: [
                {text: 'groupname', dataIndex: '_id', flex: 1},
                {
                    text: 'display', dataIndex: 'display', flex: 1,
                    editor: {
                        xtype: 'textfield',
                        allowBlank: false
                    }
                },
                {
                    text: 'createAt', dataIndex: 'createAt', flex: 1,
                    editor: {
                        xtype: 'datefield',
                        anchor: '100%'
                    }
                }, Ext.create('dm.grid.column.Action', {
                    sortable: false,
                    scope: me,
                    items: [{
                        style: 'font-size:20px;color:DarkRed;',
                        iconCls: 'fa fa-remove',
                        handler: me.onRemoveClick
                    }]
                })
            ],
            tools: [
                {
                    type: 'refresh',
                    callback: me.refresh
                },
                {
                    type: 'plus',
                    callback: me.createGroup
                }
            ]
        });
        me.callParent();
    },

    refresh: function (grid, tool, event) {
        var me = grid;
        me.getStore().reload();
    },

    createGroup: function (grid, tool, event) {
        var me = grid;
        var newUser = Ext.create('Ext.window.Window', {
            title: '组', closable: false, modal: true, items: [
                Ext.create('dm.view.system.NewGroupForm', {store: me.getStore()})
            ]
        });
        newUser.show();
    },

    onRemoveClick: function (grid, rowIndex) {
        var me = this;
        var selectionModel = me.getStore().getAt(rowIndex);
        me.setSelection(selectionModel);
        Ext.Msg.show({
            title: '确认删除',
            msg: '确认删除' + selectionModel.get('_id') + "?",
            buttons: Ext.Msg.YESNO,
            icon: Ext.Msg.QUESTION,
            fn: function (buttonId) {
                if (buttonId === 'yes') {
                    me.getStore().removeAt(rowIndex);
                }
            }
        });
    }


});