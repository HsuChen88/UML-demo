package com.uml.command;

import com.uml.model.UMLObject;
import com.uml.view.CanvasPanel;

import java.awt.Point;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Records the movement of one or more objects.
 *
 * Snapshots of (x, y) positions are captured before and after the drag
 * by {@link com.uml.controller.strategy.SelectStrategy}.
 * undo: moves every object back to its before-position.
 * redo: moves every object to its after-position.
 *
 * Uses {@link UMLObject#moveTo} for absolute positioning.
 */
public class MoveObjectsCommand implements Command { // 移動物件的命令（Use Case E 的 Undo/Redo 支援）

    private final CanvasPanel           canvas; // 目標畫布
    /** object → top-left position BEFORE the move */
    private final Map<UMLObject, Point> before; // 移動前各物件的位置快照（物件 → 左上角座標）
    /** object → top-left position AFTER the move */
    private final Map<UMLObject, Point> after; // 移動後各物件的位置快照

    public MoveObjectsCommand(CanvasPanel canvas, // 建構子：接收畫布與前後位置快照
                              Map<UMLObject, Point> before,
                              Map<UMLObject, Point> after) {
        this.canvas = canvas; // 儲存畫布參考
        this.before = new LinkedHashMap<>(before); // 深複製前快照（防止外部修改影響命令）
        this.after  = new LinkedHashMap<>(after); // 深複製後快照
    }

    @Override
    public void undo() { // 還原：將所有物件移回移動前的位置
        before.forEach((obj, pos) -> obj.moveTo(pos.x, pos.y)); // 對每個物件呼叫 moveTo 還原位置
        restoreSelection(before); // 恢復選取狀態（選取被移動過的物件）
        canvas.repaint(); // 重繪畫布
    }

    @Override
    public void redo() { // 重做：將所有物件移到移動後的位置
        after.forEach((obj, pos) -> obj.moveTo(pos.x, pos.y)); // 對每個物件呼叫 moveTo 套用新位置
        restoreSelection(after); // 恢復選取狀態
        canvas.repaint(); // 重繪畫布
    }

    private void restoreSelection(Map<UMLObject, Point> positions) { // 恢復選取狀態的輔助方法
        canvas.clearSelection(); // 先清除所有選取
        positions.keySet().forEach(o -> o.setSelected(true)); // 再選取所有被移動的物件
    }
}
