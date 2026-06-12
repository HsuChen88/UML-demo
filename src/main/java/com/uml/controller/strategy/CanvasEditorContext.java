package com.uml.controller.strategy;

import com.uml.command.Command;
import com.uml.model.DiagramDocument;
import com.uml.model.DiagramSelectionModel;
import com.uml.view.CanvasInteractionState;

public interface CanvasEditorContext { // Canvas strategy 可使用的最小編輯器上下文，避免依賴具體 CanvasPanel

    DiagramDocument getDocument(); // 取得 diagram document

    DiagramSelectionModel getSelectionModel(); // 取得 diagram selection model

    CanvasInteractionState getInteractionState(); // 取得畫布互動暫態

    void execute(Command cmd); // 執行並記錄命令

    void pushHistory(Command cmd); // 記錄已套用的命令

    void repaintCanvas(); // 請求畫布重繪
}
