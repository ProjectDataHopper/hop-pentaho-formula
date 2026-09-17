/*
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

package org.projectdatahopper.hop.pipeline.transforms.pentahoformula;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.apache.hop.core.ICheckResult;
import org.apache.hop.core.exception.HopTransformException;
import org.apache.hop.core.plugins.PluginRegistry;
import org.apache.hop.core.row.IRowMeta;
import org.apache.hop.core.row.IValueMeta;
import org.apache.hop.core.row.RowMeta;
import org.apache.hop.core.row.value.ValueMetaInteger;
import org.apache.hop.core.row.value.ValueMetaNumber;
import org.apache.hop.core.row.value.ValueMetaPlugin;
import org.apache.hop.core.row.value.ValueMetaPluginType;
import org.apache.hop.core.row.value.ValueMetaString;
import org.apache.hop.core.variables.Variables;
import org.apache.hop.core.xml.XmlHandler;
import org.apache.hop.metadata.serializer.memory.MemoryMetadataProvider;
import org.apache.hop.metadata.serializer.xml.XmlMetadataUtil;
import org.apache.hop.pipeline.PipelineMeta;
import org.apache.hop.pipeline.transform.TransformMeta;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Node;

class PentahoFormulaMetaTest {

  @BeforeEach
  void registerValueMetas() throws Exception {
    PluginRegistry registry = PluginRegistry.getInstance();
    for (String className :
        List.of(
            ValueMetaString.class.getName(),
            ValueMetaInteger.class.getName(),
            ValueMetaNumber.class.getName())) {
      registry.registerPluginClass(className, ValueMetaPluginType.class, ValueMetaPlugin.class);
    }
  }

  @Test
  void cloneIsDeep() {
    PentahoFormulaMeta meta = new PentahoFormulaMeta();
    meta.getFormulas().add(new PentahoFormulaMetaFunction("test", "1+1", 1, 10, 2, ""));
    PentahoFormulaMeta before = (PentahoFormulaMeta) meta.clone();
    meta.getFormulas().clear();
    meta.getFormulas().add(new PentahoFormulaMetaFunction("test", "2+2", 1, 10, 2, ""));
    assertEquals(1, before.getFormulas().size());
    assertEquals("1+1", before.getFormulas().get(0).getFormula());
    assertNotEquals(meta.getFormulas(), before.getFormulas());
  }

  @Test
  void getFieldsAddsNewField() throws Exception {
    PentahoFormulaMeta meta = new PentahoFormulaMeta();
    meta.getFormulas()
        .add(
            new PentahoFormulaMetaFunction(
                "total", "[qty]*[price]", IValueMeta.TYPE_NUMBER, -1, -1, ""));
    IRowMeta row = new RowMeta();
    row.addValueMeta(new ValueMetaNumber("qty"));
    meta.getFields(row, "formula", null, null, new Variables(), null);
    assertEquals(2, row.size());
    assertEquals("total", row.getValueMeta(1).getName());
    assertEquals(IValueMeta.TYPE_NUMBER, row.getValueMeta(1).getType());
  }

  @Test
  void getFieldsReplacesExistingField() throws Exception {
    PentahoFormulaMeta meta = new PentahoFormulaMeta();
    meta.getFormulas()
        .add(
            new PentahoFormulaMetaFunction(
                "qty", "[qty]*2", IValueMeta.TYPE_INTEGER, 10, 0, "qty"));
    IRowMeta row = new RowMeta();
    row.addValueMeta(new ValueMetaNumber("qty"));
    meta.getFields(row, "formula", null, null, new Variables(), null);
    assertEquals(1, row.size());
    assertEquals(10, row.getValueMeta(0).getLength());
  }

  @Test
  void getFieldsUnknownReplaceFieldThrows() {
    PentahoFormulaMeta meta = new PentahoFormulaMeta();
    meta.getFormulas()
        .add(new PentahoFormulaMetaFunction("x", "1", IValueMeta.TYPE_NUMBER, -1, -1, "missing"));
    IRowMeta row = new RowMeta();
    assertThrows(
        HopTransformException.class,
        () -> meta.getFields(row, "formula", null, null, new Variables(), null));
  }

  @Test
  void checkReportsMissingInput() {
    PentahoFormulaMeta meta = new PentahoFormulaMeta();
    List<ICheckResult> remarks = new ArrayList<>();
    meta.check(
        remarks,
        new PipelineMeta(),
        new TransformMeta(),
        new RowMeta(),
        new String[0],
        new String[0],
        null,
        new Variables(),
        null);
    assertTrue(remarks.stream().anyMatch(r -> r.getType() == ICheckResult.TYPE_RESULT_ERROR));
  }

  @Test
  void xmlRoundTrip() throws Exception {
    String xml =
        new String(
            getClass().getResourceAsStream("/pentaho-formula-meta.xml").readAllBytes(),
            StandardCharsets.UTF_8);
    Node node = XmlHandler.loadXmlString(xml, TransformMeta.XML_TAG);
    PentahoFormulaMeta meta = new PentahoFormulaMeta();
    XmlMetadataUtil.deSerializeFromXml(node, PentahoFormulaMeta.class, meta, new MemoryMetadataProvider());

    assertEquals(2, meta.getFormulas().size());
    assertEquals("total", meta.getFormulas().get(0).getFieldName());
    assertEquals("[qty]*[price]", meta.getFormulas().get(0).getFormula());
    assertEquals(IValueMeta.TYPE_NUMBER, meta.getFormulas().get(0).getValueType());
    assertEquals("IF([flag];\"Y\";\"N\")", meta.getFormulas().get(1).getFormula());
    assertEquals(IValueMeta.TYPE_STRING, meta.getFormulas().get(1).getValueType());

    String serialized = XmlHandler.openTag(TransformMeta.XML_TAG) + meta.getXml() + XmlHandler.closeTag(TransformMeta.XML_TAG);
    PentahoFormulaMeta copy = new PentahoFormulaMeta();
    XmlMetadataUtil.deSerializeFromXml(
        XmlHandler.loadXmlString(serialized, TransformMeta.XML_TAG),
        PentahoFormulaMeta.class,
        copy,
        new MemoryMetadataProvider());
    assertEquals(meta.getXml(), copy.getXml());
    assertNotNull(copy.getFormulas().get(0).getFieldName());
  }

  @Test
  void supportsErrorHandling() {
    assertTrue(new PentahoFormulaMeta().supportsErrorHandling());
  }
}
