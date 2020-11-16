import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { PartyComponent } from './party/party.component';
import { ReactiveFormsModule } from '@angular/forms';
import { HttpClientModule } from '@angular/common/http';

import {NgForm} from '@angular/forms';
import { FormsModule }   from '@angular/forms';
import { InfoService }   from './providers/info/info.service';
import { AnswersComponent } from './answers/answers.component';
import { AboutComponent } from './about/about.component';
import { RouterModule, Routes } from '@angular/router';
import { CommonModule } from '@angular/common';
import { QuestionsComponent } from './questions/questions.component';

const routes: Routes = [
  { path: 'about', component: AboutComponent },
  { path: 'questions', component: QuestionsComponent },
  { path: '', redirectTo: '/questions', pathMatch: 'full'},
  { path: 'answers', component: AnswersComponent }
];

@NgModule({
  declarations: [
    AppComponent,
    PartyComponent,
    AnswersComponent,
    AboutComponent,
    QuestionsComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    HttpClientModule,
    FormsModule,
     CommonModule,
     ReactiveFormsModule,
    RouterModule.forRoot(routes)
  ],
  exports: [RouterModule],
  providers: [InfoService],
  bootstrap: [AppComponent]
})
export class AppModule { }
