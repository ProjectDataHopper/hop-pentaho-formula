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

/**
 * Inverse of Hop {@code KettleImport}'s {@code formula_string.replaceAll(";", ",")} for OpenFormula.
 *
 * <p>Commas outside double-quoted strings and {@code [field]} references become {@code ;}. Commas
 * inside those regions are left alone.
 */
public final class OpenFormulaSeparatorRestorer {
  private OpenFormulaSeparatorRestorer() {}

  public static String restoreSemicolons(String formula) {
    if (formula == null || formula.indexOf(',') < 0) {
      return formula;
    }
    StringBuilder out = new StringBuilder(formula.length());
    boolean inString = false;
    int bracketDepth = 0;
    for (int i = 0; i < formula.length(); i++) {
      char c = formula.charAt(i);
      if (inString) {
        out.append(c);
        if (c == '"') {
          // OpenFormula doubles quotes to escape them
          if (i + 1 < formula.length() && formula.charAt(i + 1) == '"') {
            out.append(formula.charAt(++i));
          } else {
            inString = false;
          }
        }
        continue;
      }
      if (c == '"') {
        inString = true;
        out.append(c);
        continue;
      }
      if (c == '[') {
        bracketDepth++;
        out.append(c);
        continue;
      }
      if (c == ']' && bracketDepth > 0) {
        bracketDepth--;
        out.append(c);
        continue;
      }
      if (c == ',' && bracketDepth == 0) {
        out.append(';');
        continue;
      }
      out.append(c);
    }
    return out.toString();
  }
}
