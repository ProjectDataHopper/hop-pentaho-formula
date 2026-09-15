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

package org.projectdatahopper.hop.pipeline.transforms.pentahoformula.editor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.pentaho.reporting.libraries.formula.DefaultFormulaContext;
import org.pentaho.reporting.libraries.formula.function.FunctionCategory;
import org.projectdatahopper.hop.pipeline.transforms.pentahoformula.LibFormulaRuntime;
import org.pentaho.reporting.libraries.formula.function.FunctionDescription;
import org.pentaho.reporting.libraries.formula.function.FunctionRegistry;

/** Function tree for the formula editor, sourced from libformula's registry. */
public class LibFormulaFunctionCatalog {
  private final Map<String, List<FunctionDescription>> byCategory = new LinkedHashMap<>();

  public LibFormulaFunctionCatalog() {
    LibFormulaRuntime.ensureBooted();
    FunctionRegistry registry = new DefaultFormulaContext().getFunctionRegistry();
    FunctionCategory[] categories = registry.getCategories();
    Arrays.sort(categories, Comparator.comparing(c -> c.getDisplayName(Locale.getDefault())));
    for (FunctionCategory category : categories) {
      String[] names = registry.getFunctionNamesByCategory(category);
      Arrays.sort(names, String.CASE_INSENSITIVE_ORDER);
      List<FunctionDescription> functions = new ArrayList<>();
      for (String name : names) {
        FunctionDescription meta = registry.getMetaData(name);
        if (meta != null) {
          functions.add(meta);
        }
      }
      if (!functions.isEmpty()) {
        byCategory.put(category.getDisplayName(Locale.getDefault()), functions);
      }
    }
  }

  public String[] getCategories() {
    return byCategory.keySet().toArray(String[]::new);
  }

  public List<FunctionDescription> getFunctions(String category) {
    return byCategory.getOrDefault(category, List.of());
  }

  public static String syntax(FunctionDescription description) {
    StringBuilder builder = new StringBuilder(description.getCanonicalName());
    builder.append('(');
    int count = description.getParameterCount();
    Locale locale = Locale.getDefault();
    for (int i = 0; i < count; i++) {
      if (i > 0) {
        builder.append(';');
      }
      builder.append(description.getParameterDisplayName(i, locale));
    }
    if (description.isInfiniteParameterCount()) {
      if (count > 0) {
        builder.append(';');
      }
      builder.append("...");
    }
    builder.append(')');
    return builder.toString();
  }

  public static String htmlReport(FunctionDescription description) {
    Locale locale = Locale.getDefault();
    StringBuilder html = new StringBuilder();
    html.append("<html><body style='font-family:sans-serif;font-size:12px'>");
    html.append("<h3>").append(escape(description.getCanonicalName())).append("</h3>");
    html.append("<p><b>").append(escape(syntax(description))).append("</b></p>");
    String desc = description.getDescription(locale);
    if (desc != null && !desc.isBlank()) {
      html.append("<p>").append(escape(desc)).append("</p>");
    }
    int count = description.getParameterCount();
    if (count > 0) {
      html.append("<ul>");
      for (int i = 0; i < count; i++) {
        html.append("<li><b>")
            .append(escape(description.getParameterDisplayName(i, locale)))
            .append("</b>: ")
            .append(escape(nullToEmpty(description.getParameterDescription(i, locale))))
            .append("</li>");
      }
      html.append("</ul>");
    }
    html.append("</body></html>");
    return html.toString();
  }

  private static String nullToEmpty(String value) {
    return value == null ? "" : value;
  }

  private static String escape(String value) {
    if (value == null) {
      return "";
    }
    return value
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;");
  }
}
