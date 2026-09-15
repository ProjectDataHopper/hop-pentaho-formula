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

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.apache.hop.core.exception.HopValueException;
import org.apache.hop.core.row.IRowMeta;
import org.apache.hop.core.row.IValueMeta;
import org.pentaho.reporting.libraries.base.config.Configuration;
import org.pentaho.reporting.libraries.formula.DefaultFormulaContext;
import org.pentaho.reporting.libraries.formula.ErrorValue;
import org.pentaho.reporting.libraries.formula.EvaluationException;
import org.pentaho.reporting.libraries.formula.FormulaContext;
import org.pentaho.reporting.libraries.formula.LibFormulaErrorValue;
import org.pentaho.reporting.libraries.formula.LocalizationContext;
import org.pentaho.reporting.libraries.formula.function.FunctionRegistry;
import org.pentaho.reporting.libraries.formula.operators.OperatorFactory;
import org.pentaho.reporting.libraries.formula.typing.Type;
import org.pentaho.reporting.libraries.formula.typing.TypeRegistry;
import org.pentaho.reporting.libraries.formula.typing.coretypes.AnyType;
import org.pentaho.reporting.libraries.formula.typing.coretypes.NumberType;
import org.pentaho.reporting.libraries.formula.typing.coretypes.TextType;

/**
 * libformula context that resolves {@code [field]} references against the current Hop row.
 *
 * <p>Port of PDI {@code RowForumulaContext} (Apache-2.0, 9.4), with the class-name typo corrected.
 */
@Getter
@Setter
public class RowFormulaContext implements FormulaContext {
  private IRowMeta rowMeta;
  private final FormulaContext formulaContext;
  private final Map<String, Integer> valueIndexMap;
  private Object[] rowData;

  public RowFormulaContext(IRowMeta row) {
    this.formulaContext = new DefaultFormulaContext();
    this.rowMeta = row;
    this.rowData = null;
    this.valueIndexMap = new HashMap<>();
  }

  @Override
  public Type resolveReferenceType(Object name) {
    if (name instanceof String fieldName) {
      IValueMeta valueMeta = this.rowMeta.searchValueMeta(fieldName);
      if (valueMeta != null) {
        return switch (valueMeta.getType()) {
          case IValueMeta.TYPE_STRING -> TextType.TYPE;
          case IValueMeta.TYPE_INTEGER, IValueMeta.TYPE_BIGNUMBER, IValueMeta.TYPE_NUMBER ->
              NumberType.GENERIC_NUMBER;
          default -> AnyType.TYPE;
        };
      }
    }
    return AnyType.TYPE;
  }

  @Override
  public Object resolveReference(Object name) throws EvaluationException {
    if (name instanceof String fieldName) {
      IValueMeta valueMeta;
      Integer idx = valueIndexMap.get(fieldName);
      if (idx != null) {
        valueMeta = rowMeta.getValueMeta(idx);
      } else {
        int index = rowMeta.indexOfValue(fieldName);
        if (index < 0) {
          ErrorValue errorValue =
              new LibFormulaErrorValue(LibFormulaErrorValue.ERROR_INVALID_ARGUMENT);
          throw new EvaluationException(errorValue);
        }
        valueMeta = rowMeta.getValueMeta(index);
        valueIndexMap.put(fieldName, index);
        idx = index;
      }
      Object valueData = rowData[idx];
      try {
        return getPrimitive(valueMeta, valueData);
      } catch (HopValueException e) {
        throw new EvaluationException(LibFormulaErrorValue.ERROR_ARITHMETIC_VALUE);
      }
    }
    return null;
  }

  @Override
  public Configuration getConfiguration() {
    return formulaContext.getConfiguration();
  }

  @Override
  public FunctionRegistry getFunctionRegistry() {
    return formulaContext.getFunctionRegistry();
  }

  @Override
  public LocalizationContext getLocalizationContext() {
    return formulaContext.getLocalizationContext();
  }

  @Override
  public OperatorFactory getOperatorFactory() {
    return formulaContext.getOperatorFactory();
  }

  @Override
  public TypeRegistry getTypeRegistry() {
    return formulaContext.getTypeRegistry();
  }

  @Override
  public boolean isReferenceDirty(Object name) throws EvaluationException {
    return formulaContext.isReferenceDirty(name);
  }

  @Override
  public Date getCurrentDate() {
    return new Date();
  }

  public static Object getPrimitive(IValueMeta valueMeta, Object valueData)
      throws HopValueException {
    return switch (valueMeta.getType()) {
      case IValueMeta.TYPE_BIGNUMBER -> valueMeta.getBigNumber(valueData);
      case IValueMeta.TYPE_BINARY -> valueMeta.getBinary(valueData);
      case IValueMeta.TYPE_BOOLEAN -> valueMeta.getBoolean(valueData);
      case IValueMeta.TYPE_DATE -> valueMeta.getDate(valueData);
      case IValueMeta.TYPE_INTEGER -> valueMeta.getInteger(valueData);
      case IValueMeta.TYPE_NUMBER -> valueMeta.getNumber(valueData);
      case IValueMeta.TYPE_STRING, IValueMeta.TYPE_TIMESTAMP -> valueMeta.getString(valueData);
      default -> null;
    };
  }

  public static Class<?> getPrimitiveClass(int valueType) {
    return switch (valueType) {
      case IValueMeta.TYPE_BIGNUMBER -> BigDecimal.class;
      case IValueMeta.TYPE_BINARY -> byte[].class;
      case IValueMeta.TYPE_BOOLEAN -> Boolean.class;
      case IValueMeta.TYPE_DATE -> Date.class;
      case IValueMeta.TYPE_INTEGER -> Long.class;
      case IValueMeta.TYPE_NUMBER -> Double.class;
      case IValueMeta.TYPE_STRING -> String.class;
      default -> null;
    };
  }
}
