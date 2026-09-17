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
import org.apache.hop.core.Const;
import org.apache.hop.core.exception.HopTransformException;
import org.apache.hop.core.row.IRowMeta;
import org.apache.hop.core.row.value.ValueMetaFactory;
import org.apache.hop.core.util.Utils;
import org.apache.hop.core.variables.IVariables;
import org.apache.hop.i18n.BaseMessages;
import org.apache.hop.pipeline.PipelineMeta;
import org.apache.hop.pipeline.transform.TransformMeta;
import org.apache.hop.ui.core.ConstUi;
import org.apache.hop.ui.core.PropsUi;
import org.apache.hop.ui.core.dialog.BaseDialog;
import org.apache.hop.ui.core.dialog.ErrorDialog;
import org.apache.hop.ui.core.widget.ColumnInfo;
import org.apache.hop.ui.core.widget.TableView;
import org.apache.hop.ui.pipeline.transform.BaseTransformDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.projectdatahopper.hop.pipeline.transforms.pentahoformula.editor.PentahoFormulaEditor;

/**
 * Table-only dialog: name line, formula table, OK/Cancel pinned at the bottom (Hop 2.19 {@code
 * createShell}/{@code buildButtonBar}).
 */
public class PentahoFormulaDialog extends BaseTransformDialog {
  private static final Class<?> PKG = PentahoFormulaDialog.class;

  private TableView wFields;
  private final PentahoFormulaMeta currentMeta;
  private final PentahoFormulaMeta originalMeta;
  private final List<String> inputFields = new ArrayList<>();
  private ColumnInfo[] colinf;
  private String[] fieldNames;

  public PentahoFormulaDialog(
      Shell parent,
      IVariables variables,
      PentahoFormulaMeta transformMeta,
      PipelineMeta pipelineMeta) {
    super(parent, variables, transformMeta, pipelineMeta);
    currentMeta = transformMeta;
    originalMeta = (PentahoFormulaMeta) transformMeta.clone();
  }

  @Override
  public String open() {
    createShell(BaseMessages.getString(PKG, "PentahoFormulaDialog.Shell.Title"));
    buildButtonBar().ok(e -> ok()).cancel(e -> cancel()).build();

    ModifyListener lsMod = e -> currentMeta.setChanged();
    changed = currentMeta.hasChanged();

    Control lastControl = wSpacer;

    Label wlFields = new Label(shell, SWT.NONE);
    wlFields.setText(BaseMessages.getString(PKG, "PentahoFormulaDialog.Fields.Label"));
    PropsUi.setLook(wlFields);
    FormData fdlFields = new FormData();
    fdlFields.left = new FormAttachment(0, 0);
    fdlFields.top = new FormAttachment(lastControl, margin);
    wlFields.setLayoutData(fdlFields);

    int fieldsRows = currentMeta.getFormulas() != null ? currentMeta.getFormulas().size() : 1;

    colinf =
        new ColumnInfo[] {
          new ColumnInfo(
              BaseMessages.getString(PKG, "PentahoFormulaDialog.NewField.Column"),
              ColumnInfo.COLUMN_TYPE_TEXT,
              false),
          new ColumnInfo(
              BaseMessages.getString(PKG, "PentahoFormulaDialog.Formula.Column"),
              ColumnInfo.COLUMN_TYPE_TEXT,
              false),
          new ColumnInfo(
              BaseMessages.getString(PKG, "PentahoFormulaDialog.ValueType.Column"),
              ColumnInfo.COLUMN_TYPE_CCOMBO,
              ValueMetaFactory.getValueMetaNames()),
          new ColumnInfo(
              BaseMessages.getString(PKG, "PentahoFormulaDialog.Length.Column"),
              ColumnInfo.COLUMN_TYPE_TEXT,
              false),
          new ColumnInfo(
              BaseMessages.getString(PKG, "PentahoFormulaDialog.Precision.Column"),
              ColumnInfo.COLUMN_TYPE_TEXT,
              false),
          new ColumnInfo(
              BaseMessages.getString(PKG, "PentahoFormulaDialog.Replace.Column"),
              ColumnInfo.COLUMN_TYPE_CCOMBO),
        };

    wFields =
        new TableView(
            variables,
            shell,
            SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI,
            colinf,
            Math.max(fieldsRows, 1),
            lsMod,
            props);

    FormData fdFields = new FormData();
    fdFields.left = new FormAttachment(0, 0);
    fdFields.top = new FormAttachment(wlFields, margin);
    fdFields.right = new FormAttachment(100, 0);
    fdFields.bottom = new FormAttachment(wOk, -margin);
    wFields.setLayoutData(fdFields);

    new Thread(
            () -> {
              TransformMeta transformMeta = pipelineMeta.findTransform(transformName);
              if (transformMeta != null) {
                try {
                  IRowMeta row = pipelineMeta.getPrevTransformFields(variables, transformMeta);
                  for (int i = 0; i < row.size(); i++) {
                    inputFields.add(row.getValueMeta(i).getName());
                  }
                  setComboBoxes();
                } catch (HopTransformException e) {
                  logError(BaseMessages.getString(PKG, "PentahoFormulaDialog.Log.UnableToFindInput"));
                }
              }
            })
        .start();

    colinf[1].setSelectionAdapter(
        new SelectionAdapter() {
          @Override
          public void widgetSelected(SelectionEvent e) {
            if (fieldNames == null) {
              return;
            }
            TableView tv = (TableView) e.widget;
            TableItem item = tv.table.getItem(e.y);
            String formula = item.getText(e.x);
            try {
              if (!shell.isDisposed()) {
                PentahoFormulaEditor editor =
                    new PentahoFormulaEditor(
                        variables,
                        shell,
                        SWT.APPLICATION_MODAL | SWT.SHEET,
                        Const.NVL(formula, ""),
                        fieldNames);
                formula = editor.open();
                if (formula != null && !tv.isDisposed()) {
                  tv.setText(formula, e.x, e.y);
                }
              }
            } catch (Exception ex) {
              new ErrorDialog(
                  shell,
                  BaseMessages.getString(PKG, "PentahoFormulaDialog.EditorError.Title"),
                  BaseMessages.getString(PKG, "PentahoFormulaDialog.EditorError.Message"),
                  ex);
            }
          }
        });

    wFields.addModifyListener(arg0 -> shell.getDisplay().asyncExec(this::setComboBoxes));

    getData();
    currentMeta.setChanged(changed);
    focusTransformName();
    BaseDialog.defaultShellHandling(shell, c -> ok(), c -> cancel());
    return transformName;
  }

  protected void setComboBoxes() {
    shell
        .getDisplay()
        .syncExec(
            () -> {
              String[] names = ConstUi.sortFieldNames(inputFields);
              colinf[5].setComboValues(names);
              PentahoFormulaDialog.this.fieldNames = names;
            });
  }

  public void getData() {
    if (currentMeta.getFormulas() != null) {
      for (int i = 0; i < currentMeta.getFormulas().size(); i++) {
        PentahoFormulaMetaFunction fn = currentMeta.getFormulas().get(i);
        TableItem item = wFields.table.getItem(i);
        item.setText(1, Const.NVL(fn.getFieldName(), ""));
        item.setText(2, Const.NVL(fn.getFormula(), ""));
        item.setText(3, Const.NVL(ValueMetaFactory.getValueMetaName(fn.getValueType()), ""));
        if (fn.getValueLength() >= 0) {
          item.setText(4, "" + fn.getValueLength());
        }
        if (fn.getValuePrecision() >= 0) {
          item.setText(5, "" + fn.getValuePrecision());
        }
        item.setText(6, Const.NVL(fn.getReplaceField(), ""));
      }
    }
    wFields.setRowNums();
    wFields.optWidth(true);
  }

  private void cancel() {
    transformName = null;
    currentMeta.setChanged(changed);
    dispose();
  }

  private void ok() {
    if (Utils.isEmpty(wTransformName.getText())) {
      return;
    }
    transformName = wTransformName.getText();
    currentMeta.getFormulas().clear();
    int nrNonEmptyFields = wFields.nrNonEmpty();
    for (int i = 0; i < nrNonEmptyFields; i++) {
      TableItem item = wFields.getNonEmpty(i);
      currentMeta
          .getFormulas()
          .add(
              new PentahoFormulaMetaFunction(
                  item.getText(1),
                  item.getText(2),
                  ValueMetaFactory.getIdForValueMeta(item.getText(3)),
                  Const.toInt(item.getText(4), -1),
                  Const.toInt(item.getText(5), -1),
                  item.getText(6)));
    }
    if (!originalMeta.equals(currentMeta)) {
      currentMeta.setChanged();
    }
    dispose();
  }
}
