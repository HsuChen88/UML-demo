package com.uml.controller.tool;

import com.uml.controller.mode.EditorMode;
import com.uml.controller.mode.ModeManager;
import com.uml.controller.strategy.CanvasMouseStrategy;
import com.uml.controller.strategy.CreateLinkStrategy;
import com.uml.controller.strategy.CreateObjectStrategy;
import com.uml.controller.strategy.SelectStrategy;
import com.uml.model.link.AssociationLink;
import com.uml.model.link.CompositionLink;
import com.uml.model.link.GeneralizationLink;
import com.uml.model.object.OvalObject;
import com.uml.model.object.RectObject;
import com.uml.view.toolicon.ToolIcons;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class EditorToolRegistry { // 集中註冊所有編輯器工具，供 UI 與 Canvas 建立 strategy 

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
        defs.add(new EditorToolDefinition(EditorMode.SELECT, "select", ToolIcons.select(),
                modeManager -> new SelectStrategy()));
        defs.add(new EditorToolDefinition(EditorMode.ASSOCIATION, "association", ToolIcons.association(),
                modeManager -> new CreateLinkStrategy(associationFactory)));
        defs.add(new EditorToolDefinition(EditorMode.GENERALIZATION, "generalization", ToolIcons.generalization(),
                modeManager -> new CreateLinkStrategy(generalizationFactory)));
        defs.add(new EditorToolDefinition(EditorMode.COMPOSITION, "composition", ToolIcons.composition(),
                modeManager -> new CreateLinkStrategy(compositionFactory)));
        defs.add(new EditorToolDefinition(EditorMode.RECT, "rect", ToolIcons.rect(),
                modeManager -> new CreateObjectStrategy(rectFactory, modeManager), true));
        defs.add(new EditorToolDefinition(EditorMode.OVAL, "oval", ToolIcons.oval(),
                modeManager -> new CreateObjectStrategy(ovalFactory, modeManager), true));
        return new EditorToolRegistry(defs);
    }

    public List<EditorToolDefinition> getDefinitions() { // 回傳所有工具定義
        return Collections.unmodifiableList(definitions);
    }

    public EditorToolDefinition getDefinition(EditorMode mode) { // 依模式取得工具定義
        return definitionsByMode.get(mode);
    }

    public Map<EditorMode, CanvasMouseStrategy> createStrategyMap(ModeManager modeManager) { // 建立模式到 strategy 的對應表
        Map<EditorMode, CanvasMouseStrategy> strategies = new EnumMap<>(EditorMode.class);
        for (EditorToolDefinition definition : definitions) {
            strategies.put(definition.mode(), definition.createStrategy(modeManager));
        }
        return strategies;
    }
}
