# Refactored UML Editor Architecture

本文描述目前重構後的 UML Editor 設計。重點是說明各程式部件的責任邊界、資料流、工具建立流程、畫布繪製流程、Command undo/redo，以及群組 z-order 的互動方式。

## 1. Responsibility Overview

重構後的核心方向是：`CanvasPanel` 不再是資料、互動、繪製、工具建立、命令執行的唯一中心，而是 Swing adapter。真正的 diagram 結構資料由 `DiagramDocument` 管理；選取狀態由 `DiagramSelectionModel` 管理；暫時互動狀態由 `CanvasInteractionState` 管理；工具定義由 `EditorToolRegistry` 管理；繪製由 renderer layer 管理。

```mermaid
graph TB
    subgraph UI["View / Swing Adapter"]
        MF["MainFrame"]
        BP["ButtonPanel"]
        CP["CanvasPanel"]
        LD["LabelDialog"]
    end

    subgraph TOOL["Tool System"]
        ETR["EditorToolRegistry"]
        ETD["EditorToolDefinition"]
        DOF["DiagramObjectFactory"]
        DLF["DiagramLinkFactory"]
    end

    subgraph STRATEGY["Controller Strategies"]
        CMS["CanvasMouseStrategy"]
        SS["SelectStrategy"]
        COS["CreateObjectStrategy"]
        CLS["CreateLinkStrategy"]
        MM["ModeManager"]
    end

    subgraph MODEL["Diagram Model"]
        DOC["DiagramDocument"]
        SEL["DiagramSelectionModel"]
        CIS["CanvasInteractionState"]
        UO["UMLObject"]
        BO["BasicObject / PortOwner"]
        CO["CompositeObject"]
        LO["LinkObject"]
        PR["PortReference"]
    end

    subgraph RENDER["Renderer Layer"]
        CRC["CanvasRenderContext"]
        DR["DiagramRenderer"]
        UOR["UMLObjectRenderer"]
        LR["LinkRenderer"]
        COR["CanvasOverlayRenderer"]
    end

    subgraph CMD["Command Layer"]
        CH["CommandHistory"]
        C["Command"]
        CC["Create / Move / Resize / Group / Ungroup / Label Commands"]
    end

    MF --> CP
    MF --> BP
    MF --> LD
    MF --> ETR
    BP --> ETR
    CP --> ETR
    ETR --> ETD
    ETD --> DOF
    ETD --> DLF
    ETD --> CMS

    MM --> CMS
    CMS <|.. SS
    CMS <|.. COS
    CMS <|.. CLS

    CP --> DOC
    CP --> SEL
    CP --> CIS
    CP --> CH
    CP --> CRC
    CP --> DR
    CP --> COR

    DR --> UOR
    DR --> LR
    UOR --> UO
    LR --> LO
    COR --> CMS

    C <|.. CC
    CH --> C
    CC --> DOC
    CC --> SEL

    DOC --> UO
    DOC --> LO
    BO --> PR
    LO --> PR
```

## 2. Core Model Relationships

`DiagramDocument` 是 diagram 的結構資料入口，持有頂層 `UMLObject` 與 `LinkObject`，並維護 z-order。`DiagramSelectionModel` 只處理選取狀態；`CanvasInteractionState` 只處理 hover、rubber band、temporary link preview 等 UI 暫態。

```mermaid
classDiagram
    class DiagramDocument {
        -List~UMLObject~ objects
        -List~LinkObject~ links
        +getObjects() List~UMLObject~
        +getLinks() List~LinkObject~
        +addObject(UMLObject)
        +addObjectAt(int, UMLObject)
        +removeObject(UMLObject) boolean
        +bringToFront(UMLObject)
        +findObjectAt(int, int) UMLObject
        +findPortOwnerNearPort(int, int) PortOwner
        +findPortReferenceNearPoint(Point) PortReference
    }

    class DiagramSelectionModel {
        -DiagramDocument document
        -Set~UMLObject~ selectedObjects
        +selectOnly(UMLObject)
        +selectAll(Collection)
        +addToSelection(UMLObject)
        +clearSelection()
        +isSelected(UMLObject) boolean
        +getSelectedObjects() List~UMLObject~
    }

    class CanvasInteractionState {
        -UMLObject hoveredObject
        -Rectangle rubberBand
        -PortReference temporaryLinkSource
        -Point temporaryLinkEnd
        +setHoveredObject(UMLObject)
        +setRubberBand(Rectangle)
        +setTemporaryLink(PortReference, Point)
        +clearTemporaryLink()
    }

    class UMLObject {
        <<abstract>>
        +draw(Graphics2D)
        +contains(int, int) boolean
        +getBounds() Rectangle
        +move(int, int)
        +moveTo(int, int)
    }

    class PortOwner {
        <<interface>>
        +getPort(int) Point
        +getNearestPortIndex(Point) int
        +getBounds() Rectangle
    }

    class BasicObject {
        <<abstract>>
        +getPort(int) Point
        +getNearestPortIndex(Point) int
        +setBounds(int, int, int, int)
    }

    class CompositeObject {
        +getDirectChildren() List~UMLObject~
    }

    class PortReference {
        <<record>>
        +PortOwner owner
        +int portIndex
        +getPoint() Point
    }

    class LinkObject {
        <<abstract>>
        -PortReference source
        -PortReference target
        +draw(Graphics2D)
        +getSourceReference() PortReference
        +getTargetReference() PortReference
    }

    DiagramDocument --> UMLObject
    DiagramDocument --> LinkObject
    DiagramSelectionModel --> DiagramDocument
    CanvasInteractionState --> PortReference
    UMLObject <|-- BasicObject
    UMLObject <|-- CompositeObject
    PortOwner <|.. BasicObject
    LinkObject --> PortReference
    PortReference --> PortOwner
```

## 3. Tool Registration And Strategy Creation

工具定義集中在 `EditorToolRegistry`。`ButtonPanel` 不再維護自己的 `Object[][] defs`；`CanvasPanel` 也不再手寫 strategy map。兩者都由同一份 registry 建立，因此新增工具時主要新增一個 `EditorToolDefinition`。

```mermaid
sequenceDiagram
    participant MainFrame
    participant Registry as EditorToolRegistry
    participant ButtonPanel
    participant CanvasPanel
    participant ModeManager
    participant Strategy as CanvasMouseStrategy

    MainFrame->>Registry: createDefault()
    MainFrame->>CanvasPanel: new(modeManager, registry)
    CanvasPanel->>Registry: createStrategyMap(modeManager)
    Registry-->>CanvasPanel: Map<EditorMode, CanvasMouseStrategy>
    MainFrame->>ButtonPanel: new(modeManager, canvas, registry)
    ButtonPanel->>Registry: getDefinitions()
    Registry-->>ButtonPanel: tool definitions
    ButtonPanel->>ButtonPanel: create labels and icon buttons

    ButtonPanel->>ModeManager: setMode(mode)
    ModeManager-->>CanvasPanel: onModeChanged(newMode, prevMode)
    CanvasPanel->>CanvasPanel: currentStrategy = strategyMap.get(newMode)
    CanvasPanel->>Strategy: route mouse events
```

## 4. Painting Pipeline

`CanvasPanel.paintComponent` 只建立 `CanvasRenderContext`，然後委派給 `DiagramRenderer` 與 `CanvasOverlayRenderer`。一般 diagram 內容與互動 overlay 分開處理。

```mermaid
sequenceDiagram
    participant Swing
    participant CanvasPanel
    participant Context as CanvasRenderContext
    participant DiagramRenderer
    participant ObjectRenderer as UMLObjectRenderer
    participant LinkRenderer
    participant OverlayRenderer as CanvasOverlayRenderer
    participant Strategy as current CanvasMouseStrategy

    Swing->>CanvasPanel: paintComponent(Graphics)
    CanvasPanel->>Context: new(document, selectionModel, interactionState)
    CanvasPanel->>DiagramRenderer: render(g2d, context)
    DiagramRenderer->>ObjectRenderer: render each UMLObject
    ObjectRenderer->>ObjectRenderer: delegate object.draw(g2d)
    DiagramRenderer->>LinkRenderer: render each LinkObject
    LinkRenderer->>LinkRenderer: delegate link.draw(g2d)
    CanvasPanel->>OverlayRenderer: render(g2d, context, currentStrategy)
    OverlayRenderer->>Strategy: paintOverlay(g2d, context)
```

## 5. Create Object Flow

建立物件時，`CreateObjectStrategy` 不再依賴 `EditorMode.RECT/OVAL` 分支，而是呼叫注入的 `DiagramObjectFactory`。建立後由 `CreateObjectCommand` 寫入 `DiagramDocument` 並更新 `DiagramSelectionModel`。

```mermaid
sequenceDiagram
    participant User
    participant CanvasPanel
    participant Strategy as CreateObjectStrategy
    participant Factory as DiagramObjectFactory
    participant Command as CreateObjectCommand
    participant Document as DiagramDocument
    participant Selection as DiagramSelectionModel
    participant History as CommandHistory

    User->>CanvasPanel: mouseReleased
    CanvasPanel->>Strategy: onReleased(event, canvas)
    Strategy->>Factory: create(x, y, width, height)
    Factory-->>Strategy: UMLObject
    Strategy->>CanvasPanel: execute(CreateObjectCommand)
    CanvasPanel->>Command: redo()
    Command->>Document: addObject(created)
    Command->>Selection: selectOnly(created)
    CanvasPanel->>History: push(command)
    CanvasPanel->>CanvasPanel: repaint()
```

## 6. Create Link Flow

建立連線時，`CreateLinkStrategy` 透過 `DiagramDocument.findPortReferenceNearPoint` 找出起點與終點。`LinkObject` 端點現在是 `PortReference`，因此未來任何實作 `PortOwner` 的物件都能成為連線端點。

```mermaid
sequenceDiagram
    participant User
    participant CanvasPanel
    participant Strategy as CreateLinkStrategy
    participant State as CanvasInteractionState
    participant Document as DiagramDocument
    participant Factory as DiagramLinkFactory
    participant Command as CreateLinkCommand

    User->>CanvasPanel: mousePressed on source port
    CanvasPanel->>Strategy: onPressed(event, canvas)
    Strategy->>Document: findPortReferenceNearPoint(point)
    Document-->>Strategy: source PortReference
    Strategy->>State: setTemporaryLink(source, point)

    User->>CanvasPanel: mouseDragged
    CanvasPanel->>Strategy: onDragged(event, canvas)
    Strategy->>State: setTemporaryLink(source, currentPoint)
    CanvasPanel->>CanvasPanel: repaint()

    User->>CanvasPanel: mouseReleased on target port
    CanvasPanel->>Strategy: onReleased(event, canvas)
    Strategy->>State: clearTemporaryLink()
    Strategy->>Document: findPortReferenceNearPoint(point)
    Document-->>Strategy: target PortReference
    Strategy->>Factory: create(source, target)
    Factory-->>Strategy: LinkObject
    Strategy->>CanvasPanel: execute(CreateLinkCommand)
    Command->>Document: addLink(link)
```

## 7. Selection, Move, Resize Flow

`SelectStrategy` 是選取模式下的互動狀態機。它把真正的 selection 操作交給 `DiagramSelectionModel`，把物件查詢交給 `DiagramDocument`，把 undo/redo 記錄交給 Command。

```mermaid
stateDiagram-v2
    [*] --> IDLE
    IDLE --> RESIZING: mousePressed on selected BasicObject port
    IDLE --> DRAGGING_OBJECT: mousePressed on object
    IDLE --> RUBBER_BANDING: mousePressed on empty area

    RESIZING --> RESIZING: mouseDragged / BasicObject.setBounds()
    RESIZING --> IDLE: mouseReleased / push ResizeObjectCommand

    DRAGGING_OBJECT --> DRAGGING_OBJECT: mouseDragged / move selected objects
    DRAGGING_OBJECT --> IDLE: mouseReleased / push MoveObjectsCommand

    RUBBER_BANDING --> RUBBER_BANDING: mouseDragged / CanvasInteractionState.rubberBand
    RUBBER_BANDING --> IDLE: mouseReleased / DiagramSelectionModel.addToSelection()
```

```mermaid
flowchart TD
    A["mousePressed"] --> B{"按到 selected BasicObject port?"}
    B -- yes --> C["RESIZING: snapshot bounds"]
    B -- no --> D{"按到 object?"}
    D -- yes --> E["selectOnly if needed + snapshot selected positions"]
    D -- no --> F["clearSelection + start rubber band"]
    C --> G["mouseDragged: applyResize"]
    E --> H["mouseDragged: move selected objects"]
    F --> I["mouseDragged: update rubberBand"]
    G --> J["mouseReleased: push ResizeObjectCommand"]
    H --> K["mouseReleased: push MoveObjectsCommand"]
    I --> L["mouseReleased: select enclosed objects"]
```

## 8. Command And Undo/Redo Ownership

Command 不再直接依賴 `CanvasPanel`。Command 只修改 `DiagramDocument` 與必要的 `DiagramSelectionModel`；`CanvasPanel` 負責在 command 完成後 repaint。

```mermaid
sequenceDiagram
    participant UI as CanvasPanel / MainFrame
    participant Command
    participant History as CommandHistory
    participant Document as DiagramDocument
    participant Selection as DiagramSelectionModel

    UI->>Command: redo()
    Command->>Document: mutate structural state
    Command->>Selection: restore/update selection if needed
    UI->>History: push(command)
    UI->>UI: repaint()

    UI->>History: undo()
    History->>Command: undo()
    Command->>Document: restore structural state
    Command->>Selection: restore/update selection if needed
    UI->>UI: repaint()

    UI->>History: redo()
    History->>Command: redo()
    Command->>Document: reapply structural state
    Command->>Selection: restore/update selection if needed
    UI->>UI: repaint()
```

## 9. Group / Ungroup Z-Order

群組操作現在會記錄 child 原始 index，並把 composite 放到「被群組物件中最高層的位置」。undo 時 children 回到原始 z-order；redo 時 composite 回到記錄位置。解群組則記錄 composite index，children 會從 composite 位置開始展開。

```mermaid
flowchart LR
    subgraph BeforeGroup["Before group"]
        A1["0: A"]
        B1["1: B selected"]
        C1["2: C"]
        D1["3: D selected"]
    end

    subgraph AfterGroup["After group redo"]
        A2["0: A"]
        C2["1: C"]
        G2["2: Composite(B,D)"]
    end

    subgraph AfterUndo["After group undo"]
        A3["0: A"]
        B3["1: B"]
        C3["2: C"]
        D3["3: D"]
    end

    BeforeGroup --> AfterGroup
    AfterGroup --> AfterUndo
```

## 10. Extension Points

```mermaid
mindmap
  root((Extension Points))
    New Shape
      Implement UMLObject or BasicObject
      Implement PortOwner if linkable
      Add EditorToolDefinition
      Provide DiagramObjectFactory
    New Link Type
      Keep LinkObject subclass
      Add DiagramLinkFactory
      Add EditorToolDefinition
    New Interaction Mode
      Implement CanvasMouseStrategy
      Optionally override paintOverlay
      Register in EditorToolRegistry
    Save Load
      Serialize DiagramDocument
      Exclude CanvasInteractionState
      Exclude transient selection if desired
    Renderer Replacement
      Replace DiagramRenderer components
      Keep DiagramDocument unchanged
```

## 11. Important Tradeoffs

- `UMLObject.draw(Graphics2D)` and `LinkObject.draw(Graphics2D)` still exist. The renderer layer currently delegates to them to keep the refactor behavior-compatible.
- `UMLObject.selected` and `UMLObject.hovered` still exist as compatibility flags. New flows go through `DiagramSelectionModel` and `CanvasInteractionState`.
- Link subclasses remain unchanged as requested. The current extensibility improvement is at endpoint creation (`PortOwner` / `PortReference`) and factory creation (`DiagramLinkFactory`), not at arrowhead composition.
- `CanvasPanel.rawAddObject` and related raw APIs remain as compatibility adapters, but new Command code no longer depends on them.
