package com.uml.controller;

public interface ModeChangeListener { // Observer Pattern 的觀察者介面，監聽模式切換事件
    void onModeChanged(EditorMode newMode, EditorMode prevMode); // 當模式切換時呼叫；newMode 為新模式，prevMode 為前一個模式
}
