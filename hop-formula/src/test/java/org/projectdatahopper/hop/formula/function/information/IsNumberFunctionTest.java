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

package org.projectdatahopper.hop.formula.function.information;

import org.junit.jupiter.api.Test;
import org.projectdatahopper.hop.formula.common.FormulaTestBase;

public class IsNumberFunctionTest extends FormulaTestBase {

  @Test
  public void testDefault() throws Exception {
    runDefaultTest();
  }

  @Override
  public Object[][] createDataTest() {
    return new Object[][] {
      { "ISNUMBER(1)", Boolean.TRUE },
      { "ISNUMBER(\"1\")", Boolean.FALSE },
    };
  }

  @Test
  public void runAdditionalTest() throws Exception {
    performTest("ISNUMBER(55)", Boolean.TRUE);
    performTest("ISNUMBER(\"55\")", Boolean.FALSE);
  }
}
