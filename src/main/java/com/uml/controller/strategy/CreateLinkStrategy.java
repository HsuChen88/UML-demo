package com.uml.controller.strategy;

import com.uml.command.CreateLinkCommand;
import com.uml.controller.tool.DiagramLinkFactory;
import com.uml.model.PortReference;
import com.uml.model.link.LinkObject;
import com.uml.util.UMLConstants;
import com.uml.view.CanvasPanel;
import com.uml.view.renderer.CanvasRenderContext;

import java.awt.*;
import java.awt.event.MouseEvent;

/**
 * Handles Use Case B: drag from one BasicObject port to another to create a link.
 * Composite objects are explicitly excluded as endpoints (spec requirement).
 */
public class CreateLinkStrategy implements CanvasMouseStrategy { // 建立連線的策略，對應 Use Case B

    private final DiagramLinkFactory linkFactory; // 建立 LinkObject 的 factory

    private PortReference sourcePortReference = null; // 連線的起點 port reference（按下滑鼠時決定）
    private Point tempEnd = null; // 拖曳時滑鼠目前位置，用於繪製預覽虛線

    public CreateLinkStrategy(DiagramLinkFactory linkFactory) { // 建構子，注入連線 factory
        this.linkFactory = linkFactory; // 儲存連線 factory
    }

    @Override
    public void onPressed(MouseEvent e, CanvasPanel canvas) { // 滑鼠按下：尋找起點 port（Use Case B 步驟 1）
        // B.1: press must land on a BasicObject port
        sourcePortReference = canvas.getDocument().findPortReferenceNearPoint(e.getPoint()); // 找出靠近滑鼠的 port reference
        if (sourcePortReference != null) { // 若找到有效起點
            tempEnd = e.getPoint(); // 初始化預覽線的終點
            canvas.getInteractionState().setTemporaryLink(sourcePortReference, tempEnd); // 設定預覽線狀態
        }
    }

    @Override
    public void onDragged(MouseEvent e, CanvasPanel canvas) { // 滑鼠拖曳：更新預覽線終點
        if (sourcePortReference == null) return; // 若沒有起點則忽略拖曳
        tempEnd = e.getPoint();         // 更新預覽線終點為目前滑鼠位置
        canvas.getInteractionState().setTemporaryLink(sourcePortReference, tempEnd); // 通知畫布更新預覽線
        canvas.repaint(); // 重繪
    }

    @Override
    public void onReleased(MouseEvent e, CanvasPanel canvas) { // 滑鼠放開：嘗試建立連線（Use Case B 步驟 2）
        canvas.getInteractionState().clearTemporaryLink(); // 清除預覽線
        canvas.repaint(); // 重繪
        if (sourcePortReference == null) return; // 若沒有有效起點則放棄

        // B.2: release must land on a *different* BasicObject's port
        PortReference targetPortReference = canvas.getDocument().findPortReferenceNearPoint(e.getPoint()); // 找出靠近放開位置的 port reference

        if (targetPortReference != null && targetPortReference.owner() != sourcePortReference.owner()) { // 終點必須存在且不能是起點本身
            LinkObject link = linkFactory.create(sourcePortReference, targetPortReference); // 根據 factory 建立對應的連線物件
            canvas.execute(new CreateLinkCommand(canvas.getDocument(), link)); // 執行建立命令並推入歷史（支援 Undo）
        }

        sourcePortReference = null; // 重置起點 port reference
        tempEnd = null; // 重置預覽線終點
    }

    @Override
    public void paintOverlay(Graphics2D g, CanvasRenderContext context) { // 繪製暫時連線預覽
        PortReference source = context.interactionState().getTemporaryLinkSource();
        Point end = context.interactionState().getTemporaryLinkEnd();
        if (source == null || end == null) return;
        Point start = source.getPoint();
        g.setStroke(new BasicStroke(UMLConstants.STROKE_PREVIEW, BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_MITER, 10f, UMLConstants.DASH_PREVIEW, 0f));
        g.setColor(Color.GRAY);
        g.drawLine(start.x, start.y, end.x, end.y);
    }
}
