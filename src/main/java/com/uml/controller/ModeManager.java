package com.uml.controller;

import java.util.ArrayList;
import java.util.List;

/**
 * State machine that tracks the current editor mode and notifies observers
 * (ButtonPanel for highlight, CanvasPanel for strategy swap).
 */
public class ModeManager { // 模式管理器：維護目前編輯模式，並通知所有監聽者（Observer Pattern）

    private EditorMode currentMode  = EditorMode.SELECT; // 目前的編輯模式，預設為選取模式
    private EditorMode previousMode = EditorMode.SELECT; // 前一個模式，供 restorePreviousMode() 使用

    private final List<ModeChangeListener> listeners = new ArrayList<>(); // 所有已註冊的模式切換監聽者

    public void setMode(EditorMode mode) { // 切換到指定模式
        if (mode == currentMode) return; // 若模式未改變則直接返回，避免無意義的通知
        previousMode = currentMode; // 記下舊模式，供之後還原使用
        currentMode  = mode;        // 更新目前模式
        listeners.forEach(l -> l.onModeChanged(currentMode, previousMode)); // 通知所有觀察者模式已切換
    }

    public void restorePreviousMode() { // 還原上一個模式（例如：建立物件後自動切回 SELECT）
        setMode(previousMode); // 呼叫 setMode 確保監聽者也被通知
    }

    public EditorMode getCurrentMode()  { return currentMode; }  // 取得目前模式
    public EditorMode getPreviousMode() { return previousMode; } // 取得前一個模式

    public void addListener(ModeChangeListener l)    { listeners.add(l); } // 新增一個模式切換監聽者
    public void removeListener(ModeChangeListener l) { listeners.remove(l); } // 移除一個模式切換監聽者
}
