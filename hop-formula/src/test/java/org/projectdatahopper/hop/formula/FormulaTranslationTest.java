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

import static org.junit.jupiter.api.Assertions.*;

import java.util.Locale;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.projectdatahopper.hop.formula.common.TestFormulaContext;
import org.projectdatahopper.hop.formula.function.FunctionDescription;
import org.projectdatahopper.hop.formula.function.FunctionRegistry;

public class FormulaTranslationTest {
  private FormulaContext context;

  @BeforeEach
  public void setUp() throws Exception {
    context = new TestFormulaContext(TestFormulaContext.testCaseDataset);
  }

  @Test
  public void testTranslationsAvailable() {
    FunctionRegistry registry = context.getFunctionRegistry();
    String[] functions = registry.getFunctionNames();
    for (int i = 0; i < functions.length; i++) {
      String function = functions[i];
      FunctionDescription functionDesc = registry.getMetaData(function);
      assertNotNull(functionDesc, "Missing meta for " + function);
      assertFalse(functionDesc.getDisplayName(Locale.ENGLISH).isEmpty());
      assertFalse(functionDesc.getDescription(Locale.ENGLISH).isEmpty());
      int count = functionDesc.getParameterCount();
      for (int x = 0; x < count; x++) {
        assertNotNull(functionDesc.getParameterDescription(x, Locale.ENGLISH));
        assertFalse(functionDesc.getParameterDisplayName(x, Locale.ENGLISH).isEmpty());
      }
    }
  }
}
