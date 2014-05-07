/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package blue.ui.core.tables;

import blue.Tables;
import blue.projects.BlueProject;
import blue.projects.BlueProjectManager;
import blue.ui.nbutilities.MimeTypeEditorComponent;
import blue.ui.utilities.SimpleDocumentListener;
import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.Serializable;
import java.util.logging.Logger;
import javax.swing.event.DocumentEvent;
import javax.swing.undo.UndoManager;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.UndoRedo;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
//import org.openide.util.Utilities;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(
        dtd = "-//blue.ui.core.tables//Tables//EN",
        autostore = false
)
@TopComponent.Description(
        preferredID = "TablesTopComponent",
        //iconBase="SET/PATH/TO/ICON/HERE", 
        persistenceType = TopComponent.PERSISTENCE_ALWAYS
)
@TopComponent.Registration(mode = "editor", openAtStartup = false)
@ActionID(category = "Window", id = "blue.ui.core.tables.TablesTopComponent")
@ActionReferences({
    @ActionReference(path = "Menu/Window", position = 1350),
    @ActionReference(path = "Shortcuts",
            name = "D-4")
})
@TopComponent.OpenActionRegistration(
        displayName = "#CTL_TablesAction",
        preferredID = "TablesTopComponent"
)
@NbBundle.Messages({
    "CTL_TablesAction=Tables",
    "CTL_TablesTopComponent=Tables",
    "HINT_TablesTopComponent=This is a Tables window"
})
public final class TablesTopComponent extends TopComponent {

    private static TablesTopComponent instance;
    private MimeTypeEditorComponent tablesText = new MimeTypeEditorComponent(
            "text/x-csound-sco");

    private Tables tables = null;

    UndoManager undo = new UndoRedo.Manager();

    private TablesTopComponent() {
        initComponents();
        setName(NbBundle.getMessage(TablesTopComponent.class,
                "CTL_TablesTopComponent"));
        setToolTipText(NbBundle.getMessage(TablesTopComponent.class,
                "HINT_TablesTopComponent"));
//        setIcon(Utilities.loadImage(ICON_PATH, true));

        BlueProjectManager.getInstance().addPropertyChangeListener(
                new PropertyChangeListener() {

                    @Override
                    public void propertyChange(PropertyChangeEvent evt) {
                        if (BlueProjectManager.CURRENT_PROJECT.equals(
                                evt.getPropertyName())) {
                            tables = null;
                            reinitialize();
                        }
                    }
                });

        reinitialize();

        tablesText.getDocument().addDocumentListener(
                new SimpleDocumentListener() {

                    @Override
                    public void documentChanged(DocumentEvent e) {
                        if (tables != null) {
                            tables.setTables(tablesText.getText());
                        }
                    }
                });

        tablesText.setUndoManager(undo);
        tablesText.getDocument().addUndoableEditListener(undo);
        this.add(tablesText, BorderLayout.CENTER);
    }

    private void reinitialize() {
        BlueProject project = BlueProjectManager.getInstance().getCurrentProject();
        if (project == null) {
            tablesText.setText("");
            tablesText.getJEditorPane().setEditable(false);
        } else {
            Tables localTables = project.getData().getTableSet();
            tablesText.setText(localTables.getTables());
            tablesText.getJEditorPane().setEditable(true);
            tables = localTables;
        }
        undo.discardAllEdits();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setLayout(new java.awt.BorderLayout());
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
    public static synchronized TablesTopComponent getDefault() {
        if (instance == null) {
            instance = new TablesTopComponent();
        }
        return instance;
    }

    @Override
    public void componentOpened() {
        // TODO add custom code on component opening
    }

    @Override
    public void componentClosed() {
        // TODO add custom code on component closing
    }

    void writeProperties(java.util.Properties p) {
        p.setProperty("version", "1.0");
    }

    void readProperties(java.util.Properties p) {
        String version = p.getProperty("version");
    }

    /**
     * replaces this in object stream
     */
    @Override
    public Object writeReplace() {
        return new ResolvableHelper();
    }

    final static class ResolvableHelper implements Serializable {

        private static final long serialVersionUID = 1L;

        public Object readResolve() {
            return TablesTopComponent.getDefault();
        }
    }
}
