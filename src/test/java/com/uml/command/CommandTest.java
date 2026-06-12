package com.uml.command;

import com.uml.model.DiagramDocument;
import com.uml.model.DiagramSelectionModel;
import com.uml.model.PortReference;
import com.uml.model.link.AssociationLink;
import com.uml.model.link.LinkObject;
import com.uml.model.object.BasicObject;
import com.uml.model.object.OvalObject;
import com.uml.model.object.RectObject;
import com.uml.model.object.UMLObject;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class CommandTest {

    @Test
    void createObjectCommandUndoRedoUpdatesDocumentAndSelection() {
        DiagramDocument document = new DiagramDocument();
        DiagramSelectionModel selectionModel = new DiagramSelectionModel(document);
        UMLObject created = new RectObject(0, 0, 20, 20);
        Command command = new CreateObjectCommand(document, selectionModel, created);

        command.redo();

        assertEquals(1, document.getObjects().size());
        assertTrue(selectionModel.isSelected(created));

        command.undo();

        assertTrue(document.getObjects().isEmpty());
        assertFalse(selectionModel.isSelected(created));
    }

    @Test
    void createLinkCommandUndoRedoUpdatesDocument() {
        DiagramDocument document = new DiagramDocument();
        BasicObject source = new RectObject(0, 0, 20, 20);
        BasicObject target = new OvalObject(40, 0, 20, 20);
        LinkObject link = new AssociationLink(new PortReference(source, 0), new PortReference(target, 0));
        Command command = new CreateLinkCommand(document, link);

        command.redo();
        assertEquals(1, document.getLinks().size());

        command.undo();
        assertTrue(document.getLinks().isEmpty());
    }

    @Test
    void moveObjectsCommandUndoRedoRestoresPositionsAndSelection() {
        DiagramDocument document = new DiagramDocument();
        DiagramSelectionModel selectionModel = new DiagramSelectionModel(document);
        UMLObject first = new RectObject(0, 0, 20, 20);
        UMLObject second = new RectObject(30, 0, 20, 20);
        document.addObject(first);
        document.addObject(second);
        Map<UMLObject, Point> before = new LinkedHashMap<>();
        before.put(first, new Point(0, 0));
        before.put(second, new Point(30, 0));
        Map<UMLObject, Point> after = new LinkedHashMap<>();
        after.put(first, new Point(10, 5));
        after.put(second, new Point(40, 5));
        Command command = new MoveObjectsCommand(document, selectionModel, before, after);

        command.redo();
        assertEquals(new Rectangle(10, 5, 20, 20), first.getBounds());
        assertEquals(new Rectangle(40, 5, 20, 20), second.getBounds());
        assertEquals(2, selectionModel.getSelectedObjects().size());

        command.undo();
        assertEquals(new Rectangle(0, 0, 20, 20), first.getBounds());
        assertEquals(new Rectangle(30, 0, 20, 20), second.getBounds());
    }

    @Test
    void resizeObjectCommandUndoRedoRestoresBoundsAndSelection() {
        DiagramDocument document = new DiagramDocument();
        DiagramSelectionModel selectionModel = new DiagramSelectionModel(document);
        BasicObject target = new RectObject(0, 0, 20, 20);
        document.addObject(target);
        Command command = new ResizeObjectCommand(document, selectionModel, target,
                0, 0, 20, 20,
                10, 10, 40, 50);

        command.redo();
        assertEquals(new Rectangle(10, 10, 40, 50), target.getBounds());
        assertTrue(selectionModel.isSelected(target));

        command.undo();
        assertEquals(new Rectangle(0, 0, 20, 20), target.getBounds());
        assertTrue(selectionModel.isSelected(target));
    }

    @Test
    void setLabelCommandUndoRedoRestoresNameAndColor() {
        DiagramDocument document = new DiagramDocument();
        BasicObject target = new RectObject(0, 0, 20, 20);
        Command command = new SetLabelCommand(document, target,
                "before", Color.RED,
                "after", Color.BLUE);

        command.redo();
        assertEquals("after", target.getLabelName());
        assertEquals(Color.BLUE, target.getLabelColor());

        command.undo();
        assertEquals("before", target.getLabelName());
        assertEquals(Color.RED, target.getLabelColor());
    }
}
