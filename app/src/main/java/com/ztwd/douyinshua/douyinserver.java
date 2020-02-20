package com.ztwd.douyinshua;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;

import java.io.DataOutputStream;
import java.io.OutputStream;
import java.util.List;

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
    private boolean msroot;

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
//注意这个方法回调，是在主线程，不要在这里执行耗时操作
        int eventType = event.getEventType();
        AccessibilityNodeInfo rootNode = getRootInActiveWindow();
        switch (eventType) {
            case AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED:
                try {
                    int sys_hh = (Integer.parseInt(getTimeStr2().substring(11, 12)) * 10) + Integer.parseInt(getTimeStr2().substring(12, 13));
                    if (sys_hh > 8 && sys_hh < 23) {
                        List<AccessibilityNodeInfo> aoz = rootNode.findAccessibilityNodeInfosByViewId("com.ss.android.ugc.aweme.lite:id/aoz");//判断是否在视频页面
                        if (aoz != null && !aoz.isEmpty()) {
                            int ran = (int) (Math.random() * 20);//产生随机数
                            int wait_sleep = ran * 1000;
                            if (wait_sleep > 3000) {//最少停留页面3秒
                                Log.i(TAG, "延时：" + wait_sleep + "毫秒！");
                                sleepTime(wait_sleep);
                                Log.i(TAG, "延时完成准备滑动");
                                if (msroot) {
                                    execShellCmd("input swipe 800 2200 800 400"); //滑动坐标
                                    Log.i(TAG, "使用ADB命令滑动成功.");
                                    return;
                                } else {
                                    List<AccessibilityNodeInfo> a40 = rootNode.findAccessibilityNodeInfosByViewId("com.ss.android.ugc.aweme.lite:id/bg");//整个视频页面
                                    if (a40 != null && !a40.isEmpty()) {
                                        Log.i(TAG, "使用自带模拟滑动");
                                        return;
                                    }
                                }
                            }
                        }
                    }
                }catch (Exception ignored){}
                break;
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
                break;
        }
    }
    /**
     * 根据id,获取AccessibilityNodeInfo，并点击。
     */
    private void ClickId(String id) {
        AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
        if (nodeInfo != null) {
            List<AccessibilityNodeInfo> list = nodeInfo.findAccessibilityNodeInfosByViewId(id);
            for (AccessibilityNodeInfo item : list) {
                item.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            }
        }
    }
    /**
     * 应用程序运行命令获取 Root权限，设备必须已破解(获得ROOT权限)
     *
     * @return 应用程序是/否获取Root权限
     */
    public boolean upgradeRootPermission(String pkgCodePath) {
        Process process = null;
        DataOutputStream os = null;
        try {
            String cmd="chmod 777 " + pkgCodePath;
            process = Runtime.getRuntime().exec("su"); //切换到root帐号
            os = new DataOutputStream(process.getOutputStream());
            os.writeBytes(cmd + "\n");
            os.writeBytes("exit\n");
            os.flush();
            process.waitFor();
        } catch (Exception e) {
            return false;
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
                process.destroy();
            } catch (Exception e) {
            }
        }
        try {
            return process.waitFor()==0;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }
    /**
     * 回到系统桌面
     */
    private void back2Home() {
        Intent home = new Intent(Intent.ACTION_MAIN);
        home.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        home.addCategory(Intent.CATEGORY_HOME);
        startActivity(home);
    }
    /**
     * 延时MS
     */
    public static void sleepTime(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    /**
     * 模拟返回操作
     */
    public void performBackClick() {
        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        performGlobalAction(GLOBAL_ACTION_BACK);
    }
    /**
     * 执行shell命令
     *
     execShellCmd("input tap 168 252");点击某坐标
     execShellCmd("input swipe 100 250 200 280"); 滑动坐标
     */
    public static void execShellCmd(String cmd) {
        try {
            // 申请获取root权限，这一步很重要，不然会没有作用
            Process process = Runtime.getRuntime().exec("su");
            // 获取输出流
            OutputStream outputStream = process.getOutputStream();
            DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
            dataOutputStream.writeBytes(cmd);
            dataOutputStream.flush();
            dataOutputStream.close();
            outputStream.close();
        } catch (Throwable t) {
            t.printStackTrace();
        }
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
        if(upgradeRootPermission(getPackageCodePath())) msroot = true;
        Toast.makeText(this, "......抖音刷视频服务开启......", Toast.LENGTH_SHORT).show();
        super.onServiceConnected();
    }
    /**
     * 服务断开
     */
    @Override
    public boolean onUnbind(Intent intent) {
        Toast.makeText(this, "抖音刷视频服务关闭", Toast.LENGTH_SHORT).show();
        return super.onUnbind(intent);
    }
}
