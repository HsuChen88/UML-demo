package com.uml.command;

import com.uml.model.BasicObject;
import com.uml.view.CanvasPanel;

import java.awt.Color;

/**
 * Records a label name and/or colour change on a BasicObject.
 * Both before and after states are captured before the dialog closes.
 */
public class SetLabelCommand implements Command { // 設定標籤命令（Use Case G 的 Undo/Redo 支援）

    private final CanvasPanel canvas; // 目標畫布
    private final BasicObject target; // 被修改標籤的物件
    private final String      beforeName; // 修改前的標籤文字
    private final Color       beforeColor; // 修改前的標籤顏色
    private final String      afterName; // 修改後的標籤文字
    private final Color       afterColor; // 修改後的標籤顏色

    public SetLabelCommand(CanvasPanel canvas, BasicObject target, // 建構子：接收畫布、目標物件及前後狀態
                           String beforeName, Color beforeColor, // 修改前的標籤文字與顏色
                           String afterName,  Color afterColor) { // 修改後的標籤文字與顏色
        this.canvas      = canvas; // 儲存畫布參考
        this.target      = target; // 儲存目標物件
        this.beforeName  = beforeName; // 儲存修改前標籤文字
        this.beforeColor = beforeColor; // 儲存修改前標籤顏色
        this.afterName   = afterName; // 儲存修改後標籤文字
        this.afterColor  = afterColor; // 儲存修改後標籤顏色
    }

    @Override
    public void undo() { // 還原：將標籤文字與顏色恢復為修改前的狀態
        target.setLabelName(beforeName); // 還原標籤文字
        target.setLabelColor(beforeColor); // 還原標籤顏色
        canvas.repaint(); // 重繪畫布
    }

    @Override
    public void redo() { // 重做：將標籤文字與顏色設為修改後的狀態
        target.setLabelName(afterName); // 套用新標籤文字
        target.setLabelColor(afterColor); // 套用新標籤顏色
        canvas.repaint(); // 重繪畫布
    }
}
