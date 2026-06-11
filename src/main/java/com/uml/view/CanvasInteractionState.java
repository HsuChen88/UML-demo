package com.uml.view;

import com.uml.model.PortReference;
import com.uml.model.UMLObject;

import java.awt.*;

public class CanvasInteractionState { // 管理畫布互動暫態，例如 hover、框選與暫時連線預覽

    private UMLObject hoveredObject = null; // 滑鼠目前懸停的物件
    private Rectangle rubberBand = null; // 框選矩形
    private PortReference temporaryLinkSource = null; // 暫時連線起點 port
    private Point temporaryLinkEnd = null; // 暫時連線終點座標

    public UMLObject getHoveredObject() { return hoveredObject; } // 取得目前懸停物件

    public void setHoveredObject(UMLObject obj) { // 設定懸停物件並同步舊有 hovered flag
        if (hoveredObject == obj) return;
        if (hoveredObject != null) hoveredObject.setHovered(false);
        hoveredObject = obj;
        if (hoveredObject != null) hoveredObject.setHovered(true);
    }

    public void clearHover() { // 清除懸停狀態
        setHoveredObject(null);
    }

    public Rectangle getRubberBand() { return rubberBand; } // 取得框選矩形
    public void setRubberBand(Rectangle r) { rubberBand = r; } // 設定框選矩形

    public PortReference getTemporaryLinkSource() { return temporaryLinkSource; } // 取得暫時連線起點
    public Point getTemporaryLinkEnd() { return temporaryLinkEnd; } // 取得暫時連線終點

    public void setTemporaryLink(PortReference source, Point end) { // 設定暫時連線預覽
        temporaryLinkSource = source;
        temporaryLinkEnd = end;
    }

    public void clearTemporaryLink() { // 清除暫時連線預覽
        temporaryLinkSource = null;
        temporaryLinkEnd = null;
    }
}
