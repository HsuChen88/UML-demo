package com.uml.controller.tool;

import com.uml.model.PortReference;
import com.uml.model.link.LinkObject;

@FunctionalInterface
public interface DiagramLinkFactory { // 建立 LinkObject 的 factory interface

    LinkObject create(PortReference source, PortReference target); // 依起點與終點 port 建立連線
}
