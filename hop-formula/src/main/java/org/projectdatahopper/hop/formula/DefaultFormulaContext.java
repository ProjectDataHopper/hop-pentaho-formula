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

package org.projectdatahopper.hop.formula;

import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import org.projectdatahopper.hop.formula.error.FormulaErrorValue;
import org.projectdatahopper.hop.formula.function.DefaultFunctionRegistry;
import org.projectdatahopper.hop.formula.function.FunctionRegistry;
import org.projectdatahopper.hop.formula.typing.DataType;
import org.projectdatahopper.hop.formula.typing.DefaultTypeRegistry;
import org.projectdatahopper.hop.formula.typing.TypeRegistry;

/**
 * Default in-memory FormulaContext implementation.
 */
public class DefaultFormulaContext implements FormulaContext, LocalizationContext {
  private final Locale locale;
  private final TimeZone timeZone;
  private final TypeRegistry typeRegistry;
  private final FunctionRegistry functionRegistry;
  private final Map<String, Object> references;

  public DefaultFormulaContext() {
    this(Locale.US, TimeZone.getDefault());
  }

  public DefaultFormulaContext(Locale locale, TimeZone timeZone) {
    this(locale, timeZone, DefaultFunctionRegistry.getInstance());
  }

  public DefaultFormulaContext(Locale locale, TimeZone timeZone, FunctionRegistry functionRegistry) {
    this.locale = locale != null ? locale : Locale.US;
    this.timeZone = timeZone != null ? timeZone : TimeZone.getDefault();
    this.typeRegistry = new DefaultTypeRegistry(this.locale, this.timeZone);
    this.functionRegistry = functionRegistry;
    this.references = new HashMap<>();
  }

  public void defineReference(String name, Object value) {
    references.put(name, value);
  }

  @Override
  public Object resolveReference(Object name) throws EvaluationException {
    if (name == null) {
      return null;
    }
    String key = name.toString();
    if (!references.containsKey(key)) {
      throw new EvaluationException(FormulaErrorValue.ERROR_REFERENCE_NOT_RESOLVABLE_VALUE);
    }
    return references.get(key);
  }

  @Override
  public DataType resolveReferenceType(Object name) {
    if (name == null) {
      return DataType.ANY;
    }
    Object val = references.get(name.toString());
    return typeRegistry.guessType(val);
  }

  @Override
  public Locale getLocale() {
    return locale;
  }

  @Override
  public TimeZone getTimeZone() {
    return timeZone;
  }

  @Override
  public LocalizationContext getLocalizationContext() {
    return this;
  }

  @Override
  public TypeRegistry getTypeRegistry() {
    return typeRegistry;
  }

  @Override
  public FunctionRegistry getFunctionRegistry() {
    return functionRegistry;
  }
}
