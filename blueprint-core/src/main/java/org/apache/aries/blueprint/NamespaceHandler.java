/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.aries.blueprint;

import java.net.URL;
import java.util.Set;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import org.osgi.service.blueprint.reflect.ComponentMetadata;
import org.osgi.service.blueprint.reflect.Metadata;

/**
 * A processor for custom blueprint extensions
 * 
 * Namespace handlers must be registered in the OSGi service registry with the 
 * <code>osgi.service.blueprint.namespace</code> service property denoting the namespace URIs this
 * handler can process. The service property value can be either a single <code>String</code> or <code>URI</code>, 
 * or a <code>Collection</code> respectively array of <code>String</code> or <code>URI</code>.
 * 
 * During parsing when the blueprint extender encounters an element from a non-blueprint namespace it will search
 * for a namespace handler for the namespace that is compatible with blueprint bundle being processed. Then
 * for a stand-alone component the parser will invoke the <code>parse</code> method 
 * to create the <code>Metadata</code> for the xml element while for an element that is part
 * of an existing component the parser will invoke the <code>decorated</code> method to augment
 * the enclosing <code>ComponentMetadata</code> instance.  Various utilities to interact with 
 * the blueprint parser are available to a namespace handler via the <code>ParserContext</code> argument 
 * passed to <code>parse</code> and <code>decorate</code>.
 * 
 * Recommended behaviour:
 * <ul>
 * <li>New metadata objects should be created via calling <code>ParserContext.createMetadata(..)</code> and
 * casting the returned object to the appropriate <code>MutableComponentMetadata</code> interface. 
 * This method ensures that the metadata object implements the interfaces necessary for other namespace handlers
 * to be able to use the metadata object.<br/>
 * Also, to prevent id clashes, component ids should be generated by calling <code>ParserContext.generateId()</code>.
 * </li>
 * <li>A namespace handler should not return new metadata instances from the <code>decorate</code> method if 
 * the same result could also be achieved by operating on a <code>MutableComponentMetadata</code> instance.
 * </li>
 * <li>A namespace handler should not assume the existence of predefined entries in the component definition
 * registry such as <code>blueprintBundle</code> or <code>blueprintBundleContext</code>. In the case of a dry
 * parse (i.e. a parse of the blueprint xml files without a backing OSGi bundle), these values will not be 
 * available
 * </li>
 * </ul>
 */
public interface NamespaceHandler  {
    /**
     * Retrieve a URL from where the schema for a given namespace can be retrieved
     * @param namespace The schema's namespace
     * @return A URL that points to the location of the schema or null if the namespace validation
     * is not needed
     */
    URL getSchemaLocation(String namespace);

    /**
     * Specify a set of classes that must be consistent between a blueprint bundle and this namespace handler
     * 
     * The blueprint extender will not invoke a namespace handler if any of the managed classes are inconsistent
     * with the class space of the blueprint bundle (i.e. if the blueprint bundle loads any of the managed classes
     * from a different classloader).
     * 
     * @return a <code>Set</code> of classes that must be compatible with any blueprint bundle for which this namespace 
     * handler is to apply or <code>null</code> if no compatibility checks are to be performed
     */
    Set<Class> getManagedClasses();
    
    /**
     * Parse a stand-alone blueprint component 
     *
     * Given an <code>Element</code> node as a root, this method parses the stand-alone component and returns its
     * metadata. The supplied <code>ParserContext</code> should be used to parse embedded blueprint elements as well
     * as creating metadata.
     * 
     * @param element The DOM element representing the custom component
     * @param context The <code>ParserContext</code> for parsing sub-components and creating metadata objects
     * @return A metadata instance representing the custom component. This should be an instance of an appropriate
     * <code>MutableMetadata</code> type to enable further decoration by other namespace handlers
     */
    Metadata parse(Element element, ParserContext context);
    
    /**
     * Process a child node of an enclosing blueprint component. 
     * 
     * If the decorator method returns a new <code>ComponentMetadata</code> instance, this will replace the argument 
     * <code>ComponentMetadata</code> in subsequent parsing and namespace handler invocations. A namespace
     * handler that elects to return a new <code>ComponentMetadata</code> instance should
     * ensure that existing interceptors are registered against the new instance if appropriate.
     * 
     * Due to the interaction with interceptors, a namespace handler should prefer to change a component metadata
     * instead of returning a new instance wherever possible. This can be achieved by casting a 
     * <code>ComponentMetadata</code> to its corresponding <code>MutabableComponentMetadata</code> instance.
     * Note however that a given <code>ComponentMetadata</code> instance cannot be guaranteed to implement
     * the mutable interface if it was constructed by an agent other than the blueprint extender.
     * 
     * @param node The node associated with this NamespaceHandler that should be used to decorate the enclosing 
     * component
     * @param component The enclosing blueprint component
     * @param context The parser context
     * @return The decorated component to be used instead of the original enclosing component. This can of course be
     * the original component.
     */
    ComponentMetadata decorate(Node node, ComponentMetadata component, ParserContext context);
             
}
