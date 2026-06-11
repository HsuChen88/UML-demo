package com.uml.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class DiagramSelectionModel { // 管理 diagram 的選取狀態，避免選取成為 UMLObject 的核心資料

    private final DiagramDocument document; // selection 所屬的 diagram document
    private final Set<UMLObject> selectedObjects = new LinkedHashSet<>(); // 保留選取順序

    public DiagramSelectionModel(DiagramDocument document) { // 建構子：綁定一份 document
        this.document = document;
    }

    public void selectOnly(UMLObject obj) { // 清除舊選取並只選取指定物件
        clearSelection();
        if (obj != null) {
            selectedObjects.add(obj);
            obj.setSelected(true);
        }
    }

    public void selectAll(Collection<? extends UMLObject> objects) { // 清除舊選取並選取指定集合
        clearSelection();
        objects.forEach(this::addToSelection);
    }

    public void addToSelection(UMLObject obj) { // 將物件加入選取集合
        if (obj == null) return;
        selectedObjects.add(obj);
        obj.setSelected(true);
    }

    public void removeFromSelection(UMLObject obj) { // 將物件自選取集合移除
        if (obj == null) return;
        selectedObjects.remove(obj);
        obj.setSelected(false);
    }

    public void clearSelection() { // 清除所有選取，並同步舊有 selected flag
        selectedObjects.forEach(o -> o.setSelected(false));
        document.getObjects().forEach(o -> o.setSelected(false));
        selectedObjects.clear();
    }

    public boolean isSelected(UMLObject obj) { // 查詢物件是否被選取
        return selectedObjects.contains(obj);
    }

    public List<UMLObject> getSelectedObjects() { // 回傳目前選取物件清單
        List<UMLObject> visibleSelected = new ArrayList<>();
        for (UMLObject obj : document.getObjects()) {
            if (selectedObjects.contains(obj) || obj.isSelected()) visibleSelected.add(obj);
        }
        return visibleSelected;
    }
}
