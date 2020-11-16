import { Component, OnInit } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import {NgForm} from '@angular/forms';
import { FormsModule }   from '@angular/forms';
import { InfoService }   from 'src/app/providers/info/info.service';

@Component({
  selector: 'app-party',
  templateUrl: './party.component.html',
  styleUrls: ['./party.component.css']
})
export class PartyComponent implements OnInit {

    statusMessage = "could not save the answers!";
    title = 'personaltesting';
  
    questions = [];
    categories = [];
    
    ngOnInit(): void {

    const headers = new HttpHeaders().set("Access-Control-Allow-Origin", "*").set("Access-Control-Allow-Origin", this.infoService.host+"/category/lifestyle/questions");
    this.http.get<any>(this.infoService.host+'/category/lifestyle/questions', {headers}).subscribe(data => {
        console.log(data);
        this.questions = data;})
        
    this.http.get<any>(this.infoService.host+'/category', {headers}).subscribe(data => {
        this.categories = data["categories"];})
    
  }
  
   public refreshQuestions(cat){
    this.statusMessage = cat;

  }
  
  constructor(private http: HttpClient, public infoService: InfoService) {

  }
  
 onSubmit(f: NgForm) {

    const headers = {'Content-Type': 'application/json' };
 
 	console.log(f.value);
 	console.log(f.value.category);
 	this.infoService.category =  f.value.category;
 	this.infoService.refreshQuestions(f.value.category);

    this.http.post(this.infoService.host+'/answers/', f.value, {headers, responseType: 'text'}).subscribe(data => {
        console.log("data: " + data);
        this.refreshQuestions("saved");
    });
  }
  
}
