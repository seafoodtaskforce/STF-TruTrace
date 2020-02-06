//
//  FloatingMenu.swift
//  TruTracePOC
//
//  Created by Republic Systems on 2019-11-25.
//  Copyright © 2019 Republic Systems. All rights reserved.
//

import SwiftUI

struct FloatingMenu : View {
    @State var showMenuItem1 = false
    @State var showMenuItem2 = false
    
    var body: some View {

        VStack {
            Spacer()
            if showMenuItem1 {
                MenuItem(icon: "camera.fill", color: Color(red: 153/255, green: 102/255, blue: 255/255))
            }
            if showMenuItem1 {
                MenuItem(icon: "rectangle.and.paperclip", color: Color(red: 153/255, green: 102/255, blue: 255/255))
            }
            
            Button(action: {
                self.showMenu()
            }) {
                Image(systemName: "plus.circle.fill")
                .resizable()
                .frame(width: 70, height: 70)
                .foregroundColor(Color(red: 153/255, green: 102/255, blue: 255/255))
                .shadow(color: .gray, radius: 0.2, x: 1, y: 1)
            }
        }
    }
    
    func showMenu() {
        withAnimation {
            showMenuItem2.toggle()
        }
        DispatchQueue.main.asyncAfter(deadline: .now() + 0.2, execute: {
            withAnimation {
                self.showMenuItem1.toggle()
            }
        })
    }
}

struct FloatingMenu_Previews: PreviewProvider {
    static var previews: some View {
        FloatingMenu()
    }
}

struct MenuItem: View {
    var icon: String
    var color: Color
    
    var body: some View {
        HStack {
            ZStack {
                Circle()
                    .foregroundColor(color)
                    .frame(width: 55, height: 55).shadow(color: .gray, radius: 0.2, x: 1, y: 1)
                if(!icon.isEmpty){
                    Image(systemName: icon)
                    .imageScale(.large)
                    .foregroundColor(.white)
                }
            }
            .transition(.move(edge: .trailing))
            
        }
    }
}
