package com.uml.controller.strategy;

import com.uml.view.CanvasPanel;

import java.awt.event.MouseEvent;

/**
 * Strategy interface for canvas mouse interactions.
 * All methods have no-op defaults so implementors only override what they need (ISP).
 */
public interface CanvasMouseStrategy { // Strategy Pattern 的策略介面，定義所有可能的滑鼠事件處理方法（預設皆為空操作，子類別視需要覆寫）
    default void onPressed (MouseEvent e, CanvasPanel canvas) {} // 滑鼠按下事件
    default void onDragged (MouseEvent e, CanvasPanel canvas) {} // 滑鼠拖曳事件（按住移動）
    default void onReleased(MouseEvent e, CanvasPanel canvas) {} // 滑鼠放開事件
    default void onMoved   (MouseEvent e, CanvasPanel canvas) {} // 滑鼠移動事件（未按下）
    default void onClicked (MouseEvent e, CanvasPanel canvas) {} // 滑鼠點擊事件（pressed + released 在同位置）
}
