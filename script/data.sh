curl -X PUT "localhost:9200/questions/_doc/2?refresh&pretty" -H 'Content-Type: application/json' -d'
{
      "question": "How important is the gender of your partner?",
      "category": "hard_fact",
      "question_type": {
        "type": "single_choice",
        "options": [
          "not important",
          "important",
          "very important"
        ]
      }
    }
'

curl -X PUT "localhost:9200/questions/_doc/3?refresh&pretty" -H 'Content-Type: application/json' -d'
{
      "question": "Do any children under the age of 18 live with you?",
      "category": "hard_fact",
      "question_type": {
        "type": "single_choice",
        "options": [
          "yes",
          "sometimes",
          "no"
        ]
      }
    }
'
curl -X PUT "localhost:9200/questions/_doc/4?refresh&pretty" -H 'Content-Type: application/json' -d'
    {
      "question": "How should your potential partner respond to this question?",
      "category": "lifestyle",
      "question_type": {
        "type": "single_choice",
        "options": [
          "yes",
          "sometimes",
          "no"
        ]
      }
    }
'
curl -X PUT "localhost:9200/questions/_doc/5?refresh&pretty" -H 'Content-Type: application/json' -d'
    {
      "question": "Could you imagine having children with your potential partner?",
      "category": "lifestyle",
      "question_type": {
        "type": "single_choice",
        "options": [
          "yes",
          "maybe",
          "no"
        ]
      }
    }
'
curl -X PUT "localhost:9200/questions/_doc/6?refresh&pretty" -H 'Content-Type: application/json' -d'
    {
      "question": "How should your potential partner respond to this question?",
      "category": "lifestyle",
      "question_type": {
        "type": "single_choice",
        "options": [
          "yes",
          "maybe",
          "no"
        ]
      }
    }
'
curl -X PUT "localhost:9200/questions/_doc/7?refresh&pretty" -H 'Content-Type: application/json' -d'
    {
      "question": "What is your marital status?",
      "category": "hard_fact",
      "question_type": {
        "type": "single_choice",
        "options": [
          "never married",
          "separated",
          "divorced",
          "widowed"
        ]
      }
    }
'
curl -X PUT "localhost:9200/questions/_doc/8?refresh&pretty" -H 'Content-Type: application/json' -d'
    {
      "question": "How often do your drink alcohol?",
      "category": "lifestyle",
      "question_type": {
        "type": "single_choice",
        "options": [
          "never",
          "once or twice a year",
          "once or twice a month",
          "once or twice a week",
          "Im drinking my 3rd mojito now, and its only 11am"
        ]
      }
    }
'
curl -X PUT "localhost:9200/questions/_doc/9?refresh&pretty" -H 'Content-Type: application/json' -d'
    {
      "question": "How often do you smoke?",
      "category": "lifestyle",
      "question_type": {
        "type": "single_choice",
        "options": [
          "never",
          "once or twice a year",
          "socially",
          "frequently"
        ]
      }
    }
'
curl -X PUT "localhost:9200/questions/_doc/10?refresh&pretty" -H 'Content-Type: application/json' -d'
    {
      "question": "What is your attitude towards drugs?",
      "category": "lifestyle",
      "question_type": {
        "type": "single_choice",
        "options": [
          "Im completely opposed",
          "Ive been know to dabble",
          "drugs enrich my life"
        ]
      }
    }
'
curl -X PUT "localhost:9200/questions/_doc/11?refresh&pretty" -H 'Content-Type: application/json' -d'
    {
      "question": "You are looking for...",
      "category": "lifestyle",
      "question_type": {
        "type": "single_choice",
        "options": [
          "friendship",
          "a hot date",
          "an affair",
          "a short-term relationship",
          "a long-term relationship"
        ]
      }
    }
'
curl -X PUT "localhost:9200/questions/_doc/12?refresh&pretty" -H 'Content-Type: application/json' -d'
    {
      "question": "Would you like to get married?",
      "category": "lifestyle",
      "question_type": {
        "type": "single_choice",
        "options": [
          "yes",
          "probably",
          "eventually",
          "no"
        ]
      }
    }
'
curl -X PUT "localhost:9200/questions/_doc/13?refresh&pretty" -H 'Content-Type: application/json' -d'
    {
      "question": "What is your ideal living arrangement?",
      "category": "lifestyle",
      "question_type": {
        "type": "single_choice",
        "options": [
          "cohabitation",
          "separate flat / room in the same building",
          "separate flats in the same area",
          "weekend-relationship",
          "long distance relationship"
        ]
      }
    }
'
curl -X PUT "localhost:9200/questions/_doc/14?refresh&pretty" -H 'Content-Type: application/json' -d'
    {
      "question": "Do you enjoy spending time alone?",
      "category": "introversion",
      "question_type": {
        "type": "single_choice",
        "options": [
          "most of the time",
          "often",
          "sometimes",
          "rarely",
          "not at all"
        ]
      }
    }
'
curl -X PUT "localhost:9200/questions/_doc/15?refresh&pretty" -H 'Content-Type: application/json' -d'
    {
      "question": "When youre alone, do you get lonely quickly?",
      "category": "introversion",
      "question_type": {
        "type": "single_choice",
        "options": [
          "most of the time",
          "often",
          "sometimes",
          "rarely",
          "not at all"
        ]
      }
    }
'
curl -X PUT "localhost:9200/questions/_doc/16?refresh&pretty" -H 'Content-Type: application/json' -d'
    {
      "question": "Do you enjoy going on holiday by yourself?",
      "category": "introversion",
      "question_type": {
        "type": "single_choice",
        "options": [
          "most of the time",
          "often",
          "sometimes",
          "rarely",
          "not at all"
        ]
      }
    }
'

curl -X PUT "localhost:9200/questions/_doc/17?refresh&pretty" -H 'Content-Type: application/json' -d'
    {
      "question": "I consciously take \"me time\"",
      "category": "introversion",
      "question_type": {
        "type": "single_choice",
        "options": [
          "most of the time",
          "often",
          "sometimes",
          "rarely",
          "not at all"
        ]
      }
    }
'

curl -X PUT "localhost:9200/questions/_doc/18?refresh&pretty" -H 'Content-Type: application/json' -d'
    {
      "question": "Should one keep little secrets from ones partner?",
      "category": "introversion",
      "question_type": {
        "type": "single_choice",
        "options": [
          "most of the time",
          "often",
          "sometimes",
          "rarely",
          "not at all"
        ]
      }
    }
'

curl -X PUT "localhost:9200/questions/_doc/19?refresh&pretty" -H 'Content-Type: application/json' -d'
    {
      "question": "How often do you think about sex?",
      "category": "passion",
      "question_type": {
        "type": "single_choice",
        "options": [
          "a few times a day",
          "daily",
          "a few times a week",
          "a few times a month",
          "rarely"
        ]
      }
    }
'
curl -X PUT "localhost:9200/questions/_doc/20?refresh&pretty" -H 'Content-Type: application/json' -d'
    {
      "question": "If you were alone on a desert island, how long would you last before pleasuring yourself?",
      "category": "passion",
      "question_type": {
        "type": "single_choice",
        "options": [
          "less than a day",
          "one day",
          "one week",
          "one month",
          "Id never do something like that"
        ]
      }
    }
'
curl -X PUT "localhost:9200/questions/_doc/21?refresh&pretty" -H 'Content-Type: application/json' -d'
    {
      "question": "How often would you like to have sex with your prospective partner?",
      "category": "passion",
      "question_type": {
        "type": "single_choice",
        "options": [
          "every day",
          "a few times a week",
          "once a week",
          "every two weeks",
          "infrequently",
          "rarely"
        ]
      }
    }
'
curl -X PUT "localhost:9200/questions/_doc/22?refresh&pretty" -H 'Content-Type: application/json' -d'
    {
      "question": "Do you like trying out new things in bed and experimenting?",
      "category": "passion",
      "question_type": {
        "type": "single_choice",
        "options": [
          "Yes, definitely!",
          "Now and then - why not?",
          "Id give it a try",
          "I dont know",
          "Absolutely not"
        ]
      }
    }
'
curl -X PUT "localhost:9200/questions/_doc/23?refresh&pretty" -H 'Content-Type: application/json' -d'
    {
      "question": "I can enjoy sex without love",
      "category": "passion",
      "question_type": {
        "type": "single_choice",
        "options": [
          "always",
          "often",
          "sometimes",
          "rarely",
          "never"
        ]
      }
    }
'
curl -X PUT "localhost:9200/questions/_doc/24?refresh&pretty" -H 'Content-Type: application/json' -d'
    {
      "question": "For me, a stable relationship is a prerequisite for really good sex",
      "category": "passion",
      "question_type": {
        "type": "single_choice",
        "options": [
          "always",
          "often",
          "sometimes",
          "rarely",
          "never"
        ]
      }
    }
'


curl -X PUT "localhost:9200/questions/_doc/25?refresh&pretty" -H 'Content-Type: application/json' -d'
{
      "question": "How important is the age of your partner to you?",
      "category": "hard_fact",
      "question_type": {
        "type": "single_choice_conditional",
        "options": [
          "not important",
          "important",
          "very important"
        ],
        "condition": {
          "predicate": {
            "exactEquals": [
              "${selection}",
              "very important"
            ]
          },
          "if_positive": {
            "question": "What age should your potential partner be?",
            "category": "hard_fact",
            "question_type": {
              "type": "number_range",
              "range": {
                "from": 18,
                "to": 140
              }
            }
          }
        }
      }
    }
'
