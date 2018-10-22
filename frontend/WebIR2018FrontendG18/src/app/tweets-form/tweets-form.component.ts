import { WebserviceService } from './../webservice.service';
import { Component, OnInit } from '@angular/core';
import { Http } from '@angular/http';
import * as CanvasJS from '../../canvasjs.min.js';



@Component({
  selector: 'app-tweets-form',
  templateUrl: './tweets-form.component.html',
  styleUrls: ['./tweets-form.component.css']
})

export class TweetsFormComponent implements OnInit {

  palabra;
  results;
  results.ocurrencias;
  results.fecha;

  constructor(private http: Http, private service: WebserviceService){
  }

  ngOnInit() {
    this.palabra = "";
    this.results = {};
    this.results.ocurrencias=0;
    this.results.fecha = null;

    let chart = new CanvasJS.Chart("chartContainer", {
        title: {
          text: "Ocurrencias en fechas"
        },
        axisX: {
          valueFormatString: "MMM YYYY"
        },
        axisY2: {
          title: "Ocurrencias"
        },
        toolTip: {
          shared: true
        },
        legend: {
          cursor: "pointer",
          verticalAlign: "top",
          horizontalAlign: "center",
          dockInsidePlotArea: true
        },
        data: [{
          type:"line",
          axisYType: "secondary",
          name: "Sendic",
          showInLegend: true,
          markerSize: 0,
          dataPoints: [   
            { x: this.createDate(2014, 10, 13), y: 85 },
            { x: this.createDate(2014, 12, 11), y: 88 },
            { x: this.createDate(2017, 5, 21), y: 117 }
          ]
        },
        {
          type: "line",
          axisYType: "secondary",
          name: "Mujica",
          showInLegend: true,
          markerSize: 0,
          dataPoints: [
            { x: this.createDate(2014, 1, 1), y: 120 },
            { x: this.createDate(2014, 1, 12), y: 120 },
            { x: this.createDate(2017, 3, 15), y: 175 },
            { x: this.createDate(2017, 4, 1), y: 173 },
            { x: this.createDate(2017, 5, 1), y: 173 }
          ]
        },
        {
          type: "line",
          axisYType: "secondary",
          name: "Vazquez",
          showInLegend: true,
          markerSize: 0,
          dataPoints: [
            { x: this.createDate(2014, 1, 1), y: 40 },
            { x: this.createDate(2014, 1, 15), y: 41 },
            { x: this.createDate(2014, 2, 1), y: 41 },
            { x: this.createDate(2017, 5, 1), y: 67 }
          ]
        },
        {
          type: "line",
          axisYType: "secondary",
          name: "Lacalle",
          showInLegend: true,
          markerSize: 0,
          dataPoints: [
            { x: this.createDate(2014, 3, 1), y: 52 },
            { x: this.createDate(2014, 4, 1), y: 54 },
            { x: this.createDate(2017, 5, 1), y: 74 }
          ]
        }]
      });
    chart.render();
  }
  buscarTweets(){
    this.service.getTweetsCount(this.palabra).then((data:any)=>{
      this.results = data;
      var dat = data;
      debugger;
    });
  }

  createDate(year,month,day){
    let d = new Date();
    d.setFullYear(year,month,day);
    return d;
  } 

}
