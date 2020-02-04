# ng2-completer

Auto complete component for Angular 2.

This component is based on [angucomplete-alt](https://github.com/ghiden/angucomplete-alt)

Click for [demo](http://oferh.github.io/ng2-completer/) or [plunk](https://plnkr.co/edit/sVnfpBiEb5jBdtul4ls9?p=preview)

## Installation

```sh
npm install ng2-completer --save
```

## Usage

The module you want to use ng2-completer in must import `Ng2CompleterModule` and `FormsModule` (to use the ngModel 
directive on ng2-completer).  `Ng2CompleterModule` provides the `CompleterService`, and declares the `ng2-completer` 
directive.
```ts
import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { FormsModule } from "@angular/forms";
import { AppComponent } from './app.component';
import { Ng2CompleterModule } from "ng2-completer";
 
@NgModule({
  imports: [
      BrowserModule,
      Ng2CompleterModule,
      FormsModule,
  ],
  declarations: [ AppComponent ],
  bootstrap: [ AppComponent ]
})
export class AppModule { }
```

Add ng2-completer to your component and create a data source:

```ts
import { Component } from '@angular/core';
import { CompleterService, CompleterData } from 'ng2-completer';

@Component({
  selector: 'my-component',
  template: `<h1>Search color</h1>
            <ng2-completer [(ngModel)]="searchStr" [datasource]="dataService" [minSearchLength]="0"></ng2-completer>
            <h1>Search captain</h1>
            <ng2-completer [(ngModel)]="captain" [datasource]="captains" [minSearchLength]="0"></ng2-completer>`
})
export class MyComponent {

  protected searchStr: string;
  protected captain: string;
  protected dataService: CompleterData;
  protected searchData = [
    { color: 'red', value: '#f00' },
    { color: 'green', value: '#0f0' },
    { color: 'blue', value: '#00f' },
    { color: 'cyan', value: '#0ff' },
    { color: 'magenta', value: '#f0f' },
    { color: 'yellow', value: '#ff0' },
    { color: 'black', value: '#000' }
  ];
  protected captains = ['James T. Kirk', 'Benjamin Sisko', 'Jean-Luc Picard', 'Spock', 'Jonathan Archer', 'Hikaru Sulu', 'Christopher Pike', 'Rachel Garrett' ];

  constructor(private completerService: CompleterService) {
    this.dataService = completerService.local(this.searchData, 'color', 'color');
  }
}
```

ng2-completer uses [rxjs](https://github.com/Reactive-Extensions/RxJS) stream as data sources. 
There are 2 ready made data sources that can be used to fetch local and remote data but it's also possible to provide 
a custom source that generates a stream of items.

### System.js configuration

Add the following to `System.js` map configuration:
```ts
   var map = {
       ...
       'ng2-completer': 'node_modules/ng2-completer/ng2-completer.umd.js'
   }
```



## API

### ng2-completer component

|Attribute|Description|Type|Required|Default|
|:---    |:---        |:--- |:---      |:--- |
|datasource|Autocomplete list data source can be an array of strigs or a URL that results in an array of strings or a CompleterData object|Array\<string\>\|string\|CompleterData|Yes||
|dataService|**Depreacted**  use `datasource` instead. Autocomplete list data source.|CompleterData|Yes||
|ngModel| see the angular [forms API](https://angular.io/docs/js/latest/guide/forms.html).|string|Yes||
|autoMatch|Auto select an item if it is the only result and it is an exact match of the search text.|boolean|No|false
|autofocus|Set input focus when the page loads|boolean|No|false
|clearSelected|Clear the input when a result is selected.|boolean|No|false|
|disableInput|If true disable the input field.|boolean|No|false|
|fieldTabindex|Set the `tabIndex` of the input.|number|No||
|inputId|`id` attribute of the input element.|string|No||
|inputName|`name` attribute of the input element.|string|No||
|inputClass| `class` attribute of the input element.|string|No||
|matchClass|CSS class to apply for matching part of the title and description.|string|No||
|maxChars|Maximal number of characters that the user can type in the component.|number|No|524288|
|minSearchLength|Minimal number of characters required for searching.|number|No|3|
|overrideSuggested|If true will override suggested and set the model with the value in the input field.|boolean|No|false|
|fillHighlighted|If true will set the model with the value in the input field when item is highlighted.|boolean|No|true|
|pause|Number of msec. to wait before searching.|number|No|250|
|placeholder|Placeholder text for the search field.|string|No||
|selected|Event handler that is called when an item is selected.|(selected: CompleterItem): void|No||
|highlighted|Event handler that is called when an item is highlighted.|(highlighted: CompleterItem): void|No||
|textNoResults|Text displayed when the search returned no results.|string|No|
|textSearching|Text displayed while search is active.|string|No|Searching...|

### ng2-completer methods

|Method|Description|Parameters|
|:---    |:---        |:--- |
|open(searchValue: string)|Open the dropdown and do search on a value|searchValue - string to search for default is ""|
|close()|Close the dropdown| |
|focus()|Set the focus to the completer input| |

### Local data

Create local data provider by calling `CompleterService.local`.

#### Parameters

|Name|Type|Description|Required|
|:---|:---|:---       |:---    |
|data|any[] \| Observable<any[]>|A JSON array with the data to use or an Observable that emits one|Yes|
|searchFields|string|Comma separated list of fields to search on. Fields may contain dots for nested attributes; if empty or null all data will be returned.|Yes|
|titleField|string|Name of the field to use as title for the list item.|Yes|

#### Attributes
|Name|Type|Description|
|:---|:---|:---       |
|descriptionField|string|Name of the field to use as description for the list item.|
|imageField|string|Name of the field to use as image url for the list item.|

### Remote data

Create remote data provider by calling `CompleterService.remote`.

#### Parameters

|Name|Type|Description|Required|
|:---|:---|:---       |:---    |
|url|string|Base url for the search|Yes|
|searchFields|string|Comma separated list of fields to search on. Fields may contain dots for nested attributes; if empty or null all data will be returned.|Yes|
|titleField|string|Name of the field to use as title for the list item.|Yes|

#### Attributes

|Name|Type|Description|
|:---|:---|:---       |
|descriptionField|string|Name of the field to use as description for the list item.|
|imageField|string|Name of the field to use as image url for the list item.|
|urlFormater|(term: string) => string|Function that get's the searchterm and returns the search url before each search.|
|dataField|string|The field in the response that includes the data.|
|headers|Headers (@angular/http)|HTTP request headers that should be sent with the search request.

### CSS classes

* `.completer-holder`
* `.completer-input`
* `.completer-dropdown-holder` 
* `.completer-dropdown`
* `.completer-searching`
* `.completer-no-results`
* `.completer-row`
* `.completer-image-holder`
* `.completer-image`
* `.completer-image-default`
* `.completer-title`
* `.completer-description`
* `.completer-list-item-holder`
* `.completer-list-item`
* `.completer-selected-row`
