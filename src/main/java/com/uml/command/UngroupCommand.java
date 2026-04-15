package com.uml.command;

import com.uml.model.CompositeObject;
import com.uml.model.UMLObject;
import com.uml.view.CanvasPanel;

import java.util.List;

/**
 * Records an ungroup operation.
 * undo: re-groups the children back into the composite.
 * redo: re-dissolves the composite and restores the children.
 */
public class UngroupCommand implements Command { // 解散群組命令（Use Case D 解散的 Undo/Redo 支援）

    private final CanvasPanel     canvas; // 目標畫布
    private final CompositeObject composite; // 被解散的複合物件
    private final List<UMLObject> children; // 解散後還原的子物件清單

    public UngroupCommand(CanvasPanel canvas, // 建構子：接收畫布、複合物件與子物件清單
                          CompositeObject composite,
                          List<UMLObject> children) {
        this.canvas    = canvas; // 儲存畫布參考
        this.composite = composite; // 儲存複合物件參考
        this.children  = List.copyOf(children); // 不可變複製，防止外部修改影響命令
    }

    @Override
    public void undo() { // 還原：重新群組化（將子物件替換回複合物件）
        children.forEach(canvas::rawRemoveObject); // 從畫布移除所有子物件
        canvas.rawAddObject(composite); // 將複合物件重新加回畫布
        canvas.clearSelection(); // 清除選取
        children.forEach(o -> o.setSelected(false)); // 子物件已移出 canvas.objects，需手動清除選取旗標
        composite.setSelected(true); // 選取複合物件
        canvas.repaint(); // 重繪畫布
    }

    @Override
    public void redo() { // 重做：重新解散群組（將複合物件替換為子物件）
        canvas.rawRemoveObject(composite); // 從畫布移除複合物件
        children.forEach(canvas::rawAddObject); // 將所有子物件加回畫布
        canvas.clearSelection(); // 清除選取
        children.forEach(o -> o.setSelected(true)); // 選取所有子物件
        canvas.repaint(); // 重繪畫布
    }
}
