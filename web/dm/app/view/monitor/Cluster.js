Ext.define('dm.view.monitor.Cluster', {
    extend: 'Ext.grid.Panel',
    titleAlign: 'center',
    selModel: 'rowmodel',
    initComponent: function () {
        var me = this;
        Ext.apply(me, {
            tools: [
                {
                    type: 'refresh',
                    scrope: me,
                    callback: this.refresh
                }
            ],
            tbar: [
                {
                    fieldLabel: '副本数量',
                    name: 'number_of_replicas',
                    xtype: 'combo',
                    store: [0, 1, 2, 3]
                },
                {
                    xtype: 'button',
                    text: '应用',
                    handler: me.applySetting
                }
            ],
            listeners: {
                //rowdblclick: me.rowdblclick
            }
        });
        me.callParent();
        me.initSettings();
        me.refresh();


    },

    initSettings: function () {
        var me = this;
        Ext.Ajax.request({
            url: Ext.util.Cookies.get('service') + '/_settings',
            callback: function (options, success, response) {
                if (!success)return;
                var result = Ext.decode(response.responseText);
                me.down('combo[name=number_of_replicas]').setValue(result.dm.settings.index.number_of_replicas);
            }
        });
    },

    applySetting: function () {
        var me = this.up('grid');
        var number_of_replicas = me.down('combo[name=number_of_replicas]').value;

        Ext.Ajax.request({
            method: 'PUT',
            jsonData: {number_of_replicas: number_of_replicas},
            url: Ext.util.Cookies.get('service') + '/_settings',
            callback: function (options, success, response) {
                Ext.toast({
                    html: response.responseText,
                    closable: false,
                    align: 't',
                    slideInDuration: 400,
                    minWidth: 400
                });
            }
        });
    },


    refresh: function () {
        var me = this;
        var service = Ext.util.Cookies.get('service');

        if (me.xtype !== 'grid')me = me.up('grid');
        Ext.Ajax.request({
            url: service + '/_cluster/state',
            success: function (response) {
                var result = Ext.decode(response.responseText);
                var master_node = result.master_node;
                var data = [];
                var indices = Ext.Object.getAllKeys(result.metadata.indices);
                var fields = Ext.Array.union(['id', 'isMaster', 'name', 'transport_address'], Ext.Array.sort(indices));


                Ext.Object.each(result.nodes, function (key, value) {
                    var node = {
                        id: key,
                        isMaster: key === master_node,
                        name: value.name,
                        transport_address: value.transport_address
                    };

                    Ext.Array.filter(indices, function (indexName) {
                        node[indexName] = Ext.Array.sort(Ext.Array.filter(result.routing_nodes.nodes[key], function (item) {
                            return item.index === indexName;
                        }), function (a, b) {
                            return a.shard > b.shard ? 1 : -1;
                        });
                    });

                    data.push(node);
                });


                var store = Ext.create('Ext.data.Store', {
                    fields: fields,
                    data: data
                });
                var columns = [Ext.create('dm.grid.column.Action', {
                    width: 70,
                    align: 'center',
                    sortable: false,
                    dataIndex: 'isMaster',
                    text: '',
                    items: [{
                        style: 'font-size:40px',
                        getClass: function (v, metadata, r, rowIndex, colIndex, store) {
                            return v ? 'fa fa-star fa-2x' : 'fa fa-circle-o fa-2x'
                        },
                        handler: me.rowdblclick
                    }]
                }), {
                    width: 100,
                    align: 'center',
                    text: 'name',
                    dataIndex: 'name'
                }, {
                    width: 200,
                    align: 'center',
                    text: 'address',
                    dataIndex: 'transport_address'
                }];

                Ext.each(fields, function (field) {
                    if (Ext.Array.contains(['id', 'isMaster', 'name', 'transport_address'], field))return;
                    columns.push({
                        flex: 1,
                        align: 'center',
                        xtype: 'templatecolumn',
                        text: me.aliases(field, result),
                        tpl: ['<tpl for="' + field + '"><tpl if="state">',
                            '<div style="width: 35px;height: 35px;float: left;margin: 0 10px 0 0; border:<tpl if="primary">5<tpl else>2</tpl>px solid black;background-color: <tpl if="state==\'STARTED\'">green<tpl else>yellow</tpl>;color: white;text-align: center;"><div>{shard}</div>' +
                            '</div>',
                            '</tpl></tpl>']
                    });
                });

                me.reconfigure(store, columns);
            }
        });
    },

    aliases: function (indexName, result) {
        if (result.metadata.indices[indexName].aliases.length > 0) {
            return indexName + '[' + result.metadata.indices[indexName].aliases.join(',') + ']'
        }
        return indexName;
    },


    rowdblclick: function (view, rowIndex, colIndex, item, e, record) {
        var me = this.up('grid');
        var service = Ext.util.Cookies.get('service');
        var nodeId = record.get('id');
        Ext.Ajax.request({
            url: service + '/_nodes/' + nodeId,
            callback: function (options, success, response) {
                if (!success) return;
                var result = Ext.decode(response.responseText);

                Ext.create('Ext.window.Window', {
                    title: record.get('name'),
                    autoShow: true,
                    layout: 'fit',
                    height: 500,
                    width: 600,
                    scrollable: true,
                    layout: 'fit',
                    items: [Ext.create('dm.tree.CodeTree', {
                        rootVisible: false,
                        code: result.nodes[nodeId]
                    })]
                });
            }
        });


    }


})
;