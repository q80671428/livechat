package tech.mastersam.livechat;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import android.app.Activity;
import android.content.Context;

import androidx.annotation.NonNull;

import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;

import com.livechatinc.inappchat.ChatWindowConfiguration;
import com.livechatinc.inappchat.ChatWindowView;

import java.util.HashMap;

public class LivechatPlugin implements FlutterPlugin, MethodCallHandler, ActivityAware {
    private MethodChannel channel;
    private Context context;
    private Activity activity;
    private ChatWindowView windowView;

    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
        this.context = flutterPluginBinding.getApplicationContext();
        setupChannel(flutterPluginBinding.getBinaryMessenger());
    }

    private void setupChannel(BinaryMessenger messenger) {
        channel = new MethodChannel(messenger, "livechatt");
        channel.setMethodCallHandler(this);
    }

    @Override
    public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
        switch (call.method) {
            case "getPlatformVersion":
                result.success("Android " + android.os.Build.VERSION.RELEASE);
                break;
            case "beginChat":
                handleBeginChat(call, result);
                break;
            default:
                result.notImplemented();
                break;
        }
    }

    private void handleBeginChat(@NonNull MethodCall call, @NonNull Result result) {
        final String licenseNo = call.argument("licenseNo");
        final HashMap<String, String> customParams = call.argument("customParams");
        final String groupId = call.argument("groupId");
        final String visitorName = call.argument("visitorName");
        final String visitorEmail = call.argument("visitorEmail");

        if (licenseNo == null || licenseNo.trim().isEmpty()) {
            result.error("LICENSE_ERROR", "License number cannot be empty", null);
            return;
        }

        if (visitorName == null || visitorName.trim().isEmpty()) {
            result.error("VISITOR_NAME_ERROR", "Visitor name cannot be empty", null);
            return;
        }

        if (visitorEmail == null || visitorEmail.trim().isEmpty()) {
            result.error("VISITOR_EMAIL_ERROR", "Visitor email cannot be empty", null);
            return;
        }

        try {
            Intent intent = new Intent(activity, com.livechatinc.inappchat.ChatWindowActivity.class);
            Bundle config = buildChatConfig(licenseNo, groupId, visitorName, visitorEmail, customParams);
            intent.putExtras(config);
            activity.startActivity(intent);
            result.success(null);
        } catch (Exception e) {
            result.error("CHAT_WINDOW_ERROR", "Failed to start chat window", e);
        }
    }

    private Bundle buildChatConfig(String licenseNo, String groupId, String visitorName, String visitorEmail, HashMap<String, String> customParams) {
        return new ChatWindowConfiguration.Builder()
                .setLicenceNumber(licenseNo)
                .setGroupId(groupId)
                .setVisitorName(visitorName)
                .setVisitorEmail(visitorEmail)
                .setCustomParams(customParams)
                .build()
                .asBundle();
    }

    @Override
    public void onAttachedToActivity(ActivityPluginBinding binding) {
        this.activity = binding.getActivity();
    }

    @Override
    public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
        teardownChannel();
    }

    private void teardownChannel() {
        if (channel != null) {
            channel.setMethodCallHandler(null);
            channel = null;
        }
    }

    @Override
    public void onDetachedFromActivity() {
        this.activity = null;
    }

    @Override
    public void onDetachedFromActivityForConfigChanges() {}

    @Override
    public void onReattachedToActivityForConfigChanges(ActivityPluginBinding binding) {
        this.activity = binding.getActivity();
    }
}
