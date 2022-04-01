import { AppResource } from '../models/AppResource';
import { OnInit, Injectable } from '@angular/core';
import { Http } from '@angular/http';
import {Observable} from 'rxjs/Rx';
import 'rxjs/add/operator/map';
import 'rxjs/add/operator/catch';



export class ServerUtils{

    public static BACK_END_SERVER_URL: string = "backend.server.url";

}