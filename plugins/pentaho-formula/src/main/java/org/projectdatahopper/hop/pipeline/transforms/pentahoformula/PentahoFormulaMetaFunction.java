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

package org.projectdatahopper.hop.pipeline.transforms.pentahoformula;

import java.util.Objects;
import lombok.Getter;
import lombok.Setter;
import org.apache.hop.metadata.api.HopMetadataProperty;

/**
 * One OpenFormula calculation: output field, expression, target type, and optional replace field.
 *
 * <p>XML keys match PDI Formula after Hop's Kettle importer ({@code formula_string} renamed to
 * {@code formula}, {@code value_type} stored as a Hop type id).
 */
@Getter
@Setter
public class PentahoFormulaMetaFunction {
  public static final String XML_TAG = "formula";

  @HopMetadataProperty(
      key = "field_name",
      injectionKeyDescription = "PentahoFormulaMeta.Injection.FieldName")
  private String fieldName;

  @HopMetadataProperty(
      key = "formula",
      injectionKeyDescription = "PentahoFormulaMeta.Injection.FormulaString")
  private String formula;

  @HopMetadataProperty(
      key = "value_type",
      injectionKeyDescription = "PentahoFormulaMeta.Injection.ValueType",
      injectionConverter = StringToTypeConverter.class)
  private int valueType;

  @HopMetadataProperty(
      key = "value_length",
      injectionKeyDescription = "PentahoFormulaMeta.Injection.ValueLength")
  private int valueLength;

  @HopMetadataProperty(
      key = "value_precision",
      injectionKeyDescription = "PentahoFormulaMeta.Injection.ValuePrecision")
  private int valuePrecision;

  @HopMetadataProperty(
      key = "replace_field",
      injectionKeyDescription = "PentahoFormulaMeta.Injection.ReplaceField")
  private String replaceField;

  /** Discovered at runtime; not persisted. */
  @SuppressWarnings("java:S2065")
  private transient boolean needDataConversion;

  public PentahoFormulaMetaFunction() {
    valueLength = -1;
    valuePrecision = -1;
  }

  public PentahoFormulaMetaFunction(
      String fieldName,
      String formula,
      int valueType,
      int valueLength,
      int valuePrecision,
      String replaceField) {
    this.fieldName = fieldName;
    this.formula = formula;
    this.valueType = valueType;
    this.valueLength = valueLength;
    this.valuePrecision = valuePrecision;
    this.replaceField = replaceField;
  }

  public PentahoFormulaMetaFunction(PentahoFormulaMetaFunction other) {
    this.fieldName = other.fieldName;
    this.formula = other.formula;
    this.valueType = other.valueType;
    this.valueLength = other.valueLength;
    this.valuePrecision = other.valuePrecision;
    this.replaceField = other.replaceField;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof PentahoFormulaMetaFunction that)) {
      return false;
    }
    return valueType == that.valueType
        && valueLength == that.valueLength
        && valuePrecision == that.valuePrecision
        && Objects.equals(fieldName, that.fieldName)
        && Objects.equals(formula, that.formula)
        && Objects.equals(replaceField, that.replaceField);
  }

  @Override
  public int hashCode() {
    return Objects.hash(fieldName, formula, valueType, valueLength, valuePrecision, replaceField);
  }
}
