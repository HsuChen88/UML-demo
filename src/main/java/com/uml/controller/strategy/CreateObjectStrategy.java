package com.uml.controller.strategy;

import com.uml.command.CreateObjectCommand;
import com.uml.controller.EditorMode;
import com.uml.controller.ModeManager;
import com.uml.model.OvalObject;
import com.uml.model.RectObject;
import com.uml.model.UMLObject;
import com.uml.util.UMLConstants;
import com.uml.view.CanvasPanel;

import java.awt.event.MouseEvent;

/**
 * Handles Use Case A: when the user releases the mouse on the canvas while
 * in RECT or OVAL mode, a new object is created at that position.
 */
public class CreateObjectStrategy implements CanvasMouseStrategy { // 建立物件的策略，對應 Use Case A

    private final EditorMode  objectMode; // 記錄此策略對應的模式（RECT 或 OVAL）
    private final ModeManager modeManager; // 用於建立物件後還原前一個模式

    public CreateObjectStrategy(EditorMode objectMode, ModeManager modeManager) { // 建構子，注入模式與管理器
        this.objectMode  = objectMode; // 儲存物件模式
        this.modeManager = modeManager; // 儲存模式管理器
    }

    @Override
    public void onReleased(MouseEvent e, CanvasPanel canvas) { // 滑鼠放開時建立物件（Use Case A 的核心邏輯）
        int cx = e.getX() - UMLConstants.DEFAULT_W / 2; // 計算物件左上角 x，使物件以滑鼠為中心
        int cy = e.getY() - UMLConstants.DEFAULT_H / 2; // 計算物件左上角 y，使物件以滑鼠為中心

        UMLObject obj = (objectMode == EditorMode.RECT) // 根據目前模式決定建立矩形還是橢圓
                ? new RectObject(cx, cy, UMLConstants.DEFAULT_W, UMLConstants.DEFAULT_H) // 建立矩形物件
                : new OvalObject(cx, cy, UMLConstants.DEFAULT_W, UMLConstants.DEFAULT_H); // 建立橢圓物件

        canvas.execute(new CreateObjectCommand(canvas, obj)); // 執行建立命令並推入命令歷史（支援 Undo）
        modeManager.restorePreviousMode(); // 建立完成後自動切回前一個模式（通常是 SELECT）
    }
}
