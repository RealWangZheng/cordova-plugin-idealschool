      
		var exec = require("cordova/exec");         
		module.exports = {             
			setToolbarTitle: function(title){                 
				exec(                 
					function (msg){},
					function (msg){}, 
					"IdealSchool", 
					"setToolbarTitle", 
					[title] );             
			},             
			setToolbarCloseButtonShow:function(show){                 
				exec(                 
					function (msg){}, 
					function (msg){}, 
					"IdealSchool",                 
					"setToolbarCloseButtonShow",                 
					[show]                 
					);             
			},             
			setToolbarRightMenus:function(successCallback, errorCallback,menuItems){                 
				exec(         
					successCallback, 
					errorCallback,                 
					"IdealSchool",                 
					"setToolbarRightMenus",                 
					[menuItems]                 
					);             
			},             
			selectContacts:function(successCallback, errorCallback,isMulti,title){                 
				exec(                 
					successCallback, 
					errorCallback,                 
					"IdealSchool",                 
					"selectContacts",                 
					[isMulti,title]                 
					);             
			},             
			startLocation:function (successCallback,errorCallback) {                 
				exec(                 successCallback,errorCallback,                 
					'IdealSchool',                 
					'startLocation',
					[]
					);             
			},             
			stopLocation:function (successCallback,errorCallback) {                 
				exec(                 successCallback,errorCallback,                 
					'IdealSchool',                 
					'stopLocation',
					[]
					);             
			},             
			getUserInfo:function (successCallback,errorCallback) {                 
				exec(                 
					successCallback,
					errorCallback,                 
					'IdealSchool',                 
					'getUserInfo',
					[]
					);             
			}         
		} 
	