package com.wf.bluetoothledemo;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.wf.bluetoothledemo.utils.StringUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * 全局context，单例
 * @author wangpf
 *
 * @date:2017年11月21日 上午11:27:01
 * @version : v1.0
 */
public class MainApplication extends Application {
	public static final int NETTYPE_WIFI = 0x01;
	public static final int NETTYPE_CMWAP = 0x02;
	public static final int NETTYPE_CMNET = 0x03;
	
	private static MainApplication mInstance = null;

	@Override
	public void onCreate() {
		super.onCreate();
		mInstance = this;
	}

	@Override
	public void onTerminate() {
		super.onTerminate();
	}
	
	public static MainApplication getInstance() {
		return mInstance;
	}

	//获取SharedPreferences对象
	public SharedPreferences getSharedPreferences(String name) {
		return mInstance.getSharedPreferences(name, MODE_PRIVATE);
	}


	/**
	 * Retrieve a String value from the preferences.
	 * @param key
	 * @return
	 */
	public String getPreferencesString(String key) {
		if(StringUtils.isEmpty(key)) {
			return "";
		}

		try {
			SharedPreferences preference = mInstance.getSharedPreferences("bluetooth", MODE_PRIVATE);
			return preference.getString(key, "");
		}catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

	/**
	 * Set a String value in the preferences editor, to be written back once
	 * @param key
	 * @param value
	 * @return
	 */
	public void putPreferencesString(String key, String value) {
		if(StringUtils.isEmpty(key) || StringUtils.isEmpty(value)) {
			return;
		}

		try {
			SharedPreferences preference = mInstance.getSharedPreferences("bluetooth", MODE_PRIVATE);
			SharedPreferences.Editor editor = preference.edit();
			editor.putString(key, value);
			editor.commit();
		}catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Set a Set value in the preferences editor
	 * @param map
	 * @return
	 */
	public void putMultiPreferencesString(Map<String, String> map) {
		if(map == null) {
			return;
		}
		try {
			SharedPreferences preference = mInstance.getSharedPreferences("bluetooth", MODE_PRIVATE);
			SharedPreferences.Editor editor = preference.edit();
			for(Map.Entry<String, String> entry : map.entrySet()) {
				editor.putString(entry.getKey(), entry.getValue());
			}
			editor.commit();
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
    
    /**
	 * 判断当前版本是否兼容目标版本的方法
	 * @param VersionCode
	 * @return
	 */
	public static boolean isMethodsCompat(int VersionCode) {
		int currentVersion = android.os.Build.VERSION.SDK_INT;
		return currentVersion >= VersionCode;
	}

	/**
	 * 检测网络是否可用
	 * @return
	 */
	public boolean isNetworkConnected() {
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo ni = cm.getActiveNetworkInfo();
		return ni != null && ni.isConnectedOrConnecting();
	}
	
	/**
	 * 获取当前网络类型
	 * @return 0：没有网络   1：WIFI网络   2：WAP网络    3：NET网络
	 */
	public int getNetworkType() {
		int netType = 0;
		ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
		if (networkInfo == null) {
			return netType;
		}		
		int nType = networkInfo.getType();
		if (nType == ConnectivityManager.TYPE_MOBILE) {
			String extraInfo = networkInfo.getExtraInfo();
			if(!StringUtils.isEmpty(extraInfo)){
				if (extraInfo.toLowerCase().equals("cmnet")) {
					netType = NETTYPE_CMNET;
				} else {
					netType = NETTYPE_CMWAP;
				}
			}
		} else if (nType == ConnectivityManager.TYPE_WIFI) {
			netType = NETTYPE_WIFI;
		}
		return netType;
	}
	
  /**
   * 保存Activity，退出应用及返回时使用
   */
	public List<Activity> mList = new LinkedList<Activity>();
	
	public void addActivity(Activity activity) {
		  mList.add(activity);
	}

	public void removeActivity(Activity activity) {
		  mList.remove(activity);
	}

	public void exit() {
		try {
			//finish所有Activity
			for(Activity activity : mList) {
				if (activity != null)
					activity.finish();
			}
		}catch(Exception e) {
			e.printStackTrace();
		}finally {
			android.os.Process.killProcess(android.os.Process.myPid());
			//System.exit(0);
		}
	}
}