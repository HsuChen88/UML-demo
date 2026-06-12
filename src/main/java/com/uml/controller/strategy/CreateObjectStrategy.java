package com.uml.controller.strategy;

import com.uml.command.CreateObjectCommand;
import com.uml.controller.mode.ModeManager;
import com.uml.controller.tool.DiagramObjectFactory;
import com.uml.model.object.UMLObject;
import com.uml.util.UMLConstants;

import java.awt.event.MouseEvent;

/**
 * Handles Use Case A: when the user releases the mouse on the canvas while
 * in RECT or OVAL mode, a new object is created at that position.
 */
public class CreateObjectStrategy implements CanvasMouseStrategy { // 建立物件的 strategy ，對應 Use Case A

    private final DiagramObjectFactory objectFactory; // 建立 UMLObject 的 factory
    private final ModeManager modeManager; // 用於建立物件後還原前一個模式

    public CreateObjectStrategy(DiagramObjectFactory objectFactory, ModeManager modeManager) { // 建構子，注入物件 factory 與管理器
        this.objectFactory = objectFactory; // 儲存物件 factory
        this.modeManager = modeManager; // 儲存模式管理器
    }

    @Override
    public void onReleased(MouseEvent e, CanvasEditorContext context) { // 滑鼠放開時建立物件（Use Case A 的核心邏輯）
        int cx = e.getX() - UMLConstants.DEFAULT_W / 2; // 計算物件左上角 x，使物件以滑鼠為中心
        int cy = e.getY() - UMLConstants.DEFAULT_H / 2; // 計算物件左上角 y，使物件以滑鼠為中心

        UMLObject obj = objectFactory.create(cx, cy, UMLConstants.DEFAULT_W, UMLConstants.DEFAULT_H); // 由 factory 建立指定類型的物件

        context.execute(new CreateObjectCommand(context.getDocument(), context.getSelectionModel(), obj)); // 執行建立命令並推入命令歷史（支援 Undo）
        modeManager.restorePreviousMode(); // 建立完成後自動切回前一個模式（通常是 SELECT）
    }
}
