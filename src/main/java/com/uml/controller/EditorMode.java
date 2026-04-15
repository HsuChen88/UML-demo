package com.uml.controller;

public enum EditorMode { // 列舉所有可能的編輯器模式（State Pattern 的狀態定義）
    SELECT,         // 選取模式：可點選、拖曳、框選、縮放物件
    ASSOCIATION,    // 關聯線模式：在兩個 BasicObject 之間建立 Association 連線
    GENERALIZATION, // 繼承線模式：在兩個 BasicObject 之間建立 Generalization 連線
    COMPOSITION,    // 組合線模式：在兩個 BasicObject 之間建立 Composition 連線
    RECT,           // 矩形模式：在畫布上點擊後建立一個 RectObject
    OVAL;           // 橢圓模式：在畫布上點擊後建立一個 OvalObject

    /** 判斷此模式是否為「建立物件」模式（按鈕拖曳至畫布的流程需要此判斷） */
    public boolean isObjectCreation() {
        return this == RECT || this == OVAL;
    }
}
