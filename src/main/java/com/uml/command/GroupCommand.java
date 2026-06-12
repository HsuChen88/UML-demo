package com.uml.command;

import com.uml.model.DiagramDocument;
import com.uml.model.DiagramSelectionModel;
import com.uml.model.object.CompositeObject;
import com.uml.model.object.UMLObject;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Records a group operation.
 * undo: dissolves the composite and restores the original children.
 * redo: re-groups the same children into the same CompositeObject instance.
 */
public class GroupCommand implements Command { // 群組命令（Use Case D 群組的 Undo/Redo 支援）

    private final DiagramDocument document; // 目標 diagram document
    private final DiagramSelectionModel selectionModel; // diagram 選取狀態
    private final List<UMLObject> children;   // original members, order preserved // 群組化前的原始成員清單（保留順序）
    private final List<Integer> childIndexes; // 群組化前每個成員的原始 z-order 位置
    private final CompositeObject composite; // 群組化後建立的複合物件
    private final int compositeIndex; // 群組化後 composite 應插入的位置

    public GroupCommand(DiagramDocument document, // 建構子：接收 document、selection、原始成員清單與複合物件
                        DiagramSelectionModel selectionModel,
                        List<UMLObject> children,
                        CompositeObject composite,
                        List<Integer> childIndexes,
                        int compositeIndex) {
        this.document = document; // 儲存 document 參考
        this.selectionModel = selectionModel; // 儲存 selection 參考
        this.children  = List.copyOf(children); // 不可變複製，防止外部修改影響命令
        this.childIndexes = List.copyOf(childIndexes); // 不可變複製原始 z-order 位置
        this.composite = composite; // 儲存複合物件參考
        this.compositeIndex = compositeIndex; // 儲存 composite 插入位置
    }

    @Override
    public void undo() { // 還原：解散群組，將子物件逐一還原到 document 的原始 z-order
        document.removeObject(composite); // 移除複合物件
        restoreChildrenAtOriginalIndexes(); // 將所有原始子物件重新加回原本 z-order
        selectionModel.selectAll(children); // 選取所有還原的子物件
    }

    @Override
    public void redo() { // 重做：重新群組化（將子物件替換為複合物件）
        removeChildrenFromDocument(); // 從 document 移除所有子物件
        document.addObjectAt(compositeIndex, composite); // 將複合物件加入指定 z-order
        selectionModel.selectOnly(composite); // 選取複合物件
    }

    private void removeChildrenFromDocument() { // 依目前 z-order 由高到低移除 children，避免 index 位移影響
        children.stream()
                .filter(child -> document.indexOfObject(child) != -1)
                .sorted(Comparator.comparingInt(document::indexOfObject).reversed())
                .forEach(document::removeObject);
    }

    private void restoreChildrenAtOriginalIndexes() { // 依原始 index 由低到高還原 children
        Map<UMLObject, Integer> indexes = IntStream.range(0, children.size()).boxed()
                .collect(Collectors.toMap(children::get, childIndexes::get));
        List<UMLObject> orderedChildren = new ArrayList<>(children);
        orderedChildren.sort(Comparator.comparingInt(indexes::get));
        for (UMLObject child : orderedChildren) {
            document.addObjectAt(indexes.get(child), child);
        }
    }
}
