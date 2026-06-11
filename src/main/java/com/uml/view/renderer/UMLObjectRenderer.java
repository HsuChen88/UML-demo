package com.uml.view.renderer;

import com.uml.model.UMLObject;

import java.awt.*;

public class UMLObjectRenderer { // 專責繪製 UMLObject 的 renderer

    public void render(Graphics2D g, UMLObject object, CanvasRenderContext context) { // 繪製單一 UMLObject
        object.draw(g); // 第一階段先委派既有 draw()，降低重構風險
    }
}
