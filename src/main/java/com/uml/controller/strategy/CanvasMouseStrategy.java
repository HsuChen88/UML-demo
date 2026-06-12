package com.uml.controller.strategy;

import com.uml.model.object.UMLObject;
import com.uml.view.renderer.CanvasRenderContext;

import java.awt.*;
import java.awt.event.MouseEvent;

/**
 * Strategy interface for canvas mouse interactions.
 * All methods have no-op defaults so implementors only override what they need (ISP).
 */
public interface CanvasMouseStrategy { // Strategy Pattern 的 strategy 介面，定義所有可能的滑鼠事件處理方法（預設皆為空操作，子類別視需要覆寫）
    default void onPressed (MouseEvent e, CanvasEditorContext context) {} // 滑鼠按下事件
    default void onDragged (MouseEvent e, CanvasEditorContext context) {} // 滑鼠拖曳事件（按住移動）
    default void onReleased(MouseEvent e, CanvasEditorContext context) {} // 滑鼠放開事件
    default void onMoved   (MouseEvent e, CanvasEditorContext context) {  // 滑鼠移動事件（未按下）
        UMLObject hovered = context.getDocument().findObjectAt(e.getX(), e.getY()); // 找出目前 hover 物件
        if (hovered == context.getInteractionState().getHoveredObject()) return;    // 若未改變則不重繪
        context.getInteractionState().setHoveredObject(hovered); // 更新 hover 狀態
        context.repaintCanvas(); // 重繪
    }
    default void onClicked (MouseEvent e, CanvasEditorContext context) {}   // 滑鼠點擊事件（pressed + released 在同位置）
    default void paintOverlay(Graphics2D g, CanvasRenderContext context) {} // 繪製目前 strategy 的 overlay（預設無）
}
