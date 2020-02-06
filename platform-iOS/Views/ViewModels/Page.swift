//
//  Page.swift
//  TruTracePOC
//
//  Created by Republic Systems on 2019-12-03.
//  Copyright © 2019 Republic Systems. All rights reserved.
//

import Foundation

struct Page {
    
    let name: String
    let imageURL: String
    let index: Int

}

extension Page {
    
    static func all() -> [Page] {
        return [
            Page(name: "Page 1", imageURL: "sal", index: 1),
            Page(name: "Page 2", imageURL: "tom", index: 2),
            Page(name: "Page 3", imageURL: "tam", index: 3)
        ]
    }
}
