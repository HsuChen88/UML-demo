package com.uml.view.toolicon;

import com.uml.util.UMLConstants;

import javax.swing.Icon;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

public final class ToolIcons {

    private ToolIcons() {
    }

    public static Icon select() {
        return new SelectIcon();
    }

    public static Icon association() {
        return new AssociationIcon();
    }

    public static Icon generalization() {
        return new GeneralizationIcon();
    }

    public static Icon composition() {
        return new CompositionIcon();
    }

    public static Icon rect() {
        return new RectIcon();
    }

    public static Icon oval() {
        return new OvalIcon();
    }

    private abstract static class BaseIcon implements Icon { // 所有工具圖示的抽象基底類別
        @Override public int getIconWidth()  { return UMLConstants.ICON_SIZE; } // 圖示寬度
        @Override public int getIconHeight() { return UMLConstants.ICON_SIZE; } // 圖示高度

        Graphics2D prepare(Graphics g, int x, int y) { // 建立 Graphics2D 副本並平移原點
            Graphics2D g2 = (Graphics2D) g.create();
            g2.translate(x, y);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            return g2;
        }
    }

    private static class SelectIcon extends BaseIcon { // 選取工具圖示
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = prepare(g, x, y);
            g2.setColor(UMLConstants.ICON_DARK);
            int[] px = { 3,  3,  7, 10, 12,  9, 15};
            int[] py = { 2, 17, 13, 19, 18, 12, 12};
            g2.fillPolygon(px, py, px.length);
            g2.dispose();
        }
    }

    private static class AssociationIcon extends BaseIcon { // 關聯線工具圖示
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = prepare(g, x, y);
            g2.setColor(UMLConstants.ICON_DARK);
            g2.setStroke(new BasicStroke(1.8f));
            g2.drawLine(19, 12, 8, 12);
            int[] px = {8, 14, 14};
            int[] py = {12, 8, 16};
            g2.fillPolygon(px, py, px.length);
            g2.dispose();
        }
    }

    private static class GeneralizationIcon extends BaseIcon { // 繼承線工具圖示
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = prepare(g, x, y);
            g2.setColor(UMLConstants.ICON_DARK);
            g2.setStroke(new BasicStroke(1.6f));
            g2.drawLine(19, 12, 13, 12);
            int[] px = {8, 14, 14};
            int[] py = {12, 8, 16};
            g2.drawPolygon(px, py, px.length);
            g2.dispose();
        }
    }

    private static class CompositionIcon extends BaseIcon { // 組合線工具圖示
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = prepare(g, x, y);
            g2.setColor(UMLConstants.ICON_DARK);
            g2.setStroke(new BasicStroke(1.6f));
            int[] dpx = {2, 7, 12, 7};
            int[] dpy = {12, 8, 12, 16};
            g2.drawPolygon(dpx, dpy, dpx.length);
            g2.drawLine(12, 12, 22, 12);
            g2.dispose();
        }
    }

    private static class RectIcon extends BaseIcon { // 矩形工具圖示
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = prepare(g, x, y);
            g2.setColor(new Color(130, 130, 130));
            g2.fillRect(3, 5, 18, 14);
            g2.dispose();
        }
    }

    private static class OvalIcon extends BaseIcon { // 橢圓工具圖示
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = prepare(g, x, y);
            g2.setColor(new Color(150, 150, 150));
            g2.fillOval(2, 2, 20, 20);
            g2.dispose();
        }
    }
}
