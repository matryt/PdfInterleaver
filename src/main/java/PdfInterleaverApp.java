import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.io.IOException;

public class PdfInterleaverApp extends JFrame {

    private File file1;
    private File file2;

    private final JLabel lblFile1 = new JLabel("PDF 1 : aucun fichier");
    private final JLabel lblFile2 = new JLabel("PDF 2 : aucun fichier");
    private final JButton btnMerge = new JButton("Intercaler et enregistrer");

    public PdfInterleaverApp() {
        super("Intercaleur de PDF");
        initUI();
    }

    private void initUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(520, 220);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        JPanel panelFiles = new JPanel(new GridLayout(2, 2, 8, 8));
        panelFiles.setBorder(BorderFactory.createEmptyBorder(15, 15, 5, 15));

        JButton btnChoose1 = new JButton("Sélectionner PDF 1 (recto)");
        JButton btnChoose2 = new JButton("Sélectionner PDF 2 (verso)");

        btnChoose1.addActionListener(_ -> {
            File f = pickPdfFile();
            if (f != null) {
                file1 = f;
                lblFile1.setText("PDF 1 : " + f.getName());
                updateButtonState();
            }
        });

        btnChoose2.addActionListener(_ -> {
            File f = pickPdfFile();
            if (f != null) {
                file2 = f;
                lblFile2.setText("PDF 2 : " + f.getName());
                updateButtonState();
            }
        });

        panelFiles.add(btnChoose1);
        panelFiles.add(lblFile1);
        panelFiles.add(btnChoose2);
        panelFiles.add(lblFile2);

        btnMerge.setEnabled(false);
        btnMerge.addActionListener(e -> interleaveAndSave());

        JPanel panelBottom = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panelBottom.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        panelBottom.add(btnMerge);

        add(panelFiles, BorderLayout.CENTER);
        add(panelBottom, BorderLayout.SOUTH);
    }

    private File pickPdfFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("Documents PDF (*.pdf)", "pdf"));
        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            return chooser.getSelectedFile();
        }
        return null;
    }

    private void updateButtonState() {
        btnMerge.setEnabled(file1 != null && file2 != null);
    }

    private void interleaveAndSave() {
        if (file1 == null || file2 == null) {
            return;
        }

        JFileChooser saveChooser = new JFileChooser();
        saveChooser.setDialogTitle("Enregistrer le PDF fusionné");
        saveChooser.setSelectedFile(new File("intercale.pdf"));
        saveChooser.setFileFilter(new FileNameExtensionFilter("Document PDF (*.pdf)", "pdf"));

        if (saveChooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File destination = saveChooser.getSelectedFile();
        if (!destination.getName().toLowerCase().endsWith(".pdf")) {
            destination = new File(destination.getParentFile(), destination.getName() + ".pdf");
        }

        try (PDDocument doc1 = Loader.loadPDF(file1);
             PDDocument doc2 = Loader.loadPDF(file2);
             PDDocument outputDoc = new PDDocument()) {

            int count1 = doc1.getNumberOfPages();
            int count2 = doc2.getNumberOfPages();
            int maxPages = Math.max(count1, count2);

            for (int i = 0; i < maxPages; i++) {
                if (i < count1) {
                    PDPage page1 = doc1.getPage(i);
                    outputDoc.importPage(page1);
                }
                if (i < count2) {
                    PDPage page2 = doc2.getPage(i);
                    outputDoc.importPage(page2);
                }
            }

            outputDoc.save(destination);
            JOptionPane.showMessageDialog(this,
                    "Fichier généré avec succès :\n" + destination.getAbsolutePath(),
                    "Succès", JOptionPane.INFORMATION_MESSAGE);

        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this,
                    "Erreur lors du traitement : " + ex.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}
            new PdfInterleaverApp().setVisible(true);
        });
    }
}