import { Component } from '@angular/core';
 
@Component({
   selector: 'handsontable-section',
   template: `
               <div class="widgets">
                  <div class="row">
                    <div class="col-md-12">
                      <ba-card cardTitle="Examples">
                         <section id="handsontable">
                           <div class="row">
                                <div class="col-md-12">
                                    <advanced-demo></advanced-demo>
                                    <br>
                                </div>
                           </div>
                         </section>
                       </ba-card>
                    </div>
                  </div>
               </div>
             `
})
export class HandsontableSectionComponent {
    currentHeading:string = 'Basic';
 
select(e) {
    if (e.heading) {
     this.currentHeading = e.heading;
    }
 }
}
 
function escape(text: string): string {
  return text.replace(/{/g, '&#123;').replace(/}/g, '&#125;');
}
