package javaanpr.gui.windows;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
// Use javax.swing.GroupLayout instead of org.jdesktop if possible, 
// but keeping org.jdesktop to match your existing setup.
import org.jdesktop.layout.GroupLayout; 
import org.jdesktop.layout.LayoutStyle;

import javaanpr.gui.tools.FileListModel;
import javaanpr.gui.tools.ImageFileFilter;
import javaanpr.analysis.*;

public class FrameMain extends javax.swing.JFrame {
    static final long serialVersionUID = 0;
    private String recordUrl = null;
    
    // NEW: Variable for IoU Label
    private javax.swing.JLabel iouScoreLabel;
        
    public class RecognizeThread extends Thread {
        FrameMain parentFrame = null;
        
        public RecognizeThread(FrameMain parentFrame) {
            this.parentFrame = parentFrame;
        }
        public void run() {
            this.parentFrame.recognitionLabel.setText("processing ...");
            this.parentFrame.iouScoreLabel.setText("IoU: ...");
            
            Core core = new Core();
            try {
                int width = parentFrame.panelCar.getWidth();
                int height = parentFrame.panelCar.getHeight();
                BufferedImage resizeImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
                Graphics2D graphics2D = resizeImg.createGraphics();
                
                // --- CHANGED LOGIC START ---
                // We now expect a RecognitionResult object
                RecognitionResult result = core.runCore(recordUrl);
                
                // Draw the returned image
                graphics2D.drawImage(result.image, 0, 0, width, height, null);
                this.parentFrame.panelCarContent = resizeImg;
                parentFrame.panelCar.paint(this.parentFrame.panelCar.getGraphics());
                
                // Update Text
                this.parentFrame.recognitionLabel.setText("Done");
                
                // Update IoU Label
                DecimalFormat df = new DecimalFormat("#.###");
                this.parentFrame.iouScoreLabel.setText("IoU Score: " + df.format(result.iouScore));
                
                // Color Code (Green = Good Match, Red = Bad Match)
                if(result.iouScore >= 0.5) {
                    this.parentFrame.iouScoreLabel.setForeground(new Color(0, 153, 0)); // Dark Green
                } else {
                    this.parentFrame.iouScoreLabel.setForeground(Color.RED);
                }
                // --- CHANGED LOGIC END ---
                
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public class LoadImageThread extends Thread {
        FrameMain parentFrame = null;
        String url = null;
        public LoadImageThread(FrameMain parentFrame, String url) {
            this.parentFrame = parentFrame;
            this.url = url;
            recordUrl = url;
        }
        public void run() {
            try {
                int width = parentFrame.panelCar.getWidth();
                int height = parentFrame.panelCar.getHeight();
                BufferedImage resizeImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
                Graphics2D graphics2D = resizeImg.createGraphics();
                BufferedImage oriImg = ImageIO.read(new File(this.url));
                graphics2D.drawImage(oriImg, 0, 0, width, height, null);
                this.parentFrame.panelCarContent = resizeImg;
                parentFrame.panelCar.paint(this.parentFrame.panelCar.getGraphics());
                
                // Reset Label when loading new image
                this.parentFrame.iouScoreLabel.setText("IoU Score: -");
                this.parentFrame.iouScoreLabel.setForeground(Color.BLACK);
                
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    BufferedImage panelCarContent;
    JFileChooser fileChooser;
    private FileListModel fileListModel;
    int selectedIndex = -1;
    
    public FrameMain() {
        initComponents();
        
        // init : file chooser
        this.fileChooser = new JFileChooser();
        this.fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        this.fileChooser.setFileFilter(new ImageFileFilter());
        
        // init : window dimensions and visibility
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int width = this.getWidth();
        int height = this.getHeight();
        this.setLocation((screenSize.width - width)/2,(screenSize.height - height)/2);
        this.setVisible(true);
    }
    
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">
    private void initComponents() {
        recognitionLabel = new javax.swing.JLabel();
        
        // --- NEW LABEL SETUP ---
        iouScoreLabel = new javax.swing.JLabel();
        iouScoreLabel.setFont(new java.awt.Font("Arial", 1, 14));
        iouScoreLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        iouScoreLabel.setText("IoU Score: -");
        iouScoreLabel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        // -----------------------

        panelCar = new JPanel() {
            static final long serialVersionUID = 0;
            public void paint(Graphics g) {
                super.paint(g);
                g.drawImage(panelCarContent,0,0,null);
            }
        };
        fileListScrollPane = new javax.swing.JScrollPane();
        fileList = new javax.swing.JList();
        recognizeButton = new javax.swing.JButton();
        bottomLine = new javax.swing.JLabel();
        menuBar = new javax.swing.JMenuBar();
        imageMenu = new javax.swing.JMenu();
        openDirectoryItem = new javax.swing.JMenuItem();
        exitItem = new javax.swing.JMenuItem();
        helpMenu = new javax.swing.JMenu();
        aboutItem = new javax.swing.JMenuItem();
        helpItem = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("JavaANPR");
        setResizable(false);
        recognitionLabel.setBackground(new java.awt.Color(0, 0, 0));
        recognitionLabel.setFont(new java.awt.Font("Arial", 0, 24));
        recognitionLabel.setForeground(new java.awt.Color(255, 204, 51));
        recognitionLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        recognitionLabel.setText(null);
        recognitionLabel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        recognitionLabel.setOpaque(true);

        panelCar.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        GroupLayout panelCarLayout = new GroupLayout(panelCar);
        panelCar.setLayout(panelCarLayout);
        panelCarLayout.setHorizontalGroup(
            panelCarLayout.createParallelGroup(GroupLayout.LEADING)
            .add(0, 585, Short.MAX_VALUE)
        );
        panelCarLayout.setVerticalGroup(
            panelCarLayout.createParallelGroup(GroupLayout.LEADING)
            .add(0, 477, Short.MAX_VALUE)
        );

        fileListScrollPane.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        fileListScrollPane.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        fileList.setBackground(javax.swing.UIManager.getDefaults().getColor("Panel.background"));
        fileList.setFont(new java.awt.Font("Arial", 0, 11));
        fileList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                fileListValueChanged(evt);
            }
        });

        fileListScrollPane.setViewportView(fileList);

        recognizeButton.setFont(new java.awt.Font("Arial", 0, 11));
        recognizeButton.setText("Localize Plate");
        recognizeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                recognizeButtonActionPerformed(evt);
            }
        });

        bottomLine.setFont(new java.awt.Font("Arial", 0, 11));
        bottomLine.setText(" [ GUI Modified for IoU Display ] ");

        // Menu items...
        menuBar.setFont(new java.awt.Font("Arial", 0, 11));
        imageMenu.setText("Image");
        imageMenu.setFont(new java.awt.Font("Arial", 0, 11));
        openDirectoryItem.setFont(new java.awt.Font("Arial", 0, 11));
        openDirectoryItem.setText("Load snapshots from directory");
        openDirectoryItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openDirectoryItemActionPerformed(evt);
            }
        });
        imageMenu.add(openDirectoryItem);

        exitItem.setFont(new java.awt.Font("Arial", 0, 11));
        exitItem.setText("Exit");
        exitItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exitItemActionPerformed(evt);
            }
        });
        imageMenu.add(exitItem);
        menuBar.add(imageMenu);

        helpMenu.setText("Help");
        helpMenu.setFont(new java.awt.Font("Arial", 0, 11));
        aboutItem.setFont(new java.awt.Font("Arial", 0, 11));
        aboutItem.setText("About");
        aboutItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                aboutItemActionPerformed(evt);
            }
        });
        helpMenu.add(aboutItem);
        helpItem.setFont(new java.awt.Font("Arial", 0, 11));
        helpItem.setText("Help");
        helpItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                helpItemActionPerformed(evt);
            }
        });
        helpMenu.add(helpItem);
        menuBar.add(helpMenu);
        setJMenuBar(menuBar);

        // --- LAYOUT MODIFICATION ---
        // I have inserted iouScoreLabel between recognizeButton and recognitionLabel
        
        GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(GroupLayout.TRAILING)
                    .add(GroupLayout.LEADING, bottomLine, GroupLayout.DEFAULT_SIZE, 589, Short.MAX_VALUE)
                    .add(GroupLayout.LEADING, panelCar, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(LayoutStyle.RELATED)
                .add(layout.createParallelGroup(GroupLayout.TRAILING)
                    .add(fileListScrollPane, GroupLayout.DEFAULT_SIZE, 190, Short.MAX_VALUE)
                    
                    // Added iouScoreLabel here
                    .add(GroupLayout.LEADING, iouScoreLabel, GroupLayout.DEFAULT_SIZE, 190, Short.MAX_VALUE)
                    
                    .add(GroupLayout.LEADING, recognitionLabel, GroupLayout.DEFAULT_SIZE, 190, Short.MAX_VALUE)
                    .add(recognizeButton, GroupLayout.DEFAULT_SIZE, 190, Short.MAX_VALUE))
                .addContainerGap())
        );
        
        layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(fileListScrollPane, GroupLayout.DEFAULT_SIZE, 402, Short.MAX_VALUE)
                        .addPreferredGap(LayoutStyle.RELATED)
                        .add(recognizeButton)
                        .addPreferredGap(LayoutStyle.RELATED)
                        
                        // Added iouScoreLabel here
                        .add(iouScoreLabel, GroupLayout.PREFERRED_SIZE, 30, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.RELATED)
                        
                        .add(recognitionLabel, GroupLayout.PREFERRED_SIZE, 44, GroupLayout.PREFERRED_SIZE))
                    .add(panelCar, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(LayoutStyle.RELATED)
                .add(bottomLine))
        );
        pack();
    }
    // </editor-fold>

    private void helpMenuActionPerformed(java.awt.event.ActionEvent evt) {}
    private void helpItemActionPerformed(java.awt.event.ActionEvent evt) { new FrameHelp(FrameHelp.SHOW_HELP); }
    private void aboutItemActionPerformed(java.awt.event.ActionEvent evt) { new FrameHelp(FrameHelp.SHOW_ABOUT); }
    
    private void recognizeButtonActionPerformed(java.awt.event.ActionEvent evt) {
        new RecognizeThread(this).start();
    }
    
    private void fileListValueChanged(javax.swing.event.ListSelectionEvent evt) {
        int selectedNow = this.fileList.getSelectedIndex();
        if (selectedNow != -1 && this.selectedIndex != selectedNow) {
            this.recognitionLabel.setText(this.fileListModel.fileList.elementAt(selectedNow).recognizedPlate);
            this.selectedIndex = selectedNow;
            String path = ((FileListModel.FileListModelEntry)this.fileListModel.getElementAt(selectedNow)).fullPath;
            new LoadImageThread(this,path).start();
        }
    }
    
    private void exitItemActionPerformed(java.awt.event.ActionEvent evt) { System.exit(0); }
        
    private void openDirectoryItemActionPerformed(java.awt.event.ActionEvent evt) {
        int returnValue;
        String fileURL;
        this.fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        this.fileChooser.setDialogTitle("Load snapshots from directory");
        returnValue = this.fileChooser.showOpenDialog((Component)evt.getSource());
        if (returnValue != this.fileChooser.APPROVE_OPTION) return;
        fileURL = this.fileChooser.getSelectedFile().getAbsolutePath();
        File selectedFile = new File(fileURL);
        this.fileListModel = new FileListModel();
        for (String fileName : selectedFile.list()) {
            if (!ImageFileFilter.accept(fileName)) continue;
            this.fileListModel.addFileListModelEntry(fileName, selectedFile+File.separator+fileName);
        }
        this.fileList.setModel(fileListModel);
    }
    
    // Variables declaration
    private javax.swing.JMenuItem aboutItem;
    private javax.swing.JLabel bottomLine;
    private javax.swing.JMenuItem exitItem;
    private javax.swing.JList fileList;
    private javax.swing.JScrollPane fileListScrollPane;
    private javax.swing.JMenuItem helpItem;
    private javax.swing.JMenu helpMenu;
    private javax.swing.JMenu imageMenu;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JMenuItem openDirectoryItem;
    private javax.swing.JPanel panelCar;
    private javax.swing.JLabel recognitionLabel;
    private javax.swing.JButton recognizeButton;
}