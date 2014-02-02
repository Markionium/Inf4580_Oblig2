javac -cp "../../apache-jena-2.11.0/lib/*:." Simpsons.java 
java -cp "../../apache-jena-2.11.0/lib/*:." Simpsons simpsons.ttl output.ttl

cat output.ttl