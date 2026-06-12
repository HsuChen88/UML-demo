package com.uml.command;

import com.uml.model.DiagramDocument;
import com.uml.model.DiagramSelectionModel;
import com.uml.model.object.UMLObject;

/**
 * Records the creation of a new UMLObject.
 * undo: removes the object from the canvas.
 * redo: re-adds it.
 */
public class CreateObjectCommand implements Command { // 建立物件的命令（Use Case A 的 Undo/Redo 支援）

    private final DiagramDocument document;             // 目標 diagram document
    private final DiagramSelectionModel selectionModel; // diagram 選取狀態
    private final UMLObject created;                    // 被建立的物件（undo 時移除，redo 時重新加入）

    public CreateObjectCommand(DiagramDocument document, DiagramSelectionModel selectionModel, UMLObject created) { // 建構子：接收 document、selection 與被建立的物件
        this.document = document;
        this.selectionModel = selectionModel;
        this.created = created; // 儲存被建立的物件
    }

    @Override
    public void undo() {
        document.removeObject(created);  // 從 document 移除物件
        selectionModel.clearSelection(); // 清除選取狀態
    }

    @Override
    public void redo() { 
        document.addObject(created);        // 將物件加入 document
        selectionModel.selectOnly(created); // 選取新建立的物件
    }
}
