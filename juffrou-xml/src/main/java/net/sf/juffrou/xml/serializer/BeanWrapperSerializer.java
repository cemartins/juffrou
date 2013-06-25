package net.sf.juffrou.xml.serializer;

import java.util.Collection;

import net.sf.juffrou.util.reflect.BeanWrapper;
import net.sf.juffrou.xml.error.UnknownXmlElementException;
import net.sf.juffrou.xml.internal.JuffrouBeanMetadata;
import net.sf.juffrou.xml.internal.binding.BeanClassBinding;
import net.sf.juffrou.xml.internal.binding.BeanPropertyBinding;
import net.sf.juffrou.xml.internal.io.JuffrouReader;
import net.sf.juffrou.xml.internal.io.JuffrouWriter;

/**
 * Main serializer / deserializer class.<p>
 * Handles a java bean beans, and calls specific serializers for each of its properties.
 * @author cemartins
 */
public class BeanWrapperSerializer implements Serializer {

	private final JuffrouBeanMetadata xmlBeanMetadata;
	
	public BeanWrapperSerializer(JuffrouBeanMetadata xmlBeanMetadata) {
		this.xmlBeanMetadata = xmlBeanMetadata;
	}
	
	@Override
	public void serialize(JuffrouWriter writer, BeanWrapper valueOwner, String valuePropertyName) {
		BeanWrapper nestedWrapper = valueOwner.getNestedWrapper(valuePropertyName);
		serializeBeanProperties(writer, nestedWrapper);
	}

	@Override
	public void deserialize(JuffrouReader reader, BeanWrapper valueOwner, String valuePropertyName) {
		BeanWrapper nestedWrapper = valueOwner.getNestedWrapper(valuePropertyName);
		deserializeBeanProperties(reader, nestedWrapper);
	}
	
	public void serializeBeanProperties(JuffrouWriter writer, BeanWrapper bw) {
		BeanClassBinding beanClassBinding = (BeanClassBinding) bw.getContext();
		if(beanClassBinding.isEmpty())
			beanClassBinding.setAllBeanPropertiesToMarshall();
		Collection<BeanPropertyBinding> propertiesToMarshall = beanClassBinding.getPropertyBindings();
		for(BeanPropertyBinding beanPropertyBinding : propertiesToMarshall) {
			Object value = bw.getValue(beanPropertyBinding.getBeanPropertyName());
			if(value != null) {
				writer.startNode(beanPropertyBinding.getXmlElementName(), beanPropertyBinding.getNodeType());
				Serializer converter = beanPropertyBinding.getSerializer();
				if(converter == null)
					converter = xmlBeanMetadata.getSerializerForClass(beanPropertyBinding.getPropertyType());
				if(converter == null)
					serialize(writer, bw, beanPropertyBinding.getBeanPropertyName());
				else
					converter.serialize(writer, bw, beanPropertyBinding.getBeanPropertyName());
				writer.endNode();
			}
		}
	}

	public void deserializeBeanProperties(JuffrouReader reader, BeanWrapper instance) {
		BeanClassBinding beanClassBinding = (BeanClassBinding) instance.getContext();
		if(beanClassBinding.isEmpty())
			beanClassBinding.setAllBeanPropertiesToMarshall();
		String xmlElementName = reader.enterNode();
		while(xmlElementName != null) {
			deserializeElement(reader, instance, beanClassBinding, xmlElementName);
			xmlElementName = reader.next();
		}
		reader.exitNode();
	}
	
	public void deserializeElement(JuffrouReader reader, BeanWrapper instance, BeanClassBinding beanClassBinding, String xmlElementName) {
		BeanPropertyBinding beanPropertyBinding = beanClassBinding.getBeanPropertyBindingFromXmlElement(xmlElementName);
		if(beanPropertyBinding == null)
			throw new UnknownXmlElementException("I do not know the element " + xmlElementName + " of the class " + instance.getBeanClass().getSimpleName());
		Serializer converter = beanPropertyBinding.getSerializer();
		if(converter == null)
			converter = xmlBeanMetadata.getSerializerForClass(beanPropertyBinding.getPropertyType());
		if(converter == null)
			// treat it as a bean
			deserialize(reader, instance, beanPropertyBinding.getBeanPropertyName());
		else
			converter.deserialize(reader, instance, beanPropertyBinding.getBeanPropertyName());
	}

}
