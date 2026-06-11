package com.uml.command;

import com.uml.model.CompositeObject;
import com.uml.model.DiagramDocument;
import com.uml.model.DiagramSelectionModel;
import com.uml.model.RectObject;
import com.uml.model.UMLObject;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GroupUngroupCommandTest {

    @Test
    void groupUndoRedoRestoresOriginalZOrder() {
        DiagramDocument document = new DiagramDocument();
        DiagramSelectionModel selectionModel = new DiagramSelectionModel(document);
        UMLObject a = new RectObject(0, 0, 20, 20);
        UMLObject b = new RectObject(30, 0, 20, 20);
        UMLObject c = new RectObject(60, 0, 20, 20);
        UMLObject d = new RectObject(90, 0, 20, 20);
        document.addObject(a);
        document.addObject(b);
        document.addObject(c);
        document.addObject(d);
        List<UMLObject> children = List.of(b, d);
        CompositeObject composite = new CompositeObject(children);
        Command command = new GroupCommand(document, selectionModel, children, composite, List.of(1, 3), 3);

        command.redo();

        assertEquals(List.of(a, c, composite), document.getObjects());

        command.undo();

        assertEquals(List.of(a, b, c, d), document.getObjects());

        command.redo();

        assertEquals(List.of(a, c, composite), document.getObjects());
    }

    @Test
    void ungroupUndoRedoRestoresCompositePositionAndChildOrder() {
        DiagramDocument document = new DiagramDocument();
        DiagramSelectionModel selectionModel = new DiagramSelectionModel(document);
        UMLObject a = new RectObject(0, 0, 20, 20);
        UMLObject b = new RectObject(30, 0, 20, 20);
        UMLObject c = new RectObject(60, 0, 20, 20);
        UMLObject d = new RectObject(90, 0, 20, 20);
        CompositeObject composite = new CompositeObject(List.of(b, c));
        document.addObject(a);
        document.addObject(composite);
        document.addObject(d);
        Command command = new UngroupCommand(document, selectionModel, composite, List.of(b, c), 1);

        command.redo();

        assertEquals(List.of(a, b, c, d), document.getObjects());

        command.undo();

        assertEquals(List.of(a, composite, d), document.getObjects());
    }
}
