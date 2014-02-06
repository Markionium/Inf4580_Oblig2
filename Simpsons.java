import java.io.PrintWriter;
import java.util.Iterator;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.util.FileUtils;
import com.hp.hpl.jena.vocabulary.*;
import com.hp.hpl.jena.sparql.vocabulary.FOAF;

public class Simpsons {
	private Model model;
	
	//Prefixes
	String simpsonPrefix, familyPrefix;
	
	public static Simpsons create() {
		return new Simpsons();
	}
	
	private Simpsons() {
		model = ModelFactory.createDefaultModel();
	}
	
	/**
	 * Read a file or url and add the data to the model. Also gets the 
	 * sim and fam prefix from the model
	 * 
	 * @param inputFile
	 * @return
	 */
	public Simpsons readFile(String inputFile) {
		model.read( inputFile, FileUtils.guessLang(inputFile) );
		
		simpsonPrefix = model.getNsPrefixURI("sim");
		familyPrefix = model.getNsPrefixURI("fam");
		
		return this;
	}
	
	/**
	 * Add information about the simpsons to the model
	 * @return
	 */
	private Simpsons addInformation() {
		addPerson("Maggie Simpson", 1);
		Resource mona = addPerson("Mona Simpson", 70);
		Resource abraham = addPerson("Abraham Simpson", 78);
		Resource herb = addPerson("Herb Simpson");
		
		createMarraige(abraham, mona);
		
		addFatherTo(model.createResource(), herb);
		
		return this;
	}
	
	/**
	 * Create a maraige between two spouses 
	 * 
	 * @param spouseOne
	 * @param spouseTwo
	 */
	private void createMarraige(Resource spouseOne, Resource spouseTwo) {
		Property spouse = model.createProperty( prefix("hasSpouse", familyPrefix) ); 
		spouseOne.addProperty(spouse, spouseTwo);
		spouseTwo.addProperty(spouse, spouseOne);
	}
	
	/**
	 * Add a person with an age
	 * 
	 * @param fullName
	 * @param age
	 * @return
	 */
	private Resource addPerson(String fullName, Integer age) {
		//Create the Simpson
		Resource simpson = addPerson(fullName);
		
		//Add age to the Simpson
		Property ageProperty = model.createProperty( prefix("age", model.getNsPrefixURI("foaf")) );
		simpson.addProperty(ageProperty, age.toString(), XSDDatatype.XSDint);
		
		return simpson;
	}
	
	/**
	 * Add a father to a child
	 * 
	 * @param father
	 * @param child
	 */
	private void addFatherTo(Resource father, Resource child) {
		Property fatherProp = model.createProperty( prefix("hasFather", familyPrefix) );
		child.addProperty(fatherProp, father);
	}
	
	/**
	 * Adds a person to the model
	 * 
	 * @param fullName
	 * @return Simpsons
	 */
	
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
	
	/**
	 * Writes the model to the given outputFile
	 * 
	 * @param outputFile
	 * @return Simpsons
	 */
	public Simpsons writeFile(String outputFile) {
		try (PrintWriter pw = new PrintWriter(outputFile)) {
			model.write( pw, FileUtils.guessLang(outputFile) );
		} catch (Exception e) {
			System.out.printf("Something went wrong while trying to "
					+ " write to the file: %s", e.getMessage());
		}
		return this;
	}
	
	private String prefix(String name, String prefix) {
		return prefix + name;
	}
	
	/**
	 * Loops through the Simpsons in the model and sets the types based on their age
	 * 
	 * @return Simpsons
	 */
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
	
	/**
	 * Takes the simpson resource and the age of the Simpson and sets the types that 
	 * apply to this person based on their age. 
	 * Infant, Minor or Old 
	 * 
	 * @param simpson
	 * @param age
	 */
	private void setTypesForAge(Resource simpson, Integer age) {
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
		} catch (Exception e) {
			System.out.println("Please supply a input and output file.");
			return;
		}
		
		simpsons = Simpsons.create();
		simpsons.readFile(inputFile)
				.addInformation()
				.setTypesBasedOnAge()
				.writeFile(outputFile);
	}
}
