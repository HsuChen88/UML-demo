package com.uml.view.renderer;

import com.uml.model.object.BasicObject;
import com.uml.model.object.CompositeObject;
import com.uml.model.object.UMLObject;
import com.uml.util.UMLConstants;

import java.awt.*;

public class UMLObjectRenderer { // 專責繪製 UMLObject 的 renderer

    public void render(Graphics2D g, UMLObject object, CanvasRenderContext context) { // 繪製單一 UMLObject
        object.draw(g);
        if (isSelectedOrHovered(object, context)) { // 若物件被選取或 hover ，renderer 負責畫互動視覺
            if (object instanceof BasicObject basicObject) {
                basicObject.drawPorts(g);                // BasicObject 顯示 ports
            } else if (object instanceof CompositeObject compositeObject) {
                drawCompositeBorder(g, compositeObject); // CompositeObject 顯示群組外框
            }
        }
    }

    private boolean isSelectedOrHovered(UMLObject object, CanvasRenderContext context) { // 判斷物件是否處於選取或 hover 狀態
        return context.selectionModel().isSelected(object) ||
               context.interactionState().getHoveredObject() == object;
    }

    private void drawCompositeBorder(Graphics2D g, CompositeObject compositeObject) { // 繪製群組選取 / hover 外框
        Rectangle b = compositeObject.getBounds();
        int pad = UMLConstants.COMPOSITE_PAD;
        float[] dash = UMLConstants.DASH_COMPOSITE;
        g.setStroke(new BasicStroke(UMLConstants.STROKE_NORMAL,
                BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f, dash, 0f));
        g.setColor(Color.DARK_GRAY);
        g.drawRect(b.x - pad, b.y - pad, b.width + pad * 2, b.height + pad * 2);
        g.setStroke(new BasicStroke(UMLConstants.STROKE_THIN)); // 重置畫筆，避免影響後續繪製
    }
}
