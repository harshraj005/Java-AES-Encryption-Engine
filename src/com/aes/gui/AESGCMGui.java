package com.aes.gui;

import javax.crypto.SecretKey;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.io.File;

import com.aes.cryptographyEngine.AESGCMCipher;
import com.aes.file_io.SecureFileStream;
import com.aes.key.KeyGeneratorUtil;

/**
 * GUI front-end for the AES-GCM File Encryption System.
 * Drop this class into com.aes.gui and run it instead of Main.
 *
 * All original encryption / decryption logic (KeyGeneratorUtil,
 * AESGCMCipher, SecureFileStream) is reused without modification.
 */
public class AESGCMGui extends JFrame {

    // ── Palette ────────────────────────────────────────────────────────────────
    private static final Color BG_DARK      = new Color(0x0D, 0x0F, 0x18);
    private static final Color BG_CARD      = new Color(0x13, 0x16, 0x25);
    private static final Color BG_FIELD     = new Color(0x1C, 0x20, 0x35);
    private static final Color ACCENT_CYAN  = new Color(0x00, 0xE5, 0xFF);
    private static final Color ACCENT_LIME  = new Color(0x39, 0xFF, 0x14);
    private static final Color ACCENT_WARN  = new Color(0xFF, 0x6B, 0x35);
    private static final Color TEXT_PRIMARY = new Color(0xE8, 0xEA, 0xF6);
    private static final Color TEXT_DIM     = new Color(0x6B, 0x72, 0x9E);
    private static final Color BORDER_GLOW  = new Color(0x00, 0xE5, 0xFF, 60);

    // ── Crypto objects (one key+IV pair per session) ───────────────────────────
    private final KeyGeneratorUtil kgu  = new KeyGeneratorUtil();
    private final AESGCMCipher     aes  = new AESGCMCipher();
    private final SecureFileStream sfs  = new SecureFileStream();
    private SecretKey sessionKey;
    private byte[]    sessionIV;

    // ── UI fields ──────────────────────────────────────────────────────────────
    private JTextField encInputField, encOutputField;
    private JTextField decInputField, decOutputField;
    private JLabel     statusLabel;
    private JPanel     statusBar;

    // ══════════════════════════════════════════════════════════════════════════
    public AESGCMGui() {
        super("AES-GCM Secure File Vault");
        regenerateSessionKeys();
        buildUI();
    }

    // ── Key generation ─────────────────────────────────────────────────────────
    private void regenerateSessionKeys() {
        sessionKey = kgu.generateKey();
        sessionIV  = kgu.createIV();
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  UI construction
    // ══════════════════════════════════════════════════════════════════════════
    private void buildUI() {
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(780, 640);
        setMinimumSize(new Dimension(680, 560));
        setLocationRelativeTo(null);
        setUndecorated(false);

        // Root panel with dark background
        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(BG_DARK);
        setContentPane(root);

        root.add(buildHeader(), BorderLayout.NORTH);
        root.add(buildCenter(), BorderLayout.CENTER);
        root.add(buildStatusBar(), BorderLayout.SOUTH);

        applyGlobalFont();
        setVisible(true);
    }

    // ── Header ────────────────────────────────────────────────────────────────
    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // gradient top bar
                GradientPaint gp = new GradientPaint(
                        0, 0, new Color(0x00, 0x29, 0x3B),
                        getWidth(), 0, new Color(0x0D, 0x0F, 0x18));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                // bottom accent line
                g2.setColor(ACCENT_CYAN);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawLine(0, getHeight() - 1, getWidth(), getHeight() - 1);
                g2.dispose();
            }
        };
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(18, 28, 16, 28));

        // Lock icon (Unicode)
        JLabel icon = new JLabel("🔒");
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 30));
        icon.setBorder(new EmptyBorder(0, 0, 0, 12));

        JPanel titleBlock = new JPanel();
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        titleBlock.setOpaque(false);

        JLabel title = new JLabel("AES-GCM SECURE VAULT");
        title.setFont(new Font("Courier New", Font.BOLD, 20));
        title.setForeground(ACCENT_CYAN);

        JLabel sub = new JLabel("256-bit Authenticated Encryption  ·  Developed by Harsh Raj");
        sub.setFont(new Font("Courier New", Font.PLAIN, 11));
        sub.setForeground(TEXT_DIM);

        titleBlock.add(title);
        titleBlock.add(Box.createVerticalStrut(3));
        titleBlock.add(sub);

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        left.setOpaque(false);
        left.add(icon);
        left.add(titleBlock);

        // Key regenerate button
        JButton regenBtn = makeGhostButton("⟳  New Session Key", ACCENT_WARN);
        regenBtn.setFont(new Font("Courier New", Font.BOLD, 11));
        regenBtn.addActionListener(e -> {
            regenerateSessionKeys();
            showStatus("New 256-bit session key & IV generated.", StatusType.INFO);
        });

        header.add(left, BorderLayout.WEST);
        header.add(regenBtn, BorderLayout.EAST);
        return header;
    }

    // ── Center tabbed pane ────────────────────────────────────────────────────
    private JComponent buildCenter() {
        JTabbedPane tabs = new JTabbedPane(JTabbedPane.TOP);
        tabs.setBackground(BG_DARK);
        tabs.setForeground(TEXT_PRIMARY);
        tabs.setFont(new Font("Courier New", Font.BOLD, 13));
        tabs.setBorder(new EmptyBorder(12, 20, 0, 20));

        // Custom UI for tabs
        tabs.setUI(new javax.swing.plaf.basic.BasicTabbedPaneUI() {
            @Override protected void paintTabBorder(Graphics g, int tp, int ti, int x, int y, int w, int h, boolean b) {}
            //@Override protected void paintFocusIndicator(Graphics g, int tp, Rectangle[] rs, int ti, Rectangle ir, Component c, boolean b) {}
            @Override protected void paintTabBackground(Graphics g, int tabPlacement, int tabIndex,
                                                        int x, int y, int w, int h, boolean isSelected) {
                Graphics2D g2 = (Graphics2D) g;
                if (isSelected) {
                    g2.setColor(new Color(0x00, 0xE5, 0xFF, 30));
                    g2.fillRoundRect(x + 2, y + 2, w - 4, h - 2, 8, 8);
                    g2.setColor(ACCENT_CYAN);
                    g2.fillRect(x + 4, y + h - 2, w - 8, 2);
                } else {
                    g2.setColor(BG_CARD);
                    g2.fillRoundRect(x + 2, y + 2, w - 4, h - 2, 8, 8);
                }
            }
            @Override protected int calculateTabHeight(int tp, int ti, int fh) { return 38; }
        });

        tabs.addTab("  ⬆  ENCRYPT  ", buildEncryptPanel());
        tabs.addTab("  ⬇  DECRYPT  ", buildDecryptPanel());
        tabs.addTab("  ℹ  ABOUT    ", buildAboutPanel());

        // Color selected/unselected tab text
        tabs.setForegroundAt(0, ACCENT_LIME);
        tabs.setForegroundAt(1, ACCENT_CYAN);
        tabs.setForegroundAt(2, TEXT_DIM);

        return tabs;
    }

    // ── Encrypt panel ─────────────────────────────────────────────────────────
    private JPanel buildEncryptPanel() {
        JPanel p = cardPanel();

        p.add(sectionLabel("SOURCE FILE"), gbc(0, 0, 2, false));
        encInputField  = styledField("e.g.  /home/user/document.pdf");
        p.add(encInputField, gbc(0, 1, 1, true));
        p.add(browseButton("Browse…", encInputField, false), gbc(1, 1, 1, false));

        p.add(sectionLabel("OUTPUT ENCRYPTED FILE"), gbc(0, 2, 2, false));
        encOutputField = styledField("e.g.  /home/user/document.encrypted");
        p.add(encOutputField, gbc(0, 3, 1, true));
        p.add(browseButton("Save As…", encOutputField, true), gbc(1, 3, 1, false));

        p.add(infoBox(
                "AES-256-GCM  |  IV: 96-bit (random)  |  Auth Tag: 128-bit\n" +
                "The IV is prepended to the output file automatically."
        ), gbc(0, 4, 2, false));

        JButton encBtn = makeActionButton("🔒  ENCRYPT FILE", ACCENT_LIME);
        encBtn.addActionListener(e -> handleEncrypt());
        GridBagConstraints btnGbc = gbc(0, 5, 2, false);
        btnGbc.anchor = GridBagConstraints.CENTER;
        btnGbc.fill   = GridBagConstraints.NONE;
        btnGbc.insets = new Insets(20, 0, 8, 0);
        p.add(encBtn, btnGbc);

        return wrapScroll(p);
    }

    // ── Decrypt panel ─────────────────────────────────────────────────────────
    private JPanel buildDecryptPanel() {
        JPanel p = cardPanel();

        p.add(sectionLabel("ENCRYPTED FILE"), gbc(0, 0, 2, false));
        decInputField  = styledField("e.g.  /home/user/document.encrypted");
        p.add(decInputField, gbc(0, 1, 1, true));
        p.add(browseButton("Browse…", decInputField, false), gbc(1, 1, 1, false));

        p.add(sectionLabel("OUTPUT DECRYPTED FILE"), gbc(0, 2, 2, false));
        decOutputField = styledField("e.g.  /home/user/document_out.pdf");
        p.add(decOutputField, gbc(0, 3, 1, true));
        p.add(browseButton("Save As…", decOutputField, true), gbc(1, 3, 1, false));

        p.add(infoBox(
                "The app reads the IV from the first 12 bytes of the encrypted file.\n" +
                "GCM authentication tag is verified — tampered files will be rejected."
        ), gbc(0, 4, 2, false));

        JButton decBtn = makeActionButton("🔓  DECRYPT FILE", ACCENT_CYAN);
        decBtn.addActionListener(e -> handleDecrypt());
        GridBagConstraints btnGbc = gbc(0, 5, 2, false);
        btnGbc.anchor = GridBagConstraints.CENTER;
        btnGbc.fill   = GridBagConstraints.NONE;
        btnGbc.insets = new Insets(20, 0, 8, 0);
        p.add(decBtn, btnGbc);

        return wrapScroll(p);
    }

    // ── About panel ───────────────────────────────────────────────────────────
    private JPanel buildAboutPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(BG_DARK);
        p.setBorder(new EmptyBorder(30, 40, 30, 40));

        String text =
            "<html><body style='font-family:Courier New; color:#E8EAF6; width:480px; line-height:1.6'>" +
            "<h2 style='color:#00E5FF; margin-bottom:4px;'>AES-GCM Secure File Vault</h2>" +
            "<p style='color:#6B729E; font-size:11px;'>Developed by Harsh Raj</p><br>" +
            "<b style='color:#39FF14;'>Algorithm:</b> AES-256 in GCM (Galois/Counter Mode)<br><br>" +
            "<b style='color:#39FF14;'>Key size:</b> 256 bits — generated fresh each session via Java's KeyGenerator<br><br>" +
            "<b style='color:#39FF14;'>IV:</b> 96-bit random nonce (NIST SP 800-38D recommended) prepended to output<br><br>" +
            "<b style='color:#39FF14;'>Auth Tag:</b> 128-bit GCM tag — guarantees confidentiality <i>and</i> integrity<br><br>" +
            "<b style='color:#39FF14;'>Chunk size:</b> 4 KB streaming — safe for files of any size without OOM<br><br>" +
            "<b style='color:#FF6B35;'>⚠ Note:</b> The session key lives in memory only. " +
            "You must encrypt and decrypt within the same session, or integrate persistent key storage.<br>" +
            "</body></html>";

        JLabel lbl = new JLabel(text);
        p.add(lbl, new GridBagConstraints());
        return p;
    }

    // ── Status bar ────────────────────────────────────────────────────────────
    private JPanel buildStatusBar() {
        statusBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 6));
        statusBar.setBackground(new Color(0x09, 0x0B, 0x14));
        statusBar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(0x1E, 0x22, 0x40)));

        JLabel dot = new JLabel("●");
        dot.setForeground(ACCENT_LIME);
        dot.setFont(new Font("Courier New", Font.PLAIN, 10));

        statusLabel = new JLabel("System ready — session key loaded.");
        statusLabel.setFont(new Font("Courier New", Font.PLAIN, 11));
        statusLabel.setForeground(TEXT_DIM);

        statusBar.add(dot);
        statusBar.add(statusLabel);
        return statusBar;
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Action handlers
    // ══════════════════════════════════════════════════════════════════════════
    private void handleEncrypt() {
        String input  = encInputField.getText().trim();
        String output = encOutputField.getText().trim();

        if (input.isEmpty() || output.isEmpty()) {
            showStatus("Please fill in both file paths before encrypting.", StatusType.WARN);
            return;
        }
        if (!new File(input).exists()) {
            showStatus("Source file not found: " + input, StatusType.ERROR);
            return;
        }

        showStatus("Encrypting…", StatusType.INFO);
        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override protected Boolean doInBackground() {
                sfs.encryptFile(input, output, sessionKey, sessionIV, aes);
                return new File(output).exists();
            }
            @Override protected void done() {
                try {
                    if (get()) showStatus("✔  Encryption successful → " + output, StatusType.SUCCESS);
                    else        showStatus("Encryption may have failed — check console.", StatusType.WARN);
                } catch (Exception ex) {
                    showStatus("Error: " + ex.getMessage(), StatusType.ERROR);
                }
            }
        };
        worker.execute();
    }

    private void handleDecrypt() {
        String input  = decInputField.getText().trim();
        String output = decOutputField.getText().trim();

        if (input.isEmpty() || output.isEmpty()) {
            showStatus("Please fill in both file paths before decrypting.", StatusType.WARN);
            return;
        }
        if (!new File(input).exists()) {
            showStatus("Encrypted file not found: " + input, StatusType.ERROR);
            return;
        }

        showStatus("Decrypting…", StatusType.INFO);
        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override protected Boolean doInBackground() {
                sfs.decryptFile(input, output, sessionKey, aes);
                return new File(output).exists();
            }
            @Override protected void done() {
                try {
                    if (get()) showStatus("✔  Decryption successful — integrity verified → " + output, StatusType.SUCCESS);
                    else        showStatus("Decryption may have failed — tampered file?", StatusType.ERROR);
                } catch (Exception ex) {
                    showStatus("Error: " + ex.getMessage(), StatusType.ERROR);
                }
            }
        };
        worker.execute();
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  UI helpers
    // ══════════════════════════════════════════════════════════════════════════
    private enum StatusType { SUCCESS, ERROR, WARN, INFO }

    private void showStatus(String msg, StatusType type) {
        SwingUtilities.invokeLater(() -> {
            statusLabel.setText(msg);
            statusLabel.setForeground(switch (type) {
                case SUCCESS -> ACCENT_LIME;
                case ERROR   -> ACCENT_WARN;
                case WARN    -> new Color(0xFF, 0xD6, 0x00);
                case INFO    -> ACCENT_CYAN;
            });
        });
    }

    private JPanel cardPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(BG_CARD);
        p.setBorder(new CompoundBorder(
                new LineBorder(BORDER_GLOW, 1, true),
                new EmptyBorder(24, 28, 24, 28)
        ));
        return p;
    }

    private JPanel wrapScroll(JPanel inner) {
        JPanel outer = new JPanel(new GridBagLayout());
        outer.setBackground(BG_DARK);
        outer.setBorder(new EmptyBorder(16, 24, 16, 24));
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1; c.weighty = 1;
        outer.add(inner, c);
        return outer;
    }

    private JTextField styledField(String placeholder) {
        JTextField f = new JTextField(40) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getText().isEmpty()) {
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                    g2.setColor(TEXT_DIM);
                    g2.setFont(getFont().deriveFont(Font.ITALIC));
                    g2.drawString(placeholder, 10, getHeight() / 2 + g2.getFontMetrics().getAscent() / 2 - 2);
                }
            }
        };
        f.setBackground(BG_FIELD);
        f.setForeground(TEXT_PRIMARY);
        f.setCaretColor(ACCENT_CYAN);
        f.setFont(new Font("Courier New", Font.PLAIN, 13));
        f.setBorder(new CompoundBorder(
                new LineBorder(new Color(0x2A, 0x30, 0x55), 1, true),
                new EmptyBorder(8, 10, 8, 10)
        ));
        // glow on focus
        f.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                f.setBorder(new CompoundBorder(
                        new LineBorder(ACCENT_CYAN, 1, true),
                        new EmptyBorder(8, 10, 8, 10)));
            }
            @Override public void focusLost(FocusEvent e) {
                f.setBorder(new CompoundBorder(
                        new LineBorder(new Color(0x2A, 0x30, 0x55), 1, true),
                        new EmptyBorder(8, 10, 8, 10)));
            }
        });
        return f;
    }

    private JLabel sectionLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Courier New", Font.BOLD, 10));
        lbl.setForeground(TEXT_DIM);
        lbl.setBorder(new EmptyBorder(14, 0, 4, 0));
        return lbl;
    }

    private JPanel infoBox(String text) {
        JPanel box = new JPanel(new BorderLayout());
        box.setBackground(new Color(0x00, 0x2A, 0x36));
        box.setBorder(new CompoundBorder(
                new LineBorder(new Color(0x00, 0x60, 0x80), 1, true),
                new EmptyBorder(10, 14, 10, 14)
        ));
        JLabel lbl = new JLabel("<html><body style='font-family:Courier New; font-size:11px; color:#7EC8E3'>" +
                text.replace("\n", "<br>") + "</body></html>");
        box.add(lbl);
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0; c.gridy = 0; c.weightx = 1; c.fill = GridBagConstraints.HORIZONTAL;
        box.putClientProperty("gbc", c);
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setBorder(new EmptyBorder(12, 0, 0, 0));
        wrapper.add(box);
        return wrapper;
    }

    private JButton browseButton(String label, JTextField target, boolean saveMode) {
        JButton btn = makeGhostButton(label, TEXT_DIM);
        btn.setFont(new Font("Courier New", Font.PLAIN, 11));
        btn.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            fc.setBackground(BG_CARD);
            if (saveMode) {
                fc.setDialogType(JFileChooser.SAVE_DIALOG);
                if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION)
                    target.setText(fc.getSelectedFile().getAbsolutePath());
            } else {
                if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
                    target.setText(fc.getSelectedFile().getAbsolutePath());
            }
        });
        return btn;
    }

    private JButton makeActionButton(String text, Color accent) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color base = getModel().isRollover()
                        ? accent.brighter()
                        : accent.darker().darker();
                GradientPaint gp = new GradientPaint(0, 0, base, 0, getHeight(), accent.darker());
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(new Color(255, 255, 255, 40));
                g2.setStroke(new BasicStroke(1));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                g2.setColor(BG_DARK);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int tx = (getWidth()  - fm.stringWidth(getText())) / 2;
                int ty = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(getText(), tx, ty);
                g2.dispose();
            }
        };
        btn.setFont(new Font("Courier New", Font.BOLD, 14));
        btn.setForeground(BG_DARK);
        btn.setPreferredSize(new Dimension(240, 46));
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JButton makeGhostButton(String text, Color accent) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isRollover()) {
                    g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 25));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                }
                g2.setColor(accent);
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int tx = (getWidth()  - fm.stringWidth(getText())) / 2;
                int ty = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(getText(), tx, ty);
                g2.dispose();
            }
        };
        btn.setFont(new Font("Courier New", Font.PLAIN, 12));
        btn.setForeground(accent);
        btn.setPreferredSize(new Dimension(110, 36));
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private GridBagConstraints gbc(int col, int row, int width, boolean fill) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridx      = col;
        c.gridy      = row;
        c.gridwidth  = width;
        c.weightx    = fill ? 1.0 : 0.0;
        c.fill       = fill ? GridBagConstraints.HORIZONTAL : GridBagConstraints.NONE;
        c.anchor     = GridBagConstraints.WEST;
        c.insets     = new Insets(2, 0, 2, col == 0 && width == 1 ? 8 : 0);
        return c;
    }

    private void applyGlobalFont() {
        Font base = new Font("Courier New", Font.PLAIN, 12);
        UIManager.put("Label.font",        base);
        UIManager.put("Button.font",       base);
        UIManager.put("TextField.font",    base);
        UIManager.put("TabbedPane.font",   base.deriveFont(Font.BOLD, 13f));
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Entry point
    // ══════════════════════════════════════════════════════════════════════════
    public static void main(String[] args) {
        // Use system L&F as base, then override with custom painting
        try { UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName()); }
        catch (Exception ignored) {}

        SwingUtilities.invokeLater(AESGCMGui::new);
    }
}
