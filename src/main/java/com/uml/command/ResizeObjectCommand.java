package com.uml.command;

import com.uml.model.BasicObject;
import com.uml.model.DiagramDocument;
import com.uml.model.DiagramSelectionModel;

/**
 * Records a resize operation on a single BasicObject.
 *
 * Before/after bounds are captured by
 * {@link com.uml.controller.strategy.SelectStrategy} at press and release time.
 */
public class ResizeObjectCommand implements Command { // 縮放物件的命令（Use Case F 的 Undo/Redo 支援）

    private final DiagramDocument document; // 目標 diagram document
    private final DiagramSelectionModel selectionModel; // diagram 選取狀態
    private final BasicObject target; // 被縮放的物件
    private final int bx, by, bw, bh;   // 縮放前的邊界（x、y、width、height）
    private final int ax, ay, aw, ah;   // 縮放後的邊界

    public ResizeObjectCommand(DiagramDocument document, DiagramSelectionModel selectionModel, BasicObject target, // 建構子：接收 document、selection、目標物件及前後邊界快照
                               int bx, int by, int bw, int bh, // 縮放前邊界
                               int ax, int ay, int aw, int ah) { // 縮放後邊界
        this.document = document;
        this.selectionModel = selectionModel;
        this.target = target; // 儲存目標物件
        this.bx = bx; this.by = by; this.bw = bw; this.bh = bh; // 儲存縮放前邊界
        this.ax = ax; this.ay = ay; this.aw = aw; this.ah = ah; // 儲存縮放後邊界
    }

    @Override
    public void undo() { // 還原：將物件縮放回縮放前的邊界
        target.setBounds(bx, by, bw, bh); // 套用縮放前的邊界
        selectionModel.selectOnly(target); // 重新選取被縮放的物件
    }

    @Override
    public void redo() { // 重做：將物件縮放到縮放後的邊界
        target.setBounds(ax, ay, aw, ah); // 套用縮放後的邊界
        selectionModel.selectOnly(target); // 重新選取被縮放的物件
    }
}
