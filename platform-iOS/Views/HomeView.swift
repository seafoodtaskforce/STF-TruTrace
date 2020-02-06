//
//  ContentView.swift
//  TruTracePOC
//
//  Created by Republic Systems on 2019-11-25.
//  Copyright © 2019 Republic Systems. All rights reserved.
//

import SwiftUI

struct HomeView: View {
    @EnvironmentObject var viewRouter: ViewRouter
    @EnvironmentObject var sessionData: SessionData
    let timer = Timer.publish(every: 10, on: .main, in: .common).autoconnect()
    
    @State private var badgeCount: Int = 1
    private var badgeProfilePosition: CGFloat = 2
    private var badgeMyDocsPosition: CGFloat = 3
    private var badgeFeedPosition: CGFloat = 4
    private var tabsCount: CGFloat = 4
    
    @State var selectedView = 4
    
    var body: some View {

        GeometryReader { geometry in
            ZStack(alignment: .bottomLeading) {
                TabView (selection : self.$selectedView){
                    UserProfileView().environmentObject(self.viewRouter).environmentObject(self.sessionData)
                        .tabItem {
                            VStack {
                                Image("house").resizable()
                                .frame(width: 16.0, height: 16.0)
                                Text(LocalizationUtils.localizeString(text: "ios_bottom_tabs_label_home"))
                            }
                        }.tag(1)
                    
                    
                    ProfileDocView().environmentObject(self.viewRouter).environmentObject(self.sessionData)
                        .tabItem {
                            VStack {
                                Image("tray.full").resizable()
                                    .frame(width: 16.0, height: 16.0)
                                Text(LocalizationUtils.localizeString(text: "ios_bottom_tabs_label_profile"))
                            }
                        }.tag(2)

                    MyDocView().environmentObject(self.viewRouter).environmentObject(self.sessionData)
                        .tabItem {
                            Image("tray.and.arrow.up").resizable()
                                .frame(width: 16.0, height: 16.0)
                            Text(LocalizationUtils.localizeString(text: "ios_bottom_tabs_label_mydocs"))
                        }.tag(3)
                    
                    DocFeedView().environmentObject(self.viewRouter).environmentObject(self.sessionData)
                        .tabItem {
                            Image("tray.2").resizable()
                            .frame(width: 16.0, height: 16.0)
                            Text(LocalizationUtils.localizeString(text: "ios_bottom_tabs_label_docfeeds"))
                        }.tag(4)
                }
            
                // Badge View
                ZStack {
                  Circle()
                    .foregroundColor(.blue)

                    Text("\(10)")
                    .foregroundColor(.white)
                    .font(Font.system(size: 12))
                }
                .frame(width: 15, height: 15)
                .offset(x: ( ( 2 * self.badgeProfilePosition) - 0.95 ) * ( geometry.size.width / ( 2 * self.tabsCount ) ) + 2, y: -25)
                .opacity(0 == 0 ? 0 : 1.0)
            }.onAppear{
                print("Done")
                self.selectedView = self.viewRouter.currentTabView
            }
            .onReceive(self.timer) { time in
                print("<notification> The time is now \(time)")
                self.checkForNotificationData()
            }// ZSTack
        } //GeometryReader
    } // View Body
    
    
    //
    //
    // functions
    
    //
    //
    func checkForNotificationData() {
        guard let url = URL(string:
            (RESTServer.REMOTE_SERVER_URL + RESTServer.REST_GET_NOTIFICATIONS_FOR_USER_ALL))
        
        else {
            print("invalid URL")
            return
        }
        
        var request = URLRequest(url : url)
        request.httpMethod = "GET"
        request.setValue(sessionData.userCredentials.username, forHTTPHeaderField: "user-name")
        
        URLSession.shared.dataTask(with: request) {
            (data, response, error) in
            guard let data = data else { return }
            let decodedResponse = try! JSONDecoder().decode([NotificationDTO].self, from: data)
                
                DispatchQueue.main.async {
                    print("<notification> Notification Fetch Response --> \(decodedResponse)")
                    
                    for notification in decodedResponse {
                        var title : String
                        title = notification.auditData.actor.name + " " + notification.auditData.action + " " + notification.auditData.itemId
                        self.setNotification(title:title)
                        print("<notification> User : \(notification.auditData.actor.name)")
                        print("<notification> action : \(notification.auditData.action)")
                        print("<notification> session ID : \(notification.auditData.itemId)")
                    }
                    
                    print("<notification> Number of Notifications : \(decodedResponse.count)")
                    //
                    // Set the notification


                }
        }.resume()
    }
    
    
    //
    //
    //
    func setNotification(title : String) -> Void {
        let manager = LocalNotificationManager()
        manager.requestPermission()
        manager.addNotification(title: title)
        manager.scheduleNotifications()
    }
    
} // View

struct HomeView_Previews: PreviewProvider {
    static var previews: some View {
        HomeView()
            .environmentObject(ViewRouter())
            .environmentObject(SessionData())
    }
}
