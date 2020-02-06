//
//  ImageUtils.swift
//  TruTracePOC
//
//  Created by Republic Systems on 2020-01-07.
//  Copyright © 2020 Republic Systems. All rights reserved.
//

import Foundation
import SwiftUI

struct ImageViewWidget : View {
    @ObservedObject var imageLoader: ImageLoader
    
    init(imageUrl : String) {
        imageLoader = ImageLoader(imageUrl : imageUrl)
    }
    
    var body: some View {
        Image(uiImage: (imageLoader.data.count == 0) ? UIImage(systemName: "person.circle")! : UIImage(data: imageLoader.data)!).resizable()
            .aspectRatio(contentMode: .fill)
            .frame(width: 100, height: 100)
            .clipShape(Circle())
            .padding(EdgeInsets(top: 10, leading: 20, bottom: 10, trailing: 20))
    }
}

struct ImageViewRecipientProfileWidget : View {
    @ObservedObject var imageLoader: ImageLoader
    
    init(imageUrl : String) {
        imageLoader = ImageLoader(imageUrl : imageUrl)
    }
    
    var body: some View {
        Image(uiImage: (imageLoader.data.count == 0) ? UIImage(systemName: "person.circle")! : UIImage(data: imageLoader.data)!).resizable()
            .aspectRatio(contentMode: .fill)
            .frame(width: 48, height : 48)
            .clipShape(Circle())
            .overlay(Circle().stroke(Color.gray, lineWidth: 2))
    }
}

struct ImageViewPageThumbnailWidget : View {
    @ObservedObject var imageLoader: ImageLoader
    
    init(imageUrl : String) {
        imageLoader = ImageLoader(imageUrl : imageUrl)
    }
    
    var body: some View {
        Image(uiImage: (imageLoader.data.count == 0) ? UIImage(systemName: "doc.richtext")! : UIImage(data: imageLoader.data)!).resizable()
        .frame(width:64.0, height: 64.0)
    }
}

struct ImageViewPageLocalThumbnailWidget : View {
    var image : UIImage
    
    init(image : UIImage) {
        self.image = image
    }
    
    var body: some View {
        Image(uiImage: image).resizable()
        .frame(width:64.0, height: 64.0)
    }
}

struct ImageViewPageLocalZoomableWidget : View {
    @State var scale: CGFloat = 1.0
    var image : UIImage
    
    init(image : UIImage) {
        self.image = image
    }
    
    var body: some View {
        Image(uiImage: image).resizable()
        .scaleEffect(scale)
        .aspectRatio(contentMode: .fit)
        //.frame(width: 100, height: 100)
        .gesture(MagnificationGesture()
            .onChanged { value in
                self.scale = value.magnitude
            }
        )
    }
}

struct ImageViewPageZoomableWidget : View {
    @ObservedObject var imageLoader: ImageLoader
    @State var scale: CGFloat = 1.0
    
    init(imageUrl : String) {
        imageLoader = ImageLoader(imageUrl : imageUrl)
    }
    
    var body: some View {
        Image(uiImage: (imageLoader.data.count == 0) ? UIImage(systemName: "doc.richtext")! : UIImage(data: imageLoader.data)!).resizable()
        .scaleEffect(scale)
        .aspectRatio(contentMode: .fit)
        //.frame(width: 100, height: 100)
        .gesture(MagnificationGesture()
            .onChanged { value in
                self.scale = value.magnitude
            }
        )
    }
}

class ImageUtils {
    
    static func fetchProfileURL(username : String) -> String {
        let imageFetchURL = (RESTServer.REMOTE_SERVER_URL + RESTServer.REST_USER_GET_PROFILE_IMAGE)
        let imageFetchURLReplaced = imageFetchURL.replacingOccurrences(of: "{username}", with: username)
        return imageFetchURLReplaced
    }
    
    static func fetchDocumentPageURL(pageId : Int) -> String {
        let imageFetchURL = (RESTServer.REMOTE_SERVER_URL + RESTServer.REST_USER_GET_DOCUMENT_PAGE)
        let imageFetchURLReplaced = imageFetchURL.replacingOccurrences(of: "{doc_id}", with: String(pageId))
        return imageFetchURLReplaced
    }
    
    static func fetchDocumentPageThumbailURL(pageId : Int) -> String {
        let imageFetchURL = (RESTServer.REMOTE_SERVER_URL + RESTServer.REST_USER_GET_DOCUMENT_PAGE_THUMBNAIL)
        let imageFetchURLReplaced = imageFetchURL.replacingOccurrences(of: "{doc_id}", with: String(pageId))
        return imageFetchURLReplaced
    }
}

struct ImagePicker: UIViewControllerRepresentable {
    @Environment(\.presentationMode) var presentationMode
    @Binding var image: UIImage
    
    /*
     Inner Coordinator Class
     */
    class Coordinator: NSObject, UINavigationControllerDelegate, UIImagePickerControllerDelegate {
        let parent: ImagePicker

        init(_ parent: ImagePicker) {
            self.parent = parent
        }
        
        func imagePickerController(_ picker: UIImagePickerController, didFinishPickingMediaWithInfo info: [UIImagePickerController.InfoKey: Any]) {
            if let uiImage = info[.originalImage] as? UIImage {
                parent.image = uiImage
            }

            parent.presentationMode.wrappedValue.dismiss()
        }
    }

    func makeUIViewController(context: UIViewControllerRepresentableContext<ImagePicker>) -> UIImagePickerController {
        let picker = UIImagePickerController()
        picker.delegate = context.coordinator
        return picker
    }

    func updateUIViewController(_ uiViewController: UIImagePickerController, context: UIViewControllerRepresentableContext<ImagePicker>) {

    }
    
    func makeCoordinator() -> Coordinator {
        Coordinator(self)
    }
    
    func compressImage(){

    }
}

extension UIImage {
    func resized(withPercentage percentage: CGFloat) -> UIImage? {
        let canvas = CGSize(width: size.width * percentage, height: size.height * percentage)
        return UIGraphicsImageRenderer(size: canvas, format: imageRendererFormat).image {
            _ in draw(in: CGRect(origin: .zero, size: canvas))
        }
    }
    func resized(toWidth width: CGFloat) -> UIImage? {
        let canvas = CGSize(width: width, height: CGFloat(ceil(width/size.width * size.height)))
        return UIGraphicsImageRenderer(size: canvas, format: imageRendererFormat).image {
            _ in draw(in: CGRect(origin: .zero, size: canvas))
        }
    }
}

extension String {
//: ### Base64 encoding a string
    func base64Encoded() -> String? {
        if let data = self.data(using: .utf8) {
            return data.base64EncodedString()
        }
        return nil
    }

//: ### Base64 decoding a string
    func base64Decoded() -> String? {
        if let data = Data(base64Encoded: self, options: .ignoreUnknownCharacters) {
            return String(data: data, encoding: .utf8)
        }
        return nil
    }
}


