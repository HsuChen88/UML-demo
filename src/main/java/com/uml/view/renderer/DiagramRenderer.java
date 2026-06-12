package com.uml.view.renderer;

import com.uml.model.link.LinkObject;
import com.uml.model.object.UMLObject;

import java.awt.*;

public class DiagramRenderer { // 專責繪製整份 diagram 的 renderer

    private final UMLObjectRenderer objectRenderer = new UMLObjectRenderer(); // 物件 renderer
    private final LinkRenderer linkRenderer = new LinkRenderer(); // 連線 renderer

    public void render(Graphics2D g, CanvasRenderContext context) { // 繪製 document 中所有物件與連線
        for (UMLObject object : context.document().getObjects()) {
            objectRenderer.render(g, object, context);
        }
        for (LinkObject link : context.document().getLinks()) {
            linkRenderer.render(g, link, context);
        }
    }
}
