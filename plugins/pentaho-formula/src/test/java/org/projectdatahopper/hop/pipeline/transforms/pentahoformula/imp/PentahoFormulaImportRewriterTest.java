/*
 * Copyright (C) 2026 Project Data Hopper
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
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
