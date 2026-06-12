package com.uml.command;

import com.uml.model.DiagramDocument;
import com.uml.model.DiagramSelectionModel;
import com.uml.model.object.CompositeObject;
import com.uml.model.object.UMLObject;

import java.util.List;

/**
 * Records an ungroup operation.
 * undo: re-groups the children back into the composite.
 * redo: re-dissolves the composite and restores the children.
 */
public class UngroupCommand implements Command { // 解散群組命令（Use Case D 解散的 Undo/Redo 支援）

    private final DiagramDocument document;             // 目標 diagram document
    private final DiagramSelectionModel selectionModel; // diagram 選取狀態
    private final CompositeObject composite;            // 被解散的複合物件
    private final List<UMLObject> children;             // 解散後還原的子物件清單
    private final int compositeIndex;                   // composite 被解散前的 z-order 位置

    public UngroupCommand(DiagramDocument document, // 建構子：接收 document、selection、複合物件與子物件清單
                          DiagramSelectionModel selectionModel,
                          CompositeObject composite,
                          List<UMLObject> children,
                          int compositeIndex) {
        this.document = document;               // 儲存 document 參考
        this.selectionModel = selectionModel;   // 儲存 selection 參考
        this.composite = composite;             // 儲存複合物件參考
        this.children  = List.copyOf(children); // 不可變複製，防止外部修改影響命令
        this.compositeIndex = compositeIndex;   // 儲存 composite 原始位置
    }

    @Override
    public void undo() { // 還原：重新群組化（將子物件替換回複合物件）
        children.forEach(document::removeObject);        // 從 document 移除所有子物件
        document.addObjectAt(compositeIndex, composite); // 將複合物件重新加回原本 z-order
        selectionModel.selectOnly(composite);            // 選取複合物件
    }

    @Override
    public void redo() { // 重做：重新解散群組（將複合物件替換為子物件）
        document.removeObject(composite); // 從 document 移除複合物件
        for (int i = 0; i < children.size(); i++) {
            document.addObjectAt(compositeIndex + i, children.get(i)); // 將所有子物件放回 composite 所在位置
        }
        selectionModel.selectAll(children); // 選取所有子物件
    }
}
