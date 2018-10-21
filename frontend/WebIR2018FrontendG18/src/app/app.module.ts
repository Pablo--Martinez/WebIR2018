import { WebserviceService } from './webservice.service';
import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import { HttpModule } from '@angular/http';
import { FormsModule } from '@angular/forms';

import { AppComponent } from './app.component';
import { TweetsFormComponent } from './tweets-form/tweets-form.component';
import { ResultComponent } from './result/result.component';

@NgModule({
  declarations: [
    AppComponent,
    TweetsFormComponent,
    ResultComponent
  ],
  imports: [
    BrowserModule, 
    HttpModule,
    FormsModule
  ],
  providers: [WebserviceService],
  bootstrap: [AppComponent]
})
export class AppModule { }
