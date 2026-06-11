package com.uml.view.renderer;

import com.uml.model.DiagramDocument;
import com.uml.model.DiagramSelectionModel;
import com.uml.view.CanvasInteractionState;

public record CanvasRenderContext(DiagramDocument document,
                                  DiagramSelectionModel selectionModel,
                                  CanvasInteractionState interactionState) { // renderer 繪圖時需要的上下文資料
}
