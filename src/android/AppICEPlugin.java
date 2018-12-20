//
// AppICEPlugin.java
//
// AppICE, 08/11/16.
//
// AppICE analytics Plugin for Cordova
// www.appice.io
//

package com.appice.cordova;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.annotation.Retention;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import semusi.activitysdk.Api;
import semusi.activitysdk.ContextData;
import semusi.activitysdk.ContextSdk;
import semusi.activitysdk.SdkConfig;
import semusi.activitysdk.User;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

public class AppICEPlugin extends CordovaPlugin {
    private static final String TAG = "AppICE_CP";

    HashMap<String, CallbackContext> callbackIds = new HashMap<String, CallbackContext>();

    private static final Map<String, Method> exportedMethods;

    @Retention(RUNTIME)
    @interface CordovaMethod {

    }

    static {
        HashMap<String, Method> methods = new HashMap<String, Method>();

        final List<Method> allMethods = new ArrayList<Method>(Arrays.asList(AppICEPlugin.class.getDeclaredMethods()));
        for (final Method method : allMethods) {
            if (method.isAnnotationPresent(CordovaMethod.class)) {
                methods.put(method.getName(), method);
            }
        }

        exportedMethods = methods;
    }

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);

        onNewIntent(cordova.getActivity().getIntent());
    }

    private void validateCordovaIntegration(Context ctx) {
        try {
            PackageManager pm = ctx.getPackageManager();

            Intent receiverIntent = new Intent();
            receiverIntent.setClass(ctx, CampaignCampsReceiver.class);
            List<ResolveInfo> receivers = pm.queryBroadcastReceivers(receiverIntent, 0);
            if (receivers == null || receivers.size() <= 0)
                Toast.makeText(ctx, "Missing Receiver entry in AndroidManifest : CampaignCampsReceiver", Toast.LENGTH_LONG).show();

            Intent serviceIntent = new Intent();
            serviceIntent.setClass(ctx, NotificationEventService.class);
            ResolveInfo services = pm.resolveService(serviceIntent, 0);
            if (services == null)
                Toast.makeText(ctx, "Missing Service entry in AndroidManifest : NotificationEventService", Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @CordovaMethod
    private void startContext(JSONArray data, CallbackContext callbackContext) {
        try {
            validateCordovaIntegration(cordova.getActivity().getApplicationContext());

            SdkConfig config = new SdkConfig();

            try {
                String gcmid = (String) data.getJSONObject(0).get("gcmID");
                if (gcmid != null && gcmid.length() > 0) {
                    config.setGcmSenderId(gcmid);
                }
            } catch (Exception e) {
            }

            // Init sdk with your config
            Api.startContext(cordova.getActivity().getApplicationContext(), config);

            callbackContext.success();
        } catch (Exception e) {
            e.printStackTrace();
            callbackContext.error(e.getMessage());
        }
    }

    @CordovaMethod
    private void initSdk(JSONArray data, CallbackContext callbackContext) {
        try {
            validateCordovaIntegration(cordova.getActivity().getApplicationContext());

            SdkConfig config = new SdkConfig();

            String appId = "", appKey = "", apiKey = "";
            try {
                JSONObject root = data.getJSONObject(0);
                String gcmid = root.optString("gcmID");
                if (gcmid != null && gcmid.length() > 0) {
                    config.setGcmSenderId(gcmid);
                }

                appId = root.optString("appID");
                appKey = root.optString("appKey");
                apiKey = root.optString("apiKey");
            } catch (Exception e) {
            }

            // Init sdk with your config
            Api.initSdk(appId, appKey, apiKey, cordova.getActivity().getApplicationContext());
            Api.startContext(cordova.getActivity().getApplicationContext(), config);

            callbackContext.success();
        } catch (Exception e) {
            e.printStackTrace();
            callbackContext.error(e.getMessage());
        }
    }

    @CordovaMethod
    private void stopContext(JSONArray data, CallbackContext callbackContext) {
        // Stop sdk
        try {
            Api.stopContext(cordova.getActivity().getApplicationContext());
            callbackContext.success();
        } catch (Exception e) {
            e.printStackTrace();
            callbackContext.error(e.getMessage());
        }
    }

    @CordovaMethod
    private void isSemusiSensing(JSONArray data, CallbackContext callbackContext) {
        // Check whether appice sdk is running or not
        try {
            boolean key = ContextSdk.isSemusiSensing(cordova.getActivity().getApplicationContext());
            if (key)
                callbackContext.success(1);
            else
                callbackContext.success(0);
        } catch (Exception e) {
            e.printStackTrace();
            callbackContext.error(e.getMessage());
        }
    }

    @CordovaMethod
    private void setAsTestDevice(JSONArray data, CallbackContext callbackContext) {
        // Set current device is marked as test device
        try {
            ContextSdk.setAsTestDevice(true, cordova.getActivity().getApplicationContext());
            callbackContext.success();
        } catch (Exception e) {
            e.printStackTrace();
            callbackContext.error(e.getMessage());
        }
    }

    @CordovaMethod
    private void removeAsTestDevice(JSONArray data, CallbackContext callbackContext) {
        // Remove current device is marked as test device
        try {
            ContextSdk.setAsTestDevice(false, cordova.getActivity().getApplicationContext());
            callbackContext.success();
        } catch (Exception e) {
            e.printStackTrace();
            callbackContext.error(e.getMessage());
        }
    }

    @CordovaMethod
    private void getIsTestDevice(JSONArray data, CallbackContext callbackContext) {
        // Check whether current device is marked as test device
        try {
            boolean key = ContextSdk.getIsTestDevice(cordova.getActivity().getApplicationContext());
            if (key)
                callbackContext.success(1);
            else
                callbackContext.success(0);
        } catch (Exception e) {
            e.printStackTrace();
            callbackContext.error(e.getMessage());
        }
    }

    @CordovaMethod
    private void openPlayServiceUpdate(JSONArray data, CallbackContext callbackContext) {
        // Open up google play service udpate UI for user
        try {
            ContextSdk.openPlayServiceUpdate(cordova.getActivity().getApplicationContext());
            callbackContext.success();
        } catch (Exception e) {
            e.printStackTrace();
            callbackContext.error(e.getMessage());
        }
    }

    @CordovaMethod
    private void getSdkVersion(JSONArray data, CallbackContext callbackContext) {
        // Get sdk version as string value
        try {
            String key = ContextSdk.getSdkVersion();
            callbackContext.success(key);
        } catch (Exception e) {
            e.printStackTrace();
            callbackContext.error(e.getMessage());
        }
    }

    @CordovaMethod
    private void getSdkIntVersion(JSONArray data, CallbackContext callbackContext) {
        // Get sdk version as int value
        try {
            int key = ContextSdk.getSdkIntVersion();
            callbackContext.success(key);
        } catch (Exception e) {
            e.printStackTrace();
            callbackContext.error(e.getMessage());
        }
    }

    @CordovaMethod
    private void setDeviceId(JSONArray data, CallbackContext callbackContext) {
        // Set new deviceId in system
        try {
            String deviceID = (String) data.getJSONObject(0).get("deviceID");
            if (deviceID != null && deviceID.length() > 0) {
                ContextSdk.setDeviceId(cordova.getActivity().getApplicationContext(), deviceID);
                callbackContext.success();
            }
        } catch (Exception e) {
            e.printStackTrace();
            callbackContext.error(e.getMessage());
        }
    }

    @CordovaMethod
    private void getDeviceId(JSONArray data, CallbackContext callbackContext) {
        // Gather existing device-id from system
        try {
            String key = new ContextSdk(cordova.getActivity().getApplicationContext()).getDeviceId();
            callbackContext.success(key);
        } catch (Exception e) {
            e.printStackTrace();
            callbackContext.error(e.getMessage());
        }
    }

    @CordovaMethod
    private void getAndroidId(JSONArray data, CallbackContext callbackContext) {
        // Gather existing android-id from system
        try {
            String key = new ContextSdk(cordova.getActivity().getApplicationContext()).getAndroidId();
            callbackContext.success(key);
        } catch (Exception e) {
            e.printStackTrace();
            callbackContext.error(e.getMessage());
        }
    }

    @CordovaMethod
    private void getAppKey(JSONArray data, CallbackContext callbackContext) {
        // Gather existing app-key from system
        try {
            String key = new ContextSdk(cordova.getActivity().getApplicationContext()).getAppKey();
            callbackContext.success(key);
        } catch (Exception e) {
            e.printStackTrace();
            callbackContext.error(e.getMessage());
        }
    }

    @CordovaMethod
    private void getApiKey(JSONArray data, CallbackContext callbackContext) {
        // Gather existing api-key from system
        try {
            String key = new ContextSdk(cordova.getActivity().getApplicationContext()).getApiKey();
            callbackContext.success(key);
        } catch (Exception e) {
            e.printStackTrace();
            callbackContext.error(e.getMessage());
        }
    }

    @CordovaMethod
    private void getAppId(JSONArray data, CallbackContext callbackContext) {
        // Gather existing app-id from system
        try {
            String key = new ContextSdk(cordova.getActivity().getApplicationContext()).getAppId();
            callbackContext.success(key);
        } catch (Exception e) {
            e.printStackTrace();
            callbackContext.error(e.getMessage());
        }
    }

    @CordovaMethod
    private void getCurrentContext(JSONArray data, CallbackContext callbackContext) {
        // Get user current context from system
        try {
            ContextData userData = new ContextSdk(cordova.getActivity().getApplicationContext()).getCurrentContext();
            callbackContext.success();
        } catch (Exception e) {
            e.printStackTrace();
            callbackContext.error(e.getMessage());
        }
    }

    @CordovaMethod
    private void setAlias(JSONArray data, CallbackContext callbackContext) {
        // Set new deviceId in system
        try {
            String alias = (String) data.getJSONObject(0).get("alias");
            if (alias != null && alias.length() > 0) {
                ContextSdk.setAlias(alias, cordova.getActivity().getApplicationContext());
                callbackContext.success();
            }
        } catch (Exception e) {
            e.printStackTrace();
            callbackContext.error(e.getMessage());
        }
    }

    @CordovaMethod
    private void getAlias(JSONArray data, CallbackContext callbackContext) {
        // Get alias value from system
        try {
            String key = ContextSdk.getAlias(cordova.getActivity().getApplicationContext());
            callbackContext.success(key);
        } catch (Exception e) {
            e.printStackTrace();
            callbackContext.error(e.getMessage());
        }
    }

    @CordovaMethod
    private void setUser(JSONArray data, CallbackContext callbackContext) {
        // Set user in system
        try {
            User userinfo = ContextSdk.getUser(cordova.getActivity().getApplicationContext());

            String name = (String) data.getJSONObject(0).get("name");
            if (name != null && name.length() > 0) {
                userinfo.setName(name);
            }
            String phone = (String) data.getJSONObject(0).get("phone");
            if (phone != null && phone.length() > 0) {
                userinfo.setPhone(phone);
            }
            String email = (String) data.getJSONObject(0).get("email");
            if (email != null && email.length() > 0) {
                userinfo.setEmail(email);
            }
            ContextSdk.setUser(userinfo, cordova.getActivity().getApplicationContext());

            callbackContext.success();
        } catch (Exception e) {
            e.printStackTrace();
            callbackContext.error(e.getMessage());
        }
    }

    @CordovaMethod
    private void getUser(JSONArray data, CallbackContext callbackContext) {
        // Get current configured user
        try {
            User userInfo = ContextSdk.getUser(cordova.getActivity().getApplicationContext());
            callbackContext.success(userInfo.toString());
        } catch (Exception e) {
            e.printStackTrace();
            callbackContext.error(e.getMessage());
        }
    }

    @CordovaMethod
    private void setChildId(JSONArray data, CallbackContext callbackContext) {
        // Set new child-id in system
        try {
            String childID = (String) data.getJSONObject(0).get("childID");
            if (childID != null && childID.length() > 0) {
                ContextSdk.setChildId(childID, cordova.getActivity().getApplicationContext());
                callbackContext.success();
            }
        } catch (Exception e) {
            e.printStackTrace();
            callbackContext.error(e.getMessage());
        }
    }

    @CordovaMethod
    private void getChildId(JSONArray data, CallbackContext callbackContext) {
        // Get child-id value from system
        try {
            String key = ContextSdk.getChildId(cordova.getActivity().getApplicationContext());
            callbackContext.success(key);
        } catch (Exception e) {
            e.printStackTrace();
            callbackContext.error(e.getMessage());
        }
    }

    @CordovaMethod
    private void setReferrer(JSONArray data, CallbackContext callbackContext) {
        // Set new referrer in system
        try {
            String key = (String) data.getJSONObject(0).get("referrer");
            if (key != null && key.length() > 0) {
                ContextSdk.setReferrer(key, cordova.getActivity().getApplicationContext());
                callbackContext.success();
            }
        } catch (Exception e) {
            e.printStackTrace();
            callbackContext.error(e.getMessage());
        }
    }

    @CordovaMethod
    private void getReferrer(JSONArray data, CallbackContext callbackContext) {
        // Get referrer value from system
        try {
            String key = ContextSdk.getReferrer(cordova.getActivity().getApplicationContext());
            callbackContext.success(key);
        } catch (Exception e) {
            e.printStackTrace();
            callbackContext.error(e.getMessage());
        }
    }

    @CordovaMethod
    private void setInstallReferrer(JSONArray data, CallbackContext callbackContext) {
        // Set new install referrer in system
        try {
            String key = (String) data.getJSONObject(0).get("installRef");
            if (key != null && key.length() > 0) {
                ContextSdk.setInstallReferrer(key, cordova.getActivity().getApplicationContext());
                callbackContext.success();
            }
        } catch (Exception e) {
            e.printStackTrace();
            callbackContext.error(e.getMessage());
        }
    }

    @CordovaMethod
    private void getInstallReferrer(JSONArray data, CallbackContext callbackContext) {
        // Get install referrer value from system
        try {
            String key = ContextSdk.getInstallReferrer(cordova.getActivity().getApplicationContext());
            callbackContext.success(key);
        } catch (Exception e) {
            e.printStackTrace();
            callbackContext.error(e.getMessage());
        }
    }

    @CordovaMethod
    private void setInstaller(JSONArray data, CallbackContext callbackContext) {
        // Set new installer in system
        try {
            String key = (String) data.getJSONObject(0).get("installer");
            if (key != null && key.length() > 0) {
                ContextSdk.setInstaller(key, cordova.getActivity().getApplicationContext());
                callbackContext.success();
            }
        } catch (Exception e) {
            e.printStackTrace();
            callbackContext.error(e.getMessage());
        }
    }

    @CordovaMethod
    private void getInstaller(JSONArray data, CallbackContext callbackContext) {
        // Get installer value from system
        try {
            String key = ContextSdk.getInstaller(cordova.getActivity().getApplicationContext());
            callbackContext.success(key);
        } catch (Exception e) {
            e.printStackTrace();
            callbackContext.error(e.getMessage());
        }
    }

    @CordovaMethod
    private void getGCMSenderId(JSONArray data, CallbackContext callbackContext) {
        // Get gcm sender id
        try {
            String key = ContextSdk.getGCMSenderId(cordova.getActivity().getApplicationContext());
            callbackContext.success(key);
        } catch (Exception e) {
            e.printStackTrace();
            callbackContext.error(e.getMessage());
        }
    }

    @CordovaMethod
    private void getDeviceToken(JSONArray data, CallbackContext callbackContext) {
        // Get gcm sender id
        try {
            String senderId = ContextSdk.getGCMSenderId(cordova.getActivity().getApplicationContext());

            InstanceID instanceID = InstanceID.getInstance(cordova.getActivity().getApplicationContext());
            String registrationId = instanceID.getToken(senderId, GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);

            callbackContext.success(registrationId);
        } catch (Exception e) {
            e.printStackTrace();
            callbackContext.error(e.getMessage());
        }
    }

    @CordovaMethod
    private void setCustomVariable(JSONArray data, CallbackContext callbackContext) {
        // Set custom variable in system
        try {
            String key = (String) data.getJSONObject(0).get("key");
            Object value = data.getJSONObject(0).get("value");
            if (key != null && key.length() > 0 && value != null) {
                Class valueClass = value.getClass();
                if (valueClass == Integer.class)
                    ContextSdk.setCustomVariable(key, (Integer) value, cordova.getActivity().getApplicationContext());
                else if (valueClass == Float.class || valueClass == Double.class)
                    ContextSdk.setCustomVariable(key, (Float) value, cordova.getActivity().getApplicationContext());
                else if (valueClass == Long.class)
                    ContextSdk.setCustomVariable(key, (Long) value, cordova.getActivity().getApplicationContext());
                else if (valueClass == String.class)
                    ContextSdk.setCustomVariable(key, (String) value, cordova.getActivity().getApplicationContext());
                else if (valueClass == Boolean.class)
                    ContextSdk.setCustomVariable(key, (Boolean) value, cordova.getActivity().getApplicationContext());

                callbackContext.success();
            }
        } catch (Exception e) {
            e.printStackTrace();
            callbackContext.error(e.getMessage());
        }
    }

    @CordovaMethod
    private void getCustomVariable(JSONArray data, CallbackContext callbackContext) {
        // Get custom variable value from system
        try {
            String key = (String) data.getJSONObject(0).get("key");
            Object value = ContextSdk.getCustomVariable(key, cordova.getActivity().getApplicationContext());
            callbackContext.success((String) value);
        } catch (Exception e) {
            e.printStackTrace();
            callbackContext.error(e.getMessage());
        }
    }

    @CordovaMethod
    private void removeCustomVariable(JSONArray data, CallbackContext callbackContext) {
        // Get remove custom variable value from system
        try {
            String key = (String) data.getJSONObject(0).get("key");
            ContextSdk.removeCustomVariable(key, cordova.getActivity().getApplicationContext());
            callbackContext.success();
        } catch (Exception e) {
            e.printStackTrace();
            callbackContext.error(e.getMessage());
        }
    }

    @CordovaMethod
    private void tagEvent(JSONArray data, CallbackContext callbackContext) {
        // Set event in system
        try {
            String key = (String) data.getJSONObject(0).get("key");
            if (key != null && key.length() > 0) {
                HashMap<String, String> mapData = new HashMap<String, String>();
                try {
                    JSONObject hashData = (JSONObject) data.getJSONObject(0).get("data");
                    if (hashData != null && hashData.length() > 0) {
                        Iterator<String> keys = hashData.keys();
                        while (keys.hasNext()) {
                            String dataKey = keys.next();
                            if (dataKey != null && dataKey.length() > 0) {
                                mapData.put(dataKey, hashData.getString(dataKey));
                            }
                        }
                    }
                } catch (Exception e) {
                }

                ContextSdk.tagEvent(key, mapData, cordova.getActivity().getApplicationContext());
                callbackContext.success();
            }
        } catch (Exception e) {
            e.printStackTrace();
            callbackContext.error(e.getMessage());
        }
    }

    @CordovaMethod
    private void setSmallIcon(JSONArray data, CallbackContext callbackContext) {
        // Set small icon in system
        try {
            String key = (String) data.getJSONObject(0).get("icon");
            if (key != null && key.length() > 0) {
                ContextSdk.setSmallIcon(key, cordova.getActivity().getApplicationContext());
            }
            callbackContext.success();
        } catch (Exception e) {
            e.printStackTrace();
            callbackContext.error(e.getMessage());
        }
    }

    @CordovaMethod
    private void setSessionTimeout(JSONArray data, CallbackContext callbackContext) {
        // Set session timeout in system
        try {
            int key = data.getJSONObject(0).getInt("timeout");
            ContextSdk.setSessionTimeout(key, cordova.getActivity().getApplicationContext());

            callbackContext.success();
        } catch (Exception e) {
            e.printStackTrace();
            callbackContext.error(e.getMessage());
        }
    }

    @CordovaMethod
    private void getSessionTimeout(JSONArray data, CallbackContext callbackContext) {
        // Get installer value from system
        try {
            int key = ContextSdk.getSessionTimeout(cordova.getActivity().getApplicationContext());
            callbackContext.success(key);
        } catch (Exception e) {
            e.printStackTrace();
            callbackContext.error(e.getMessage());
        }
    }

    @CordovaMethod
    private void trackTouches(JSONArray data, CallbackContext callbackContext) {
        try {
            DisplayMetrics dm = new DisplayMetrics();
            cordova.getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
            int screenWidth = dm.widthPixels;
            int screenHeight = dm.heightPixels;

            JSONObject root = data.getJSONObject(0);
            if (root != null) {
                double webWidth = (double) root.optInt("w");
                double webHeight = (double) root.optInt("h");

                double wRatio = (screenWidth / webWidth);
                double hRatio = (screenHeight / webHeight);

                // store rescaled x-y co-ordinates of touch
                double x = root.optDouble("x");
                double y = root.optDouble("y");
                x = x * wRatio;
                y = y * hRatio;
                root.put("x", x);
                root.put("y", y);

                // store rescaled px-py-pw-ph co-ordinates of touch
                double px = root.optDouble("px");
                double py = root.optDouble("py");
                double pw = root.optDouble("pw");
                double ph = root.optDouble("ph");
                px = px * wRatio;
                py = py * hRatio;
                pw = pw * wRatio;
                ph = ph * hRatio;
                root.put("px", px);
                root.put("py", py);
                root.put("pw", pw);
                root.put("ph", ph);

                // re-store rescaled x-y-w-h co-ordinates of input fld
                JSONArray newObjects = new JSONArray();
                JSONArray objects = root.optJSONArray("arr");
                if (objects != null && objects.length() > 0) {
                    for (int i = 0; i < objects.length(); i++) {
                        JSONObject inputFld = objects.optJSONObject(i);
                        if (inputFld != null && inputFld.length() > 0) {
                            JSONObject obj = new JSONObject(inputFld.toString());
                            obj.put("x", inputFld.optDouble("x") * wRatio);
                            obj.put("y", (inputFld.optDouble("y") * hRatio) + inputFld.optDouble("h"));
                            obj.put("w", inputFld.optDouble("w") * wRatio);
                            obj.put("h", inputFld.optDouble("h") * hRatio);
                            newObjects.put(obj);
                        }
                    }
                }
                root.put("arr", newObjects);
            }

            View rootView = cordova.getActivity().getWindow().getDecorView().getRootView();
            ContextSdk.trackTouches(root, cordova.getActivity().getApplicationContext(), rootView, cordova.getActivity());

            callbackContext.success();
        } catch (Exception e) {
            e.printStackTrace();
            callbackContext.error(e.getMessage());
        }
    }

    @CordovaMethod
    private void trackSwipes(JSONArray data, CallbackContext callbackContext) {
        try {
            DisplayMetrics dm = new DisplayMetrics();
            cordova.getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
            int screenWidth = dm.widthPixels;
            int screenHeight = dm.heightPixels;

            JSONObject root = data.getJSONObject(0);
            if (root != null) {
                double webWidth = (double) root.optInt("w");
                double webHeight = (double) root.optInt("h");

                double wRatio = (screenWidth / webWidth);
                double hRatio = (screenHeight / webHeight);

                // store rescaled x-y co-ordinates of touch
                double x1 = root.optDouble("x1");
                double y1 = root.optDouble("y1");
                x1 = x1 * wRatio;
                y1 = y1 * hRatio;
                root.put("x1", x1);
                root.put("y1", y1);

                // store rescaled x-y co-ordinates of touch
                double x2 = root.optDouble("x2");
                double y2 = root.optDouble("y2");
                x2 = x2 * wRatio;
                y2 = y2 * hRatio;
                root.put("x2", x2);
                root.put("y2", y2);

                // re-store rescaled x-y-w-h co-ordinates of input fld
                JSONArray newObjects = new JSONArray();
                JSONArray objects = root.optJSONArray("arr");
                if (objects != null && objects.length() > 0) {
                    for (int i = 0; i < objects.length(); i++) {
                        JSONObject inputFld = objects.optJSONObject(i);
                        if (inputFld != null && inputFld.length() > 0) {
                            JSONObject obj = new JSONObject(inputFld.toString());
                            obj.put("x", inputFld.optDouble("x") * wRatio);
                            obj.put("y", (inputFld.optDouble("y") * hRatio) + inputFld.optDouble("h"));
                            obj.put("w", inputFld.optDouble("w") * wRatio);
                            obj.put("h", inputFld.optDouble("h") * hRatio);
                            newObjects.put(obj);
                        }
                    }
                }
                root.put("arr", newObjects);
            }

            View rootView = cordova.getActivity().getWindow().getDecorView().getRootView();
            ContextSdk.trackSwipes(root, cordova.getActivity().getApplicationContext(), rootView, cordova.getActivity());

            callbackContext.success();
        } catch (Exception e) {
            e.printStackTrace();
            callbackContext.error(e.getMessage());
        }
    }

    @CordovaMethod
    private void trackScreens(JSONArray data, CallbackContext callbackContext) {
        try {
            DisplayMetrics dm = new DisplayMetrics();
            cordova.getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
            int screenWidth = dm.widthPixels;
            int screenHeight = dm.heightPixels;

            JSONObject root = data.getJSONObject(0);
            if (root != null) {
                double webWidth = (double) root.optInt("w");
                double webHeight = (double) root.optInt("h");

                double wRatio = (screenWidth / webWidth);
                double hRatio = (screenHeight / webHeight);

                // re-store rescaled x-y-w-h co-ordinates of input fld
                JSONArray newObjects = new JSONArray();
                JSONArray objects = root.optJSONArray("arr");
                if (objects != null && objects.length() > 0) {
                    for (int i = 0; i < objects.length(); i++) {
                        JSONObject inputFld = objects.optJSONObject(i);
                        if (inputFld != null && inputFld.length() > 0) {
                            JSONObject obj = new JSONObject(inputFld.toString());
                            obj.put("x", inputFld.optDouble("x") * wRatio);
                            obj.put("y", (inputFld.optDouble("y") * hRatio) + inputFld.optDouble("h"));
                            obj.put("w", inputFld.optDouble("w") * wRatio);
                            obj.put("h", inputFld.optDouble("h") * hRatio);
                            newObjects.put(obj);
                        }
                    }
                }
                root.put("arr", newObjects);
            }

            View rootView = cordova.getActivity().getWindow().getDecorView().getRootView();
            ContextSdk.trackScreens(root, cordova.getActivity().getApplicationContext(), rootView, cordova.getActivity());

            callbackContext.success();
        } catch (Exception e) {
            e.printStackTrace();
            callbackContext.error(e.getMessage());
        }
    }

    @Override
    public boolean execute(String action, JSONArray data, CallbackContext callbackId) {
        Method method = exportedMethods.get(action);
        if (method == null) {
            return false;
        }

        try {
            Boolean result = (Boolean) method.invoke(this, data, callbackId);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void sendNotification(JSONObject bundle, Context context) {
        if (!AppICEPlugin.hasNotificationsCallback() && AppICEPlugin.inBackground()) {
            String packageName = context.getPackageName();
            if (AppICEPlugin.notificationStack == null) {
                AppICEPlugin.notificationStack = new ArrayList<JSONObject>();
            }
            notificationStack.add(bundle);

            /* start the main activity, if not running */
            Intent intent = new Intent("android.intent.action.MAIN");
            intent.setComponent(new ComponentName(packageName, packageName + ".MainActivity"));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            intent.putExtra("cdvStartInBackground", true);
            context.startActivity(intent);

            return;
        }

        try {
            final CallbackContext callbackContext = AppICEPlugin.notificationCallbackContext;
            if (callbackContext != null && bundle != null) {

                PluginResult pluginresult = new PluginResult(PluginResult.Status.OK, bundle);
                pluginresult.setKeepCallback(true);
                callbackContext.sendPluginResult(pluginresult);


            } else if (callbackContext == null) {
                String packageName = context.getPackageName();
                if (AppICEPlugin.notificationStack == null) {
                    AppICEPlugin.notificationStack = new ArrayList<JSONObject>();
                }
                notificationStack.add(bundle);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static boolean inBackground = true;
    private static ArrayList<JSONObject> notificationStack = null;
    private static CallbackContext notificationCallbackContext;

    @Override
    public void onPause(boolean multitasking) {
        AppICEPlugin.inBackground = true;
    }

    @Override
    public void onResume(boolean multitasking) {
        AppICEPlugin.inBackground = false;
    }

    @Override
    public void onReset() {
        AppICEPlugin.notificationCallbackContext = null;
    }

    public static boolean inBackground() {
        return AppICEPlugin.inBackground;
    }

    public static boolean hasNotificationsCallback() {
        return AppICEPlugin.notificationCallbackContext != null;
    }

    @CordovaMethod
    private void onNotificationOpen(JSONArray data, final CallbackContext callbackContext) {
        try {
            AppICEPlugin.notificationCallbackContext = callbackContext;
            if (AppICEPlugin.notificationStack != null) {
                for (JSONObject bundle : AppICEPlugin.notificationStack) {
                    AppICEPlugin.sendNotification(bundle, this.cordova.getActivity().getApplicationContext());
                }
                AppICEPlugin.notificationStack.clear();
            }
        } catch (Exception e) {
        }
    }

}