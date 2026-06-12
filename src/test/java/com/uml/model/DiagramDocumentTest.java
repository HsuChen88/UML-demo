package com.uml.model;

import com.uml.model.object.BasicObject;
import com.uml.model.object.OvalObject;
import com.uml.model.object.RectObject;
import com.uml.model.object.UMLObject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DiagramDocumentTest {

    @Test
    void addAtAndBringToFrontMaintainZOrder() {
        DiagramDocument document = new DiagramDocument();
        UMLObject a = new RectObject(0, 0, 20, 20);
        UMLObject b = new OvalObject(30, 0, 20, 20);
        UMLObject c = new RectObject(60, 0, 20, 20);

        document.addObject(a);
        document.addObject(c);
        document.addObjectAt(1, b);

        assertEquals(a, document.getObjects().get(0));
        assertEquals(b, document.getObjects().get(1));
        assertEquals(c, document.getObjects().get(2));

        document.bringToFront(a);

        assertEquals(b, document.getObjects().get(0));
        assertEquals(c, document.getObjects().get(1));
        assertEquals(a, document.getObjects().get(2));
    }

    @Test
    void hitTestReturnsTopmostObject() {
        DiagramDocument document = new DiagramDocument();
        UMLObject bottom = new RectObject(0, 0, 100, 100);
        UMLObject top = new RectObject(10, 10, 100, 100);

        document.addObject(bottom);
        document.addObject(top);

        assertSame(top, document.findObjectAt(20, 20));
    }

    @Test
    void findsPortOwnerAndPortReferenceNearPoint() {
        DiagramDocument document = new DiagramDocument();
        BasicObject rect = new RectObject(10, 20, 100, 80);

        document.addObject(rect);

        PortReference reference = document.findPortReferenceNearPoint(rect.getPort(0));

        assertNotNull(reference);
        assertSame(rect, reference.owner());
        assertEquals(0, reference.portIndex());
    }
}
