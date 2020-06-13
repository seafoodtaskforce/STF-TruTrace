//
//  DocumentNotes.swift
//  TruTracePOC
//
//  Created by Republic Systems on 2019-11-25.
//  Copyright © 2019 Republic Systems. All rights reserved.
//

import SwiftUI

struct DocumentNotes: View {
    var docNote : DocumentNote
    
    var body: some View {
        Text("Hello,")
    }
}

struct DocumentNotes_Previews: PreviewProvider {
    static var previews: some View {
        DocumentNotes(docNote : DocumentNote())
    }
}
