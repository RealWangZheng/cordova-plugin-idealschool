//
//  IdealSchoolPlugin.h
//  IdealSchool
//
//  Created by CVIT on 2018/2/7.
//

#import <Cordova/CDVPlugin.h>
#import <AMapLocationKit/AMapLocationKit.h>

@interface IdealSchool : CDVPlugin<AMapLocationManagerDelegate>
@property (nonatomic, strong) AMapLocationManager *locationManager;
@property (nonatomic, copy) AMapLocatingCompletionBlock completionBlock;



-(void)setToolbarTitle:(CDVInvokedUrlCommand*)command;
-(void)setToolbarCloseButtonShow:(CDVInvokedUrlCommand*)command;
-(void)setToolbarRightMenus:(CDVInvokedUrlCommand*)command;
-(void)selectContacts:(CDVInvokedUrlCommand*)command;
-(void)getUserInfo:(CDVInvokedUrlCommand*)command;

-(void)startLocation:(CDVInvokedUrlCommand*)command;
-(void)stopLocation:(CDVInvokedUrlCommand*)command;
@end
