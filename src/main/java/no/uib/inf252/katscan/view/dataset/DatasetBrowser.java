package no.uib.inf252.katscan.view.dataset;

import java.awt.EventQueue;
import java.awt.Rectangle;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import javax.swing.SwingUtilities;
import javax.swing.event.EventListenerList;
import javax.swing.plaf.basic.BasicTreeUI;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import no.uib.inf252.katscan.data.LoadedData;
import no.uib.inf252.katscan.event.DataHolderListener;
import no.uib.inf252.katscan.event.DatasetBrowserListener;
import no.uib.inf252.katscan.event.KatViewListener;
import no.uib.inf252.katscan.model.DataFile;
import no.uib.inf252.katscan.model.KatNode;
import no.uib.inf252.katscan.model.Project;
import no.uib.inf252.katscan.model.KatView;
import no.uib.inf252.katscan.view.KatViewHandler;

/**
 *
 * @author Marcelo Lima
 */
public class DatasetBrowser extends javax.swing.JPanel implements DataHolderListener, KatViewListener {

    private Project project;
    private final EventListenerList listenerList;

    /**
     * Creates new form DatasetBrowser
     */
    public DatasetBrowser() {
        initComponents();
        
        project = new Project();
        listenerList = new EventListenerList();
        
        DatasetBrowserRenderer renderer = new DatasetBrowserRenderer();
        treDatasets.setCellRenderer(renderer);
        treDatasets.setShowsRootHandles(false);
        treDatasets.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        treDatasets.setModel(new DefaultTreeModel(project));
        treDatasets.setSelectionRow(0);

        treDatasets.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    showPopup(e.getX(), e.getY());
                }
            }
        });

        treDatasets.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                if (e.getKeyChar() == ' ') {
                    TreePath node = treDatasets.getSelectionPath();
                    if (node == null) {
                        return;
                    }
                    
                    Rectangle pathBounds = treDatasets.getPathBounds(node);
                    showPopup(pathBounds.x + pathBounds.width / 2, pathBounds.y + pathBounds.height / 2);
                }
            }
        });
        
        LoadedData.getInstance().addDataHolderListener(this);
        KatViewHandler.getInstance().addKatViewListener(this);
    }
    
    public void focusTree() {
        treDatasets.requestFocusInWindow();
    }
    
    public synchronized void addDatasetBrowserListener(DatasetBrowserListener listener) {
        if (listener == null) {
            return;
        }
        
        listenerList.add(DatasetBrowserListener.class, listener);
        listener.treeChanged(project);
    }
    
    public synchronized void removeDatasetBrowserListener(DatasetBrowserListener listener) {
        if (listener == null) {
            return;
        }
        
        listenerList.remove(DatasetBrowserListener.class, listener);
    }
    
    private void showPopup(int x, int y) {
        TreePath path = treDatasets.getPathForLocation(x, y);
        if (path == null) {
            return;
        }

        KatNode node = (KatNode) path.getLastPathComponent();
        node.getPopupMenu().show(treDatasets, x, y);
    }
    
    @Override
    public void dataAdded(String name, String file) {
        DefaultTreeModel model = (DefaultTreeModel) treDatasets.getModel();
        model.insertNodeInto(new DataFile(new File(file)), project, project.getChildCount());
        treDatasets.expandRow(0);
        fireTreeChanged();
    }

    @Override
    public void dataRemoved(String name) {
        for (int i = 0; i < project.getChildCount(); i++) {
            DataFile node = project.getChildAt(i);
            if (node.equals(name)) {
                DefaultTreeModel model = (DefaultTreeModel) treDatasets.getModel();
                model.removeNodeFromParent(node);
                fireTreeChanged();
                return;
            }
        }
    }
    
    private void fireTreeChanged() {
        DatasetBrowserListener[] listeners = listenerList.getListeners(DatasetBrowserListener.class);

        for (final DatasetBrowserListener listener : listeners) {
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    listener.treeChanged(project);
                }
            });
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnlMain = new javax.swing.JPanel();
        scrDatasets = new javax.swing.JScrollPane();
        treDatasets = new javax.swing.JTree();

        setLayout(new java.awt.BorderLayout());

        scrDatasets.setViewportView(treDatasets);

        javax.swing.GroupLayout pnlMainLayout = new javax.swing.GroupLayout(pnlMain);
        pnlMain.setLayout(pnlMainLayout);
        pnlMainLayout.setHorizontalGroup(
            pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlMainLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(scrDatasets, javax.swing.GroupLayout.DEFAULT_SIZE, 303, Short.MAX_VALUE)
                .addContainerGap())
        );
        pnlMainLayout.setVerticalGroup(
            pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlMainLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(scrDatasets, javax.swing.GroupLayout.DEFAULT_SIZE, 288, Short.MAX_VALUE)
                .addContainerGap())
        );

        add(pnlMain, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel pnlMain;
    private javax.swing.JScrollPane scrDatasets;
    private javax.swing.JTree treDatasets;
    // End of variables declaration//GEN-END:variables

    @Override
    public void viewAddRequested(KatView view) {}

    @Override
    public void viewAdded(KatView view) {
        DefaultTreeModel model = (DefaultTreeModel) treDatasets.getModel();
        model.insertNodeInto(view, view.getParent(), view.getParent().getChildCount());
        fireTreeChanged();
    }

    @Override
    public void viewRemoved(KatView view) {
        DefaultTreeModel model = (DefaultTreeModel) treDatasets.getModel();
        model.removeNodeFromParent(view);
        fireTreeChanged();
    }

}
