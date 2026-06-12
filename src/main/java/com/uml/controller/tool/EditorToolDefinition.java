package com.uml.controller.tool;

import com.uml.controller.mode.EditorMode;
import com.uml.controller.mode.ModeManager;
import com.uml.controller.strategy.CanvasMouseStrategy;

import javax.swing.*;
import java.util.function.Function;

public record EditorToolDefinition(EditorMode mode,
                                   String label,
                                   Icon icon,
                                   Function<ModeManager, CanvasMouseStrategy> strategyFactory,
                                   boolean canvasDropCreatesObject) { // 一個編輯器工具的定義：UI 顯示資訊 + strategy 建立函式

    public EditorToolDefinition(EditorMode mode,
                                String label,
                                Icon icon,
                                Function<ModeManager, CanvasMouseStrategy> strategyFactory) { // 一般工具預設不支援從工具列拖放建立物件
        this(mode, label, icon, strategyFactory, false);
    }

    public CanvasMouseStrategy createStrategy(ModeManager modeManager) { // 建立此工具對應的滑鼠 strategy 
        return strategyFactory.apply(modeManager);
    }
}
