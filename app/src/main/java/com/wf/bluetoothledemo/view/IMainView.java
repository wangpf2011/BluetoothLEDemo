package com.wf.bluetoothledemo.view;

/**
 * 主界面--interface
 *
 * Created by wangpf
 */

public interface IMainView {
    void sendHandler(int type, String msg);

    void sendEmptyHandler(int type);
}
