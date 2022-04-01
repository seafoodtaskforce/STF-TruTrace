import {Component, ElementRef} from '@angular/core';
import {Location} from '@angular/common';
import { Router } from '@angular/router'

import 'leaflet-map';
import { DocumentsComponent } from '../documents/documents.component';
import { Group } from 'app/models/group';
import { DocumentService } from '../documents/document.service';

@Component({
  selector: 'leaflet-maps',
  templateUrl: './leafletMaps.html',
  styleUrls: ['./leafletMaps.scss']
})
export class LeafletMaps {
  groupsForMap : Group[] = new Array<Group>();
  pathLatLngs : number[][] = new Array<Array<number>>();
  //
  // Flags
  //
  mapShowOrganizationPathToggleFlag : boolean = true;
  mapShowOrganizationPopupToggleFlag : boolean = false;
  mapShowDocumentToggleFlag : boolean = false;
  mapShowDocInfoToggleFlag : boolean = false;
  mapShowDocPathToggleFlag : boolean = false;
  //
  // Map elements
  //
  map : any;
  orgPolyLinePath : any;
  documentLayers : any;

  
  constructor(private _documentService: DocumentService, private _elementRef:ElementRef, private location: Location, private router: Router ) {
  }

  ngAfterViewInit() {
    L.Map.extend({
      openPopup: function(popup) {
      //        this.closePopup();  // just comment this
      this._popup = popup;

      return this.addLayer(popup).fire('popupopen', {
                                       popup: this._popup
                                       });
      }
      }); /***  end of hack ***/

    //
    // get the trace map data
    //
    this.groupsForMap = JSON.parse(localStorage.getItem(DocumentsComponent.SESSION_STORAGE_KEY_MAP_GROUPS));
    console.log('[Leaflet] <mapping data> '.concat(JSON.stringify(this.groupsForMap)));

    //
    // Get the map element in div
    let el = this._elementRef.nativeElement.querySelector('.leaflet-maps');

    L.Icon.Default.imagePath = 'assets/img/theme/vendor/leaflet';
    //var map = L.map(el).setView([51.505, -0.09], 13);
    //L.tileLayer('http://{s}.tile.osm.org/{z}/{x}/{y}.png', {
    //  attribution: '&copy; <a href="http://osm.org/copyright">OpenStreetMap</a> contributors'
    //}).addTo(map);

    //L.marker([51.5, -0.09]).addTo(map)
    //  .bindPopup('A pretty CSS3 popup.<br> Easily customizable.')
    //  .openPopup();



      if(this.groupsForMap.length > 0) {
        //
        // add the markers
        var firstFlag : boolean = false;
        for (var i = 0; i < this.groupsForMap.length; i++) {
          console.log('<initializeMap> iteration: <' + i + ">");
          if(this.groupsForMap[i] != null){
            //
            // initialize the map
            if (firstFlag == false) {
              var gpsCoordinates = this.groupsForMap[i].gpsCoordinates.split(",");
              var x: number = +gpsCoordinates[0];
              var y: number = +gpsCoordinates[1];
              console.log('<initializeMap> setting startup view...' + this.groupsForMap[i].gpsCoordinates + ' ' + this.groupsForMap[i].name);
              this.map = L.map(el);
              this.map.setView([x, y], 13);
              firstFlag = true;
              //
              //
              L.tileLayer('http://{s}.tile.osm.org/{z}/{x}/{y}.png', {
                attribution: '&copy; <a href="http://osm.org/copyright">OpenStreetMap</a> contributors'
              }).addTo(this.map);
              console.log('<initializeMap> Title Layer...');
            }
            console.log('<initializeMap> adding pins... ' + this.groupsForMap[i].gpsCoordinates + ' ' + this.groupsForMap[i].name);
            gpsCoordinates = this.groupsForMap[i].gpsCoordinates.split(",");
            x = +gpsCoordinates[0];
            y = +gpsCoordinates[1];
            //
            // Marker Icon
            // 
            var iconOptions = {
              iconUrl: '/assets/icon/doc_image.png',
              iconSize: [25, 25] 
            }
            // Creating a custom icon
            var customIcon = L.icon(iconOptions);
            var markerOptions = {
              title: this.groupsForMap[i].legalBusinessName,
              clickable: true,
              draggable: false,
              //icon: customIcon
           }
            //
            // Marker
            //
            var marker = L.marker([x, y], markerOptions);
            
            //
            // Popup
            marker.bindPopup(this.makeOrgPopup(this.groupsForMap[i]), {closeOnClick: true, autoClose: false, icon: iconOptions});
            // marker.bindTooltip("my tooltip text").openTooltip()
            //
            // Add to map
            marker.addTo(this.map);//.openPopup();
            //marker.bindTooltip("my tooltip text").openTooltip();

            //
            // Add path elements
            this.pathLatLngs.push([x, y]);
          }
        }
        this.orgPolyLinePath = L.polyline(this.pathLatLngs, {color: 'red', weight: '3'}).addTo(this.map);
        this.map.fitBounds(this.orgPolyLinePath.getBounds());
        console.log('<initializeMap> Done...');
      }  
  }



  dismissTraceMap() {
    console.log('[Leaflet] <re-routing> '.concat('/pages/documents/'));
    localStorage.setItem(DocumentsComponent.SESSION_STORAGE_KEY_MAP_GROUPS, JSON.stringify(new Array<Group>()));
    this.location.back();
  }

  showOrgPaths(showFlag : boolean) {
    //
    // Get the map element in div
    let el = this._elementRef.nativeElement.querySelector('.leaflet-maps');
    L.Icon.Default.imagePath = 'assets/img/theme/vendor/leaflet';


    if(showFlag == true ){
      if(this.groupsForMap.length > 0) {
        //
        // add the markers
        var firstFlag : boolean = false;
        for (var i = 0; i < this.groupsForMap.length; i++) {
          console.log('<initializeMap> iteration: <' + i + ">");
          if(this.groupsForMap[i] != null){
            //
            // initialize the map
            if (firstFlag == false) {
              var gpsCoordinates = this.groupsForMap[i].gpsCoordinates.split(",");
              var x: number = +gpsCoordinates[0];
              var y: number = +gpsCoordinates[1];
              console.log('<initializeMap> setting startup view...' + this.groupsForMap[i].gpsCoordinates + ' ' + this.groupsForMap[i].name);
              if(this.map != undefined){
                this.map.off();
                this.map.remove();
                this.map = new L.map(el);
              }
              this.map.setView([x, y], 13);
              firstFlag = true;
              //
              //
              L.tileLayer('http://{s}.tile.osm.org/{z}/{x}/{y}.png', {
                attribution: '&copy; <a href="http://osm.org/copyright">OpenStreetMap</a> contributors'
              }).addTo(this.map);
              console.log('<initializeMap> Title Layer...');
            }
            console.log('<initializeMap> adding pins... ' + this.groupsForMap[i].gpsCoordinates + ' ' + this.groupsForMap[i].name);
            gpsCoordinates = this.groupsForMap[i].gpsCoordinates.split(",");
            x = +gpsCoordinates[0];
            y = +gpsCoordinates[1];
            var marker = L.marker([x, y])
              .bindPopup(this.groupsForMap[i].legalBusinessName)
              .addTo(this.map);
            //
            // Add path elements
            this.pathLatLngs.push([x, y]);
          }
        }
        var polyline = L.polyline(this.pathLatLngs, {color: 'red', weight: '3'}).addTo(this.map);
        this.map.fitBounds(polyline.getBounds());
        console.log('<initializeMap> Done...');
      }  
    } else {
      if(this.groupsForMap.length > 0) {
        //
        // add the markers
        var firstFlag : boolean = false;
        for (var i = 0; i < this.groupsForMap.length; i++) {
          console.log('<initializeMap> iteration: <' + i + ">");
          if(this.groupsForMap[i] != null){
            //
            // initialize the map
            if (firstFlag == false) {
              var gpsCoordinates = this.groupsForMap[i].gpsCoordinates.split(",");
              var x: number = +gpsCoordinates[0];
              var y: number = +gpsCoordinates[1];
              console.log('<initializeMap> setting startup view...' + this.groupsForMap[i].gpsCoordinates + ' ' + this.groupsForMap[i].name);
              if(this.map != undefined){
                this.map.off();
                this.map.remove();
                this.map = new L.map(el);
              }
              this.map.setView([x, y], 13);
              firstFlag = true;
              //
              //
              L.tileLayer('http://{s}.tile.osm.org/{z}/{x}/{y}.png', {
                attribution: '&copy; <a href="http://osm.org/copyright">OpenStreetMap</a> contributors'
              }).addTo(this.map);
              console.log('<initializeMap> Title Layer...');
            }
            console.log('<initializeMap> adding pins... ' + this.groupsForMap[i].gpsCoordinates + ' ' + this.groupsForMap[i].name);
            gpsCoordinates = this.groupsForMap[i].gpsCoordinates.split(",");
            x = +gpsCoordinates[0];
            y = +gpsCoordinates[1];
            var marker = L.marker([x, y])
              .bindPopup(this.groupsForMap[i].legalBusinessName)
              .addTo(this.map);
            //
            // Add path elements
            this.pathLatLngs.push([x, y]);
          }
        }
        var polyline = L.polyline(this.pathLatLngs, {color: 'red', weight: '3'})
        this.map.fitBounds(polyline.getBounds());
        console.log('<initializeMap> Done...');
      }
    }
  }

  showOrgPaths2() {
    //
    // Get the map element in div
    let el = this._elementRef.nativeElement.querySelector('.leaflet-maps');
    L.Icon.Default.imagePath = 'assets/img/theme/vendor/leaflet';
    this.mapShowOrganizationPathToggleFlag = !this.mapShowOrganizationPathToggleFlag;

    if(this.mapShowOrganizationPathToggleFlag == true ){
      if(this.groupsForMap.length > 0) {
        //
        // add the markers
        for (var i = 0; i < this.groupsForMap.length; i++) {
          console.log('<initializeMap> iteration: <' + i + ">");
          if(this.groupsForMap[i] != null){
            console.log('<initializeMap> adding pins... ' + this.groupsForMap[i].gpsCoordinates + ' ' + this.groupsForMap[i].name);
            var gpsCoordinates = this.groupsForMap[i].gpsCoordinates.split(",");
            var x: number = +gpsCoordinates[0];
            var y: number = +gpsCoordinates[1];
            //
            // Add path elements
            this.pathLatLngs.push([x, y]);
          }
        }
        this.orgPolyLinePath = L.polyline(this.pathLatLngs, {color: 'red', weight: '3'}).addTo(this.map);
        console.log('<initializeMap> Done...');
      }  
    } else {
      this.pathLatLngs = new Array<Array<number>>();
      this.map.removeLayer(this.orgPolyLinePath);
    }
  }

  showOrgPopups() {
    //
    // Get the map element in div
    let el = this._elementRef.nativeElement.querySelector('.leaflet-maps');
    L.Icon.Default.imagePath = 'assets/img/theme/vendor/leaflet';
    this.mapShowOrganizationPopupToggleFlag = !this.mapShowOrganizationPopupToggleFlag;

    if(this.mapShowOrganizationPopupToggleFlag == true ){
      if(this.groupsForMap.length > 0) {
        //
        // add the markers
        var firstFlag : boolean = false;
        for (var i = 0; i < this.groupsForMap.length; i++) {
          console.log('<initializeMap> iteration: <' + i + ">");
          if(this.groupsForMap[i] != null){
            //
            // initialize the map
            if (firstFlag == false) {
              var gpsCoordinates = this.groupsForMap[i].gpsCoordinates.split(",");
              var x: number = +gpsCoordinates[0];
              var y: number = +gpsCoordinates[1];
              console.log('<initializeMap> setting startup view...' + this.groupsForMap[i].gpsCoordinates + ' ' + this.groupsForMap[i].name);
              firstFlag = true;
            }
            console.log('<initializeMap> adding pins... ' + this.groupsForMap[i].gpsCoordinates + ' ' + this.groupsForMap[i].name);
            gpsCoordinates = this.groupsForMap[i].gpsCoordinates.split(",");
            x = +gpsCoordinates[0];
            y = +gpsCoordinates[1];
            //
            // Add path elements
            this.pathLatLngs.push([x, y]);
          }
        }
        this.orgPolyLinePath = L.polyline(this.pathLatLngs, {color: 'red', weight: '3'}).addTo(this.map);
        console.log('<initializeMap> Done...');
      }  
    } else {
      this.map.removeLayer(this.orgPolyLinePath)
    }
  }

  showAllDocs(){
    this.mapShowDocumentToggleFlag = !this.mapShowDocumentToggleFlag;

    if (this.mapShowDocumentToggleFlag) {
      var littleton = L.marker([39.61, -105.02]).bindPopup('This is Littleton, CO.'),
      denver    = L.marker([39.74, -104.99]).bindPopup('This is Denver, CO.'),
      aurora    = L.marker([39.73, -104.8]).bindPopup('This is Aurora, CO.'),
      golden    = L.marker([39.77, -105.23]).bindPopup('This is Golden, CO.');
  
      this.documentLayers = L.layerGroup([littleton, denver, aurora, golden]);
  
      this.documentLayers.addTo(this.map);
      console.log('<show all docs> Done...');
    } else {
      //
      this.map.removeLayer(this.documentLayers)
    }
  }


  showOrgDocument() {

    var docIcon = L.icon({
      iconUrl: 'leaf-green.png',
      shadowUrl: 'leaf-shadow.png',
  
      iconSize:     [38, 95], // size of the icon
      shadowSize:   [50, 64], // size of the shadow
      iconAnchor:   [22, 94], // point of the icon which will correspond to marker's location
      shadowAnchor: [4, 62],  // the same for the shadow
      popupAnchor:  [-3, -76] // point from which the popup should open relative to the iconAnchor
  });

  }
  //
  // Utils
  //
  makeOrgPopup(data: Group): string {
    return `` +
      `<div><b>Organization:</b> ${ data.legalBusinessName }</div>` +
      `<div><b>Address:</b> ${ data.businessAddress }</div>`
  }

}
