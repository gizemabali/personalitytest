import { Component, OnInit } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import {NgForm} from '@angular/forms';
import { FormsModule }   from '@angular/forms';
import { InfoService }   from 'src/app/providers/info/info.service';

@Component({
  selector: 'app-questions',
  templateUrl: './questions.component.html',
  styleUrls: ['./questions.component.css']
})
export class QuestionsComponent implements OnInit {

    title = 'personaltesting';
    statusMessage = "could not save the answers!";
    displayValue = false;
    statusData = {"response":""};
  
    categories = [];

    
    ngOnInit(): void {
        

    const headers = new HttpHeaders().set("Access-Control-Allow-Origin", "*").set("Access-Control-Allow-Origin", this.infoService.host+"/category/lifestyle/questions");
   
        
    this.http.get<any>(this.infoService.host+'/category', {headers}).subscribe(data => {
         console.log(data);
        this.categories = data["categories"];})
    
  }
  
  constructor(private http: HttpClient, public infoService: InfoService) {

  }
  
 onSubmit(f: NgForm) {
    console.log("values: " + f);
    const headers = {'Content-Type': 'application/json' };
    this.http.post(this.infoService.host+'/answers/save/' +this.infoService.category, f, {headers, responseType: 'text'}).subscribe(data => {
        console.log("data post: " + data);
        this.displayValue = true;
        this.statusData = JSON.parse(data);
     });
  }
  

}
