package com.uml.controller.tool;

import com.uml.model.object.UMLObject;

@FunctionalInterface
public interface DiagramObjectFactory { // 建立 UMLObject 的 factory interface

    UMLObject create(int x, int y, int width, int height); // 依位置與尺寸建立物件
}
