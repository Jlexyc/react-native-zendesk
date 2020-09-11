/**
* Oleksii Kozulin
 * 17.10.2018
 * https://github.com/jlexyc/react-native-zendesk-support2
 */

package io.jlexyc.rnzendesk;

import android.content.Intent;
import android.app.Activity;

import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.BaseActivityEventListener;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import zendesk.core.AnonymousIdentity;
import zendesk.support.request.RequestActivity;
import zendesk.support.request.RequestUiConfig;
import zendesk.support.requestlist.RequestListActivity;
import zendesk.support.requestlist.RequestListUiConfig;
import zendesk.support.RequestUpdates;
import zendesk.support.RequestProvider;
import com.zendesk.service.ZendeskCallback;
import com.zendesk.service.ErrorResponse;
import zendesk.core.Zendesk;
import zendesk.support.Support;
import zendesk.support.CustomField;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import java.io.Serializable;

public class RNZendeskBridge extends ReactContextBaseJavaModule {

  private static final int REQUEST_CODE = 304869;
  private static ReactApplicationContext mainContext = null;

  public RNZendeskBridge(final ReactApplicationContext reactContext) {
    super(reactContext);
    mainContext = reactContext;
    reactContext.addActivityEventListener(new BaseActivityEventListener() {
      @Override
      public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent intent) {
        if (requestCode == REQUEST_CODE) {
          if (resultCode == Activity.RESULT_CANCELED) {
            reactContext
              .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
              .emit("submitRequestCancelled", null);
          } else if (resultCode == Activity.RESULT_OK) {
            reactContext
              .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
              .emit("submitRequestCompleted", null);
          }
        }
      }
    });
  }

  @Override
  public String getName() {
    return "RNZendeskBridge";
  }

  private static long[] toLongArray(ArrayList<?> values) {
    long[] arr = new long[values.size()];
    for (int i = 0; i < values.size(); i++)
      arr[i] = Long.parseLong((String) values.get(i));
    return arr;
  }

  @ReactMethod
  public void initialize(ReadableMap config) {
    String appId = config.getString("appId");
    String zendeskUrl = config.getString("zendeskUrl");
    String clientId = config.getString("clientId");
    Zendesk.INSTANCE.init(getReactApplicationContext(), zendeskUrl, appId, clientId);
  }

  @ReactMethod
    public void setupIdentity(ReadableMap identity) {
      AnonymousIdentity.Builder builder = new AnonymousIdentity.Builder();

      if (identity != null && identity.hasKey("customerEmail")) {
        builder.withEmailIdentifier(identity.getString("customerEmail"));
      }

      if (identity != null && identity.hasKey("customerName")) {
        builder.withNameIdentifier(identity.getString("customerName"));
      }

      Zendesk.INSTANCE.setIdentity(builder.build());
      Support.INSTANCE.init(Zendesk.INSTANCE);
    }

  
  @ReactMethod
  public void callSupport(final ReadableMap options) {

    RequestUiConfig.Builder builder = RequestActivity.builder();

    if(options.hasKey("subject")) {
      String subject = options.getString("subject");
      builder.withRequestSubject(subject);
    }

    if(options.hasKey("tags")){
      ReadableArray tagsArray = options.getArray("tags");
      List<String> tags = new ArrayList(tagsArray.size());
      for(int i = 0; i < tagsArray.size(); i++){
        tags.add(tagsArray.getString(i));
      }
      builder.withTags(tags);
    }

    if(options.hasKey("customFields")){
      List<CustomField> fields = new ArrayList<>();

      for (Map.Entry<String, Object> next : options.getMap("customFields").toHashMap().entrySet())
        fields.add(new CustomField(Long.parseLong(next.getKey()), (String) next.getValue()));

      builder.withCustomFields(fields);
    }

    Activity activity = getCurrentActivity();

    if(activity != null) {
      builder.show(activity);
    }
  }

  @ReactMethod
  public void ticketsList(final ReadableMap options) {

    RequestUiConfig.Builder builder = RequestActivity.builder();
    Activity activity = getCurrentActivity();

    if(options.hasKey("subject")) {
      String subject = options.getString("subject");
      builder.withRequestSubject(subject);
    }

    if(options.hasKey("tags")){
      ReadableArray tagsArray = options.getArray("tags");
      List<String> tags = new ArrayList(tagsArray.size());
      for(int i = 0; i < tagsArray.size(); i++){
        tags.add(tagsArray.getString(i));
      }
      builder.withTags(tags);
    }

    if(options.hasKey("customFields")){
      List<CustomField> fields = new ArrayList<>();

      for (Map.Entry<String, Object> next : options.getMap("customFields").toHashMap().entrySet())
        fields.add(new CustomField(Long.parseLong(next.getKey()), (String) next.getValue()));

      builder.withCustomFields(fields);
    }

    RequestListUiConfig.Builder listBuilder = RequestListActivity.builder();
    if(activity != null) {
        listBuilder.show(activity, builder.config());
    }
    
  }

  @ReactMethod
  public void getUpdates() {
    final RequestProvider requestProvider = Support.INSTANCE.provider().requestProvider();
    requestProvider.getUpdatesForDevice(new ZendeskCallback<RequestUpdates>() {
    @Override
    public void onSuccess(RequestUpdates requestUpdates) {
      mainContext
        .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
        .emit("zendeskUpdatesReceived", requestUpdates.totalUpdates());
    }

    @Override
    public void onError(ErrorResponse errorResponse) {
      mainContext
        .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
        .emit("zendeskUpdatesReceived", 0);
    }
});

  }
}
