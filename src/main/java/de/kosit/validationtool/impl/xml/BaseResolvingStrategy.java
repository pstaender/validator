/*
 * Copyright 2017-2022  Koordinierungsstelle für IT-Standards (KoSIT)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.kosit.validationtool.impl.xml;

import static java.lang.String.format;

import javax.xml.XMLConstants;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.xml.sax.SAXException;

import lombok.extern.slf4j.Slf4j;

import de.kosit.validationtool.api.ResolvingConfigurationStrategy;

/**
 * @author Andreas Penski
 */
@Slf4j
public abstract class BaseResolvingStrategy implements ResolvingConfigurationStrategy {

    private static final String ORACLE_XERCES_CLASS = "com.sun.org.apache.xerces.internal.impl.Constants";

    public static void forceOpenJdkXmlImplementation() {
        if (!isOpenJdkXmlImplementationAvailable()) {
            throw new IllegalStateException("No OpenJDK version of XERCES found");
        }
    }

    public static boolean isOpenJdkXmlImplementationAvailable() {
        try {
            Class.forName(ORACLE_XERCES_CLASS);
            return true;
        } catch (final ClassNotFoundException e) {
            log.warn("No oracle JDK version of XERCES found. Configured security features may not have any effect.");
            log.warn("Please take care of XML security while checking your xml contents");
            return false;
        }
    }

    private void setProperty(final PropertySetter setter, final boolean lenient, final String errorMessage) {
        try {
            setter.apply();
        } catch (final SAXException e) {

            if (lenient) {
                log.warn(errorMessage);
                log.debug(e.getMessage(), e);
            } else {
                throw new IllegalStateException(errorMessage);
            }
        }
    }

    protected void allowExternalSchema(final Validator validator, final String... scheme) {
        allowExternalSchema(validator, false, scheme);
    }

    protected void allowExternalSchema(final SchemaFactory schemaFactory, final String... scheme) {
        allowExternalSchema(schemaFactory, false, scheme);
    }

    protected void allowExternalSchema(final Validator validator, final boolean lenient, final String... schemes) {
        final String schemeString = String.join(",", schemes);
        setProperty(() -> validator.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, schemeString), lenient, format(
                "Can set  external schema  access to schemes (%s). Maybe an unsupported JAXP implementation is used.", schemeString));
    }

    protected void allowExternalSchema(final SchemaFactory schemaFactory, final boolean lenient, final String... schemes) {
        final String schemeString = String.join(",", schemes);
        setProperty(() -> schemaFactory.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, schemeString), lenient, format(
                "Can set  external schema  access to schemes (%s). Maybe an unsupported JAXP implementation is used.", schemeString));
    }

    protected void disableExternalEntities(final Validator validator) {
        disableExternalEntities(validator, false);
    }

    protected void disableExternalEntities(final SchemaFactory schemaFactory) {
        disableExternalEntities(schemaFactory, false);
    }

    protected void disableExternalEntities(final Validator validator, final boolean lenient) {
        log.debug("Try to disable extern DTD access");
        setProperty(() -> validator.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, ""), lenient,
                "Can not disable external DTD access. Maybe an unsupported JAXP implementation is used.");

    }

    protected void disableExternalEntities(final SchemaFactory schemaFactory, final boolean lenient) {
        log.debug("Try to disable extern DTD access");
        setProperty(() -> schemaFactory.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, ""), lenient,
                "Can not disable external DTD access. Maybe an unsupported JAXP implementation is used.");

    }

    @FunctionalInterface
    private interface PropertySetter {

        void apply() throws SAXException;
    }
}
