import { WebserviceService } from './../webservice.service';
import { Component, OnInit } from '@angular/core';
import { Http } from '@angular/http';



@Component({
  selector: 'app-tweets-form',
  templateUrl: './tweets-form.component.html',
  styleUrls: ['./tweets-form.component.css']
})

export class TweetsFormComponent implements OnInit {

  ocurrencias;
  palabra;

  constructor(private http: Http, private service: WebserviceService){ 
  }

  ngOnInit() {
    this.palabra = "";
    this.ocurrencias=0;
  }

  buscarTweets(){
    this.service.getTweetsCount(this.palabra).then((data:any)=>{
      this.ocurrencias = data;
    });
  }

}
