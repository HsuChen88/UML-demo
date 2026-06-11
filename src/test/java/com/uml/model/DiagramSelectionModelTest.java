package com.uml.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DiagramSelectionModelTest {

    @Test
    void selectOnlyClearsPreviousSelectionAndSyncsObjectFlag() {
        DiagramDocument document = new DiagramDocument();
        UMLObject first = new RectObject(0, 0, 20, 20);
        UMLObject second = new OvalObject(30, 0, 20, 20);
        document.addObject(first);
        document.addObject(second);
        DiagramSelectionModel selectionModel = new DiagramSelectionModel(document);

        selectionModel.selectOnly(first);
        selectionModel.selectOnly(second);

        assertFalse(first.isSelected());
        assertTrue(second.isSelected());
        assertEquals(List.of(second), selectionModel.getSelectedObjects());
    }

    @Test
    void selectAllAndClearSelectionSyncObjectFlags() {
        DiagramDocument document = new DiagramDocument();
        UMLObject first = new RectObject(0, 0, 20, 20);
        UMLObject second = new OvalObject(30, 0, 20, 20);
        document.addObject(first);
        document.addObject(second);
        DiagramSelectionModel selectionModel = new DiagramSelectionModel(document);

        selectionModel.selectAll(List.of(first, second));

        assertTrue(first.isSelected());
        assertTrue(second.isSelected());

        selectionModel.clearSelection();

        assertFalse(first.isSelected());
        assertFalse(second.isSelected());
        assertTrue(selectionModel.getSelectedObjects().isEmpty());
    }
}
