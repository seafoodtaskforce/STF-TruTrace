//
//  UserProfileView.swift
//  TruTracePOC
//
//  Created by Republic Systems on 2019-12-11.
//  Copyright © 2019 Republic Systems. All rights reserved.
//

import SwiftUI


struct UserProfileView: View {
    @EnvironmentObject var viewRouter: ViewRouter
    @EnvironmentObject var sessionData: SessionData
    
    @State private var inputImage = UIImage()
    @State private var profileImage = UIImage()
    @State private var isShowingImagePicker = false
    
    static let pillColor: Color =  Color.red
    
    var body: some View {
        NavigationView {
            VStack {
                
                // Profile Image
                // - mutable
                
                VStack{
                    ImageViewWidget(imageUrl: ImageUtils.fetchProfileURL(username : self.sessionData.userCredentials.username, sessionData : sessionData))
                    Button(action: {
                        // add another image to the mix
                        self.isShowingImagePicker = true
                    }) {
                        Text(LocalizationUtils.localizeString(text: "ios_user_data_page_button_change_profile")).font(.footnote)
                    }.sheet(isPresented: $isShowingImagePicker, onDismiss: loadImage) {
                        ImagePicker(image: self.$inputImage)
                    }
                }
                //
                // Gallery Image Options
                

                
                // Profile username
                // - immutable
                Text(self.sessionData.userCredentials.username).font(.largeTitle)
                Spacer()
                Spacer()
                Spacer()
                
                List {
                    Section(header: Text(LocalizationUtils.localizeString(text: "ios_user_data_page_section_header_personal_info"))) {
                        // First Name
                        // - mutable
                        NavigationLink(
                        destination: PersonalInformationEditView().environmentObject(self.sessionData)) {
                            HStack{
                                Image(systemName: "f.square.fill").padding(EdgeInsets(top: 0, leading: 10, bottom: 0, trailing: 10))
                                Text(LocalizationUtils.localizeString(text: "ios_user_data_page_section_personal_info_label_first_name")).bold()
                                Spacer()
                                Text(self.sessionData.contactInfo.firstName)
                            }
                        }
                        // Last Name
                        // - mutable
                        NavigationLink(
                        destination: PersonalInformationEditView().environmentObject(self.sessionData)) {
                            HStack{
                                Image(systemName: "l.square.fill").padding(EdgeInsets(top: 0, leading: 10, bottom: 0, trailing: 10))
                                Text(LocalizationUtils.localizeString(text: "ios_user_data_page_section_personal_info_label_last_name")).bold()
                                Spacer()
                                Text(self.sessionData.contactInfo.lastName)
                            }
                        }
                        // Nickname
                        // - mutable
                        NavigationLink(
                        destination: PersonalInformationEditView().environmentObject(self.sessionData)) {
                            HStack{
                                Image(systemName: "person.circle.fill").padding(EdgeInsets(top: 0, leading: 10, bottom: 0, trailing: 10))
                                Text(LocalizationUtils.localizeString(text: "ios_user_data_page_section_personal_info_label_nickname")).bold()
                                Spacer()
                                Text(self.sessionData.contactInfo.nickName)
                            }
                        }
                    }
                    
                    
                    Section(header: Text(LocalizationUtils.localizeString(text: "ios_user_data_page_section_header_organization_info"))) {
                        // Organization
                        // - immutable
                        HStack{
                            Image(systemName: "person.3.fill").padding(EdgeInsets(top: 0, leading: 0, bottom: 0, trailing: 4))
                            Text(LocalizationUtils.localizeString(text: "ios_user_data_page_section_organization_info_label_organization")).bold()
                            Spacer()
                            Text((self.sessionData.userGroups.count != 0)
                                ? self.sessionData.userGroups[0].name : "")
                        }
                        // LIneID
                        // - mutable
                        NavigationLink(
                        destination: OrganizationInformationEditView().environmentObject(self.sessionData)) {
                            HStack{
                                Image(systemName: "bubble.left.fill").padding(EdgeInsets(top: 0, leading: 10, bottom: 0, trailing: 8))
                                Text(LocalizationUtils.localizeString(text: "ios_user_data_page_section_organization_info_label_lineid")).bold()
                                Spacer()
                                Text(self.sessionData.contactInfo.lineId)
                            }
                        }
                        // Email
                        // - mutable
                        NavigationLink(
                        destination: OrganizationInformationEditView().environmentObject(self.sessionData)) {
                            HStack{
                                Image(systemName: "envelope.fill").padding(EdgeInsets(top: 0, leading: 10, bottom: 0, trailing: 8))
                                Text(LocalizationUtils.localizeString(text: "ios_user_data_page_section_organization_info_label_email")).bold()
                                Spacer()
                                Text(self.sessionData.contactInfo.emailAddress)
                            }
                        }
                    }
                    
                    Section(header: Text(LocalizationUtils.localizeString(text: "ios_user_data_page_section_header_security_info"))) {
                        // Password
                        // - mutable
                        NavigationLink(
                        destination: SecurityInformationEditView().environmentObject(self.sessionData)) {
                            HStack{
                                Image(systemName: "keyboard").padding(EdgeInsets(top: 0, leading: 6, bottom: 0, trailing: 6))
                                Text(LocalizationUtils.localizeString(text: "ios_user_data_page_section_security_info_Label_password")).bold()
                            }
                        }
                    }
                    
                    //
                    //
                    Section(header: Text(LocalizationUtils.localizeString(text: "ios_user_data_page_section_header_storage_info"))) {
                        // Password
                        // - mutable
                            HStack{
                                if(self.sessionData.isOnlineFlag == true){
                                    Image(systemName: "wifi").padding(EdgeInsets(top: 0, leading: 6, bottom: 0, trailing: 6))
                                    Text(LocalizationUtils.localizeString(text: "ios_user_data_page_section_storge_info_Label_online")).bold()

                                }else{
                                    Image(systemName: "wifi.slash").padding(EdgeInsets(top: 0, leading: 6, bottom: 0, trailing: 6))
                                    Text(LocalizationUtils.localizeString(text: "ios_user_data_page_section_storge_info_Label_offline")).bold()
                                }
                                Text("\(OfflineDataUtils.getOfflineDocCount())")
                                .font(.caption)
                                .fontWeight(.black)
                                .padding(5)
                                .background(Self.pillColor)
                                .clipShape(Circle())
                                .foregroundColor(.white)
                                Spacer()
                                Toggle(isOn : self.$sessionData.isOnlineFlag){
                                    Text("")
                                }.onTapGesture {
                                    print("<getOfflineDocuments> calling...")
                                    if(!self.sessionData.isOnlineFlag){
                                        let offlineDocs = OfflineDataUtils.getOfflineDocuments()
                                        for i in 0 ..< offlineDocs.count {
                                            print("<getOfflineDocuments> saving doc...")
                                            RESTServer.uploadNewSerializedDocument(document : offlineDocs[i], pages : offlineDocs[i].pages, sessionData: self.sessionData)
                                        }
                                        OfflineDataUtils.clearOfflineDocuments()
                                    }
                                }
                             }
                    }

                } // List End
            } // Outer VStack End
            .navigationBarTitle(LocalizationUtils.localizeString(text: "ios_user_data_page_navigation_header"))
            .navigationBarItems(
                trailing:
                Button(LocalizationUtils.localizeString(text: "ios_user_data_page_navigation_button_logout")) {
                    print("Logging User Out")
                    // clear session
                    self.sessionData.appResources = [ResourceItem]()
                    self.sessionData.userCredentials = AuthCredentials()
                    self.sessionData.contactInfo = UserContactInfo()
                    self.sessionData.userGroups = [UserGroup]()
                    // route to loging page
                    self.viewRouter.currentPage = ViewRouter.LOGIN_PAGE
                })
        }
    }
    
    /*
     Load Image Page to the server proxy call
     */
    func loadImage() {
        saveImageToServer(image: inputImage)
    }
    
    /*
     Save the Image page to the server
     */
    func saveImageToServer(image : UIImage){
        
        //
        // pre condition
        if image.size.width == 0 { return }
        //
        // Shrink the image
        let resizedImage = image.resized(toWidth: 320.0)
        
        let boundary = UUID().uuidString
        let filename = "profile.jpg"
        let urlString = sessionData.serverURL + RESTServer.REST_USER_POST_PROFILE_IMAGE
        
        let fieldName = "userName"
        let fieldValue = sessionData.userCredentials.username

        print("Mark Read - URL .\(urlString)")
        guard let url = URL(string: urlString)
        
        else {
            print("invalid URL")
            return
        }
        let config = URLSessionConfiguration.default
        let session = URLSession(configuration: config)
        
        var request = URLRequest(url : url)
        request.httpMethod = "POST"
        request.setValue("multipart/form-data; boundary=\(boundary)", forHTTPHeaderField: "Content-Type")
        
        var data = Data()
        
        //
        // Add user name
        data.append("\r\n--\(boundary)\r\n".data(using: .utf8)!)
        data.append("Content-Disposition: form-data; name=\"\(fieldName)\"\r\n\r\n".data(using: .utf8)!)
        data.append("\(fieldValue)".data(using: .utf8)!)
        
        //
        //
        data.append("\r\n--\(boundary)\r\n".data(using: .utf8)!)
        data.append("Content-Disposition: form-data; name=\"file\"; filename=\"\(filename)\"\r\n".data(using: .utf8)!)
        data.append("Content-Type: image/jpg\r\n\r\n".data(using: .utf8)!)
        data.append((resizedImage?.jpegData(compressionQuality: 0.5)!)!)

        // End the raw http request data, note that there is 2 extra dash ("-") at the end, this is to indicate the end of the data
        // According to the HTTP 1.1 specification https://tools.ietf.org/html/rfc7230
        data.append("\r\n--\(boundary)--\r\n".data(using: .utf8)!)
        
        session.uploadTask(with: request, from: data, completionHandler: {
            responseData, response, error in
            
            if(error != nil){
                print("\(error!.localizedDescription)")
            }
            
            guard let responseData = responseData else {
                print("no response data")
                return
            }
            
            if let responseString = String(data: responseData, encoding: .utf8) {
                print("uploaded to: \(responseString)")
            }
        }).resume()
    }
    
    //
    //
    // Offfline Functionality
    
    

}

struct UserProfileView_Previews: PreviewProvider {
    static var previews: some View {
        UserProfileView()
        .environmentObject(ViewRouter())
        .environmentObject(SessionData())
    }
}


