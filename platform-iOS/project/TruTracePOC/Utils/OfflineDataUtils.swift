//
//  OfflineDataUtils.swift
//  TruTracePOC
//
//  Created by Piotr Paweska on 2020-02-07.
//  Copyright © 2020 Piotr Paweska. All rights reserved.
//

import SwiftUI

class OfflineDataUtils {
    
    //
    //
    // Number of oflfine documents in storage
    static func getOfflineDocCount() -> Int {
        let keys = UserDefaults.standard.dictionaryRepresentation().keys.filter{
            $0.contains(DocumentDTO.DOC_SERIALIZATION_PREFIX)
        }
        
        // number of ofline documents
        return keys.count
    }
    
    //
    //
    // Number of oflfine documents in storage
    static func getOfflineDocuments() -> [DocumentDTO] {
        print("<getOfflineDocuments> called...")
        let defaults = UserDefaults.standard
        var offlineDocs : [DocumentDTO] = [DocumentDTO]()
        let keys = UserDefaults.standard.dictionaryRepresentation().keys.filter{
            $0.contains(DocumentDTO.DOC_SERIALIZATION_PREFIX)
        }
        
        //
        //
        for i in 0 ..< keys.count {
            if let savedDoc = defaults.object(forKey: keys[i]) as? Data {
                let decoder = JSONDecoder()
                if let loadedDoc = try? decoder.decode(DocumentDTO.self, from: savedDoc) {
                    offlineDocs.append(loadedDoc)
                    print("<getOfflineDocuments> \(loadedDoc.syncID)")
                }
            }
        }
        
        // number of ofline documents
        print("<getOfflineDocuments> done...")
        return offlineDocs
    }
    
    //
    //
    // Number of oflfine documents in storage
    static func clearOfflineDocuments() {
        print("<clearOfflineDocuments> called...")
        let defaults = UserDefaults.standard
        let keys = UserDefaults.standard.dictionaryRepresentation().keys.filter{
            $0.contains(DocumentDTO.DOC_SERIALIZATION_PREFIX)
        }
        
        //
        //
        for i in 0 ..< keys.count {
            defaults.removeObject(forKey: keys[i])
        }
        
        // number of ofline documents
        print("<clearOfflineDocuments> done...")
    }
    
}
