import { Injectable } from '@angular/core';
import { HttpClientModule } from '@angular/common/http';
import { HttpClient, HttpHeaders } from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})

export class InfoService {
  questions = [];
  category = 'lifestyle';
  host = 'http://127.0.0.1:8080'
  
  public refreshQuestions(cat){
    this.category = cat;
    console.log("category: " + cat);
    this.getQuestionsFromServer(cat)
  }
        
  constructor(private http: HttpClient) { 
     this.getQuestionsFromServer(this.category)
  }
  
  public getQuestionsFromServer(category) {
     const headers = new HttpHeaders().set("Access-Control-Allow-Origin", "*").set("Access-Control-Allow-Origin", "http://127.0.0.1:8080/category/lifestyle/questions");
 
     this.http.get<any>('http://127.0.0.1:8080/category/'+category+'/questions', {headers}).subscribe(data => {
         console.log(data);
        this.questions = data;
        })
  }
}
