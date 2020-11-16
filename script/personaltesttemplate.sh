
curl -X PUT "localhost:9200/_template/template_answers?pretty" -H 'Content-Type: application/json' -d'
{"index_patterns":["answer*"],"settings":{"number_of_shards":1},"mappings":{"_source":{"enabled":true},"properties":{"nickname":{"type":"keyword"},"date":{"type":"date","format":"yyyy-MM-dd HH:mm:ss"},"answers":{"type":"nested","properties":{"question":{"type":"keyword"},"answer":{"type":"keyword"}}}}}}
'

curl -X PUT "localhost:9200/_template/template_question?pretty" -H 'Content-Type: application/json' -d'
{"template_question":{"order":0,"index_patterns":["question*"],"settings":{},"mappings":{"_source":{"enabled":true},"properties":{"question":{"type":"keyword"},"question_type":{"type":"nested","properties":{"options":{"type":"keyword"},"type":{"type":"keyword"}}},"category":{"type":"keyword"}}},"aliases":{}}}
'



