package com.uml.controller.tool;

import com.uml.controller.EditorMode;
import com.uml.controller.ModeManager;
import com.uml.controller.strategy.CanvasMouseStrategy;

import javax.swing.*;
import java.util.function.Function;

public record EditorToolDefinition(EditorMode mode,
                                   String label,
                                   Icon icon,
                                   Function<ModeManager, CanvasMouseStrategy> strategyFactory,
                                   boolean objectCreation,
                                   DiagramObjectFactory objectFactory,
                                   DiagramLinkFactory linkFactory) { // 一個編輯器工具的完整定義

    public CanvasMouseStrategy createStrategy(ModeManager modeManager) { // 建立此工具對應的滑鼠策略
        return strategyFactory.apply(modeManager);
    }
}
