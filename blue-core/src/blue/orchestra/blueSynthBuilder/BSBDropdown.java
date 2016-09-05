/*
 * blue - object composition environment for csound
 * Copyright (c) 2000-2004 Steven Yi (stevenyi@gmail.com)
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
package blue.orchestra.blueSynthBuilder;

//import blue.orchestra.editor.blueSynthBuilder.BSBDropdownView;
//import blue.orchestra.editor.blueSynthBuilder.BSBObjectView;
import blue.automation.Parameter;
import blue.automation.ParameterListener;
import blue.automation.ParameterTimeManagerFactory;
import blue.utility.XMLUtilities;
import electric.xml.Element;
import electric.xml.Elements;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * @author Steven Yi
 */
public class BSBDropdown extends AutomatableBSBObject implements
        ParameterListener, Randomizable, Externalizable {

    private ObservableList<BSBDropdownItem> dropdownItems;
    private IntegerProperty selectedIndex;
    private BooleanProperty randomizable;

    public BSBDropdown() {
        dropdownItems = FXCollections.observableArrayList();
        selectedIndex = new SimpleIntegerProperty(-1);
        randomizable = new SimpleBooleanProperty(true);
    }

    public static BSBObject loadFromXML(Element data) {
        BSBDropdown dropDown = new BSBDropdown();
        initBasicFromXML(data, dropDown);

        Elements nodes = data.getElements();

        while (nodes.hasMoreElements()) {
            Element node = nodes.next();
            String nodeName = node.getName();
            Elements subNodes;
            switch (nodeName) {
                case "bsbDropdownItemList":
                    subNodes = node.getElements();
                    while (subNodes.hasMoreElements()) {
                        dropDown.dropdownItems.add(BSBDropdownItem.loadFromXML(subNodes.next()));
                    }
                    break;
                case "selectedIndex":
                    dropDown.setSelectedIndex(Integer.parseInt(node.getTextString()));
                    break;
                case "randomizable":
                    dropDown.setRandomizable(XMLUtilities.readBoolean(node));
                    break;
            }
        }

        return dropDown;
    }

    @Override
    public Element saveAsXML() {
        Element retVal = getBasicXML(this);

        retVal.addElement("selectedIndex").setText(
                Integer.toString(this.getSelectedIndex()));
        retVal.addElement(XMLUtilities.writeBoolean("randomizable",
                isRandomizable()));
        Element items = retVal.addElement("bsbDropdownItemList");

        for (BSBDropdownItem item : dropdownItems) {
            items.addElement(item.saveAsXML());
        }

        return retVal;
    }

    /*
     * (non-Javadoc)
     * 
     * @see blue.orchestra.blueSynthBuilder.BSBObject#setupForCompilation(blue.orchestra.blueSynthBuilder.BSBCompilationUnit)
     */
    @Override
    public void setupForCompilation(BSBCompilationUnit compilationUnit) {

        if (parameters != null) {
            Parameter param = parameters.getParameter(this.getObjectName());
            if (param != null && param.getCompilationVarName() != null) {
                compilationUnit.addReplacementValue(objectName, param
                        .getCompilationVarName());
                return;
            }
        }

        if (dropdownItems.size() == 0) {
            compilationUnit.addReplacementValue(objectName, "0");
        } else {

            String replaceVal;

            if (isAutomationAllowed()) {
                replaceVal = "" + selectedIndex;
            } else {
                BSBDropdownItem item = dropdownItems.get(getSelectedIndex());
                replaceVal = item.getValue();
            }

            compilationUnit.addReplacementValue(objectName, replaceVal);
        }

    }

    public ObservableList<BSBDropdownItem> dropdownItemsProperty() {
        return dropdownItems;
    }

    /**
     * @return Returns the selectedIndex.
     */
    public int getSelectedIndex() {
        return selectedIndex.get();
    }

    /**
     * @param selectedIndex The selectedIndex to set.
     */
    public void setSelectedIndex(int selectedIndex) {
        int tempIndex = selectedIndex;
        if (tempIndex >= dropdownItems.size()) {
            tempIndex = dropdownItems.size() - 1;
        }

        int oldValue = getSelectedIndex();
        this.selectedIndex.set(tempIndex);

        if (parameters != null) {
            Parameter param = parameters.getParameter(this.getObjectName());
            if (param != null) {
                param.setValue(selectedIndex);
            }
        }

        if (propListeners != null) {
            propListeners.firePropertyChange("value", new Integer(oldValue),
                    new Integer(selectedIndex));
        }
    }

    public IntegerProperty selectedIndexProperty() {
        return selectedIndex;
    }

    public final void setRandomizable(boolean value) {
        randomizable.set(value);
        fireBSBObjectChanged();
    }

    @Override
    public final boolean isRandomizable() {
        return randomizable.get();
    }

    public final BooleanProperty randomizableProperty() {
        return randomizable;
    }

    /*
     * (non-Javadoc)
     * 
     * @see blue.orchestra.blueSynthBuilder.BSBObject#getPresetValue()
     */
    @Override
    public String getPresetValue() {
        BSBDropdownItem item = dropdownItems.get(getSelectedIndex());
        return "id:" + item.getUniqueId();
    }

    /*
     * (non-Javadoc)
     * 
     * @see blue.orchestra.blueSynthBuilder.BSBObject#setPresetValue(java.lang.String)
     */
    @Override
    public void setPresetValue(String val) {
        if (val.startsWith("id:")) {
            String uniqueId = val.substring(3);
            int index = getIndexOfItemByUniqueId(uniqueId);

            if (index >= 0) {
                setSelectedIndex(index);
            }
        } else {
            setSelectedIndex(Integer.parseInt(val));
        }
    }

    protected int getIndexOfItemByUniqueId(String uniqueId) {
        if (uniqueId == null) {
            return -1;
        }
        for (int i = 0; i < dropdownItems.size(); i++) {
            BSBDropdownItem item = dropdownItems.get(i);
            if (uniqueId.equals(item.getUniqueId())) {
                return i;
            }
        }
        return -1;
    }

    /* RANDOMIZABLE METHODS */
    @Override
    public void randomize() {
        if (isRandomizable()) {

            int randomIndex = (int) (Math.random() * dropdownItems.size());

            if (randomIndex != this.getSelectedIndex()) {
                int oldIndex = getSelectedIndex();
                setSelectedIndex(randomIndex);

                if (propListeners != null) {
                    propListeners.firePropertyChange("selectedIndex", oldIndex,
                            randomIndex);
                }
            }
        }
    }

    /* AUTOMATABLE METHODS */
    @Override
    protected void initializeParameters() {
        if (parameters == null) {
            return;
        }

        if (!automationAllowed) {
            if (objectName != null && objectName.length() != 0) {
                Parameter param = parameters.getParameter(objectName);
                if (param != null && param.isAutomationEnabled()) {
                    automationAllowed = true;
                } else {
                    parameters.removeParameter(objectName);
                    return;
                }
            }
        }

        if (this.objectName == null || this.objectName.trim().length() == 0) {
            return;
        }

        Parameter parameter = parameters.getParameter(this.objectName);

        if (parameter != null) {
            parameter.addParameterListener(this);

            if (!parameter.isAutomationEnabled()) {
                parameter.setValue(getSelectedIndex());
            }

            return;
        }

        Parameter param = new Parameter();
        param.setValue(getSelectedIndex());
        param.setMax(dropdownItems.size() - 1, true);
        param.setMin(0.0f, true);
        param.setName(getObjectName());
        param.setResolution(1.0f);
        param.addParameterListener(this);

        parameters.addParameter(param);
    }

    @Override
    public void setAutomationAllowed(boolean allowAutomation) {
        this.automationAllowed = allowAutomation;

        if (parameters != null) {
            if (allowAutomation) {
                initializeParameters();
            } else if (objectName != null && objectName.length() != 0) {
                parameters.removeParameter(objectName);
            }
        }
    }

    @Override
    public void parameterChanged(Parameter param) {
    }

    private void updateSelectedIndex(int index) {
        int oldValue = getSelectedIndex();
        setSelectedIndex(index);

        if (propListeners != null) {
            propListeners.firePropertyChange("updateValue",
                    new Integer(oldValue), new Integer(index));
        }
    }

    @Override
    public void lineDataChanged(Parameter param) {
        Parameter parameter = parameters.getParameter(this.objectName);

        if (parameter != null) {
            float time = ParameterTimeManagerFactory.getInstance().getTime();
            int val = Math.round(parameter.getLine().getValue(time));

            updateSelectedIndex(val);
        }
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeUTF(getObjectName());
        out.writeInt(getSelectedIndex());
        out.writeBoolean(isRandomizable());
        out.writeInt(dropdownItems.size());
        for(BSBDropdownItem item : dropdownItems) {
            out.writeObject(item);
        }
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        setObjectName(in.readUTF());
        setSelectedIndex(in.readInt());
        setRandomizable(in.readBoolean());

        int itemCount = in.readInt();
        for(int i = 0; i < itemCount; i++) {
            dropdownItems.add((BSBDropdownItem)in.readObject());
        }
    }
}
