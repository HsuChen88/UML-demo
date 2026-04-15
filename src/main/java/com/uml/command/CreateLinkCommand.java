package com.uml.command;

import com.uml.model.link.LinkObject;
import com.uml.view.CanvasPanel;

/**
 * Records the creation of a new LinkObject.
 * undo: removes the link.
 * redo: re-adds it.
 */
public class CreateLinkCommand implements Command { // 建立連線的命令（Use Case B 的 Undo/Redo 支援）

    private final CanvasPanel canvas; // 目標畫布
    private final LinkObject  created; // 被建立的連線物件

    public CreateLinkCommand(CanvasPanel canvas, LinkObject created) { // 建構子：接收畫布與被建立的連線
        this.canvas  = canvas; // 儲存畫布參考
        this.created = created; // 儲存被建立的連線
    }

    @Override
    public void undo() { // 還原：從畫布移除連線
        canvas.rawRemoveLink(created); // 直接移除連線（不觸發 repaint）
        canvas.repaint(); // 重繪畫布
    }

    @Override
    public void redo() { // 重做（同時作為初次執行）：將連線加回畫布
        canvas.rawAddLink(created); // 直接加入連線（不觸發 repaint）
        canvas.repaint(); // 重繪畫布
    }
}
