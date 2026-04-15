package com.uml.model;

import java.awt.*;

import com.uml.util.UMLConstants;

public abstract class UMLObject { // 所有 UML 物件的抽象基底類別（BasicObject 與 CompositeObject 皆繼承自此）

    private int     depth    = 50;      // 渲染深度（預留欄位，目前以清單順序決定層疊）
    private boolean selected = false;   // 選取時顯示 port
    private boolean hovered  = false;   // hover 時顯示 port
    private String  labelName  = null;  // 物件的標籤文字（預設 null 表示無標籤）
    private Color   labelColor = UMLConstants.COLOR_DEFAULT_LABEL; // 物件的填色（預設淺灰）

    public abstract void      draw(Graphics2D g);       // 抽象方法：繪製物件到畫布
    public abstract boolean   contains(int x, int y);   // 抽象方法：判斷點 (x, y) 是否在物件範圍內（點擊測試）
    public abstract Rectangle getBounds();              // 抽象方法：回傳物件的包圍矩形
    public abstract void      move(int dx, int dy);     // 抽象方法：以相對位移移動物件（拖曳時使用）
    public abstract void      moveTo(int x, int y);     // 抽象方法：移動到絕對位置（Undo/Redo 時使用）

    public int     getDepth()              { return depth; }
    public void    setDepth(int depth)     { this.depth = depth; }
    public boolean isSelected()            { return selected; }
    public void    setSelected(boolean s)  { this.selected = s; }
    public boolean isHovered()             { return hovered; }
    public void    setHovered(boolean h)   { this.hovered  = h; }
    public String  getLabelName()          { return labelName; }
    public void    setLabelName(String n)  { this.labelName = n; }
    public Color   getLabelColor()         { return labelColor; }
    public void    setLabelColor(Color c)  { this.labelColor = c; }
}
