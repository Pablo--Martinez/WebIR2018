Elastic Search

Obtener la imagen de docker
  docker pull docker.elastic.co/elasticsearch/elasticsearch:6.4.1
  
Correr el container con ES
  docker run -p 9200:9200 -p 9300:9300 -e "discovery.type=single-node" docker.elastic.co/elasticsearch/elasticsearch:6.4.1
