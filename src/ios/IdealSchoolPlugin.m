//
//  IdealSchoolPlugin.m
//  IdealSchool
//
//  Created by CVIT on 2018/2/7.
//

#import "IdealSchoolPlugin.h"
#import "NIMContactSelectViewController.h"
#import "NTESOrganizationPersonnelViewController.h"
//引用高德地图
#import <AMapLocationKit/AMapLocationKit.h>
#import <AMapFoundationKit/AMapFoundationKit.h>
#import "NTESUserUtil.h"
#import "PGMultiView.h"
#import "IDSAllPersonnelViewController.h"
@implementation IdealSchoolPlugin

- (void)setToolbarTitle:(CDVInvokedUrlCommand*)command
{
    NSLog(@"%@",command.arguments);
   
    
    PGMultiViewController *ntes=(PGMultiViewController *)self.viewController;
    [ntes setToolbarTitle_Show: [command.arguments objectAtIndex:0]];

}


-(void)setToolbarCloseButtonShow:(CDVInvokedUrlCommand*)command
{
        NSLog(@"setToolbarCloseButtonShow   %@",command.arguments);
    
    PGMultiViewController *ntes=(PGMultiViewController *)self.viewController;
    if(command.arguments){
        [ntes setToolbarCloseButtonShow_Show:YES];
    }else{
        [ntes setToolbarCloseButtonShow_Show:NO];
    }
}

-(void)setToolbarRightMenus:(CDVInvokedUrlCommand*)command{
    NSLog(@"setToolbarRightMenus    %@",command.arguments);
    
    if(command.arguments[0] !=nil && [command.arguments[0] count]!=0){
       NSLog(@"setToolbarRightMenus2    %@",command.arguments[0][0][@"icon"]);
        PGMultiViewController *ntes=(PGMultiViewController *)self.viewController;
        [ntes setToolbarRightMenus_Show:command.arguments And:command.callbackId];
    }else{
        PGMultiViewController *ntes=(PGMultiViewController *)self.viewController;
        [ntes setToolbarRightMenus_Show:command.arguments And:command.callbackId];
    }
}
-(void)selectContacts:(CDVInvokedUrlCommand*)command{
    Boolean needMutiSelected = false;
    
        if ([command.arguments[0] integerValue]==0) {
             needMutiSelected = NO;
        }else{
            needMutiSelected = YES;
        }
    //初始化联系人选择器
    IDSAllPersonnelViewController *vc = [[IDSAllPersonnelViewController alloc]init];
    vc.isMulti=needMutiSelected;
    vc.title=command.arguments[1];
    
//    vc.isBuildTeam=true;
    [[SelectArrayManager sharedManager] toReturnSelectResultsArray:^(NSArray *uids) {
        NSLog(@"%@",uids);
        NSData *data=[NSJSONSerialization dataWithJSONObject:uids options:NSJSONWritingPrettyPrinted error:nil];
        
        NSString *jsonStr=[[NSString alloc]initWithData:data encoding:NSUTF8StringEncoding];
        //给js返回
                CDVPluginResult* pluginResult = nil;
                if (uids != nil) {
                    pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsArray:jsonStr];
                    NSLog(@"%@",jsonStr);
                    
                } else {
                    pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"jsonStr was null"];
                }
                [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    }];
    [self.viewController.navigationController pushViewController:vc animated:YES];
    
//     __weak typeof(self) weakSelf = self;
//    NIMContactFriendSelectConfig *config = [[NIMContactFriendSelectConfig alloc] init];
////    NSLog(@"command.arguments[0]=====    %@",command.arguments[0]);
////    NSLog(@"command.arguments[1]=====    %@",command.arguments[1]);
////      NSLog(@"command.arguments[2]=====    %@",command.arguments[2]);
//    
//    NSLog(@"command.callbackId=====    %@",command.callbackId);
//    
//    if ([command.arguments[0] integerValue]==0) {
//         config.needMutiSelected = NO;
//    }else{
//        config.needMutiSelected = YES;
//    }
//    
//
//    NIMContactSelectViewController *vc = [[NIMContactSelectViewController alloc] initWithConfig:config];
//    vc.finshBlock = ^(NSArray *array){
//
//        NSLog(@"session array    %@",array);
//        //给js返回
//        CDVPluginResult* pluginResult = nil;
//        if (array != nil) {
//            pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsArray:array];
//        } else {
//            pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"Arg was null"];
//        }
//        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
//    };
//    [vc show];
}


//停止定位
-(void)stopLocation:(CDVInvokedUrlCommand*)command{
//    [self.locationManager stopUpdatingLocation];
}

//init Config
-(void) initConfig{
    if(!self.locationManager){
        //set APIKey
        [AMapServices sharedServices].apiKey = @"c0d2580fd0fbe80b6334863eaf0dd60d";
        //init locationManager
        self.locationManager = [[AMapLocationManager alloc]init];
        self.locationManager.delegate = self;
        //set DesiredAccuracy
        [self.locationManager setDesiredAccuracy:kCLLocationAccuracyHundredMeters];
        
        //        [self.locationManager startUpdatingLocation];
    }
}
//开始定位
- (void)startLocation:(CDVInvokedUrlCommand*)command
{
    [self initConfig];
    
    //   定位超时时间，最低2s，此处设置为5s
    self.locationManager.locationTimeout = 5;
    //   逆地理请求超时时间，最低2s，此处设置为5s
    self.locationManager.reGeocodeTimeout = 5;
    
   __weak typeof(self) weakSelf = self;
    [self.locationManager requestLocationWithReGeocode:YES completionBlock:^(CLLocation *location, AMapLocationReGeocode *regeocode, NSError *error) {
        
        if (error) {
            NSDictionary *addressInfo = @{@"status": @"定位失败"};
            CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsDictionary:addressInfo];
            [weakSelf.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
        } else {
            NSDateFormatter *dateFormatter = [[NSDateFormatter alloc] init];
            [dateFormatter setDateFormat:@"yyyy-MM-dd HH:mm:ss"];
            
//            NSDictionary *addressInfo = @{@"latitude": [NSNumber numberWithDouble:location.coordinate.latitude],
//                                          @"longitude": [NSNumber numberWithDouble:location.coordinate.longitude],
//                                          @"speed": [NSNumber numberWithDouble:location.speed],
//                                          @"bearing": [NSNumber numberWithDouble:location.course],
//                                          @"accuracy": [NSNumber numberWithDouble:location.horizontalAccuracy],
//                                          @"date": [dateFormatter stringFromDate:location.timestamp],
//                                          @"address": regeocode.formattedAddress ?: @"",
//                                          @"country": regeocode.country ?: @"",
//                                          @"province": regeocode.province ?: @"",
//                                          @"city": regeocode.city ?: @"",
//                                          @"cityCode": regeocode.citycode ?: @"",
//                                          @"district": regeocode.district ?: @"",
//                                          @"street": regeocode.street ?: @"",
//                                          @"streetNum": regeocode.number ?: @"",
//                                          @"adCode": regeocode.adcode ?: @"",
//                                          @"poiName": regeocode.POIName ?: @"",
//                                          @"aoiName": regeocode.AOIName ?: @""};
            NSDictionary *addressInfo = @{@"latitude": [NSNumber numberWithDouble:location.coordinate.latitude],
                                          @"longitude": [NSNumber numberWithDouble:location.coordinate.longitude],
                                          @"status": @"定位成功"};
            NSLog(@"获取定位返回的JSON信息： %@",addressInfo);
            CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:addressInfo];
            [weakSelf.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
            
        }
    }];
}
//获取当前登录人员信息
-(void)getUserInfo:(CDVInvokedUrlCommand*)command{
       __weak typeof(self) weakSelf = self;
    CDVPluginResult* pluginResult= nil;
    AppDelegate *appDelegate=[[UIApplication sharedApplication] delegate];
    NSMutableDictionary * category = [[NSMutableDictionary alloc] init];
   
    {

        NSMutableDictionary * app = [[NSMutableDictionary alloc] init];
        if (appDelegate.school_server_url) {
            [app setObject:appDelegate.school_server_url forKey:@"server_url"];
        }else{
            [app setObject:@"" forKey:@"server_url"];
        }
        
        if (appDelegate.school_logo_url) {
            [app setObject:appDelegate.school_logo_url forKey:@"logo_url"];
        }else{
            [app setObject:@"" forKey:@"logo_url"];
        }
        if (appDelegate.school_root_dept_id) {
                [app setObject:appDelegate.school_root_dept_id forKey:@"root_dept_id"];
        }else{
            [app setObject:@"" forKey:@"root_dept_id"];
        }
        
        if (appDelegate.school_code) {
           [app setObject:appDelegate.school_code forKey:@"code"];
        }else{
            [app setObject:@"" forKey:@"code"];
        }
        
        if (appDelegate.school_id) {
              [app setObject:appDelegate.school_id forKey:@"id"];
        }else{
            [app setObject:@"" forKey:@"id"];
        }
        
        if (appDelegate.school_name) {
                    [app setObject:appDelegate.school_name forKey:@"name"];
        }else{
            [app setObject:@"" forKey:@"name"];
        }
        [category setObject:app forKey:@"school"];
    }
      NIMUser *me = [[NIMSDK sharedSDK].userManager userInfo:[[NIMSDK sharedSDK].loginManager currentAccount]];
//        NSLog(@"调用js返回的 当前用户的id  %@",me.userId);
    {
        NSMutableDictionary * app = [[NSMutableDictionary alloc] init];
        if (appDelegate.user_id) {
            [app setObject:appDelegate.user_id forKey:@"id"];
        }else{
            [app setObject:@"" forKey:@"id"];
        }

        if (me.userId) {
            [app setObject:me.userId forKey:@"accid"];
        }else{
            [app setObject:@"" forKey:@"accid"];
        }
        
        if (appDelegate.user_username) {
            [app setObject:appDelegate.user_username forKey:@"username"];
        }else{
            [app setObject:@"" forKey:@"username"];
        }
        
       if (me.userInfo.nickName) {
        [app setObject:me.userInfo.nickName forKey:@"name"];
       }else{
        [app setObject:@"" forKey:@"name"];
       }
        
        if (me.userInfo.mobile) {
            [app setObject:me.userInfo.mobile forKey:@"mobile"];
        }else{
            [app setObject:@"" forKey:@"mobile"];
        }

        if (me.userInfo.avatarUrl) {
            [app setObject:me.userInfo.avatarUrl forKey:@"icon"];
        }else{
            [app setObject:@"" forKey:@"name"];
        }

        if (me.userInfo.gender) {
            [app setObject:[NTESUserUtil genderString:me.userInfo.gender] forKey:@"gender"];
        }else{
            [app setObject:@"" forKey:@"gender"];
        }
           [category setObject:app forKey:@"user"];
    }
    NSData *data=[NSJSONSerialization dataWithJSONObject:category options:NSJSONWritingPrettyPrinted error:nil];
    NSString *jsonStr=[[NSString alloc]initWithData:data encoding:NSUTF8StringEncoding];
    NSLog(@"jsonStr==%@",jsonStr);
    
    NSLog(@"调用js返回的 %@",category);
    
    if(me){
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:jsonStr];
    }else{
          pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"jsonStr was null"];
    }
    [weakSelf.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}
@end
