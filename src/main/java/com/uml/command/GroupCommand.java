package com.uml.command;

import com.uml.model.CompositeObject;
import com.uml.model.UMLObject;
import com.uml.view.CanvasPanel;

import java.util.List;

/**
 * Records a group operation.
 * undo: dissolves the composite and restores the original children.
 * redo: re-groups the same children into the same CompositeObject instance.
 */
public class GroupCommand implements Command { // 群組命令（Use Case D 群組的 Undo/Redo 支援）

    private final CanvasPanel     canvas; // 目標畫布
    private final List<UMLObject> children;   // original members, order preserved // 群組化前的原始成員清單（保留順序）
    private final CompositeObject composite; // 群組化後建立的複合物件

    public GroupCommand(CanvasPanel canvas, // 建構子：接收畫布、原始成員清單與複合物件
                        List<UMLObject> children,
                        CompositeObject composite) {
        this.canvas    = canvas; // 儲存畫布參考
        this.children  = List.copyOf(children); // 不可變複製，防止外部修改影響命令
        this.composite = composite; // 儲存複合物件參考
    }

    @Override
    public void undo() { // 還原：解散群組，將子物件逐一還原到畫布
        canvas.rawRemoveObject(composite); // 移除複合物件
        children.forEach(canvas::rawAddObject); // 將所有原始子物件重新加回畫布
        canvas.clearSelection(); // 清除選取
        children.forEach(o -> o.setSelected(true)); // 選取所有還原的子物件
        canvas.repaint(); // 重繪畫布
    }

    @Override
    public void redo() { // 重做：重新群組化（將子物件替換為複合物件）
        children.forEach(canvas::rawRemoveObject); // 從畫布移除所有子物件
        canvas.rawAddObject(composite); // 將複合物件加入畫布
        canvas.clearSelection(); // 清除選取
        children.forEach(o -> o.setSelected(false)); // 子物件已移出 canvas.objects，需手動清除選取旗標
        composite.setSelected(true); // 選取複合物件
        canvas.repaint(); // 重繪畫布
    }
}
