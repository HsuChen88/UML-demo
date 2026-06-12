package com.uml.view.renderer;

import com.uml.model.link.LinkObject;

import java.awt.*;

public class LinkRenderer { // 專責繪製 LinkObject 的 renderer

    public void render(Graphics2D g, LinkObject link, CanvasRenderContext context) { // 繪製單一連線
        link.draw(g);
    }
}
