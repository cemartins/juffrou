package org.juffrou.xml.internal;

import org.juffrou.xml.converter.Converter;
import org.juffrou.xml.internal.binding.BeanClassBinding;
import org.juffrou.xml.internal.io.JuffrouWriter;

public class JuffrouMarshaller {
	
	private JuffrouBeanMetadata xmlBeanMetadata;
	
	public JuffrouMarshaller() {
		this.xmlBeanMetadata = new JuffrouBeanMetadata();
	}

	public void marshallBean(JuffrouWriter writer, Object bean) {
		BeanClassBinding beanClassBinding = xmlBeanMetadata.getBeanClassBinding(bean);
		writer.startNode(beanClassBinding.getXmlElementName());
		beanClassBinding.getConverter().toXml(this, writer, bean);
		writer.endNode();
	}
	
	public void marshallProperty(JuffrouWriter writer, Object bean) {
		Converter converter = xmlBeanMetadata.getConverterForType(bean.getClass());
		if(converter == null) {
			BeanClassBinding beanClassBinding = xmlBeanMetadata.getBeanClassBinding(bean.getClass());
			converter = beanClassBinding.getConverter();
		}
		converter.toXml(this, writer, bean);
	}
	
}