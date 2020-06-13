//
//  ImageUtils.swift
//  TruTracePOC
//
//  Created by Republic Systems on 2020-01-02.
//  Copyright © 2020 Republic Systems. All rights reserved.
//

import SwiftUI

//
//  Load an Image from a URL
//
class ImageLoader: ObservableObject {
    @Published var data = Data()
    
    init(imageUrl: String){
        // fetch image data
        guard let url = URL(string: imageUrl) else { return }
        URLSession.shared.dataTask(with: url) { (data, response, error) in
            
            guard let data = data else { return }
            DispatchQueue.main.async {
                self.data = data
                print("<ImageLoader> \(data)")
                print("<ImageLoader> \(data.count)")
            }
            
        }.resume()
    }
}

//
//  Load a list of Documents from a URL
//
class DocumentListLoader: ObservableObject {
    @Published var data = Data()
    
    init(imageUrl: String){
        // fetch image data
        guard let url = URL(string: imageUrl) else { return }
        URLSession.shared.dataTask(with: url) { (data, _, _) in
            
            guard let data = data else { return }
            DispatchQueue.main.async {
                self.data = data
                print(data)
                print(data.count)
            }
            
        }.resume()
    }
}
