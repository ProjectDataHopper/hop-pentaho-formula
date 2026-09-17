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
