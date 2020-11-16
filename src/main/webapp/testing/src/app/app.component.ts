import { Component, OnInit } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import {NgForm} from '@angular/forms';
import { FormsModule }   from '@angular/forms';
import { InfoService }   from './providers/info/info.service';
import { ReactiveFormsModule } from '@angular/forms';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent implements OnInit {
    title = 'personaltesting';
  
  
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
    
    const headers = {'Content-Type': 'application/json' };
    const body = { title: 'Angular POST Request Example' };
    this.http.post(this.infoService.host+'/answers/', f.value, {headers, responseType: 'text'}).subscribe(data => {console.log(data);});
  }
  
}
