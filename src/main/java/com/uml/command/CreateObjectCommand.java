package com.uml.command;

import com.uml.model.UMLObject;
import com.uml.view.CanvasPanel;

/**
 * Records the creation of a new UMLObject.
 * undo: removes the object from the canvas.
 * redo: re-adds it.
 */
public class CreateObjectCommand implements Command { // 建立物件的命令（Use Case A 的 Undo/Redo 支援）

    private final CanvasPanel canvas; // 目標畫布（執行 rawAdd/rawRemove 的對象）
    private final UMLObject   created; // 被建立的物件（undo 時移除，redo 時重新加入）

    public CreateObjectCommand(CanvasPanel canvas, UMLObject created) { // 建構子：接收畫布與被建立的物件
        this.canvas  = canvas; // 儲存畫布參考
        this.created = created; // 儲存被建立的物件
    }

    @Override
    public void undo() { // 還原：從畫布移除物件
        canvas.rawRemoveObject(created); // 直接移除物件（不觸發 repaint）
        canvas.clearSelection(); // 清除選取狀態
        canvas.repaint(); // 重繪畫布
    }

    @Override
    public void redo() { // 重做（同時作為初次執行）：將物件加回畫布
        canvas.rawAddObject(created); // 直接加入物件（不觸發 repaint）
        canvas.clearSelection(); // 清除其他物件的選取
        created.setSelected(true); // 選取新建立的物件
        canvas.repaint(); // 重繪畫布
    }
}
