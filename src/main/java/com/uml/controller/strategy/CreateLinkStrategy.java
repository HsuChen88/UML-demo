package com.uml.controller.strategy;

import com.uml.command.CreateLinkCommand;
import com.uml.controller.EditorMode;
import com.uml.model.BasicObject;
import com.uml.model.link.AssociationLink;
import com.uml.model.link.CompositionLink;
import com.uml.model.link.GeneralizationLink;
import com.uml.model.link.LinkObject;
import com.uml.view.CanvasPanel;

import java.awt.*;
import java.awt.event.MouseEvent;

/**
 * Handles Use Case B: drag from one BasicObject port to another to create a link.
 * Composite objects are explicitly excluded as endpoints (spec requirement).
 */
public class CreateLinkStrategy implements CanvasMouseStrategy { // 建立連線的策略，對應 Use Case B

    private final EditorMode linkMode; // 記錄此策略要建立的連線類型（ASSOCIATION、GENERALIZATION、COMPOSITION）

    private BasicObject sourceObject    = null; // 連線的起點物件（按下滑鼠時決定）
    private int         sourcePortIndex = -1;   // 連線起點的 port 索引（-1 表示尚未選取）
    private Point       tempEnd         = null; // 拖曳時滑鼠目前位置，用於繪製預覽虛線

    public CreateLinkStrategy(EditorMode linkMode) { // 建構子，注入連線模式
        this.linkMode = linkMode; // 儲存連線模式
    }

    @Override
    public void onPressed(MouseEvent e, CanvasPanel canvas) { // 滑鼠按下：尋找起點 port（Use Case B 步驟 1）
        // B.1: press must land on a BasicObject port
        sourceObject = canvas.findBasicObjectNearPort(e.getX(), e.getY());    // 找出靠近滑鼠的 BasicObject
        if (sourceObject != null) { // 若找到有效起點
            sourcePortIndex = sourceObject.getNearestPortIndex(e.getPoint()); // 記錄最近的 port 索引
            tempEnd         = e.getPoint();                                   // 初始化預覽線的終點
        }
    }

    @Override
    public void onDragged(MouseEvent e, CanvasPanel canvas) { // 滑鼠拖曳：更新預覽線終點
        if (sourceObject == null) return; // 若沒有起點則忽略拖曳
        tempEnd = e.getPoint();         // 更新預覽線終點為目前滑鼠位置
        canvas.setTempLinkEnd(tempEnd); // 通知畫布更新預覽線（觸發 repaint）
    }

    @Override
    public void onReleased(MouseEvent e, CanvasPanel canvas) { // 滑鼠放開：嘗試建立連線（Use Case B 步驟 2）
        canvas.setTempLinkEnd(null);      // 清除預覽線
        if (sourceObject == null) return; // 若沒有有效起點則放棄

        // B.2: release must land on a *different* BasicObject's port
        BasicObject targetObject = canvas.findBasicObjectNearPort(e.getX(), e.getY()); // 找出靠近放開位置的 BasicObject

        if (targetObject != null && targetObject != sourceObject) { // 終點必須存在且不能是起點本身
            int tgtPort = targetObject.getNearestPortIndex(e.getPoint()); // 取得終點的 port 索引
            LinkObject link = buildLink(sourceObject, sourcePortIndex, targetObject, tgtPort); // 根據模式建立對應的連線物件
            canvas.execute(new CreateLinkCommand(canvas, link)); // 執行建立命令並推入歷史（支援 Undo）
        }

        sourceObject    = null; // 重置起點物件
        sourcePortIndex = -1;   // 重置起點 port 索引
        tempEnd         = null; // 重置預覽線終點
    }

    private LinkObject buildLink(BasicObject src, int sp, BasicObject tgt, int tp) { // 工廠方法：根據 linkMode 建立對應型別的連線
        return switch (linkMode) { // 使用 switch expression 選擇連線型別
            case ASSOCIATION    -> new AssociationLink   (src, sp, tgt, tp); // 關聯線
            case GENERALIZATION -> new GeneralizationLink(src, sp, tgt, tp); // 繼承線
            case COMPOSITION    -> new CompositionLink   (src, sp, tgt, tp); // 組合線
            default -> throw new IllegalStateException("Unexpected link mode: " + linkMode); // 不應發生的情況
        };
    }

    public Point      getTempEnd()        { return tempEnd; }         // 取得目前預覽線終點（供畫布繪製使用）
    public BasicObject getSourceObject()  { return sourceObject; }    // 取得連線起點物件（供畫布繪製使用）
    public int        getSourcePortIndex(){ return sourcePortIndex; } // 取得連線起點 port 索引（供畫布繪製使用）
}
