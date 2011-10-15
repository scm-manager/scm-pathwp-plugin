/* *
 * Copyright (c) 2010, Sebastian Sdorra
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of SCM-Manager; nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * http://bitbucket.org/sdorra/scm-manager
 * 
 */

Ext.ns('Sonia.pathwp');

Sonia.pathwp.ConfigPanel = Ext.extend(Sonia.repository.PropertiesFormPanel, {
  
  formTitleText: 'Path Write Protection',
  enabledText: 'Enable',
  colPathText: 'Path',
  colNameText: 'Name',
  colGroupText: 'Is Group',
  addText: 'Add',
  removeTest: 'Remove',
  
  addIcon: 'resources/images/add.gif',
  removeIcon: 'resources/images/delete.gif',
  
  pathwpStore: null,
  
  initComponent: function(){
    this.pathwpStore = new Ext.data.ArrayStore({
      root: 'permissions',
      fields: [
        {name: 'path'},
        {name: 'name'},
        {name: 'group', type: 'boolean'}
      ],
      sortInfo: {
        field: 'path'
      }
    });
    
    this.loadPathwpPermissions(this.pathwpStore, this.item);
  
    var selectionModel = new Ext.grid.RowSelectionModel({
      singleSelect: true
    });
  
    var pathwpColModel = new Ext.grid.ColumnModel({
      defaults: {
        sortable: true,
        editable: true
      },
      columns: [{
        id: 'path',
        dataIndex: 'path',
        header: this.colPathText,
        editor: Ext.form.TextField
      },{
        id: 'name',
        dataIndex: 'name',
        header: this.colNameText,
        editor: Ext.form.TextField
      },{
        id: 'group',
        dataIndex: 'group',
        xtype: 'checkcolumn',
        header: this.colGroupText,
        width: 40,
        editable: true
      }]
    });

    var config = {
      title: this.formTitleText,
      items: [{
        xtype: 'checkbox',
        fieldLabel: this.enabledText,
        name: 'pathwpEnabled',
        inputValue: 'true',
        property: 'pathwp.enabled'
      },{
        id: 'pathwpGrid',
        xtype: 'editorgrid',
        clicksToEdit: 1,
        autoExpandColumn: 'path',
        frame: true,
        width: '100%',
        autoHeight: true,
        autoScroll: false,
        colModel: pathwpColModel,
        sm: selectionModel,
        store: this.pathwpStore,
        viewConfig: {
          forceFit:true
        },
        tbar: [{
          text: this.addText,
          scope: this,
          icon: this.addIcon,
          handler : function(){
            var Permission = this.pathwpStore.recordType;
            var p = new Permission();
            var grid = Ext.getCmp('pathwpGrid');
            grid.stopEditing();
            this.pathwpStore.insert(0, p);
            grid.startEditing(0, 0);
          }
        },{
          text: this.removeText,
          scope: this,
          icon: this.removeIcon,
          handler: function(){
            var grid = Ext.getCmp('pathwpGrid');
            var selected = grid.getSelectionModel().getSelected();
            if ( selected ){
              this.pathwpStore.remove(selected);
            }
          }
        }]
      }]
    };
    
    Ext.apply(this, Ext.apply(this.initialConfig, config));
    Sonia.pathwp.ConfigPanel.superclass.initComponent.apply(this, arguments);
  }, 
  
  loadPathwpPermissions: function(store, repository){
    if (debug){
      console.debug('load pathpw properties');
    }
    if (!repository.properties){
      repository.properties = [];
    }
    Ext.each(repository.properties, function(prop){
      if ( prop.key == 'pathwp.permissions' ){
        // todo load
      }
    });
  },
  
  storeExtraProperties: function(repository){
    if (debug){
      console.debug('store pathpw properties');
    }
    var permissionString = '';
    this.pathwpStore.data.each(function(r){
      var p = r.data;
      permissionString += '[' + p.path + ',';
      if (p.group){
        permissionString += '@';
      }
      permissionString += name + ']';
    });
    
    if (debug){
      console.debug('add pathwp permission string: ' + permissionString);
    }
    
    repository.properties.push({
      key: 'pathwp.permissions',
      value: permissionString
    });
  }
  
});

// register xtype
Ext.reg("pathwpConfigPanel", Sonia.pathwp.ConfigPanel);

// register panel
Sonia.repository.openListeners.push(function(repository, panels){
  if (Sonia.repository.isOwner(repository)){
    panels.push({
      xtype: 'pathwpConfigPanel',
      item: repository
    });
  }
});

