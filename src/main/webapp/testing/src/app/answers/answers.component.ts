import { Component, OnInit } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import {NgForm} from '@angular/forms';
import { FormsModule }   from '@angular/forms';
import { InfoService }   from 'src/app/providers/info/info.service';

@Component({
  selector: 'app-answers',
  templateUrl: './answers.component.html',
  styleUrls: ['./answers.component.css']
})
export class AnswersComponent implements OnInit {

  constructor(private http: HttpClient, public infoService: InfoService) {
   }
   
   answers = "";

  ngOnInit(): void {
   
  }

 onSubmit(f: NgForm) {
    console.log(f.value); 
    console.log(f.valid);  
    const headers = new HttpHeaders().set("Access-Control-Allow-Origin", "*").set("Access-Control-Allow-Origin", this.infoService.host+"/category/lifestyle/questions");
    this.http.get<any>(this.infoService.host+'/answers/'+f.value.nickname, {headers}).subscribe(data => {
         console.log(data);
         this.answers = data;
 })
   
  }
}
