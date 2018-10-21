import { Injectable } from '@angular/core';
import { Http, Headers } from '@angular/http';


@Injectable()
export class WebserviceService {

  endpoint = "http://localhost:8080/";

  constructor(private http: Http) { }

  getTweetsCount(palabra){
    let ocurrencias = 0;
    return new Promise(resolve=>{
      const req = this.http.get(this.endpoint+'get_tweets_count?search='+palabra)
      .subscribe(
        data => {
          ocurrencias = data.json()["count"];   
          resolve(ocurrencias);
        },  //changed
      (err)=>alert("Error obteniendo ocurrencias"));
    })
  }

}

