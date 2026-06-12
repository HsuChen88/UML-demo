package com.uml.model;

import com.uml.model.object.OvalObject;
import com.uml.model.object.RectObject;
import com.uml.model.object.UMLObject;
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

        assertFalse(selectionModel.isSelected(first));
        assertTrue(selectionModel.isSelected(second));
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

        assertTrue(selectionModel.isSelected(first));
        assertTrue(selectionModel.isSelected(second));

        selectionModel.clearSelection();

        assertFalse(selectionModel.isSelected(first));
        assertFalse(selectionModel.isSelected(second));
        assertTrue(selectionModel.getSelectedObjects().isEmpty());
    }
}
