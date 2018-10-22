import { Injectable } from '@angular/core';
import { Http, Headers } from '@angular/http';


@Injectable()
export class WebserviceService {

  endpoint = "http://localhost:8080/";

  constructor(private http: Http) { }

  getTweetsCount(palabra){
    let results = {};
    results.ocurrencias = 0;
    results.fecha = null;
    return new Promise(resolve=>{
      const req = this.http.get(this.endpoint+'get_tweets_count?search='+palabra)
      .subscribe(
        data => {
          results.ocurrencias = data.json()["total_count"];
          results.fecha = data.json()['results'];   
          resolve(results);
        },  //changed
      (err)=>alert("Error obteniendo ocurrencias"));
    })
  }

}

