classDiagram
direction BT
class AssociationLink {
  # drawArrowHead(Graphics2D, Point, Point) void
}
class BasicObject {
  + setBounds(int, int, int, int) void
  + getNearestPortIndex(Point) int
  + move(int, int) void
  # computePorts() List~Point~
  + getResizeAnchor(int) Point
  + getPort(int) Point
  # drawPorts(Graphics2D) void
  + getResizeConstraint(int) ResizeConstraint
  + contains(int, int) boolean
  # drawShape(Graphics2D) void
  # drawLabel(Graphics2D) void
  + moveTo(int, int) void
  + draw(Graphics2D) void
   int y
   List~Point~ ports
   int height
   Rectangle bounds
   int width
   int x
}
class ButtonPanel {
  + onModeChanged(EditorMode, EditorMode) void
  - highlightButton(EditorMode) void
}
class CanvasMouseStrategy {
<<Interface>>
  + onPressed(MouseEvent, CanvasPanel) void
  + onMoved(MouseEvent, CanvasPanel) void
  + onDragged(MouseEvent, CanvasPanel) void
  + onClicked(MouseEvent, CanvasPanel) void
  + onReleased(MouseEvent, CanvasPanel) void
}
class CanvasPanel {
  + rawRemoveObject(UMLObject) void
  + rawRemoveLink(LinkObject) void
  + removeObject(UMLObject) void
  + execute(Command) void
  + rawAddObject(UMLObject) void
  + updateHoverAt(int, int) void
  + redo() void
  + canUndo() boolean
  # paintComponent(Graphics) void
  + clearHover() void
  + canRedo() boolean
  + rawAddLink(LinkObject) void
  + findObjectAt(int, int) UMLObject
  + pushHistory(Command) void
  + ungroup() void
  + addObject(UMLObject) void
  + bringToFront(UMLObject) void
  + addLink(LinkObject) void
  + group() void
  + simulateRelease(int, int) void
  + findBasicObjectNearPort(int, int) BasicObject
  + clearSelection() void
  + undo() void
   Rectangle rubberBand
   List~UMLObject~ selectedObjects
   List~UMLObject~ objects
   UMLObject hoveredObject
   Point tempLinkEnd
}
class Command {
<<Interface>>
  + redo() void
  + undo() void
}
class CommandHistory {
  + redo() void
  + push(Command) void
  + canRedo() boolean
  + canUndo() boolean
  + undo() void
}
class CompositeObject {
  + move(int, int) void
  + draw(Graphics2D) void
  + moveTo(int, int) void
  + contains(int, int) boolean
   Rectangle bounds
   List~UMLObject~ directChildren
}
class CompositionLink {
  # drawArrowHead(Graphics2D, Point, Point) void
}
class CreateLinkCommand {
  + undo() void
  + redo() void
}
class CreateLinkStrategy {
  - buildLink(BasicObject, int, BasicObject, int) LinkObject
  + onReleased(MouseEvent, CanvasPanel) void
  + onDragged(MouseEvent, CanvasPanel) void
  + onPressed(MouseEvent, CanvasPanel) void
   int sourcePortIndex
   BasicObject sourceObject
}
class CreateObjectCommand {
  + redo() void
  + undo() void
}
class CreateObjectStrategy {
  + onReleased(MouseEvent, CanvasPanel) void
}
class EditorMode {
<<enumeration>>
  + values() EditorMode[]
  + valueOf(String) EditorMode
   boolean objectCreation
}
class GeneralizationLink {
  # drawArrowHead(Graphics2D, Point, Point) void
}
class GroupCommand {
  + redo() void
  + undo() void
}
class HitTestUtil {
  + isCompletelyInside(Rectangle, Rectangle) boolean
  + normalise(int, int, int, int) Rectangle
  + isNearPort(Point, Point) boolean
}
class LabelDialog {
   Color labelColor
   String labelName
   boolean confirmed
}
class LinkObject {
  + draw(Graphics2D) void
  # angle(Point, Point) double
  # drawArrowHead(Graphics2D, Point, Point) void
   BasicObject source
   BasicObject target
}
class Main {
  + main(String[]) void
}
class MainFrame {
  - buildMenuBar() JMenuBar
  - refreshUndoRedoState(JMenuItem, JMenuItem) void
  - openLabelDialog() void
}
class ModeChangeListener {
<<Interface>>
  + onModeChanged(EditorMode, EditorMode) void
}
class ModeManager {
  + restorePreviousMode() void
  + addListener(ModeChangeListener) void
   EditorMode mode
   EditorMode currentMode
}
class MoveObjectsCommand {
  - restoreSelection(Map~UMLObject, Point~) void
  + redo() void
  + undo() void
}
class OvalObject {
  + getResizeAnchor(int) Point
  # drawShape(Graphics2D) void
  # computePorts() List~Point~
  + getResizeConstraint(int) ResizeConstraint
  + contains(int, int) boolean
}
class RectObject {
  # drawShape(Graphics2D) void
  # computePorts() List~Point~
  + getResizeConstraint(int) ResizeConstraint
  + getResizeAnchor(int) Point
}
class ResizeObjectCommand {
  + redo() void
  + undo() void
}
class SelectStrategy {
  + onMoved(MouseEvent, CanvasPanel) void
  + onDragged(MouseEvent, CanvasPanel) void
  + onReleased(MouseEvent, CanvasPanel) void
  + onPressed(MouseEvent, CanvasPanel) void
  + onClicked(MouseEvent, CanvasPanel) void
  - applyResize(BasicObject, int, int) void
}
class SetLabelCommand {
  + undo() void
  + redo() void
}
class UMLConstants
class UMLObject {
  + draw(Graphics2D) void
  + contains(int, int) boolean
  + move(int, int) void
  + moveTo(int, int) void
   Rectangle bounds
   String labelName
   boolean hovered
   boolean selected
   Color labelColor
}
class UngroupCommand {
  + undo() void
  + redo() void
}

AssociationLink  -->  LinkObject 
BasicObject  -->  UMLObject 
ButtonPanel  ..>  ModeChangeListener 
CompositeObject  -->  UMLObject 
CompositionLink  -->  LinkObject 
CreateLinkCommand  ..>  Command 
CreateLinkStrategy  ..>  CanvasMouseStrategy 
CreateObjectCommand  ..>  Command 
CreateObjectStrategy  ..>  CanvasMouseStrategy 
GeneralizationLink  -->  LinkObject 
GroupCommand  ..>  Command 
MoveObjectsCommand  ..>  Command 
OvalObject  -->  BasicObject 
RectObject  -->  BasicObject 
ResizeObjectCommand  ..>  Command 
SelectStrategy  ..>  CanvasMouseStrategy 
SetLabelCommand  ..>  Command 
UngroupCommand  ..>  Command 
