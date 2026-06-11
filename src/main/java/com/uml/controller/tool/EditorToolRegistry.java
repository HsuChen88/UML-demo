package com.uml.controller.tool;

import com.uml.controller.EditorMode;
import com.uml.controller.ModeManager;
import com.uml.controller.strategy.CanvasMouseStrategy;
import com.uml.controller.strategy.CreateLinkStrategy;
import com.uml.controller.strategy.CreateObjectStrategy;
import com.uml.controller.strategy.SelectStrategy;
import com.uml.model.OvalObject;
import com.uml.model.RectObject;
import com.uml.model.link.AssociationLink;
import com.uml.model.link.CompositionLink;
import com.uml.model.link.GeneralizationLink;
import com.uml.util.UMLConstants;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class EditorToolRegistry { // 集中註冊所有編輯器工具，供 UI 與 Canvas 建立策略

    private final List<EditorToolDefinition> definitions; // 依工具列顯示順序排列的工具定義
    private final Map<EditorMode, EditorToolDefinition> definitionsByMode; // 模式 → 工具定義

    public EditorToolRegistry(List<EditorToolDefinition> definitions) { // 建構子：接收完整工具定義清單
        this.definitions = List.copyOf(definitions);
        this.definitionsByMode = new EnumMap<>(EditorMode.class);
        for (EditorToolDefinition definition : definitions) {
            definitionsByMode.put(definition.mode(), definition);
        }
    }

    public static EditorToolRegistry createDefault() { // 建立目前 editor 內建的六個工具
        DiagramObjectFactory rectFactory = RectObject::new;
        DiagramObjectFactory ovalFactory = OvalObject::new;
        DiagramLinkFactory associationFactory = AssociationLink::new;
        DiagramLinkFactory generalizationFactory = GeneralizationLink::new;
        DiagramLinkFactory compositionFactory = CompositionLink::new;

        List<EditorToolDefinition> defs = new ArrayList<>();
        defs.add(new EditorToolDefinition(EditorMode.SELECT, "select", new SelectIcon(),
                modeManager -> new SelectStrategy(), false, null, null));
        defs.add(new EditorToolDefinition(EditorMode.ASSOCIATION, "association", new AssociationIcon(),
                modeManager -> new CreateLinkStrategy(associationFactory), false, null, associationFactory));
        defs.add(new EditorToolDefinition(EditorMode.GENERALIZATION, "generalization", new GeneralizationIcon(),
                modeManager -> new CreateLinkStrategy(generalizationFactory), false, null, generalizationFactory));
        defs.add(new EditorToolDefinition(EditorMode.COMPOSITION, "composition", new CompositionIcon(),
                modeManager -> new CreateLinkStrategy(compositionFactory), false, null, compositionFactory));
        defs.add(new EditorToolDefinition(EditorMode.RECT, "rect", new RectIcon(),
                modeManager -> new CreateObjectStrategy(rectFactory, modeManager), true, rectFactory, null));
        defs.add(new EditorToolDefinition(EditorMode.OVAL, "oval", new OvalIcon(),
                modeManager -> new CreateObjectStrategy(ovalFactory, modeManager), true, ovalFactory, null));
        return new EditorToolRegistry(defs);
    }

    public List<EditorToolDefinition> getDefinitions() { // 回傳所有工具定義
        return Collections.unmodifiableList(definitions);
    }

    public EditorToolDefinition getDefinition(EditorMode mode) { // 依模式取得工具定義
        return definitionsByMode.get(mode);
    }

    public Map<EditorMode, CanvasMouseStrategy> createStrategyMap(ModeManager modeManager) { // 建立模式到策略的對應表
        Map<EditorMode, CanvasMouseStrategy> strategies = new EnumMap<>(EditorMode.class);
        for (EditorToolDefinition definition : definitions) {
            strategies.put(definition.mode(), definition.createStrategy(modeManager));
        }
        return strategies;
    }

    private static abstract class BaseIcon implements Icon { // 所有工具圖示的抽象基底類別
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
