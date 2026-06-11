package com.uml.model;

import com.uml.model.link.LinkObject;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DiagramDocument { // 一份 UML diagram 的結構資料，負責 objects、links 與 z-order

    /** Render order: higher index = top layer (drawn last / hit-tested first). */
    private final List<UMLObject> objects = new ArrayList<>(); // 儲存所有頂層 UML 物件
    private final List<LinkObject> links  = new ArrayList<>(); // 儲存所有連線物件

    public List<UMLObject> getObjects() { // 回傳唯讀物件清單，防止外部任意修改 z-order
        return Collections.unmodifiableList(objects);
    }

    public List<LinkObject> getLinks() { // 回傳唯讀連線清單
        return Collections.unmodifiableList(links);
    }

    public void addObject(UMLObject obj) { // 將物件加到最上層
        objects.add(obj);
    }

    public void addObjectAt(int index, UMLObject obj) { // 將物件插入指定 z-order 位置
        objects.add(clampIndex(index), obj);
    }

    public boolean removeObject(UMLObject obj) { // 移除指定物件
        return objects.remove(obj);
    }

    public int indexOfObject(UMLObject obj) { // 查詢物件目前的 z-order 位置
        return objects.indexOf(obj);
    }

    public void addLink(LinkObject link) { // 新增連線
        links.add(link);
    }

    public void addLinkAt(int index, LinkObject link) { // 將連線插入指定順序位置
        links.add(clampLinkIndex(index), link);
    }

    public boolean removeLink(LinkObject link) { // 移除連線
        return links.remove(link);
    }

    public int indexOfLink(LinkObject link) { // 查詢連線目前順序位置
        return links.indexOf(link);
    }

    public void bringToFront(UMLObject obj) { // 將物件移到最上層
        if (!objects.remove(obj)) return;
        objects.add(obj);
    }

    public UMLObject findObjectAt(int x, int y) { // 從最上層往下做點擊測試
        for (int i = objects.size() - 1; i >= 0; i--) {
            UMLObject obj = objects.get(i);
            if (obj.contains(x, y)) return obj;
        }
        return null;
    }

    public PortOwner findPortOwnerNearPort(int x, int y) { // 找出滑鼠附近具有 port 的物件
        Point mouse = new Point(x, y);
        for (int i = objects.size() - 1; i >= 0; i--) {
            UMLObject obj = objects.get(i);
            if (obj instanceof PortOwner owner && owner.getNearestPortIndex(mouse) != -1) {
                return owner;
            }
        }
        return null;
    }

    public PortReference findPortReferenceNearPoint(Point mouse) { // 找出滑鼠附近的具體 port reference
        for (int i = objects.size() - 1; i >= 0; i--) {
            UMLObject obj = objects.get(i);
            if (obj instanceof PortOwner owner) {
                int portIndex = owner.getNearestPortIndex(mouse);
                if (portIndex != -1) return new PortReference(owner, portIndex);
            }
        }
        return null;
    }

    private int clampIndex(int index) { // 將物件插入位置限制在合法範圍
        return Math.max(0, Math.min(index, objects.size()));
    }

    private int clampLinkIndex(int index) { // 將連線插入位置限制在合法範圍
        return Math.max(0, Math.min(index, links.size()));
    }
}
