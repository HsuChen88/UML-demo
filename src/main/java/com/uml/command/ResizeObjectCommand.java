package com.uml.command;

import com.uml.model.BasicObject;
import com.uml.view.CanvasPanel;

/**
 * Records a resize operation on a single BasicObject.
 *
 * Before/after bounds are captured by
 * {@link com.uml.controller.strategy.SelectStrategy} at press and release time.
 */
public class ResizeObjectCommand implements Command { // 縮放物件的命令（Use Case F 的 Undo/Redo 支援）

    private final CanvasPanel canvas; // 目標畫布
    private final BasicObject target; // 被縮放的物件
    private final int bx, by, bw, bh;   // 縮放前的邊界（x、y、width、height）
    private final int ax, ay, aw, ah;   // 縮放後的邊界

    public ResizeObjectCommand(CanvasPanel canvas, BasicObject target, // 建構子：接收畫布、目標物件及前後邊界快照
                               int bx, int by, int bw, int bh, // 縮放前邊界
                               int ax, int ay, int aw, int ah) { // 縮放後邊界
        this.canvas = canvas; // 儲存畫布參考
        this.target = target; // 儲存目標物件
        this.bx = bx; this.by = by; this.bw = bw; this.bh = bh; // 儲存縮放前邊界
        this.ax = ax; this.ay = ay; this.aw = aw; this.ah = ah; // 儲存縮放後邊界
    }

    @Override
    public void undo() { // 還原：將物件縮放回縮放前的邊界
        target.setBounds(bx, by, bw, bh); // 套用縮放前的邊界
        canvas.clearSelection(); // 清除所有選取
        target.setSelected(true); // 重新選取被縮放的物件
        canvas.repaint(); // 重繪畫布
    }

    @Override
    public void redo() { // 重做：將物件縮放到縮放後的邊界
        target.setBounds(ax, ay, aw, ah); // 套用縮放後的邊界
        canvas.clearSelection(); // 清除所有選取
        target.setSelected(true); // 重新選取被縮放的物件
        canvas.repaint(); // 重繪畫布
    }
}
