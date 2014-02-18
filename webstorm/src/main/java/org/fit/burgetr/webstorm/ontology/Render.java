/* CVS $Id: $ */
package org.fit.burgetr.webstorm.ontology; 
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.ontology.*;
 
/**
 * Vocabulary definitions from /home/burgetr/local/rdf/render.owl 
 * @author Auto-generated by schemagen on 13 II 2014 14:21 
 */
public class Render {
    /** <p>The ontology model that holds the vocabulary terms</p> */
    private static OntModel m_model = ModelFactory.createOntologyModel( OntModelSpec.OWL_MEM, null );
    
    /** <p>The namespace of the vocabulary as a string</p> */
    public static final String NS = "http://www.fit.vutbr.cz/~imilicka/public/ontology/render.owl#";
    
    /** <p>The namespace of the vocabulary as a string</p>
     *  @see #NS */
    public static String getURI() {return NS;}
    
    /** <p>The namespace of the vocabulary as a resource</p> */
    public static final Resource NAMESPACE = m_model.createResource( NS );
    
    public static final ObjectProperty belongsTo = m_model.createObjectProperty( "http://www.fit.vutbr.cz/~imilicka/public/ontology/render.owl#belongsTo" );
    
    public static final ObjectProperty containsImage = m_model.createObjectProperty( "http://www.fit.vutbr.cz/~imilicka/public/ontology/render.owl#containsImage" );
    
    public static final ObjectProperty containsObject = m_model.createObjectProperty( "http://www.fit.vutbr.cz/~imilicka/public/ontology/render.owl#containsObject" );
    
    public static final ObjectProperty downOf = m_model.createObjectProperty( "http://www.fit.vutbr.cz/~imilicka/public/ontology/render.owl#downOf" );
    
    public static final ObjectProperty hasBottomBorder = m_model.createObjectProperty( "http://www.fit.vutbr.cz/~imilicka/public/ontology/render.owl#hasBottomBorder" );
    
    public static final ObjectProperty hasLeftBorder = m_model.createObjectProperty( "http://www.fit.vutbr.cz/~imilicka/public/ontology/render.owl#hasLeftBorder" );
    
    public static final ObjectProperty hasRightBorder = m_model.createObjectProperty( "http://www.fit.vutbr.cz/~imilicka/public/ontology/render.owl#hasRightBorder" );
    
    public static final ObjectProperty hasTopBorder = m_model.createObjectProperty( "http://www.fit.vutbr.cz/~imilicka/public/ontology/render.owl#hasTopBorder" );
    
    public static final ObjectProperty isChildOf = m_model.createObjectProperty( "http://www.fit.vutbr.cz/~imilicka/public/ontology/render.owl#isChildOf" );
    
    public static final ObjectProperty leftOf = m_model.createObjectProperty( "http://www.fit.vutbr.cz/~imilicka/public/ontology/render.owl#leftOf" );
    
    public static final ObjectProperty rightOf = m_model.createObjectProperty( "http://www.fit.vutbr.cz/~imilicka/public/ontology/render.owl#rightOf" );
    
    public static final ObjectProperty topOf = m_model.createObjectProperty( "http://www.fit.vutbr.cz/~imilicka/public/ontology/render.owl#topOf" );
    
    public static final DatatypeProperty backgroundColor = m_model.createDatatypeProperty( "http://www.fit.vutbr.cz/~imilicka/public/ontology/render.owl#backgroundColor" );
    
    public static final DatatypeProperty color = m_model.createDatatypeProperty( "http://www.fit.vutbr.cz/~imilicka/public/ontology/render.owl#color" );
    
    public static final DatatypeProperty fontDecoration = m_model.createDatatypeProperty( "http://www.fit.vutbr.cz/~imilicka/public/ontology/render.owl#fontDecoration" );
    
    public static final DatatypeProperty fontFamily = m_model.createDatatypeProperty( "http://www.fit.vutbr.cz/~imilicka/public/ontology/render.owl#fontFamily" );
    
    public static final DatatypeProperty fontSize = m_model.createDatatypeProperty( "http://www.fit.vutbr.cz/~imilicka/public/ontology/render.owl#fontSize" );
    
    public static final DatatypeProperty fontStyle = m_model.createDatatypeProperty( "http://www.fit.vutbr.cz/~imilicka/public/ontology/render.owl#fontStyle" );
    
    public static final DatatypeProperty fontVariant = m_model.createDatatypeProperty( "http://www.fit.vutbr.cz/~imilicka/public/ontology/render.owl#fontVariant" );
    
    public static final DatatypeProperty fontWeight = m_model.createDatatypeProperty( "http://www.fit.vutbr.cz/~imilicka/public/ontology/render.owl#fontWeight" );
    
    public static final DatatypeProperty hasText = m_model.createDatatypeProperty( "http://www.fit.vutbr.cz/~imilicka/public/ontology/render.owl#hasText" );
    
    public static final DatatypeProperty height = m_model.createDatatypeProperty( "http://www.fit.vutbr.cz/~imilicka/public/ontology/render.owl#height" );
    
    public static final DatatypeProperty imageUrl = m_model.createDatatypeProperty( "http://www.fit.vutbr.cz/~imilicka/public/ontology/render.owl#imageUrl" );
    
    public static final DatatypeProperty objectInformation = m_model.createDatatypeProperty( "http://www.fit.vutbr.cz/~imilicka/public/ontology/render.owl#objectInformation" );
    
    public static final DatatypeProperty positionX = m_model.createDatatypeProperty( "http://www.fit.vutbr.cz/~imilicka/public/ontology/render.owl#positionX" );
    
    public static final DatatypeProperty positionY = m_model.createDatatypeProperty( "http://www.fit.vutbr.cz/~imilicka/public/ontology/render.owl#positionY" );
    
    public static final DatatypeProperty sourceUrl = m_model.createDatatypeProperty( "http://www.fit.vutbr.cz/~imilicka/public/ontology/render.owl#sourceUrl" );
    
    public static final DatatypeProperty width = m_model.createDatatypeProperty( "http://www.fit.vutbr.cz/~imilicka/public/ontology/render.owl#width" );
    
    public static final OntClass Border = m_model.createClass( "http://www.fit.vutbr.cz/~imilicka/public/ontology/render.owl#Border" );
    
    public static final OntClass Box = m_model.createClass( "http://www.fit.vutbr.cz/~imilicka/public/ontology/render.owl#Box" );
    
    public static final OntClass CommonObject = m_model.createClass( "http://www.fit.vutbr.cz/~imilicka/public/ontology/render.owl#CommonObject" );
    
    public static final OntClass ContainerBox = m_model.createClass( "http://www.fit.vutbr.cz/~imilicka/public/ontology/render.owl#ContainerBox" );
    
    public static final OntClass ContentBox = m_model.createClass( "http://www.fit.vutbr.cz/~imilicka/public/ontology/render.owl#ContentBox" );
    
    public static final OntClass Image = m_model.createClass( "http://www.fit.vutbr.cz/~imilicka/public/ontology/render.owl#Image" );
    
    public static final OntClass Page = m_model.createClass( "http://www.fit.vutbr.cz/~imilicka/public/ontology/render.owl#Page" );
    
    public static final OntClass Rectangle = m_model.createClass( "http://www.fit.vutbr.cz/~imilicka/public/ontology/render.owl#Rectangle" );
    
}
