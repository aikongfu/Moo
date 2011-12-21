package com.codiform.moo.translator;

import java.lang.annotation.Annotation;

/**
 * Represents a destination into which a value can be stored.
 */
public interface Property {

	/**
	 * Gets the name of the property this represents. Property names are
	 * determined through the use of reflection.
	 * 
	 * @return the name
	 */
	String getName();

	/**
	 * Sets the value of the property on an object instance.
	 * 
	 * @param instance
	 *            the object instance on which the value is to be set
	 * @param value
	 *            the value to be set
	 */
	void setValue(Object instance, Object value);

	/**
	 * Gets the type that can be stored in this property.
	 * 
	 * @return the type
	 */
	Class<?> getType();
	
	/**
	 * Gets the class in which this property is declared.
	 */
	Class<?> getDeclaringClass();

	/**
	 * Gets the translation expression used to find this property on another
	 * object.
	 * 
	 * @return the expression
	 */
	String getTranslationExpression();

	/**
	 * Indicates if the property should be translated (has translate=true in the {@link Property} annotation)
	 * 
	 * @return boolean if the property should be translated
	 */
	boolean shouldBeTranslated();

	/**
	 * Gets the annotation of a specified type applied to this property, if
	 * present.
	 * 
	 * @param <A>
	 *            links the requested annotation class to the return value
	 * @param annotationClass
	 *            the class of the annotation requested
	 * @return the annotation, if present, and null otherwise
	 */
	<A extends Annotation> A getAnnotation(Class<A> annotationClass);

	/**
	 * Indicates if the property supports a null value (e.g. can store and retrieve one).
	 * 
	 * @return true if a null value is supported; false otherwise
	 */
	boolean canSupportNull();
	
	/**
	 * Indicates if the property was explicitly defined using @Property or if it was implicitly 
	 * defined by the presence of a field or method matching the access mode and other criteria.
	 * 
	 * @return true if the property was explicitly defined
	 */
	boolean isExplicit();
	
	/**
	 * Indicates if the property should be ignored (no value stored during Translate or Update).
	 * 
	 * @return true if the property should be ignored
	 */
	boolean isIgnored();

	/**
	 * Indicates if the property should be updated rather than replaced when an update is taking 
	 * place. In particular, this implies that an update can succeed because the destination is 
	 * mutable and the important properties can be copied from one side to the other.
	 * 
	 * @return true if the object should be updated, assuming it can be
	 */
	boolean shouldUpdate();

	/**
	 * Indicates if the property can be used to retrieve a value, or if the value can only be set.
	 * 
	 * @return true if the value can be retrieved, false if the value can only be set
	 */
	boolean canGetValue();

	/**
	 * Retrieves the value represented by the property from an object instance.
	 * 
	 * @param the instance from which the value should be retrieved
	 * @return the current value of the property
	 * @throws UnsupportedOperationException if the property's value can not be retrieved
	 * @see #canGetValue() 
	 */
	Object getValue( Object instance );
}
