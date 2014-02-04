import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;

import com.hp.hpl.jena.vocabulary.*;
import com.hp.hpl.jena.sparql.vocabulary.FOAF;

public class Simpsons {
	private String defaultSyntax = "TURTLE";
	private Model model;
	
	//Prefixes
	String rdfPrefix, foafPrefix, simpsonPrefix, familyPrefix;
	
	@SuppressWarnings("serial")
	private static final Map<String, String> jenaReadFlags = new HashMap<String, String>() {
		{
			//"RDF/XML", "N-TRIPLE", "TURTLE" (or "TTL") and "N3"
			put("ttl", "TURTLE");
			put("rdf", "RDF/XML");
			put("n3", "N-TRIPLE");
		}
	};
	
	public static Simpsons create() {
		return new Simpsons();
	}
	
	private Simpsons() {
		model = ModelFactory.createDefaultModel();
	}
	
	private String getSyntaxFromFilename(String fileName) {
		String[] fileNameParts = fileName.split("\\.");
		String extension = fileNameParts[fileNameParts.length - 1];
		
		//Attempt to get the extension
		String type = jenaReadFlags.get(extension);
		
		//return the default type when no type can be found
		if (type == null) {
			return defaultSyntax;
		}
		return type;
	}
	
	public Simpsons readFile(String inputFile) {
		model.read(inputFile, getSyntaxFromFilename(inputFile));
		
		simpsonPrefix = model.getNsPrefixURI("sim");
		familyPrefix = model.getNsPrefixURI("fam");
		
		return this;
	}
	
	private Simpsons addInformation() {
		addPerson("Maggie Simpson", 1);
		Resource mona = addPerson("Mona Simpson", 70);
		Resource abraham = addPerson("Abraham Simpson", 78);
		Resource herb = addPerson("Herb Simpson");
		
		createMarraige(abraham, mona);
		
		addFatherTo(model.createResource(), herb);
		
		return this;
	}
	
	private void createMarraige(Resource spouseOne, Resource spouseTwo) {
		Property spouse = model.createProperty( prefix("hasSpouse", familyPrefix) ); 
		spouseOne.addProperty(spouse, spouseTwo);
		spouseTwo.addProperty(spouse, spouseOne);
	}

	private Resource addPerson(String fullName, Integer age) {
		//Create the Simpson
		Resource simpson = addPerson(fullName);
		
		//Add age to the Simpson
		Property ageProperty = model.createProperty( prefix("age", model.getNsPrefixURI("foaf")) );
		simpson.addProperty(ageProperty, age.toString(), XSDDatatype.XSDint);
		
		return simpson;
	}
	
	private void addFatherTo(Resource father, Resource child) {
		Property fatherProp = model.createProperty( prefix("hasFather", familyPrefix) );
		child.addProperty(fatherProp, father);
	}
	
	private Resource addPerson(String fullName) {
		//Take the first name as the name for the RDF identifier
		String name = fullName.split(" ")[0];
		
		//Setup resources and properties
		Resource simpson = model.createResource( prefix(name, simpsonPrefix) );
		
		//Hook up the Simpson from the pieces :)
		simpson.addProperty(RDF.type, FOAF.Person);
		simpson.addProperty(FOAF.name, fullName);
		
		return simpson;
	}
	
	public Simpsons writeFile(String outputFile) {
		FileWriter fw;
		try {
			fw = new FileWriter(outputFile);
		} catch (IOException e) {
			System.out.println("Can not open file for writing");
			return this;
		}
		model.write(fw, getSyntaxFromFilename(outputFile));
		return this;
	}
	
	private String prefix(String name, String prefix) {
		return prefix + name;
	}
	
	private Simpsons setTypesBasedOnAge() {
		Property ageProperty = model.createProperty( prefix("age", model.getNsPrefixURI("foaf")) );
		
		//Get all statements for where the subject has an age
		Iterator<Statement> statements = model.listStatements((Resource) null, ageProperty, (Resource) null);
		
		while(statements.hasNext()) {
			Statement statement = statements.next();
			Literal ageLiteral = (Literal) statement.getObject();
			Integer age = ageLiteral.getInt();
			Resource simpson = (Resource) statement.getSubject();
			
			//Check for minors
			setTypesForAge(simpson, age);
		}
		
		return this;
	}

	private void setTypesForAge(Resource simpson, Integer age) {
		System.out.println(age);
		Resource infant = model.createResource( prefix("Infant", familyPrefix) );
		Resource minor = model.createResource( prefix("Minor", familyPrefix) );
		Resource old = model.createResource( prefix("Old", familyPrefix) );
		
		//Check for minors and infants
		if (age < 18) {
			simpson.addProperty(RDF.type, minor);
			
			//If under two it's also an infant
			if (age < 2) {
				simpson.addProperty(RDF.type, infant);
			}
		}
		
		//Check for old people
		if (age > 70) {
			simpson.addProperty(RDF.type, old);
		}
	}
	
	/**
	 * Main method that executes the program
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		String inputFile, outputFile;
		Simpsons simpsons;
		
		//Check for valid file names
		try {
			inputFile = args[0];
			outputFile = args[1];
		} catch (ArrayIndexOutOfBoundsException e) {
			System.out.println("Please give a input and output file.");
			return;
		}
		
		simpsons = Simpsons.create();
		simpsons.readFile(inputFile)
				.addInformation()
				.setTypesBasedOnAge()
				.writeFile(outputFile);
	}
}
