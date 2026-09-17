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

package org.projectdatahopper.hop.pipeline.transforms.pentahoformula.imp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import javax.xml.parsers.DocumentBuilderFactory;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

class PentahoFormulaImportRewriterTest {

  @Test
  void remapsPdiFormulaAndRestoresSeparators() throws Exception {
    Document document =
        DocumentBuilderFactory.newInstance()
            .newDocumentBuilder()
            .parse(getClass().getResourceAsStream("/imported-pdi-formula.hpl"));
    int remapped = PentahoFormulaImportRewriter.rewrite(document);
    assertEquals(1, remapped);
    Element transform = (Element) document.getElementsByTagName("transform").item(0);
    assertEquals("PentahoFormula", text(child(transform, "type")));
    NodeList formulaTexts = transform.getElementsByTagName("formula");
    boolean found = false;
    for (int i = 0; i < formulaTexts.getLength(); i++) {
      Element el = (Element) formulaTexts.item(i);
      if ("formula".equals(el.getParentNode().getNodeName())
          && el.getChildNodes().getLength() == 1
          && el.getTextContent().contains("IF")) {
        assertEquals("IF([flag];\"Y\";\"N\")", el.getTextContent());
        found = true;
      }
    }
    assertTrue(found, "expected restored IF formula");
  }

  @Test
  void leavesPoiFormulaWithSetNaAlone() throws Exception {
    Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
    Element pipeline = document.createElement("pipeline");
    document.appendChild(pipeline);
    Element transform = document.createElement("transform");
    pipeline.appendChild(transform);
    Element type = document.createElement("type");
    type.setTextContent("Formula");
    transform.appendChild(type);
    Element formulas = document.createElement("formulas");
    transform.appendChild(formulas);
    Element formula = document.createElement("formula");
    formulas.appendChild(formula);
    Element expr = document.createElement("formula");
    expr.setTextContent("1+1");
    formula.appendChild(expr);
    Element setNa = document.createElement("set_na");
    setNa.setTextContent("N");
    formula.appendChild(setNa);

    assertEquals(0, PentahoFormulaImportRewriter.rewrite(document));
    assertEquals("Formula", type.getTextContent());
  }

  private static Element child(Element parent, String name) {
    NodeList children = parent.getChildNodes();
    for (int i = 0; i < children.getLength(); i++) {
      if (name.equals(children.item(i).getNodeName())) {
        return (Element) children.item(i);
      }
    }
    return null;
  }

  private static String text(Element element) {
    return element.getTextContent();
  }
}
