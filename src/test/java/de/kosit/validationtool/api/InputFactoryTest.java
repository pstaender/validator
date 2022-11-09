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

package de.kosit.validationtool.api;

import static de.kosit.validationtool.impl.Helper.Simple.SIMPLE_VALID;
import static de.kosit.validationtool.impl.input.StreamHelper.drain;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;

import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import de.kosit.validationtool.impl.Helper;
import de.kosit.validationtool.impl.Helper.Simple;
import de.kosit.validationtool.impl.TestObjectFactory;
import de.kosit.validationtool.impl.input.SourceInput;
import de.kosit.validationtool.impl.model.Result;
import de.kosit.validationtool.model.reportInput.XMLSyntaxError;

import net.sf.saxon.dom.NodeOverNodeInfo;
import net.sf.saxon.s9api.BuildingContentHandler;
import net.sf.saxon.s9api.DocumentBuilder;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmNode;

/**
 * Testet den Hashcode-Service.
 * 
 * @author Andreas Penski
 */
public class InputFactoryTest {

    public static final String SOME_VALUE = "some value";

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void testDefaultDigestAlgorithm() {
        assertThat(new InputFactory().getAlgorithm()).isEqualTo(InputFactory.DEFAULT_ALGORITH);
        assertThat(new InputFactory("").getAlgorithm()).isEqualTo(InputFactory.DEFAULT_ALGORITH);
    }

    @Test
    public void testHashCodeGeneration() throws IOException {
        final byte[] s1 = drain(InputFactory.read(Simple.SIMPLE_VALID.toURL())).getHashCode();
        final byte[] s2 = drain(InputFactory.read(Simple.SIMPLE_VALID.toURL())).getHashCode();
        final byte[] s3 = drain(InputFactory.read(Simple.SCHEMA_INVALID.toURL())).getHashCode();
        assertThat(s1).isNotEmpty();
        assertThat(s1).isEqualTo(s2);
        assertThat(s3).isNotEmpty();
        assertThat(s1).isNotEqualTo(s3);
    }

    @Test
    public void testWrongAlgorithm() {
        this.expectedException.expect(IllegalArgumentException.class);
        new InputFactory("unknown");
    }

    @Test
    public void testNullInputURL() {
        this.expectedException.expect(IllegalArgumentException.class);
        InputFactory.read((URL) null);
    }

    @Test
    public void testInputByte() {
        final Input input = InputFactory.read(SOME_VALUE.getBytes(), SOME_VALUE);
        assertThat(input).isNotNull();
    }

    @Test
    public void testInputStream() {
        final Input input = InputFactory.read(new ByteArrayInputStream(SOME_VALUE.getBytes()), SOME_VALUE);
        assertThat(input).isNotNull();
    }

    @Test
    public void testNullStream() {
        this.expectedException.expect(IllegalArgumentException.class);
        final Input input = InputFactory.read((InputStream) null, SOME_VALUE);
    }

    @Test
    public void testInputFile() throws URISyntaxException {
        final Input input = InputFactory.read(new File(Simple.SIMPLE_VALID));
        assertThat(input).isNotNull();
    }

    @Test
    public void testInputPath() throws URISyntaxException {
        final Input input = InputFactory.read(Paths.get(Simple.SIMPLE_VALID));
        assertThat(input).isNotNull();
    }

    @Test
    public void testNullInput() {
        this.expectedException.expect(IllegalArgumentException.class);
        InputFactory.read((byte[]) null, SOME_VALUE);
    }

    @Test
    public void testNullInputName() {
        this.expectedException.expect(IllegalArgumentException.class);
        InputFactory.read(SOME_VALUE.getBytes(), null);
    }

    @Test
    public void testEmptyInputName() throws IOException {
        this.expectedException.expect(IllegalArgumentException.class);
        final Input input = InputFactory.read(SOME_VALUE.getBytes(), "");
        drain(input);
    }

    @Test
    public void testSourceInput() throws IOException {
        try ( final InputStream s = Simple.SIMPLE_VALID.toURL().openStream() ) {
            final SourceInput input = (SourceInput) InputFactory.read(new StreamSource(s));
            assertThat(input.getSource()).isNotNull();
            drain(input);
            assertThat(input.getHashCode()).isNotNull();
            assertThat(input.getLength()).isGreaterThan(0L);
            this.expectedException.expect(IllegalStateException.class);
            input.getSource();
        }
    }

    @Test
    public void testSourceInputReader() throws IOException {
        try ( final InputStream s = Simple.SIMPLE_VALID.toURL().openStream();
              final InputStreamReader reader = new InputStreamReader(s) ) {
            final SourceInput input = (SourceInput) InputFactory.read(new StreamSource(reader));
            assertThat(input.getSource()).isNotNull();
            drain(input);
            assertThat(input.getHashCode()).isNotNull();
            assertThat(input.getLength()).isGreaterThan(0L);
            this.expectedException.expect(IllegalStateException.class);
            input.getSource();
        }
    }

    @Test
    public void testUnexistingInput() {
        this.expectedException.expect(IllegalArgumentException.class);
        InputFactory.read(Simple.NOT_EXISTING);
    }

    @Test
    public void testDomSource() throws SaxonApiException, SAXException, IOException {
        final DocumentBuilder builder = TestObjectFactory.createProcessor().newDocumentBuilder();

        final BuildingContentHandler handler = builder.newBuildingContentHandler();
        handler.startDocument();
        handler.startElement("http://some.ns", "mynode", "mynode", new AttributesImpl());
        final Document dom = NodeOverNodeInfo.wrap(handler.getDocumentNode().getUnderlyingNode()).getOwnerDocument();
        final Input domInput = InputFactory.read(new DOMSource(dom), "MD5", "id".getBytes());
        assertThat(domInput).isNotNull();
        assertThat(domInput.getSource()).isNotNull();
        final Result<XdmNode, XMLSyntaxError> parsed = Helper.parseDocument(domInput);
        assertThat(parsed.isValid()).isTrue();

        // read twice
        assertThat(Helper.parseDocument(domInput).getObject()).isNotNull();
    }

    @Test
    public void testXdmNode() throws Exception {
        final XdmNode node = TestObjectFactory.createProcessor().newDocumentBuilder().build(new StreamSource(SIMPLE_VALID.toASCIIString()));
        final Input nodeInput = InputFactory.read(node, "node test");
        assertThat(nodeInput).isNotNull();
        assertThat(nodeInput.getSource()).isNotNull();
        final Result<XdmNode, XMLSyntaxError> parsed = Helper.parseDocument(nodeInput);
        assertThat(parsed.isValid()).isTrue();

        // read twice
        assertThat(Helper.parseDocument(nodeInput).getObject()).isNotNull();
    }

}
