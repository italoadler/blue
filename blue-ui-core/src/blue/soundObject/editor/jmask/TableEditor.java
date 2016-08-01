/*
 * blue - object composition environment for csound
 * Copyright (c) 2000-2008 Steven Yi (stevenyi@gmail.com)
 *
 * Based on CMask by Andre Bartetzki
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by  the Free Software Foundation; either version 2 of the License or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; see the file COPYING.LIB.  If not, write to
 * the Free Software Foundation Inc., 59 Temple Place - Suite 330,
 * Boston, MA  02111-1307 USA
 */
package blue.soundObject.editor.jmask;

import blue.components.lines.LineBoundaryDialog;
import blue.soundObject.jmask.Table;
import blue.utility.NumberUtilities;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 *
 * @author  syi
 */
public class TableEditor extends javax.swing.JPanel implements PropertyChangeListener {

    Table table = null;
    
    boolean positivesOnly = false;
    
    boolean updating = false;

    /** Creates new form TableEditor */
    public TableEditor() {
        initComponents();
    }

    @Override
    public void addNotify() {
        super.addNotify();

        if (this.table != null) {
            this.table.addPropertyChangeListener(this);
        }
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        
        if (this.table != null) {
            this.table.removePropertyChangeListener(this);
        }
    }
    
    public void setDuration(double duration) {
        this.tableCanvas.setDuration(duration);
    }

    public void setPositiveValues() {
        positivesOnly = true;
    }
    
    public void setMinMaxEnabled(boolean enabled) {
//        minLabel.setVisible(enabled);
//        minSpinner.setVisible(enabled);
//        maxLabel.setVisible(enabled);
//        maxSpinner.setVisible(enabled);
        
        minTextField.setText("");
        maxTextField.setText("");
        
        minTextField.setEnabled(enabled);
        maxTextField.setEnabled(enabled);
    }

    public void setTable(Table table) {

        if (this.table != null) {
            this.table.removePropertyChangeListener(this);
        }

        this.table = null;

        interpolationComboBox.setSelectedIndex(table.getInterpolationType());
        interpolationSpinner.setValue(new Double(table.getInterpolation()));
        minTextField.setText(NumberUtilities.formatDouble(table.getMin()));
        maxTextField.setText(NumberUtilities.formatDouble(table.getMax()));

        this.table = table;

        if (this.table != null) {
            this.table.addPropertyChangeListener(this);
        }

        this.tableCanvas.setTable(table);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        tableCanvas = new blue.soundObject.editor.jmask.TableCanvas();
        interpolationComboBox = new javax.swing.JComboBox();
        interpolationSpinner = new javax.swing.JSpinner();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        minLabel = new javax.swing.JLabel();
        maxLabel = new javax.swing.JLabel();
        minTextField = new javax.swing.JTextField();
        maxTextField = new javax.swing.JTextField();

        javax.swing.GroupLayout tableCanvasLayout = new javax.swing.GroupLayout(tableCanvas);
        tableCanvas.setLayout(tableCanvasLayout);
        tableCanvasLayout.setHorizontalGroup(
            tableCanvasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 659, Short.MAX_VALUE)
        );
        tableCanvasLayout.setVerticalGroup(
            tableCanvasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );

        interpolationComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Off", "On", "Cos" }));
        interpolationComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                interpolationComboBoxActionPerformed(evt);
            }
        });

        interpolationSpinner.setModel(new javax.swing.SpinnerNumberModel(
            new Double(0.0d), null,
            null, new Double(0.1d)));
    interpolationSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
        public void stateChanged(javax.swing.event.ChangeEvent evt) {
            interpolationSpinnerStateChanged(evt);
        }
    });

    jLabel1.setText("Interpolation Type");

    jLabel2.setText("Interpolation Value");

    minLabel.setText("Min");

    maxLabel.setText("Max");

    minTextField.setText("jTextField1");
    minTextField.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            minTextFieldActionPerformed(evt);
        }
    });
    minTextField.addFocusListener(new java.awt.event.FocusAdapter() {
        public void focusLost(java.awt.event.FocusEvent evt) {
            minTextFieldFocusLost(evt);
        }
    });

    maxTextField.setText("jTextField2");
    maxTextField.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            maxTextFieldActionPerformed(evt);
        }
    });
    maxTextField.addFocusListener(new java.awt.event.FocusAdapter() {
        public void focusLost(java.awt.event.FocusEvent evt) {
            maxTextFieldFocusLost(evt);
        }
    });

    javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
    this.setLayout(layout);
    layout.setHorizontalGroup(
        layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addComponent(tableCanvas, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        .addGroup(layout.createSequentialGroup()
            .addComponent(jLabel1)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(interpolationComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 77, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(jLabel2)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(interpolationSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 77, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
            .addComponent(minLabel)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(minTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
            .addComponent(maxLabel)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(maxTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 77, javax.swing.GroupLayout.PREFERRED_SIZE))
    );

    layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {interpolationComboBox, interpolationSpinner, maxTextField, minTextField});

    layout.setVerticalGroup(
        layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(layout.createSequentialGroup()
            .addComponent(tableCanvas, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(jLabel1)
                .addComponent(interpolationComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jLabel2)
                .addComponent(interpolationSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(minLabel)
                .addComponent(maxLabel)
                .addComponent(minTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(maxTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
    );
    }// </editor-fold>//GEN-END:initComponents
    private void interpolationComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_interpolationComboBoxActionPerformed
        if (this.table != null) {
            this.table.setInterpolationType(interpolationComboBox.getSelectedIndex());
        }
}//GEN-LAST:event_interpolationComboBoxActionPerformed

    private void interpolationSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_interpolationSpinnerStateChanged
        if (this.table != null) {
            this.table.setInterpolation(((Double) interpolationSpinner.getValue()).doubleValue());
        }
    }//GEN-LAST:event_interpolationSpinnerStateChanged

    private void maxTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_maxTextFieldActionPerformed
        updateMax();
    }//GEN-LAST:event_maxTextFieldActionPerformed

    private void minTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_minTextFieldActionPerformed
        updateMin();
    }//GEN-LAST:event_minTextFieldActionPerformed

    private void minTextFieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_minTextFieldFocusLost
        if(!updating) {
            minTextField.setText(NumberUtilities.formatDouble(this.table.getMin()));
        }
    }//GEN-LAST:event_minTextFieldFocusLost

    private void maxTextFieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_maxTextFieldFocusLost
        if(!updating) {
            maxTextField.setText(NumberUtilities.formatDouble(this.table.getMax()));
        }
    }//GEN-LAST:event_maxTextFieldFocusLost
   
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox interpolationComboBox;
    private javax.swing.JSpinner interpolationSpinner;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel maxLabel;
    private javax.swing.JTextField maxTextField;
    private javax.swing.JLabel minLabel;
    private javax.swing.JTextField minTextField;
    private blue.soundObject.editor.jmask.TableCanvas tableCanvas;
    // End of variables declaration//GEN-END:variables
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getSource() == this.table) {
            repaint();
        }
    }

    private void updateMax() {
        if(updating) {
            return;
        }

        double newValue;
        
        updating = true;
        
        try {
            newValue = Double.parseDouble(maxTextField.getText());
        } catch (NumberFormatException nfe) {
            maxTextField.setText(NumberUtilities.formatDouble(this.table.getMax()));
            updating = false;
            return;
        }
        
        if(newValue <= this.table.getMin()) {
            maxTextField.setText(NumberUtilities.formatDouble(this.table.getMax()));
            updating = false;
            return;
        } else if (positivesOnly && newValue <= 0) {
            maxTextField.setText(NumberUtilities.formatDouble(this.table.getMax()));
            updating = false;
            return;
        }
        
        String retVal = LineBoundaryDialog.getLinePointMethod();

        if (retVal == null) {
            maxTextField.setText(NumberUtilities.formatDouble(this.table.getMax()));
            updating = false;
            return;
        }
            
        boolean truncate = retVal.equals(LineBoundaryDialog.TRUNCATE);
            
        this.table.setMax(newValue, truncate);
        
        updating = false;
    }

    private void updateMin() {
        if(updating) {
            return;
        }
        
        double newValue;
        
        updating = true;

        try {
            newValue = Double.parseDouble(minTextField.getText());
        } catch (NumberFormatException nfe) {
            minTextField.setText(NumberUtilities.formatDouble(this.table.getMin()));
            updating = false;
            return;
        }
        
        if(newValue >= this.table.getMax()) {
            minTextField.setText(NumberUtilities.formatDouble(this.table.getMin()));
            updating = false;
            return;
        } else if (positivesOnly && newValue <= 0) {
            minTextField.setText(NumberUtilities.formatDouble(this.table.getMin()));
            updating = false;
            return;
        }
        
        String retVal = LineBoundaryDialog.getLinePointMethod();

        if (retVal == null) {
            minTextField.setText(NumberUtilities.formatDouble(this.table.getMin()));
            updating = false;
            return;
        }
            
        boolean truncate = retVal.equals(LineBoundaryDialog.TRUNCATE);
            
        this.table.setMin(newValue, truncate);
        
        updating = false;
    }
}
