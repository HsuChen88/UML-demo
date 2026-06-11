package com.uml.command;

import com.uml.model.link.LinkObject;
import com.uml.model.DiagramDocument;

/**
 * Records the creation of a new LinkObject.
 * undo: removes the link.
 * redo: re-adds it.
 */
public class CreateLinkCommand implements Command { // 建立連線的命令（Use Case B 的 Undo/Redo 支援）

    private final DiagramDocument document; // 目標 diagram document
    private final LinkObject created; // 被建立的連線物件

    public CreateLinkCommand(DiagramDocument document, LinkObject created) { // 建構子：接收 document 與被建立的連線
        this.document = document;
        this.created = created; // 儲存被建立的連線
    }

    @Override
    public void undo() { // 還原：從畫布移除連線
        document.removeLink(created); // 從 document 移除連線
    }

    @Override
    public void redo() { // 重做（同時作為初次執行）：將連線加回畫布
        document.addLink(created); // 將連線加入 document
    }
}
