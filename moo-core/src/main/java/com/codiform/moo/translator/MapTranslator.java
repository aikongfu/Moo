package com.codiform.moo.translator;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codiform.moo.TranslationException;
import com.codiform.moo.UnsupportedTranslationException;
import com.codiform.moo.configuration.Configuration;
import com.codiform.moo.property.MapProperty;
import com.codiform.moo.property.source.SourcePropertyFactory;
import com.codiform.moo.session.TranslationSource;

public class MapTranslator {

	private Configuration configuration;
	private Logger log;
	private SourcePropertyFactory sourcePropertyFactory;

	/**
	 * Create a map translator.
	 * 
	 * @param configuration
	 *            configuration that may be relevant to the map translator
	 * @param the
	 *            source property factory used to retrieve source properties
	 */
	public MapTranslator( Configuration configuration, SourcePropertyFactory sourcePropertyFactory ) {
		this.configuration = configuration;
		this.sourcePropertyFactory = sourcePropertyFactory;
		this.log = LoggerFactory.getLogger( getClass() );
	}

	public Object translate( Object value, MapProperty property, TranslationSource translationSource ) {
		if ( shouldTranslate( property ) ) {
			return translateMap( value, property, translationSource );
		} else if ( shouldCopy( property ) ) {
			return copy( value, property, translationSource );
		} else {
			if ( isCompatible( value, property ) ) {
				return value;
			} else {
				return copy( value, property, translationSource );
			}
		}
	}

	private Object translateMap( Object value, MapProperty property, TranslationSource translationSource ) {
		if ( value instanceof Map ) {
			Object target = createTargetMap( value, property, translationSource );
			if ( target instanceof Map ) {
				throw new UnsupportedTranslationException(
						"Support for translating the contents of maps is not yet part of Moo (see GitHub issues #37)." );
			} else {
				throw new TranslationException( "Cannot translate map to target of type: " + target.getClass().getName() );
			}
		} else {
			throw new TranslationException( "Cannot translate map from type: " + value.getClass().getName() );
		}
	}

	@SuppressWarnings( "unchecked" )
	private Object copy( Object value, MapProperty property, TranslationSource translationSource ) {
		if ( value instanceof Map ) {
			Object target = createTargetMap( value, property, translationSource );
			if ( target instanceof Map ) {
				Map<Object, Object> targetMap = (Map<Object, Object>)target;
				targetMap.putAll( (Map<Object, Object>)value );
				return targetMap;
			} else {
				throw new TranslationException( "Cannot translate Map to target of type: " + target.getClass().getName() );
			}
		} else {
			throw new TranslationException( "Cannot translate collection from type: " + value.getClass().getName() );
		}
	}

	private Object createTargetMap( Object value, MapProperty property, TranslationSource cache ) {
		Class<? extends TranslationTargetFactory> factoryType = property.getFactory();
		TranslationTargetFactory factory = cache.getTranslationTargetFactory( factoryType );
		Object targetMap = factory.getTranslationTargetInstance( value, property.getType() );
		log.trace( "Target factory type {} created target map of type {} for source {}", factoryType, targetMap.getClass(), value );
		return targetMap;
	}

	private boolean isCompatible( Object value, MapProperty property ) {
		return property.getType().isInstance( value );
	}

	private boolean shouldCopy( MapProperty property ) {
		return !hasDefaultFactory( property ) || configuration.isPerformingDefensiveCopies();
	}

	private boolean hasDefaultFactory( MapProperty property ) {
		return property.getFactory() == DefaultMapTargetFactory.class;
	}

	//		if ( property.shouldItemsBeTranslated() ) {
	//			Object target = createTargetCollection( value, property, cache );
	//			translateToTargetCollection( value, target, property, cache );
	//			return target;
	//		} else if ( !hasDefaultFactory( property ) || configuration.isPerformingDefensiveCopies() ) {
	//			Object target = createTargetCollection( value, property, cache );
	//			copyToTargetCollection( value, target, property );
	//			return target;
	//		} else {
	//			Class<?> targetClass = property.getType();
	//			if ( targetClass.isInstance( value ) ) {
	//				return value;
	//			} else {
	//				Object target = createTargetCollection( value, property, cache );
	//				copyToTargetCollection( value, target, property );
	//				return target;
	//			}
	//		}
	private boolean shouldTranslate( MapProperty property ) {
		// TODO: Field and Value Translation
		return false;
	}

	@SuppressWarnings( "unchecked" )
	public void updateMap( Object source, Map<Object, Object> destinationMap, TranslationSource translationSource, MapProperty property ) {
		if ( source instanceof Map ) {
			Map<Object, Object> sourceMap = (Map<Object, Object>)source;
			updateMapByKey( sourceMap, destinationMap, translationSource, property );
		} else {
			throw new UnsupportedTranslationException( "Cannot update Map from " + source.getClass().getName() );
		}
	}

	private void updateMapByKey( Map<Object, Object> sourceMap, Map<Object, Object> destinationMap, TranslationSource translationSource,
			MapProperty property ) {
		for ( Map.Entry<Object, Object> item : sourceMap.entrySet() ) {
			Object destinationValue = destinationMap.get( item.getKey() );
			Object sourceValue = item.getValue();
			if ( destinationValue != null && sourceValue != null ) {
				translationSource.update( sourceValue, destinationValue );
			} else if ( property.getValueClass() != null && sourceValue != null ) {
				destinationMap.put( item.getKey(), translationSource.getTranslation( sourceValue, property.getValueClass() ) );
			} else {
				destinationMap.put( item.getKey(), sourceValue );
			}
		}

		if ( property.shouldRemoveOrphans() ) {
			removeOrphans( sourceMap, destinationMap );
		}
	}

	private void removeOrphans( Map<Object, Object> sourceMap, Map<Object, Object> destinationMap ) {
		Set<Object> toRemove = new HashSet<Object>( destinationMap.keySet() );
		toRemove.removeAll( sourceMap.keySet() );
		for ( Object key : toRemove ) {
			destinationMap.remove( key );
		}
	}

}