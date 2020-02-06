//
//  ScannerUtils.swift
//  DocumentDetector
//
//  Created by Republic Systems on 2020-01-10.
//  Copyright © 2020 Republic Systems. All rights reserved.
//

import SwiftUI
import UIKit
import Vision
import VisionKit

final class TextRecognizer {
    let cameraScan: VNDocumentCameraScan
     
    init(cameraScan: VNDocumentCameraScan) {
        self.cameraScan = cameraScan
    }
     
    private let queue = DispatchQueue(label: "com.augmentedcode.scan", qos: .default, attributes: [], autoreleaseFrequency: .workItem)
     
    
    //
    // Grab the pages captured by the scanner
    func grabPages(withCompletionHandler completionHandler: @escaping ([String]) -> Void) {
        queue.async {
        
            print("Found \(self.cameraScan.pageCount)")
            let images = (0..<self.cameraScan.pageCount).compactMap({
                self.cameraScan.imageOfPage(at: $0).cgImage
            })
            DispatchQueue.main.async {
                completionHandler(["Hello"])
            }
        }
    }
    
    func recognizeText(withCompletionHandler completionHandler: @escaping ([String]) -> Void) {
        queue.async {
            let images = (0..<self.cameraScan.pageCount).compactMap({ self.cameraScan.imageOfPage(at: $0).cgImage })
            let imagesAndRequests = images.map({ (image: $0, request: VNRecognizeTextRequest()) })
            let textPerPage = imagesAndRequests.map { image, request -> String in
                let handler = VNImageRequestHandler(cgImage: image, options: [:])
                do {
                    try handler.perform([request])
                    guard let observations = request.results as? [VNRecognizedTextObservation] else { return "" }
                    return observations.compactMap({ $0.topCandidates(1).first?.string }).joined(separator: "\n")
                }
                catch {
                    print(error)
                    return ""
                }
            }
            DispatchQueue.main.async {
                completionHandler(textPerPage)
            }
        }
    }
}

//
//
// Main Scanner Structure
struct ScannerView: UIViewControllerRepresentable {
    @EnvironmentObject var sessionData: SessionData
    private let completionHandler: ([String]?) -> Void
    private let document: DocumentDTO
     

    
    init(completion: @escaping ([String]?) -> Void, document: DocumentDTO) {
        self.completionHandler = completion
        self.document = document
    }
    
    typealias UIViewControllerType = VNDocumentCameraViewController
     
    func makeUIViewController(context: UIViewControllerRepresentableContext<ScannerView>) -> VNDocumentCameraViewController {
        let viewController = VNDocumentCameraViewController()
        viewController.delegate = context.coordinator
        return viewController
    }
     
    func updateUIViewController(_ uiViewController: VNDocumentCameraViewController, context: UIViewControllerRepresentableContext<ScannerView>) {}
     
    func makeCoordinator() -> Coordinator {
        return Coordinator(completion: completionHandler, document: document, parent: self)
    }
     
    //
    // Inner Delegate Class for the Scanner
    final class Coordinator: NSObject, VNDocumentCameraViewControllerDelegate {
        private let completionHandler: ([String]?) -> Void
        private let document: DocumentDTO
        private let parent: ScannerView
         
        init(completion: @escaping ([String]?) -> Void, parent : ScannerView) {
            self.completionHandler = completion
            self.document = DocumentDTO()
            self.parent = parent
        }
        
        init(completion: @escaping ([String]?) -> Void, document: DocumentDTO, parent : ScannerView) {
            self.completionHandler = completion
            self.document = document
            self.parent = parent
        }
         
        //
        // Main callback controller for the pagw grab
        func documentCameraViewController(_ controller: VNDocumentCameraViewController, didFinishWith scan: VNDocumentCameraScan) {
            print("Document camera view controller did finish with ", scan)
            let recognizer = TextRecognizer(cameraScan: scan)
            recognizer.grabPages(withCompletionHandler: completionHandler)
            print("Found In Main Handler \(scan.pageCount)")

            for i in 0 ..< scan.pageCount {
                let img = scan.imageOfPage(at: i)
                // ... your code here
                addNewPage(image : img)
            }
            
            DispatchQueue.main.async {
                //completionHandler(textPerPage)
            }
        }
         
        func documentCameraViewControllerDidCancel(_ controller: VNDocumentCameraViewController) {
            completionHandler(nil)
        }
         
        func documentCameraViewController(_ controller: VNDocumentCameraViewController, didFailWithError error: Error) {
            print("Document camera view controller did finish with error ", error)
            completionHandler(nil)
        }
        
        func addNewPage(image : UIImage){
            print("Adding a new page for doc id: .\(document.id)")
            print("Adding a new page for doc id <list>: .\(parent.sessionData.myDocumentList.count)")
            // is existing doc?
            if let index = parent.sessionData.myDocumentList.firstIndex(of: document) {
                print("Adding a new page: .\(index)")
                print("Adding a new page <before>: .\(parent.sessionData.myDocumentList[index].pages.count)")
                parent.sessionData.myDocumentList[index].pages.append(DocumentPage(localImage: image, pageNumber: parent.sessionData.myDocumentList[index].pages.count))
                print("Adding a new page <after>: .\(parent.sessionData.myDocumentList[index].pages.count)")
            }
            // otherwise check new doc
            if(parent.sessionData.newWorkDocumentFlag) {
                parent.sessionData.newWorkDocument.pages.append(DocumentPage(localImage: image, pageNumber: parent.sessionData.newWorkDocument.pages.count))
            }
        }
        
        
    }
}
