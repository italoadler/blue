/*
 * blue - object composition environment for csound
 * Copyright (C) 2016
 * Steven Yi <stevenyi@gmail.com>
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package blue.orchestra.editor.blueSynthBuilder.jfx;

import blue.orchestra.blueSynthBuilder.BSBGraphicInterface;
import blue.orchestra.blueSynthBuilder.BSBGroup;
import blue.orchestra.blueSynthBuilder.BSBObject;
import blue.orchestra.blueSynthBuilder.BSBObjectEntry;
import blue.orchestra.blueSynthBuilder.GridSettings;
import blue.orchestra.editor.blueSynthBuilder.EditModeOnly;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;
import javafx.beans.InvalidationListener;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.SetChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import org.openide.util.Exceptions;

/**
 *
 * @author stevenyi
 */
public class BSBEditPane extends Pane {

    private static final Color GRID_COLOR
            = Color.rgb(38, 51, 76).brighter();

    private BSBGraphicInterface bsbInterface;
    private BSBEditSelection selection;

    private ContextMenu popupMenu;
    private ContextMenu nonEditPopupMenu;

    private SetChangeListener<BSBObject> scl;

    private Rectangle marquee;
    int addX = 0, addY = 0;

    double startMarqueeX = -1.0;
    double startMarqueeY = -1.0;

    Set<BSBObject> startSet = null;
    Set<BSBObject> selecting = new HashSet<>();

    private Pane interfaceItemsPane;
    private Canvas gridCanvas;
    Pane mousePane = new Pane();

    private InvalidationListener gridListener;

    private BooleanProperty marqueeSelecting;

    final boolean allowEditing;

    ObservableList<BSBGroup> groupsList = FXCollections.observableArrayList();

    public BSBEditPane(BSBObjectEntry[] bsbObjectEntries) {
        this(bsbObjectEntries, true);
    }

    public BSBEditPane(BSBObjectEntry[] bsbObjectEntries, boolean allowEditing) {
        setFocusTraversable(true);
        this.allowEditing = allowEditing;

        gridCanvas = new Canvas();
        interfaceItemsPane = new Pane();

        gridListener = cl -> {
            redrawGrid();
        };

        gridCanvas.setManaged(false);
        gridCanvas.widthProperty().bind(widthProperty());
        gridCanvas.heightProperty().bind(heightProperty());

        widthProperty().addListener(gridListener);
        heightProperty().addListener(gridListener);

        getChildren().addAll(gridCanvas, interfaceItemsPane);

        selection = new BSBEditSelection(interfaceItemsPane.getChildren());

        popupMenu = new ContextMenu();
        EventHandler<ActionEvent> al = e -> {
            MenuItem m = (MenuItem) e.getSource();
            Class<? extends BSBObject> clazz = (Class<? extends BSBObject>) m.getUserData();
            try {
                BSBObject bsbObj = clazz.newInstance();
                bsbObj.setX(addX);
                bsbObj.setY(addY);
                groupsList.get(groupsList.size() - 1).addBSBObject(bsbObj);
            } catch (InstantiationException | IllegalAccessException ex) {
                Exceptions.printStackTrace(ex);
            }
        };

        for (BSBObjectEntry entry : bsbObjectEntries) {
            MenuItem m = new MenuItem("Add " + entry.label);
            m.setUserData(entry.bsbObjectClass);
            m.setOnAction(al);
            popupMenu.getItems().add(m);
        }

        MenuItem paste = new MenuItem("Paste");
        paste.setOnAction(ae -> paste(addX, addY));
        paste.disableProperty().bind(
                Bindings.createBooleanBinding(
                        () -> selection.copyBufferProperty().size() == 0,
                        selection.copyBufferProperty()));
        popupMenu.getItems().addAll(new SeparatorMenuItem(), paste);

        nonEditPopupMenu = new ContextMenu();
        MenuItem randomize = new MenuItem("Randomize");
        randomize.setOnAction(ae -> {
            if (bsbInterface != null) {
                bsbInterface.getRootGroup().randomize();
            }
        });
        nonEditPopupMenu.getItems().add(randomize);

        marquee = new Rectangle();
        marquee.setFill(null);
        marquee.setStroke(Color.WHITE);

        marqueeSelecting = new SimpleBooleanProperty(false);

        setOnMousePressed(me -> {
            if (!me.isConsumed() && bsbInterface != null) {
                if (bsbInterface.isEditEnabled() && allowEditing) {
                    if (me.isSecondaryButtonDown()) {
                        addX = (int) me.getX();
                        addY = (int) me.getY();

                        Iterator<BSBGroup> iter = getGroupsList().iterator();
                        // advance past root
                        iter.next();

                        if (iter.hasNext()) {
                            Point2D localPoint = getPointForBSBGroup(
                                    new Point2D(me.getX(), me.getY()), iter,
                                    interfaceItemsPane.getChildrenUnmodifiable());

                            if (localPoint != null) {
                                addX = (int) localPoint.getX();
                                addY = (int) localPoint.getY();
                            }
                        }
                        popupMenu.show(BSBEditPane.this, me.getScreenX(), me.getScreenY());
                    } else if (me.isPrimaryButtonDown()) {
                        if (!me.isShiftDown()) {
                            selection.selection.clear();
                        }

                        startSet = new HashSet<>(selection.selection);
                        startMarqueeX = me.getX();
                        startMarqueeY = me.getY();
                        setMarqueeSelecting(true);
                        updateMarquee(startMarqueeX, startMarqueeY);
                        getChildren().add(marquee);
                    }
                } else if (me.isSecondaryButtonDown()) {
                    nonEditPopupMenu.show(BSBEditPane.this, me.getScreenX(), me.getScreenY());
                }
            }
        });

        setOnMouseDragged(me
                -> {
            if (startMarqueeX >= 0) {
                updateMarquee(me.getX(), me.getY());
            }
        }
        );

        setOnMouseReleased(me
                -> {
            if (startMarqueeX >= 0) {
                startMarqueeX = -1.0;
                startMarqueeY = -1.0;
                getChildren().remove(marquee);
            }
            setMarqueeSelecting(false);
            BSBEditPane.this.requestFocus();
        }
        );

        scl = sce -> {
            if (sce.wasAdded()) {
                addBSBObject(sce.getElementAdded());
            } else {
                removeBSBObject(sce.getElementRemoved());
            }
        };

        installKeyEventHandler();

    }

    public BSBEditSelection getSelection() {
        return selection;
    }

    public ObservableList<BSBGroup> getGroupsList() {
        return groupsList;
    }

    private void setMarqueeSelecting(boolean val) {
        marqueeSelecting.set(val);
    }

    public boolean isMarqueeSelecting() {
        return marqueeSelecting.get();
    }

    public BooleanProperty marqueeSelectingProperty() {
        return marqueeSelecting;
    }

    public void editBSBGraphicInterface(BSBGraphicInterface bsbInterface) {

        if (this.bsbInterface != null) {
            this.bsbInterface.getRootGroup().interfaceItemsProperty().
                    removeListener(scl);
            GridSettings gridSettings = this.bsbInterface.getGridSettings();
            gridSettings.widthProperty().removeListener(gridListener);
            gridSettings.heightProperty().removeListener(gridListener);
            gridSettings.gridStyleProperty().removeListener(gridListener);
        }

        interfaceItemsPane.getChildren().clear();
        gridCanvas.visibleProperty().unbind();

        this.bsbInterface = bsbInterface;
        selection.initialize(groupsList, bsbInterface.getGridSettings());

        if (bsbInterface != null) {
            for (BSBObject bsbObj : bsbInterface.getRootGroup()) {
                addBSBObject(bsbObj);
            }
            bsbInterface.getRootGroup().interfaceItemsProperty().addListener(scl);
            if (allowEditing) {
                gridCanvas.visibleProperty().bind(bsbInterface.editEnabledProperty());
            } else {
                gridCanvas.setVisible(false);
            }
            GridSettings gridSettings = bsbInterface.getGridSettings();
            gridSettings.widthProperty().addListener(gridListener);
            gridSettings.heightProperty().addListener(gridListener);
            gridSettings.gridStyleProperty().addListener(gridListener);

            groupsList.setAll(bsbInterface.getRootGroup());
        }

    }

    protected void addBSBObject(BSBObject bsbObj) {
        try {
            Region objectView = BSBObjectEditorFactory.getView(bsbObj);
            BooleanProperty editEnabledProperty = allowEditing ? bsbInterface.editEnabledProperty() : null;
            BSBObjectViewHolder viewHolder = new BSBObjectViewHolder(editEnabledProperty,
                    selection, groupsList, objectView);
            if (objectView instanceof EditModeOnly) {
                if (allowEditing) {
                    viewHolder.visibleProperty().bind(bsbInterface.editEnabledProperty());
                } else {
                    viewHolder.setVisible(false);
                }
            }
            if(bsbObj instanceof BSBGroup) {
                BSBGroupView bsbGroupView = (BSBGroupView) objectView;
                bsbGroupView.initialize(editEnabledProperty, selection, groupsList);
            }

            interfaceItemsPane.getChildren().add(viewHolder);
        } catch (Exception e) {
            Exceptions.printStackTrace(e);
        }
    }

    protected void removeBSBObject(BSBObject bsbObj) {
        Node found = null;
        for (Node n : interfaceItemsPane.getChildren()) {
            if (n.getUserData() == bsbObj) {
                found = n;
                break;
            }
        }

        if (found != null) {
            interfaceItemsPane.getChildren().remove(found);
            found.visibleProperty().unbind();
        }
    }

    protected void paste(int x, int y) {
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        GridSettings gridSettings = bsbInterface.getGridSettings();

        if (gridSettings.isSnapEnabled()) {
            x = x - (x % gridSettings.getWidth());
            y = y - (y % gridSettings.getHeight());
        }

        for (BSBObject bsbObj : selection.copyBufferProperty()) {
            minX = Math.min(minX, bsbObj.getX());
            minY = Math.min(minX, bsbObj.getY());
        }

        for (BSBObject bsbObj : selection.copyBufferProperty()) {
            BSBObject copy = bsbObj.deepCopy();
            copy.setX(x + copy.getX() - minX);
            copy.setY(y + copy.getY() - minY);

            groupsList.get(groupsList.size() - 1).addBSBObject(copy);
        }
    }

    protected void updateMarquee(double newMouseX, double newMouseY) {
        double left, right, top, bottom;

        if (newMouseX < startMarqueeX) {
            left = newMouseX;
            right = startMarqueeX;
        } else {
            left = startMarqueeX;
            right = newMouseX;
        }

        if (newMouseY < startMarqueeY) {
            top = newMouseY;
            bottom = startMarqueeY;
        } else {
            bottom = newMouseY;
            top = startMarqueeY;
        }

        top = Math.max(0.0, top);
        left = Math.max(0.0, left);

        double width = right - left;
        double height = bottom - top;

        marquee.setX(left);
        marquee.setY(top);
        marquee.setWidth(width);
        marquee.setHeight(height);

        selecting.clear();

        BSBGroup group = groupsList.get(groupsList.size() - 1);

        ObservableList<Node> bsbItems = interfaceItemsPane.getChildren();

        if(groupsList.size() > 1) {
            Iterator<BSBGroup> iter = groupsList.iterator();
            iter.next();
            bsbItems = getChildrenForBSBGroupList(iter, bsbItems);
        }

        for (Node n : bsbItems) {
            Bounds b = n.getBoundsInParent();
            Node temp = n;
            while(temp.getParent() != marquee.getParent()) {
                b = temp.getParent().localToParent(b);
                temp = temp.getParent();
            }
            
            if (n != marquee && marquee.intersects(b)) {
                Object obj = n.getUserData();
                if (obj instanceof BSBObject) {
                    BSBObject bsbObj = (BSBObject) obj;

                    if (group.contains(bsbObj)) {
                        selecting.add((BSBObject) obj);
                    }
                }
            }
        }

        selecting.addAll(startSet);
        selection.selection.addAll(selecting);
        selection.selection.retainAll(selecting);
    }

    private double snap(double v) {
        return ((int) v) + 0.5;
    }

    private void redrawGrid() {
        int totalWidth = (int) getWidth();
        int totalHeight = (int) getHeight();

        GraphicsContext gc = gridCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, getWidth(), getHeight());

        if (this.bsbInterface == null) {
            return;
        }

        gc.setFill(GRID_COLOR);
        gc.setStroke(GRID_COLOR);
        GridSettings grid = bsbInterface.getGridSettings();

        int w = grid.getWidth();
        int h = grid.getHeight();

        if (w < 1 || h < 1) {
            return;
        }

        switch (grid.getGridStyle()) {
            case DOT:
                for (int x = 0; x < totalWidth; x += w) {
                    for (int y = 0; y < totalHeight; y += h) {
                        gc.strokeRect(snap(x), snap(y), 1, 1);
                    }
                }
                break;
            case LINE:
                for (int x = 0; x < totalWidth; x += w) {
                    gc.strokeLine(snap(x), 0, snap(x), snap(totalHeight));
                }
                for (int y = 0; y < totalHeight; y += h) {
                    gc.strokeLine(0, snap(y), totalWidth, snap(y));
                }
                break;
        }

    }

    private Optional<Integer> sizeOfGridSnap() {
        if (bsbInterface == null || !bsbInterface.getGridSettings().isSnapEnabled()) {
            return Optional.empty();
        }
        return Optional.of(bsbInterface.getGridSettings().getHeight());
    }

    private void installKeyEventHandler() {
        EventHandler<KeyEvent> handler = new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if (event.getSource() != BSBEditPane.this
                        || bsbInterface == null
                        || !bsbInterface.isEditEnabled()
                        || !allowEditing) {
                    return;
                }
                if (event.isControlDown()) {
                    switch (event.getCode()) {
                        case C:
                            selection.copy();
                            event.consume();
                            break;
                        case X:
                            selection.cut();
                            event.consume();
                            break;
                    }
                } else if (event.isShiftDown()) {
                    switch (event.getCode()) {
                        case UP:
                            selection.nudgeVertical(-sizeOfGridSnap().orElse(10));
                            event.consume();
                            break;
                        case DOWN:
                            selection.nudgeVertical(sizeOfGridSnap().orElse(10));
                            event.consume();
                            break;
                        case LEFT:
                            selection.nudgeHorizontal(-sizeOfGridSnap().orElse(10));
                            event.consume();
                            break;
                        case RIGHT:
                            selection.nudgeHorizontal(sizeOfGridSnap().orElse(10));
                            event.consume();
                            break;
                    }
                } else {
                    switch (event.getCode()) {
                        case DELETE:
                        case BACK_SPACE:
                            selection.remove();
                            event.consume();
                            break;
                        case UP:
                            selection.nudgeVertical(-sizeOfGridSnap().orElse(1));
                            event.consume();
                            break;
                        case DOWN:
                            selection.nudgeVertical(sizeOfGridSnap().orElse(1));
                            event.consume();
                            break;
                        case LEFT:
                            selection.nudgeHorizontal(-sizeOfGridSnap().orElse(1));
                            event.consume();
                            break;
                        case RIGHT:
                            selection.nudgeHorizontal(sizeOfGridSnap().orElse(1));
                            event.consume();
                            break;
                    }
                }
            }
        };

        setOnKeyPressed(handler);
    }

    protected static Point2D getPointForBSBGroup(Point2D point,
            Iterator<BSBGroup> groupIterator, ObservableList<Node> children) {

        if (!groupIterator.hasNext()) {
            return null;
        }

        BSBGroup group = groupIterator.next();

        for (Node node : children) {
            if (node instanceof BSBObjectViewHolder) {
                BSBObjectViewHolder holder = (BSBObjectViewHolder) node;
                Region r = holder.getBSBObjectView();

                if (r.getUserData() == group) {
                    Point2D local = holder.parentToLocal(point);
                    BSBGroupView groupView = ((BSBGroupView)r);
                    local = groupView.getEditorPane().parentToLocal(local);
                    if (r.contains(local)) {
                        if (groupIterator.hasNext()) {
                            return getPointForBSBGroup(local, groupIterator,
                                    groupView.getBSBChildNodes());
                        } else {
                            return local;
                        }
                    }
                }
            }
        }
        return null;
    }

    protected static ObservableList<Node> getChildrenForBSBGroupList(
            Iterator<BSBGroup> groupIterator, ObservableList<Node> currentChildren) {
        
        if (!groupIterator.hasNext()) {
            return null;
        }

        BSBGroup group = groupIterator.next();

        for (Node node : currentChildren) {
            if (node instanceof BSBObjectViewHolder) {
                BSBObjectViewHolder holder = (BSBObjectViewHolder) node;
                Region r = holder.getBSBObjectView();

                if (r.getUserData() == group) {
                    if (groupIterator.hasNext()) {
                        return getChildrenForBSBGroupList(groupIterator,
                                ((BSBGroupView)r).getBSBChildNodes());
                    } else {
                        return ((BSBGroupView)r).getBSBChildNodes();
                    }
                }
            }
        }
        return null;
    }
}
