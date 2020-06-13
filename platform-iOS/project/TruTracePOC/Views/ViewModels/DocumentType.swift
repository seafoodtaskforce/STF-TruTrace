//
//  DocumentType.swift
//  TruTracePOC
//
//  Created by Republic Systems on 2019-12-02.
//  Copyright © 2019 Republic Systems. All rights reserved.
//

import Foundation

struct DocumentType : Identifiable {

    let id = UUID()
    let hexColorCode : String
    let documentDesignation: String
    let name: String
    let value: String
}

extension DocumentType {

    static func allDocTypes() -> [DocumentType] {
        return [
            DocumentType(hexColorCode : "#edcbc5", documentDesignation : "Passthrough", name: "port_info_of_conveyance", value : "Doc Type 1"),
            DocumentType(hexColorCode : "#a31aff", documentDesignation : "Passthrough", name: "port_info_of_conveyance", value : "Doc Type 2"),
            DocumentType(hexColorCode : "#ffff00", documentDesignation : "Passthrough", name: "port_info_of_conveyance", value : "Doc Type 3"),
            DocumentType(hexColorCode : "#808000", documentDesignation : "Passthrough", name: "port_info_of_conveyance", value : "Doc Type 4"),
            DocumentType(hexColorCode : "#808000", documentDesignation : "Passthrough", name: "port_info_of_conveyance", value : "Doc Type 5"),
            DocumentType(hexColorCode : "#ffff00", documentDesignation : "Profile", name: "port_info_of_conveyance", value : "Doc Type 6"),
            DocumentType(hexColorCode : "#ffff00", documentDesignation : "Profile", name: "port_info_of_conveyance", value : "Doc Type 7")
        ]
    }
}
