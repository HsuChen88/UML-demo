package com.uml.view.renderer;

import com.uml.controller.strategy.CanvasMouseStrategy;

import java.awt.*;

public class CanvasOverlayRenderer { // 專責繪製畫布互動 overlay

    public void render(Graphics2D g, CanvasRenderContext context, CanvasMouseStrategy strategy) { // 委派目前 strategy 繪製 overlay
        if (strategy != null) strategy.paintOverlay(g, context);
    }
}
