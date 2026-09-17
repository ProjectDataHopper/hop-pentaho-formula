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

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Rewrites Hop XML produced by Kettle import so PDI Formula steps bind to this plugin.
 *
 * <p>Hop's importer keeps {@code <type>Formula</type>} (the POI plugin) and turns OpenFormula {@code
 * ;} argument separators into commas. This rewriter maps the type to {@code PentahoFormula} and
 * restores {@code ;}.
 */
public final class PentahoFormulaImportRewriter {
  public static final String PDI_FORMULA_TYPE = "Formula";
  public static final String PENTAHO_FORMULA_TYPE = "PentahoFormula";

  private PentahoFormulaImportRewriter() {}

  /**
   * @return number of Formula transforms remapped
   */
  public static int rewrite(Document document) {
    if (document == null) {
      return 0;
    }
    int remapped = 0;
    NodeList transforms = document.getElementsByTagName("transform");
    for (int i = 0; i < transforms.getLength(); i++) {
      Element transform = (Element) transforms.item(i);
      if (rewriteTransform(transform)) {
        remapped++;
      }
    }
    return remapped;
  }

  static boolean rewriteTransform(Element transform) {
    Element typeElement = child(transform, "type");
    if (typeElement == null || !PDI_FORMULA_TYPE.equals(text(typeElement))) {
      return false;
    }
    Element formulas = child(transform, "formulas");
    if (formulas == null) {
      return false;
    }
    // Hop's POI Formula persists set_na; PDI Formula (after Kettle import) does not.
    if (hasSetNa(formulas)) {
      return false;
    }
    typeElement.setTextContent(PENTAHO_FORMULA_TYPE);
    NodeList formulaNodes = formulas.getChildNodes();
    for (int i = 0; i < formulaNodes.getLength(); i++) {
      Node node = formulaNodes.item(i);
      if (node.getNodeType() != Node.ELEMENT_NODE || !"formula".equals(node.getNodeName())) {
        continue;
      }
      Element formulaText = child((Element) node, "formula");
      if (formulaText != null) {
        formulaText.setTextContent(OpenFormulaSeparatorRestorer.restoreSemicolons(text(formulaText)));
      }
    }
    return true;
  }

  private static boolean hasSetNa(Element formulas) {
    NodeList formulaNodes = formulas.getElementsByTagName("formula");
    for (int i = 0; i < formulaNodes.getLength(); i++) {
      if (child((Element) formulaNodes.item(i), "set_na") != null) {
        return true;
      }
    }
    return false;
  }

  private static Element child(Element parent, String name) {
    NodeList children = parent.getChildNodes();
    for (int i = 0; i < children.getLength(); i++) {
      Node node = children.item(i);
      if (node.getNodeType() == Node.ELEMENT_NODE && name.equals(node.getNodeName())) {
        return (Element) node;
      }
    }
    return null;
  }

  private static String text(Element element) {
    return element.getTextContent() == null ? "" : element.getTextContent();
  }
}
