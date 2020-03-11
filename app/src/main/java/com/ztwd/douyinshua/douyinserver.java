package com.ztwd.douyinshua;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.AppOpsManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Path;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;

import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import static com.ztwd.douyinshua.AccessibilityHelper.execShellCmd;
import static com.ztwd.douyinshua.AccessibilityHelper.sleepTime;
import static com.ztwd.douyinshua.AccessibilityHelper.upgradeRootPermission;
import static com.ztwd.douyinshua.StringTimeUtils.getTimeStr2;

/**
 *  抖音广告页面的activity：com.ss.android.excitingvideo.ExcitingVideoActivity
 *  抖音主页面的activity：com.ss.android.ugc.aweme.main.MainActivity
 *  抖音视频页面计时ID：com.ss.android.ugc.aweme.lite:id/aoz
 *  来赚钱ID：com.ss.android.ugc.aweme.lite:id/ke
 *  点赞ID：com.ss.android.ugc.aweme.lite:id/a4m
 *  加关注ID:com.ss.android.ugc.aweme.lite:id/a4k
 *  整个视频页面ID：com.ss.android.ugc.aweme.lite:id/a40
 * */
public class douyinserver extends AccessibilityService {
    private final static String TAG = "douyinserver";

    @SuppressLint("ObsoleteSdkInt")
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
//注意这个方法回调，是在主线程，不要在这里执行耗时操作
        int eventType = event.getEventType();
        AccessibilityNodeInfo rootNode = getRootInActiveWindow();
        switch (eventType) {
            case AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED:
                try {
                    String toppackname;
                    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP){
                        toppackname = getHigherPackageName();
                    }else {
                        toppackname = getLowerVersionPackageName();
                    }
                    if(toppackname !=null&& toppackname.equals("com.ss.android.ugc.aweme.lite")){
                        int sys_hh = (Integer.parseInt(getTimeStr2().substring(11, 12)) * 10) + Integer.parseInt(getTimeStr2().substring(12, 13));
                        if (sys_hh > 8 && sys_hh < 23) {
                            if (findBottom(rootNode,"推荐")){
                                int ran = (int) (Math.random() * 12);//产生随机数
                                int wait_sleep = ran * 1000;
                                if (wait_sleep > 3000) {//最少停留页面3秒
                                    Log.i(TAG, "延时：" + wait_sleep/1000 + "秒！");
                                    sleepTime(wait_sleep);
                                    Log.i(TAG, "延时完成准备滑动");
                                    if(Build.VERSION.SDK_INT>=26) {
                                        Log.i(TAG, "使用触摸屏事件滑动");
                                        Path path = new Path();
                                        path.moveTo(800, 2200);
                                        path.lineTo(800, 400);
                                        GestureDescription.StrokeDescription sd = new GestureDescription.StrokeDescription(path, 0, 200,false);
                                        dispatchGesture(new GestureDescription.Builder().addStroke(sd).build(), null, null);
                                        return;
                                    }else {
                                        if(upgradeRootPermission(getPackageCodePath())) {
                                            int ran1 = (int) (Math.random() * 40);//产生随机数
                                            if(ran1<30) {
                                                execShellCmd("input swipe 800 2200 800 400"); //滑动坐标
                                                Log.i(TAG, "使用ADB命令下滑成功.");
                                                return;
                                            }else {
                                                execShellCmd("input swipe 800 400 800 2200"); //滑动坐标
                                                Log.i(TAG, "使用ADB命令上滑成功.");
                                                return;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }catch (Exception ignored){}
                break;
        }
    }
    /**
     * 查找TextView控件
     * @param rootNode 根结点
     */
    private boolean findBottom(AccessibilityNodeInfo rootNode , String str0) {
        int count = rootNode.getChildCount();
        try {
            for (int i = 0; i < count; i++) {
                AccessibilityNodeInfo node = rootNode.getChild(i);
                if (null != node.getClassName() && "android.widget.TextView".contains(node.getClassName())) {
                    String ls = (String) node.getText();
                    Log.i(TAG, "node包含的信息:" + ls);
                    if (ls != null && ls.contains(str0)) {
                        Log.i(TAG, "<<=======确定包含=======>>:" + str0);
                        return true;
                    }
                }
                findBottom(node, str0);
            }
        }catch (Exception ignored){}
        return false;
    }
    /**
     * 判断  用户查看使用情况的权利是否给予app
     * @return booble/false
     */
    @SuppressLint("ObsoleteSdkInt")
    private boolean isUseGranted() {
        Context appContext = getApplicationContext();
        AppOpsManager appOps = (AppOpsManager) appContext.getSystemService(Context.APP_OPS_SERVICE);
        int mode = -1;
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            mode = appOps.checkOpNoThrow("android:get_usage_stats", android.os.Process.myUid(), appContext.getPackageName());
        }
        boolean granted = mode == AppOpsManager.MODE_ALLOWED;
        return granted;
    }

    /**
     * 高版本：获取顶层的activity的包名
     *
     * @return
     */
    @SuppressLint("ObsoleteSdkInt")
    private String getHigherPackageName() {
        String topPackageName = "";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            UsageStatsManager mUsageStatsManager = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
            long time = System.currentTimeMillis();
            //time - 1000 * 1000, time 开始时间和结束时间的设置，在这个时间范围内 获取栈顶Activity 有效
            List<UsageStats> stats = mUsageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, time - 1000 * 1000, time);
            // Sort the stats by the last time used
            if (stats != null) {
                SortedMap<Long, UsageStats> mySortedMap = new TreeMap<Long, UsageStats>();
                for (UsageStats usageStats : stats) {
                    mySortedMap.put(usageStats.getLastTimeUsed(), usageStats);
                }
                if (mySortedMap != null && !mySortedMap.isEmpty()) {
                    topPackageName = mySortedMap.get(mySortedMap.lastKey()).getPackageName();
                    Log.i("TopPackage Name", topPackageName);
                }
            }
        } else {
            ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
            ComponentName topActivity = activityManager.getRunningTasks(1).get(0).topActivity;
            topPackageName = topActivity.getPackageName();
        }
        return topPackageName;
    }

    /**
     * 低版本：获取栈顶app的包名
     *
     * @return
     */
    private String getLowerVersionPackageName() {
        String topPackageName;//低版本  直接获取getRunningTasks
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        ComponentName topActivity = activityManager.getRunningTasks(1).get(0).topActivity;
        topPackageName = topActivity.getPackageName();
        return topPackageName;
    }
    /**
     * 服务中断
     */
    @Override
    public void onInterrupt() {

    }
    /**
     * 服务连接
     */
    @Override
    protected void onServiceConnected() {
        int apis = Build.VERSION.SDK_INT;
        Log.i(TAG, "当前API等级:"+apis);
        if (!isUseGranted()) {
            //开启应用授权界面
            Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
        Toast.makeText(this, "抖音视频服务开启", Toast.LENGTH_SHORT).show();
        super.onServiceConnected();
    }
    /**
     * 服务断开
     */
    @Override
    public boolean onUnbind(Intent intent) {
        Toast.makeText(this, "抖音视频服务关闭", Toast.LENGTH_SHORT).show();
        return super.onUnbind(intent);
    }
}
