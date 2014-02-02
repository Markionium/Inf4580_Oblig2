
JAVA_CP = "../../apache-jena-2.11.0/lib/*:."
SIMPSONS_FILE = http://sws.ifi.uio.no/inf3580/v14/oblig/2/simpsons.ttl

java = java -cp $(JAVA_CP)
javac = javac -cp $(JAVA_CP)

%.class: %.java
	@$(javac) $<

output.ttl: Simpsons.class
	@$(java) $(basename $(<F)) $(SIMPSONS_FILE) $@

run_test: Test.class
	@$(java) $(basename $(<F)) $(SIMPSONS_FILE) $@
