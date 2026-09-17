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

package org.projectdatahopper.hop.pipeline.transforms.pentahoformula;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;
import org.apache.hop.core.CheckResult;
import org.apache.hop.core.ICheckResult;
import org.apache.hop.core.annotations.Transform;
import org.apache.hop.core.exception.HopTransformException;
import org.apache.hop.core.row.IRowMeta;
import org.apache.hop.core.row.IValueMeta;
import org.apache.hop.core.row.value.ValueMetaFactory;
import org.apache.hop.core.util.Utils;
import org.apache.hop.core.variables.IVariables;
import org.apache.hop.i18n.BaseMessages;
import org.apache.hop.metadata.api.HopMetadataProperty;
import org.apache.hop.metadata.api.IHopMetadataProvider;
import org.apache.hop.pipeline.PipelineMeta;
import org.apache.hop.pipeline.transform.BaseTransformMeta;
import org.apache.hop.pipeline.transform.TransformMeta;

/**
 * Metadata for the Pentaho Formula transform (libformula / OpenFormula).
 *
 * <p>Plugin id {@code PentahoFormula} is distinct from Hop's built-in POI {@code Formula}
 * transform. PDI Formula steps are remapped to this id by the Kettle import extension.
 */
@Getter
@Setter
@Transform(
    id = "PentahoFormula",
    image = "pentaho-formula.svg",
    name = "i18n::PentahoFormula.Name",
    description = "i18n::PentahoFormula.Description",
    categoryDescription = "i18n:org.apache.hop.pipeline.transform:BaseTransform.Category.Scripting",
    keywords = "i18n::PentahoFormula.keywords",
    classLoaderGroup = "pentaho-formula")
public class PentahoFormulaMeta extends BaseTransformMeta<PentahoFormula, PentahoFormulaData> {

  private static final Class<?> PKG = PentahoFormulaMeta.class;

  @HopMetadataProperty(
      groupKey = "formulas",
      key = "formula",
      injectionGroupKey = "FORMULAS",
      injectionGroupDescription = "PentahoFormulaMeta.Injection.Formulas")
  private List<PentahoFormulaMetaFunction> formulas;

  public PentahoFormulaMeta() {
    super();
    formulas = new ArrayList<>();
  }

  public PentahoFormulaMeta(PentahoFormulaMeta other) {
    this();
    for (PentahoFormulaMetaFunction formula : other.formulas) {
      this.formulas.add(new PentahoFormulaMetaFunction(formula));
    }
  }

  @Override
  public Object clone() {
    PentahoFormulaMeta copy = (PentahoFormulaMeta) super.clone();
    List<PentahoFormulaMetaFunction> cloned = new ArrayList<>();
    if (formulas != null) {
      for (PentahoFormulaMetaFunction formula : formulas) {
        cloned.add(new PentahoFormulaMetaFunction(formula));
      }
    }
    copy.formulas = cloned;
    return copy;
  }

  @Override
  public boolean supportsErrorHandling() {
    return true;
  }

  @Override
  public void setDefault() {
    formulas = new ArrayList<>();
  }

  @Override
  public void getFields(
      IRowMeta row,
      String name,
      IRowMeta[] info,
      TransformMeta nextTransform,
      IVariables variables,
      IHopMetadataProvider metadataProvider)
      throws HopTransformException {
    for (PentahoFormulaMetaFunction fn : formulas) {
      if (Utils.isEmpty(fn.getReplaceField())) {
        if (!Utils.isEmpty(fn.getFieldName())) {
          try {
            IValueMeta v = ValueMetaFactory.createValueMeta(fn.getFieldName(), fn.getValueType());
            v.setLength(fn.getValueLength(), fn.getValuePrecision());
            v.setOrigin(name);
            row.addValueMeta(v);
          } catch (Exception e) {
            throw new HopTransformException(e);
          }
        }
      } else {
        int index = row.indexOfValue(fn.getReplaceField());
        if (index < 0) {
          throw new HopTransformException(
              BaseMessages.getString(
                  PKG, "PentahoFormulaMeta.Exception.UnknownReplaceField", fn.getReplaceField()));
        }
        IValueMeta v = row.getValueMeta(index).clone();
        v.setLength(fn.getValueLength(), fn.getValuePrecision());
        v.setOrigin(name);
        row.setValueMeta(index, v);
      }
    }
  }

  @Override
  public void check(
      List<ICheckResult> remarks,
      PipelineMeta pipelineMeta,
      TransformMeta transformMeta,
      IRowMeta prev,
      String[] input,
      String[] output,
      IRowMeta info,
      IVariables variables,
      IHopMetadataProvider metadataProvider) {
    if (prev == null || prev.size() == 0) {
      remarks.add(
          new CheckResult(
              ICheckResult.TYPE_RESULT_WARNING,
              BaseMessages.getString(PKG, "PentahoFormulaMeta.CheckResult.ExpectedInputError"),
              transformMeta));
    } else {
      remarks.add(
          new CheckResult(
              ICheckResult.TYPE_RESULT_OK,
              BaseMessages.getString(
                  PKG, "PentahoFormulaMeta.CheckResult.FieldsReceived", "" + prev.size()),
              transformMeta));
    }

    if (input.length > 0) {
      remarks.add(
          new CheckResult(
              ICheckResult.TYPE_RESULT_OK,
              BaseMessages.getString(PKG, "PentahoFormulaMeta.CheckResult.ExpectedInputOk"),
              transformMeta));
    } else {
      remarks.add(
          new CheckResult(
              ICheckResult.TYPE_RESULT_ERROR,
              BaseMessages.getString(PKG, "PentahoFormulaMeta.CheckResult.ExpectedInputError"),
              transformMeta));
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof PentahoFormulaMeta that)) {
      return false;
    }
    return Objects.equals(formulas, that.formulas);
  }

  @Override
  public int hashCode() {
    return Objects.hash(formulas);
  }
}
