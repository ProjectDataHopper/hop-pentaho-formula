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
