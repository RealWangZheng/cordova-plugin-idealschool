package com.idealworkshops.idealschool.apps.cordova;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.alibaba.fastjson.util.Base64;
import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.google.gson.Gson;
import com.idealworkshops.idealschool.config.preference.Preferences;
import com.idealworkshops.idealschool.contact.activity.NewContactsSelectActivity;
import com.idealworkshops.idealschool.data.models.School;
import com.idealworkshops.idealschool.data.models.UsersOfDepartmentItem;
import com.netease.nim.uikit.api.NimUIKit;
import com.netease.nim.uikit.api.model.SimpleCallback;
import com.netease.nim.uikit.business.contact.selector.activity.ContactSelectActivity;
import com.netease.nimlib.sdk.uinfo.model.NimUserInfo;
import com.netease.nimlib.sdk.uinfo.model.UserInfo;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaArgs;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import phonegap.pgmultiview.PGMultiViewActivity;

import static android.text.Spanned.SPAN_INCLUSIVE_INCLUSIVE;


@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class IdealSchoolPlugin extends CordovaPlugin {
    public final static String TAG = IdealSchoolPlugin.class.getSimpleName();

    class MenuDefinition {
        final JSONArray mDefinition;
        final CallbackContext mCallbackContext;


        public MenuDefinition(JSONArray definition, final CallbackContext callbackContext) {
            mDefinition = definition;
            mCallbackContext = callbackContext;
        }

        public void createMenu(final Menu menu, final Activity ctx) {
            ctx.runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    int count = mDefinition.length();
                    if (count == 1) {
                        for (int i = 0; i < mDefinition.length(); ++i) {
                            try {
                                final JSONObject itemDef = mDefinition.getJSONObject(i);
                                final String title = itemDef.isNull("action") ? "" : itemDef.getString("action");
                                final String icon = itemDef.isNull("icon") ? "" : itemDef.getString("icon");
                                MenuItem item = menu.add(title);

                                if (count == 1) {

                                    if (!TextUtils.isEmpty(icon)) {
                                        try {
                                            byte[] aa = Base64.decodeFast(icon);
                                            if (aa != null) {
                                                Bitmap bitmap =  BitmapFactory.decodeByteArray(aa, 0, aa.length);
                                                float newW = pxFromDp(ctx, 20);
                                                float newH = pxFromDp(ctx, 20);

                                                Bitmap bitmap2 = Bitmap.createScaledBitmap(bitmap, (int)newW, (int)newH, true);
                                                Drawable image = new BitmapDrawable(ctx.getResources(),bitmap2);


                                                item.setIcon(image);
                                                //item.setIcon(R.drawable.nim_actionbar_search_dark_icon);
                                            }
                                        } catch (Exception ex) {

                                        }

                                    }
                                    item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
                                }

                                SpannableString s = new SpannableString(item.getTitle()); //get text from our menu item.
                                s.setSpan(new ForegroundColorSpan(0xFFFFFFFF), 0, s.length(), 0);
                                int textSize1 = spToPx(16,ctx );
                                s.setSpan(new AbsoluteSizeSpan(textSize1), 0, s.length(), SPAN_INCLUSIVE_INCLUSIVE);
                                item.setTitle(s);
                            } catch (JSONException e) {
                                fire("ERROR processing menu" + e);
                            }
                        }
                    } else {
                        for (int i = 0; i < mDefinition.length(); ++i) {
                            try {
                                final JSONObject itemDef = mDefinition.getJSONObject(i);
                                final String title = itemDef.isNull("action") ? "" : itemDef.getString("action");
                                MenuItem item = menu.add(title);
                                SpannableString s = new SpannableString(item.getTitle()); //get text from our menu item.
                                s.setSpan(new ForegroundColorSpan(0xFFFFFFFF), 0, s.length(), 0);
                                int textSize1 = spToPx(16,ctx );
                                s.setSpan(new AbsoluteSizeSpan(textSize1), 0, s.length(), SPAN_INCLUSIVE_INCLUSIVE);
                                item.setTitle(s);
                            } catch (JSONException e) {
                                fire("ERROR processing menu" + e);
                            }
                        }
                    }
                    //ActivityCompat.invalidateOptionsMenu(ctx);
                    fire("SUCCESS processing menu");
                }
            });
        }

        public void fire(String action) {
            PluginResult result = new PluginResult(PluginResult.Status.OK, action);
            result.setKeepCallback(true);
            mCallbackContext.sendPluginResult(result);
        }
    }

    public static int spToPx(float sp, Context context) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, context.getResources().getDisplayMetrics());
    }

    public static float pxFromDp(final Context context, final float dp) {
        return dp * context.getResources().getDisplayMetrics().density;
    }


    private MenuDefinition menuDef = null;

    private CallbackContext closeContext = null;
    private CallbackContext rightContext = null;
    private CallbackContext selectContactContext = null;
    private String rightAction = null;


    //声明AMapLocationClient类对象
    public AMapLocationClient locationClient = null;
    //声明定位参数
    public AMapLocationClientOption locationOption = null;


    /**
     * JS回调接口对象
     */
    public static CallbackContext locationcb = null;


    public static CallbackContext getUserInfoCallback = null;

    final Gson gson = new Gson();

    @Override
    public boolean execute(String action, CordovaArgs args, CallbackContext callbackContext) throws JSONException {
        Log.d(TAG, "execute" + " action=" + action);
        if ("show".equals(action)) {
            return true;
        } else if ("setToolbarTitle".equals(action)) {
            final String title = args.getString(0);
            Log.d(TAG, "execute" + " action=" + action + " appTitle=" + title);
            final CordovaInterface ctx = cordova;
            if (ctx.getActivity() != null) {
                Activity activity = ctx.getActivity();
                if (activity != null) {
                    if (activity instanceof PGMultiViewActivity) {
                        final PGMultiViewActivity pa = (PGMultiViewActivity) activity;
                        if (pa.getSupportToolbar() != null) {
                            pa.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    pa.setToolbarTitle(true, title);
                                    //pa.getSupportToolbar().setTitle(title);
                                    ActivityCompat.invalidateOptionsMenu(pa);
                                }
                            });
                            return true;
                        }
                    }
                }
            }
            return false;
        } else if ("setToolbarCloseButtonShow".equals(action)) {
            final boolean button = args.getBoolean(0);
            Log.d(TAG, "execute" + " action=" + action + " button=" + button);
            final CordovaInterface ctx = cordova;
            if (ctx.getActivity() != null) {
                Activity activity = ctx.getActivity();
                if (activity != null) {
                    if (activity instanceof PGMultiViewActivity) {
                        final PGMultiViewActivity pa = (PGMultiViewActivity) activity;
                        if (pa.getSupportToolbar() != null) {
                            closeContext = callbackContext;
                            pa.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    pa.setToolbarCloseButtonShow(button);
                                    ActivityCompat.invalidateOptionsMenu(pa);
                                }
                            });
                            return true;
                        }
                    }
                }
            }
            return true;
        } else if ("setToolbarRightMenus".equals(action)) {
            rightAction = null;
            final JSONArray menusCfg = args.getJSONArray(0);
            Log.d(TAG, "execute" + " action=" + action + " menusCfg=" + menusCfg);
            if (menusCfg.length() == 0) {
                menuDef = null;
                final CordovaInterface ctx = cordova;
                rightContext = callbackContext;
                if (ctx.getActivity() != null) {
                    Activity activity = ctx.getActivity();
                    if (activity != null) {
                        if (activity instanceof PGMultiViewActivity) {
                            final PGMultiViewActivity pa = (PGMultiViewActivity) activity;
                            if (pa.getSupportToolbar() != null) {
                                pa.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        menuDef = null;
                                        pa.showRightButton(false, "");
                                        rightAction = null;
                                        ActivityCompat.invalidateOptionsMenu(pa);
                                    }
                                });
                                return true;
                            }
                        }
                    }
                }
            } else if (menusCfg.length() == 1) {
                menuDef = new MenuDefinition(menusCfg, callbackContext);
                final CordovaInterface ctx = cordova;
                if (ctx.getActivity() != null) {
                    final Activity activity = ctx.getActivity();
                    if (activity != null) {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ActivityCompat.invalidateOptionsMenu(activity);
                            }
                        });
                        return true;
                    }
                }
            } else {
                menuDef = new MenuDefinition(menusCfg, callbackContext);
                final CordovaInterface ctx = cordova;
                if (ctx.getActivity() != null) {
                    final Activity activity = ctx.getActivity();
                    if (activity != null) {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ActivityCompat.invalidateOptionsMenu(activity);
                            }
                        });
                        return true;
                    }
                }
            }
            return false;
        } else if ("selectContacts".equals(action)) {
            final boolean isMulti = args.getBoolean(0);
            final String title = args.getString(1);
            //final JSONArray defaultSelected = args.getJSONArray(2);
/*            final ArrayList<String> defaultSelectedIds = new ArrayList<>();
            if (defaultSelected != null) {
                for (int i = 0; i < defaultSelected.length(); i++) {
                    defaultSelectedIds.add(defaultSelected.getString(i));
                    //Log.d(TAG, "ids=" + defaultSelectedIds.get(i));
                }
            }*/

            Log.d(TAG, "execute" + " action=" + action + " isMulti=" + isMulti + ",title=" + title);
            final CordovaInterface ctx = cordova;
            if (ctx.getActivity() != null) {
                final Activity activity = ctx.getActivity();
                if (activity != null) {
                    if (activity instanceof PGMultiViewActivity) {
                        final PGMultiViewActivity pa = (PGMultiViewActivity) activity;
                        if (pa.getSupportToolbar() != null) {
                            selectContactContext = callbackContext;
                            pa.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {


                                    ContactSelectActivity.Option option = new ContactSelectActivity.Option();
                                    option.multi = isMulti;
                                    option.title = title;
                                    option.maxSelectNum = 2000;
                                    if (!isMulti) {
                                        option.maxSelectNum = 1;
                                    }

                                    //option.alreadySelectedAccounts = defaultSelectedIds;


                                    Intent intent = new Intent();
                                    intent.putExtra(ContactSelectActivity.EXTRA_DATA, option);
                                    intent.setClass(activity, NewContactsSelectActivity.class);
                                    ctx.startActivityForResult(IdealSchoolPlugin.this, intent, PGMultiViewActivity.CONTACT_SELECT_REQUEST_CODE);
                                }
                            });
                            return true;
                        }
                    }
                }
            }
            return true;
        } else if ("startLocation".equals(action)) {
            //http://blog.csdn.net/u010730897/article/details/54969638
            //https://www.jianshu.com/p/065dfcc46f26
            //https://github.com/DeepAQ/AQMissionHelper/blob/master/android_plugin/src/android/AMapBridge.java
            //https://github.com/supaide/android_plugins/blob/master/AMap/app/src/main/java/org/apache/cordova/plugin/map/amap/AMap.java

            locationcb = callbackContext;
            PluginResult pluginResult = new PluginResult(PluginResult.Status.NO_RESULT);
            pluginResult.setKeepCallback(true);
            locationcb.sendPluginResult(pluginResult);

            cordova.getThreadPool().execute(new Runnable() {
                public void run() {
                    IdealSchoolPlugin.this.startLocation();
                }
            });

            return true;
        } else if ("stopLocation".equals(action)) {
            cordova.getThreadPool().execute(new Runnable() {
                public void run() {
                    IdealSchoolPlugin.this.stopLocation();
                }
            });

            PluginResult pluginResult = new PluginResult(PluginResult.Status.OK);
            callbackContext.sendPluginResult(pluginResult);
            return true;
        } else if ("getUserInfo".equals(action)) {

            final CordovaInterface ctx = cordova;
            if (ctx.getActivity() != null) {
                final Activity activity = ctx.getActivity();

                final SchoolUserInfo sui = new SchoolUserInfo();


                if (Preferences.getDSSchoolInfo() != null) {
                    sui.school = Preferences.getDSSchoolInfo();

                    if (sui.school == null) {
                        PluginResult pluginResult = new PluginResult(PluginResult.Status.ERROR);
                        callbackContext.sendPluginResult(pluginResult);
                        return true;
                    }

                    sui.user = new UsersOfDepartmentItem();

                    sui.user.id = Preferences.getDSUserID();
                    sui.user.accid = Preferences.getNIMUserAccount();
                    sui.user.username = Preferences.getDSUserLoginName();

                    if (TextUtils.isEmpty(sui.user.id) ||
                            TextUtils.isEmpty(sui.user.accid) ||
                            TextUtils.isEmpty(sui.user.username)
                            ) {
                        PluginResult pluginResult = new PluginResult(PluginResult.Status.ERROR);
                        callbackContext.sendPluginResult(pluginResult);
                        return true;
                    }

                    final UserInfo uii = NimUIKit.getUserInfoProvider().getUserInfo(sui.user.accid);
                    if (uii == null) {
                        getUserInfoCallback = callbackContext;
                        PluginResult pluginResult = new PluginResult(PluginResult.Status.NO_RESULT);
                        pluginResult.setKeepCallback(true);
                        callbackContext.sendPluginResult(pluginResult);

                        NimUIKit.getUserInfoProvider().getUserInfoAsync(sui.user.accid, new SimpleCallback() {
                            @Override
                            public void onResult(boolean success, Object result, int code) {
                                if (success) {
                                    fillSchoolUserInfo(sui, uii);

                                    PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, gson.toJson(sui));
                                    getUserInfoCallback.sendPluginResult(pluginResult);
                                } else {
                                    PluginResult pluginResult = new PluginResult(PluginResult.Status.ERROR);
                                    getUserInfoCallback.sendPluginResult(pluginResult);
                                }
                            }
                        });

                        return true;
                    } else {

                        fillSchoolUserInfo(sui, uii);

                        PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, gson.toJson(sui));
                        callbackContext.sendPluginResult(pluginResult);
                        return true;
                    }
                }
            }

            PluginResult pluginResult = new PluginResult(PluginResult.Status.ERROR);
            callbackContext.sendPluginResult(pluginResult);
            return true;
        }
        return super.execute(action, args, callbackContext);
    }

    private void fillSchoolUserInfo(SchoolUserInfo sui, UserInfo uii) {
        if (uii == null) return;

        NimUserInfo nuii = (NimUserInfo) uii;

        sui.user.name = nuii.getName();
        sui.user.mobile = nuii.getMobile();
        sui.user.icon = nuii.getAvatar();

        int gen = 0;

        switch (nuii.getGenderEnum()) {
            case MALE:
                gen = 1;
                break;
            case FEMALE:
                gen = 2;
                break;
        }
        sui.user.gender = String.valueOf(gen);
    }

    public class SchoolUserInfo {
        School school;
        UsersOfDepartmentItem user;
    }

    private void initLocation() {
        //初始化client
        locationClient = new AMapLocationClient(this.webView.getContext());
        //设置定位参数
        locationClient.setLocationOption(getDefaultOption());
        // 设置定位监听
        locationClient.setLocationListener(locationListener);
    }

    private void startLocation() {
        if (locationClient == null) {
            this.initLocation();
        }
        // 启动定位
        locationClient.startLocation();
    }

    private void stopLocation() {
        if (locationClient != null) {
            // 停止定位
            locationClient.stopLocation();
            destroyLocation();
        }
    }

    private void destroyLocation() {
        if (null != locationClient) {
            /**
             * 如果AMapLocationClient是在当前Activity实例化的，
             * 在Activity的onDestroy中一定要执行AMapLocationClient的onDestroy
             */
            locationClient.onDestroy();
            locationClient = null;
            locationOption = null;
        }
    }

    private AMapLocationClientOption getDefaultOption() {
        AMapLocationClientOption mOption = new AMapLocationClientOption();
        mOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);//可选，设置定位模式，可选的模式有高精度、仅设备、仅网络。默认为高精度模式
        mOption.setGpsFirst(true);//可选，设置是否gps优先，只在高精度模式下有效。默认关闭
        mOption.setHttpTimeOut(30000);//可选，设置网络请求超时时间。默认为30秒。在仅设备模式下无效
        mOption.setInterval(2000);//可选，设置定位间隔。默认为2秒
        mOption.setNeedAddress(true);//可选，设置是否返回逆地理地址信息。默认是true
        mOption.setOnceLocation(false);//可选，设置是否单次定位。默认是false
        mOption.setOnceLocationLatest(false);//可选，设置是否等待wifi刷新，默认为false.如果设置为true,会自动变为单次定位，持续定位时不要使用
        AMapLocationClientOption.setLocationProtocol(AMapLocationClientOption.AMapLocationProtocol.HTTP);//可选， 设置网络请求的协议。可选HTTP或者HTTPS。默认为HTTP
        mOption.setSensorEnable(false);//可选，设置是否使用传感器。默认是false
        mOption.setWifiScan(true); //可选，设置是否开启wifi扫描。默认为true，如果设置为false会同时停止主动刷新，停止以后完全依赖于系统刷新，定位位置可能存在误差
        mOption.setLocationCacheEnable(true); //可选，设置是否使用缓存定位，默认为true
        return mOption;
    }

    AMapLocationListener locationListener = new AMapLocationListener() {
        @Override
        public void onLocationChanged(AMapLocation location) {
            try {
                JSONObject json = new JSONObject();
                if (null != location) {
                    //解析定位结果
                    //errCode等于0代表定位成功，其他的为定位失败，具体的可以参照官网定位错误码说明
                    if (location.getErrorCode() == 0) {
                        json.put("status", "定位成功");
                        //定位类型
                        json.put("type", location.getLocationType());
                        //纬度
                        json.put("latitude", location.getLatitude());
                        //经度
                        json.put("longitude", location.getLongitude());
                        //精度
                        json.put("accuracy", location.getAccuracy());
                        //角度
                        json.put("bearing", location.getBearing());
                        // 获取当前提供定位服务的卫星个数
                        //星数
                        json.put("satellites", location.getSatellites());
                        //国家
                        json.put("country", location.getCountry());
                        //省
                        json.put("province", location.getProvince());
                        //市
                        json.put("city", location.getCity());
                        //城市编码
                        json.put("citycode", location.getCityCode());
                        //区
                        json.put("district", location.getDistrict());
                        //区域码
                        json.put("adcode", location.getAdCode());
                        //地址
                        json.put("address", location.getAddress());
                        //兴趣点
                        json.put("poi", location.getPoiName());
                        //兴趣点
                        json.put("time", location.getTime());
                    } else {
                        json.put("status", "定位失败");
                        json.put("errcode", location.getErrorCode());
                        json.put("errinfo", location.getErrorInfo());
                        json.put("detail", location.getLocationDetail());
                    }
                    //定位之后的回调时间
                    json.put("backtime", System.currentTimeMillis());
                } else {

                }
                PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, json);
                pluginResult.setKeepCallback(true);
                locationcb.sendPluginResult(pluginResult);
            } catch (JSONException e) {
                PluginResult pluginResult = new PluginResult(PluginResult.Status.ERROR, e.getMessage());
                pluginResult.setKeepCallback(true);
                locationcb.sendPluginResult(pluginResult);
            } finally {
                locationClient.stopLocation();
            }
        }
    };


    private PGMultiViewActivity getPGActivity() {
        if (cordova != null && cordova.getActivity() != null) {
            if (cordova.getActivity() instanceof PGMultiViewActivity) {
                PGMultiViewActivity pa = (PGMultiViewActivity) cordova.getActivity();
                return pa;
            }
        }
        return null;
    }


    private void setBusyShow(boolean show) {
        if (show) {
            if (getPGActivity() != null && getPGActivity().progressBar != null) {
                getPGActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        getPGActivity().progressBar.setVisibility(View.VISIBLE);
                    }
                });
            }
        } else {
            if (getPGActivity() != null && getPGActivity().progressBar != null) {
                getPGActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        getPGActivity().progressBar.setVisibility(View.GONE);
                    }
                });
            }
        }
    }

    @Override
    public Object onMessage(String id, Object data) {
        Log.d(TAG, "onMessage" + " id=" + id);
        switch (id) {
            case "onCreateOptionsMenu":
                Menu m = (Menu) data;
                if (m != null) {
                    m.clear();
                }
                if (menuDef != null) {
                    if (cordova != null && cordova.getActivity() != null) {
                        menuDef.createMenu((Menu) data, cordova.getActivity());
                    }
                }
                break;
            case "onOptionsItemSelected":
                if (menuDef != null) {
                    menuDef.fire(data.toString());
                }
                break;
            case "onButtonClick":
                if (data instanceof String) {
                    String dd = (String) data;
                    if (dd.equals("action_back")) {

                    } else if (dd.equals("action_close")) {
                        if (closeContext != null) {
                            PluginResult result = new PluginResult(PluginResult.Status.OK, (String) data);
                            result.setKeepCallback(true);
                            closeContext.sendPluginResult(result);
                        }

                    } else if (dd.equals("action_right")) {
                        if (rightContext != null) {
                            PluginResult result = new PluginResult(PluginResult.Status.OK, rightAction);
                            result.setKeepCallback(true);
                            rightContext.sendPluginResult(result);
                        }
                    }
                }
                if (menuDef != null) {
                    menuDef.fire(data.toString());
                }
                break;
            case "onPageStarted":
                setBusyShow(true);
                break;
            case "onPageFinished":
                setBusyShow(false);
                break;
            case "onReceivedError":
                setBusyShow(false);
                break;
            default:
                return null;
        }

        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        Log.d(TAG, "onActivityResult");

        if (PGMultiViewActivity.CONTACT_SELECT_REQUEST_CODE == requestCode) {
            if (resultCode == Activity.RESULT_OK) {
                final ArrayList<String> selected = intent.getStringArrayListExtra(ContactSelectActivity.RESULT_DATA);


                String aa = intent.getStringExtra(NewContactsSelectActivity.RESULT_DATA_EX);
                /*
                ArrayList<UsersOfDepartmentItem> dd = null;
                if (!TextUtils.isEmpty(aa)) {
                    try {
                        dd = gson.fromJson(aa, new TypeToken<ArrayList<UsersOfDepartmentItem>>() {
                        }.getType());
                    } catch (Exception ex) {
                    }
                }
                */

                /*
                ArrayList<String> ret = new ArrayList<String>();

                if (selected != null && !selected.isEmpty()) {
                    ret.addAll(selected);
                }*/

                String jsonStr = "[]";

                if (!TextUtils.isEmpty(aa)) {
                    jsonStr = aa;
                }

                if (selectContactContext != null) {
                    PluginResult result = new PluginResult(PluginResult.Status.OK, jsonStr);
                    result.setKeepCallback(true);
                    selectContactContext.sendPluginResult(result);
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                if (selectContactContext != null) {
                    PluginResult result = new PluginResult(PluginResult.Status.ERROR, "选择取消");
                    result.setKeepCallback(true);
                    selectContactContext.sendPluginResult(result);
                }
            }
        }
    }
}
