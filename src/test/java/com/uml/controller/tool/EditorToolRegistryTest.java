package com.uml.controller.tool;

import com.uml.controller.mode.EditorMode;
import com.uml.controller.mode.ModeManager;
import com.uml.controller.strategy.CanvasMouseStrategy;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class EditorToolRegistryTest {

    @Test
    void defaultRegistryCreatesAllBuiltInStrategies() {
        EditorToolRegistry registry = EditorToolRegistry.createDefault();
        Map<EditorMode, CanvasMouseStrategy> strategies = registry.createStrategyMap(new ModeManager());

        assertEquals(6, registry.getDefinitions().size());
        for (EditorMode mode : EditorMode.values()) {
            assertNotNull(registry.getDefinition(mode));
            assertNotNull(strategies.get(mode));
        }
    }

    @Test
    void registryCanBeCreatedWithAdditionalDefinitionWithoutCanvasChanges() {
        EditorToolDefinition definition = new EditorToolDefinition(EditorMode.SELECT, "custom", new ImageIcon(),
                modeManager -> new CanvasMouseStrategy() {});
        EditorToolRegistry registry = new EditorToolRegistry(List.of(definition));

        assertSame(definition, registry.getDefinition(EditorMode.SELECT));
        assertNotNull(registry.createStrategyMap(new ModeManager()).get(EditorMode.SELECT));
    }

    @Test
    void onlyObjectCreationToolsSupportCanvasDropCreation() {
        EditorToolRegistry registry = EditorToolRegistry.createDefault();

        assertTrue(registry.getDefinition(EditorMode.RECT).canvasDropCreatesObject());
        assertTrue(registry.getDefinition(EditorMode.OVAL).canvasDropCreatesObject());
        assertFalse(registry.getDefinition(EditorMode.SELECT).canvasDropCreatesObject());
        assertFalse(registry.getDefinition(EditorMode.ASSOCIATION).canvasDropCreatesObject());
        assertFalse(registry.getDefinition(EditorMode.GENERALIZATION).canvasDropCreatesObject());
        assertFalse(registry.getDefinition(EditorMode.COMPOSITION).canvasDropCreatesObject());
    }
}
