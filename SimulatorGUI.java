
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.LayoutManager;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

public class SimulatorGUI extends JFrame {

    private static final long serialVersionUID = 1L;

    private static final String SJF = "Preemptive SJF";
    private static final String ACS_ALGORITHM =
            "Adaptive Container Scheduler (ACS)";
    private static final String BOTH = "Run both algorithms";

    private static final Color DARK_OLIVE =
            new Color(53, 75, 53);

    private static final Color OLIVE =
            new Color(120, 134, 107);

    private static final Color SAGE =
            new Color(163, 177, 138);

    private static final Color BACKGROUND =
            new Color(243, 245, 239);

    private static final Color TEXT =
            new Color(43, 58, 43);

    private static final Color BORDER =
            new Color(224, 230, 218);

    private static final Color TERRACOTTA =
            new Color(190, 111, 83);

    private static final Color GOLD =
            new Color(205, 166, 83);

    private static final Color TEAL =
            new Color(88, 132, 125);

    private static final Color MUTED =
            new Color(112, 121, 105);

    private static final Color[] PROCESS_COLORS = {
            new Color(120, 134, 107),
            new Color(190, 111, 83),
            new Color(88, 132, 125),
            new Color(205, 166, 83),
            new Color(130, 116, 145),
            new Color(93, 133, 171)
    };

    private final DefaultTableModel processModel =
            new DefaultTableModel(
                    new Object[]{
                            "Process",
                            "Arrival Time",
                            "Burst Time",
                            "Priority"
                    }, 0) {

                @Override
                public boolean isCellEditable(int row, int column) {
                    return column != 0;
                }
            };

    private final JTable processTable =
            new JTable(processModel);

    private final JComboBox<String> algorithmChoice =
            new JComboBox<>(
                    new String[]{SJF, ACS_ALGORITHM, BOTH});

    private final JTextField quantumField =
            new JTextField("2", 6);

    private final JTextField contextSwitchField =
            new JTextField("0", 6);

    private final JButton runButton =
            new JButton("Run Simulation");

    private final JButton playPauseButton =
            createButton("Pause", OLIVE);

    private final JButton stepButton =
            createButton("Step", TEAL);

    private final JProgressBar progressBar =
            new JProgressBar();

    private final JLabel statusLabel =
            new JLabel("Ready to simulate");

    private final JLabel clockValue =
            new JLabel("0");

    private final JLabel cpuValue =
            new JLabel("IDLE");

    private final JLabel schedulerValue =
            new JLabel("Waiting");

    private final JLabel waitingMetric =
            new JLabel("--");

    private final JLabel turnaroundMetric =
            new JLabel("--");

    private final JLabel responseMetric =
            new JLabel("--");

    private final JLabel readyQueueValue =
            new JLabel("No ready processes");

    private final JLabel tierOneValue =
            new JLabel("—");

    private final JLabel tierTwoValue =
            new JLabel("—");

    private final DefaultTableModel liveModel =
            new DefaultTableModel(
                    new Object[]{
                            "Process", "Arrival", "Burst",
                            "Remaining", "Priority", "State"
                    }, 0) {
                private static final long serialVersionUID = 1L;

                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };

    private final JTable liveTable =
            new JTable(liveModel);

    private final GanttChart ganttChart =
            new GanttChart();

    private Timer playbackTimer;
    private List<RunAnimation> animationRuns =
            Collections.emptyList();
    private List<Process> animationProcesses =
            Collections.emptyList();
    private int currentRunIndex;
    private int currentStepIndex;

    public SimulatorGUI() {

        super("CPU Scheduling Simulator");

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setMinimumSize(new Dimension(950, 650));
        setSize(1200, 780);

        setLocationRelativeTo(null);

        JPanel root = new JPanel(
                new BorderLayout(0, 18));

        root.setBorder(
                BorderFactory.createEmptyBorder(
                        24, 24, 24, 24));

        root.setBackground(BACKGROUND);

        root.add(createHeader(), BorderLayout.NORTH);
        root.add(createWorkspace(), BorderLayout.CENTER);

        setContentPane(root);

        styleComboBox(algorithmChoice);
        styleTextField(quantumField);
        styleTextField(contextSwitchField);

        algorithmChoice.addActionListener(
                event -> updateQuantumAvailability());

        updateQuantumAvailability();
    }

    private JPanel createHeader() {

        JPanel header = new JPanel(
                new BorderLayout());

        header.setOpaque(false);

        JLabel title = new JLabel(
                "CPU Scheduling Simulator");

        title.setFont(
                new Font("SansSerif", Font.BOLD, 27));

        title.setForeground(DARK_OLIVE);

        JLabel subtitle = new JLabel(
                "Manage processes and visualize scheduling performance");

        subtitle.setFont(
                new Font("SansSerif", Font.PLAIN, 14));

        subtitle.setForeground(OLIVE);

        JPanel labels = new JPanel(
                new GridLayout(2, 1, 0, 5));

        labels.setOpaque(false);

        labels.add(title);
        labels.add(subtitle);

        header.add(labels, BorderLayout.CENTER);

        return header;
    }

    private JSplitPane createWorkspace() {

        JSplitPane splitPane = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                createInputPanel(),
                createOutputPanel());

        splitPane.setResizeWeight(0.44);
        splitPane.setDividerSize(10);
        splitPane.setBorder(null);
        splitPane.setOpaque(false);

        return splitPane;
    }

    private JPanel createInputPanel() {

        JPanel panel = cardPanel(
                new BorderLayout(0, 15));

        panel.add(
                sectionTitle("Simulation Setup"),
                BorderLayout.NORTH);

        JPanel center = new JPanel(
                new BorderLayout(0, 15));

        center.setOpaque(false);

        center.add(
                createSettingsPanel(),
                BorderLayout.NORTH);

        configureProcessTable();

        JScrollPane tableScroll =
                new JScrollPane(processTable);

        tableScroll.setBorder(
                BorderFactory.createLineBorder(BORDER));

        center.add(
                tableScroll,
                BorderLayout.CENTER);

        panel.add(center, BorderLayout.CENTER);

        panel.add(
                createProcessButtons(),
                BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createSettingsPanel() {

        JPanel settings = new JPanel(
                new GridLayout(0, 2, 12, 12));

        settings.setOpaque(false);

        settings.add(fieldLabel("Algorithm"));
        settings.add(algorithmChoice);

        settings.add(fieldLabel("Time Quantum"));
        settings.add(quantumField);

        settings.add(fieldLabel("Context Switch"));
        settings.add(contextSwitchField);

        return settings;
    }

    private JPanel createProcessButtons() {

        JPanel controls = new JPanel(
                new BorderLayout(8, 10));

        controls.setOpaque(false);

        JPanel rowButtons = new JPanel(
                new GridLayout(1, 2, 8, 0));

        rowButtons.setOpaque(false);

        JButton addButton =
                createButton("Add Process", OLIVE);

        JButton removeButton =
                createButton("Remove Selected", DARK_OLIVE);

        addButton.addActionListener(
                event -> addProcess());

        removeButton.addActionListener(
                event -> removeSelectedProcesses());

        rowButtons.add(addButton);
        rowButtons.add(removeButton);

        styleButton(runButton, DARK_OLIVE);

        runButton.setText("Run Simulation");

        runButton.setFont(
                new Font("SansSerif", Font.BOLD, 15));

        runButton.setPreferredSize(
                new Dimension(0, 46));

        runButton.addActionListener(
                event -> runSimulation());

        controls.add(
                rowButtons,
                BorderLayout.NORTH);

        controls.add(
                runButton,
                BorderLayout.SOUTH);

        return controls;
    }

    private JPanel createOutputPanel() {

        JPanel panel = cardPanel(
                new BorderLayout(0, 12));

        JPanel heading = new JPanel(
                new BorderLayout());

        heading.setOpaque(false);

        heading.add(
                sectionTitle("Live Scheduler Dashboard"),
                BorderLayout.WEST);

        JButton clearButton =
                createButton("Reset View", OLIVE);

        clearButton.addActionListener(event -> resetVisualization());

        heading.add(
                clearButton,
                BorderLayout.EAST);

        panel.add(heading, BorderLayout.NORTH);

        JPanel dashboard = new JPanel();
        dashboard.setOpaque(false);
        dashboard.setLayout(
                new javax.swing.BoxLayout(
                        dashboard, javax.swing.BoxLayout.Y_AXIS));
        dashboard.add(createLiveStatusCards());
        dashboard.add(javax.swing.Box.createVerticalStrut(10));
        dashboard.add(createMetricsPanel());
        dashboard.add(javax.swing.Box.createVerticalStrut(10));
        dashboard.add(createTimelinePanel());
        dashboard.add(javax.swing.Box.createVerticalStrut(10));
        dashboard.add(createQueuesPanel());
        dashboard.add(javax.swing.Box.createVerticalStrut(10));
        dashboard.add(createLiveProcessPanel());

        JScrollPane dashboardScroll = new JScrollPane(
                dashboard,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        dashboardScroll.setBorder(null);
        dashboardScroll.setOpaque(false);
        dashboardScroll.getViewport().setOpaque(false);
        panel.add(dashboardScroll, BorderLayout.CENTER);

        JPanel bottom = new JPanel(
                new BorderLayout(8, 7));

        bottom.setOpaque(false);

        statusLabel.setForeground(OLIVE);

        statusLabel.setFont(
                new Font("SansSerif", Font.BOLD, 12));

        progressBar.setIndeterminate(true);

        progressBar.setForeground(OLIVE);
        progressBar.setBackground(BACKGROUND);
        progressBar.setBorderPainted(false);
        progressBar.setPreferredSize(
                new Dimension(0, 5));

        progressBar.setVisible(false);

        JPanel playbackButtons = new JPanel(
                new GridLayout(1, 2, 6, 0));
        playbackButtons.setOpaque(false);
        styleButton(playPauseButton, OLIVE);
        styleButton(stepButton, TEAL);
        playPauseButton.setEnabled(false);
        stepButton.setEnabled(false);
        playPauseButton.addActionListener(event -> togglePlayback());
        stepButton.addActionListener(event -> {
            if (playbackTimer != null) {
                playbackTimer.stop();
            }
            playPauseButton.setText("Play");
            advancePlayback();
        });
        playbackButtons.add(playPauseButton);
        playbackButtons.add(stepButton);

        JPanel progressAndStatus = new JPanel(
                new BorderLayout(0, 7));
        progressAndStatus.setOpaque(false);
        progressAndStatus.add(statusLabel, BorderLayout.NORTH);
        progressAndStatus.add(progressBar, BorderLayout.SOUTH);

        bottom.add(playbackButtons, BorderLayout.EAST);
        bottom.add(progressAndStatus, BorderLayout.CENTER);

        panel.add(bottom, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createLiveStatusCards() {
        JPanel cards = new JPanel(new GridLayout(1, 3, 8, 0));
        cards.setOpaque(false);
        cards.add(createValueCard("SIMULATION TIME", clockValue, OLIVE));
        cards.add(createValueCard("CPU", cpuValue, TERRACOTTA));
        cards.add(createValueCard("SCHEDULER", schedulerValue, TEAL));
        return cards;
    }

    private JPanel createMetricsPanel() {
        JPanel cards = new JPanel(new GridLayout(1, 3, 8, 0));
        cards.setOpaque(false);
        cards.add(createMetricCard("AVG. WAITING", waitingMetric, GOLD));
        cards.add(createMetricCard("AVG. TURNAROUND", turnaroundMetric, OLIVE));
        cards.add(createMetricCard("AVG. RESPONSE", responseMetric, TEAL));
        return cards;
    }

    private JPanel createValueCard(
            String title, JLabel value, Color accent) {
        JPanel card = cardPanel(new BorderLayout(0, 4));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)));
        JLabel heading = new JLabel(title);
        heading.setFont(new Font("SansSerif", Font.BOLD, 10));
        heading.setForeground(MUTED);
        value.setFont(new Font("SansSerif", Font.BOLD, 16));
        value.setForeground(accent);
        card.add(heading, BorderLayout.NORTH);
        card.add(value, BorderLayout.CENTER);
        return card;
    }

    private JPanel createMetricCard(
            String title, JLabel value, Color accent) {
        JPanel card = cardPanel(new BorderLayout(0, 3));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(7, 10, 7, 10)));
        JLabel heading = new JLabel(title);
        heading.setFont(new Font("SansSerif", Font.BOLD, 9));
        heading.setForeground(MUTED);
        value.setFont(new Font("SansSerif", Font.BOLD, 15));
        value.setForeground(accent);
        card.add(heading, BorderLayout.NORTH);
        card.add(value, BorderLayout.CENTER);
        return card;
    }

    private JPanel createTimelinePanel() {
        JPanel section = new JPanel(new BorderLayout(0, 5));
        section.setOpaque(false);
        JLabel title = sectionTitle("CPU Timeline");
        title.setFont(new Font("SansSerif", Font.BOLD, 14));
        section.add(title, BorderLayout.NORTH);

        ganttChart.setPreferredSize(new Dimension(560, 128));
        JScrollPane scroll = new JScrollPane(
                ganttChart,
                JScrollPane.VERTICAL_SCROLLBAR_NEVER,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER));
        scroll.getViewport().setBackground(BACKGROUND);
        section.add(scroll, BorderLayout.CENTER);
        section.setPreferredSize(new Dimension(560, 175));
        section.setMaximumSize(new Dimension(Integer.MAX_VALUE, 175));
        return section;
    }

    private JPanel createQueuesPanel() {
        JPanel queues = new JPanel(new GridLayout(1, 3, 8, 0));
        queues.setOpaque(false);
        queues.add(createQueueCard("READY QUEUE", readyQueueValue, OLIVE));
        queues.add(createQueueCard("TIER 1", tierOneValue, TEAL));
        queues.add(createQueueCard("TIER 2", tierTwoValue, TERRACOTTA));
        queues.setPreferredSize(new Dimension(560, 62));
        queues.setMaximumSize(new Dimension(Integer.MAX_VALUE, 62));
        return queues;
    }

    private JPanel createQueueCard(
            String title, JLabel value, Color accent) {
        JPanel card = cardPanel(new BorderLayout(0, 3));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(6, 9, 6, 9)));
        JLabel heading = new JLabel(title);
        heading.setFont(new Font("SansSerif", Font.BOLD, 9));
        heading.setForeground(MUTED);
        value.setFont(new Font("SansSerif", Font.BOLD, 11));
        value.setForeground(accent);
        card.add(heading, BorderLayout.NORTH);
        card.add(value, BorderLayout.CENTER);
        return card;
    }

    private JPanel createLiveProcessPanel() {
        JPanel section = new JPanel(new BorderLayout(0, 5));
        section.setOpaque(false);
        JLabel title = sectionTitle("Process States");
        title.setFont(new Font("SansSerif", Font.BOLD, 14));
        section.add(title, BorderLayout.NORTH);

        liveTable.setRowHeight(25);
        liveTable.setFillsViewportHeight(true);
        liveTable.setFont(new Font("SansSerif", Font.PLAIN, 11));
        liveTable.setForeground(TEXT);
        liveTable.setGridColor(BORDER);
        liveTable.getTableHeader().setReorderingAllowed(false);
        liveTable.getTableHeader().setBackground(DARK_OLIVE);
        liveTable.getTableHeader().setForeground(Color.WHITE);
        liveTable.getTableHeader().setFont(
                new Font("SansSerif", Font.BOLD, 10));
        liveTable.getColumnModel().getColumn(5)
                .setCellRenderer(new DefaultTableCellRenderer() {
                    private static final long serialVersionUID = 1L;

                    @Override
                    public Component getTableCellRendererComponent(
                            JTable table,
                            Object value,
                            boolean selected,
                            boolean focus,
                            int row,
                            int column) {
                        Component component =
                                super.getTableCellRendererComponent(
                                        table, value, selected, focus,
                                        row, column);
                        String state = String.valueOf(value);
                        if (!selected) {
                            if (state.startsWith("Running")) {
                                component.setBackground(DARK_OLIVE);
                                component.setForeground(Color.WHITE);
                            } else if (state.startsWith("Completed")) {
                                component.setBackground(
                                        new Color(221, 231, 211));
                                component.setForeground(DARK_OLIVE);
                            } else if (state.startsWith("Waiting")) {
                                component.setBackground(
                                        new Color(245, 225, 214));
                                component.setForeground(TERRACOTTA);
                            } else if (state.startsWith("Ready")) {
                                component.setBackground(
                                        new Color(220, 235, 230));
                                component.setForeground(TEAL);
                            } else {
                                component.setBackground(Color.WHITE);
                                component.setForeground(MUTED);
                            }
                        }
                        return component;
                    }
                });
        JScrollPane scroll = new JScrollPane(liveTable);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER));
        section.add(scroll, BorderLayout.CENTER);
        section.setPreferredSize(new Dimension(560, 195));
        section.setMaximumSize(new Dimension(Integer.MAX_VALUE, 195));
        return section;
    }

    private JPanel cardPanel(LayoutManager layout) {

        JPanel panel = new JPanel(layout);

        panel.setBackground(Color.WHITE);

        panel.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(BORDER),
                        BorderFactory.createEmptyBorder(
                                18, 18, 18, 18)));

        return panel;
    }

    private JLabel sectionTitle(String text) {

        JLabel label = new JLabel(text);

        label.setFont(
                new Font("SansSerif", Font.BOLD, 18));

        label.setForeground(DARK_OLIVE);

        return label;
    }

    private JLabel fieldLabel(String text) {

        JLabel label = new JLabel(text);

        label.setHorizontalAlignment(
                SwingConstants.LEFT);

        label.setFont(
                new Font("SansSerif", Font.BOLD, 13));

        label.setForeground(TEXT);

        return label;
    }

    private JButton createButton(
            String text, Color color) {

        JButton button = new JButton(text);

        styleButton(button, color);

        return button;
    }

    private void styleButton(
            JButton button, Color color) {

        button.setBackground(color);
        button.setForeground(Color.WHITE);

        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);

        button.setFont(
                new Font("SansSerif", Font.BOLD, 12));

        button.setCursor(
                new Cursor(Cursor.HAND_CURSOR));

        button.setBorder(
                BorderFactory.createEmptyBorder(
                        10, 12, 10, 12));

        button.addMouseListener(
                new MouseAdapter() {

                    @Override
                    public void mouseEntered(MouseEvent e) {
                        if (button.isEnabled()) {
                            button.setBackground(SAGE);
                            button.setForeground(DARK_OLIVE);
                        }
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        button.setBackground(color);
                        button.setForeground(Color.WHITE);
                    }
                });
    }

    private void styleTextField(
            JTextField field) {

        field.setFont(
                new Font("SansSerif", Font.PLAIN, 13));

        field.setForeground(TEXT);
        field.setBackground(BACKGROUND);

        field.setCaretColor(DARK_OLIVE);

        field.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(BORDER),
                        BorderFactory.createEmptyBorder(
                                8, 10, 8, 10)));
    }

    private void styleComboBox(
            JComboBox<String> combo) {

        combo.setFont(
                new Font("SansSerif", Font.PLAIN, 13));

        combo.setForeground(TEXT);
        combo.setBackground(BACKGROUND);

        combo.setFocusable(false);
    }

    private void configureProcessTable() {

        processTable.setRowHeight(36);

        processTable.setFillsViewportHeight(true);

        processTable.setBackground(Color.WHITE);
        processTable.setForeground(TEXT);

        processTable.setSelectionBackground(
                new Color(220, 229, 210));

        processTable.setSelectionForeground(
                DARK_OLIVE);

        processTable.setGridColor(
                new Color(230, 234, 224));

        processTable.setFont(
                new Font("SansSerif", Font.PLAIN, 13));

        processTable.setShowVerticalLines(false);

        processTable.setSelectionMode(
                javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        JTableHeader header =
                processTable.getTableHeader();

        header.setReorderingAllowed(false);

        header.setPreferredSize(
                new Dimension(0, 38));

        header.setDefaultRenderer(
                new DefaultTableCellRenderer() {

                    @Override
                    public Component getTableCellRendererComponent(
                            JTable table,
                            Object value,
                            boolean selected,
                            boolean focus,
                            int row,
                            int column) {

                        JLabel label = (JLabel)
                                super.getTableCellRendererComponent(
                                        table, value,
                                        selected, focus,
                                        row, column);

                        label.setBackground(DARK_OLIVE);
                        label.setForeground(Color.WHITE);

                        label.setFont(
                                new Font("SansSerif",
                                        Font.BOLD, 12));

                        label.setHorizontalAlignment(
                                SwingConstants.CENTER);

                        label.setBorder(
                                BorderFactory.createEmptyBorder(
                                        5, 5, 5, 5));

                        return label;
                    }
                });

        DefaultTableCellRenderer renderer =
                new DefaultTableCellRenderer() {

                    @Override
                    public Component getTableCellRendererComponent(
                            JTable table,
                            Object value,
                            boolean selected,
                            boolean focus,
                            int row,
                            int column) {

                        Component component =
                                super.getTableCellRendererComponent(
                                        table, value,
                                        selected, focus,
                                        row, column);

                        if (!selected) {
                            component.setBackground(
                                    row % 2 == 0
                                            ? Color.WHITE
                                            : BACKGROUND);
                        }

                        component.setForeground(TEXT);

                        setBorder(
                                BorderFactory.createEmptyBorder(
                                        4, 10, 4, 10));

                        return component;
                    }
                };

        for (int i = 0;
                i < processTable.getColumnCount();
                i++) {

            processTable.getColumnModel()
                    .getColumn(i)
                    .setCellRenderer(renderer);
        }
    }

    private void updateQuantumAvailability() {

        String selection =
                (String) algorithmChoice.getSelectedItem();

        boolean enabled =
                !SJF.equals(selection);

        quantumField.setEnabled(enabled);

        quantumField.setBackground(
                enabled ? BACKGROUND : new Color(230, 232, 225));

        quantumField.setToolTipText(
                enabled
                        ? "Positive integer"
                        : "Only used by ACS");
    }

    private void addProcess() {

        int nextId = 1;

        while (containsProcessName("P" + nextId)) {
            nextId++;
        }

        processModel.addRow(
                new Object[]{
                        "P" + nextId,
                        "0",
                        "1",
                        "1"
                });
    }

    private boolean containsProcessName(
            String name) {

        for (int row = 0;
                row < processModel.getRowCount();
                row++) {

            if (name.equals(
                    processModel.getValueAt(row, 0))) {

                return true;
            }
        }

        return false;
    }

    private void removeSelectedProcesses() {

        int[] selectedRows =
                processTable.getSelectedRows();

        for (int i = selectedRows.length - 1;
                i >= 0; i--) {

            processModel.removeRow(selectedRows[i]);
        }
    }

    private void runSimulation() {

        if (processTable.isEditing()
                && !processTable.getCellEditor()
                        .stopCellEditing()) {

            return;
        }

        final List<Process> processes;
        final int contextSwitch;
        final int quantum;

        final String selection =
                (String) algorithmChoice.getSelectedItem();

        try {

            processes = readProcesses();

            if (processes.isEmpty()) {

                throw new IllegalArgumentException(
                        "Add at least one process before running the simulation.");
            }

            contextSwitch = parseInteger(
                    contextSwitchField.getText(),
                    "Context switch", 0);

            quantum = SJF.equals(selection)
                    ? 1
                    : parseInteger(
                            quantumField.getText(),
                            "Time quantum", 1);

        } catch (IllegalArgumentException exception) {

            JOptionPane.showMessageDialog(
                    this,
                    exception.getMessage(),
                    "Check your inputs",
                    JOptionPane.WARNING_MESSAGE);

            return;
        }

        resetVisualization();

        runButton.setEnabled(false);

        statusLabel.setText("Simulation is running...");

        progressBar.setVisible(true);
        progressBar.setIndeterminate(true);
        new SwingWorker<List<RunAnimation>, Void>() {

            @Override
            protected List<RunAnimation> doInBackground() {

                ByteArrayOutputStream buffer =
                        new ByteArrayOutputStream();

                try (PrintStream output =
                        new PrintStream(
                                buffer,
                                true,
                                StandardCharsets.UTF_8)) {

                    if (SJF.equals(selection)
                            || BOTH.equals(selection)) {

                        output.println(
                                "========== PREEMPTIVE SJF RESULTS ==========");

                        PreemptiveSJF.runSJF(
                                processes,
                                contextSwitch,
                                output);
                    }

                    if (ACS_ALGORITHM.equals(selection)
                            || BOTH.equals(selection)) {

                        output.println(
                                "\n========== ACS RESULTS ==========");

                        ACS.runACS(
                                processes,
                                quantum,
                                contextSwitch,
                                output);
                    }
                }

                return parseOutput(
                        new String(
                                buffer.toByteArray(),
                                StandardCharsets.UTF_8));
            }

            @Override
            protected void done() {

                try {

                    startPlayback(get(), processes);

                } catch (Exception exception) {

                    Throwable cause =
                            exception.getCause() == null
                                    ? exception
                                    : exception.getCause();

                    statusLabel.setText(
                            "Simulation failed");

                    JOptionPane.showMessageDialog(
                            SimulatorGUI.this,
                            cause.getMessage(),
                            "Simulation failed",
                            JOptionPane.ERROR_MESSAGE);

                } finally {

                    runButton.setEnabled(true);

                    progressBar.setIndeterminate(false);
                    progressBar.setVisible(false);
                }
            }

        }.execute();
    }

    private static List<RunAnimation> parseOutput(String text) {
        List<RunAnimation> runs = new ArrayList<>();
        RunAnimation currentRun = null;
        Pattern stepPattern = Pattern.compile(
                "^Time (\\d+) -> (\\d+) : (?:Running (\\S+)"
                        + "(?: \\[Tier (\\d+)\\])? \\(Remaining: (\\d+)\\)"
                        + "|\\[(IDLE|Context Switch)\\])$");
        Pattern queuePattern = Pattern.compile(
                "^Time (\\d+) \\| Tier 1: \\[(.*?)] \\| Tier 2: \\[(.*?)]$");
        Pattern waitingPattern = Pattern.compile(
                "Average Waiting Time\\s*:\\s*([\\d.]+)");
        Pattern turnaroundPattern = Pattern.compile(
                "Average Turnaround Time\\s*:\\s*([\\d.]+)");
        Pattern responsePattern = Pattern.compile(
                "Average Response Time\\s*:\\s*([\\d.]+)");

        for (String line : text.split("\\R")) {
            if (line.contains("PREEMPTIVE SJF RESULTS")) {
                currentRun = new RunAnimation(SJF);
                runs.add(currentRun);
                continue;
            }
            if (line.contains("ACS RESULTS")) {
                currentRun = new RunAnimation("ACS");
                runs.add(currentRun);
                continue;
            }
            if (currentRun == null) {
                continue;
            }

            Matcher queueMatcher = queuePattern.matcher(line.trim());
            if (queueMatcher.matches()) {
                currentRun.queues.put(
                        Integer.parseInt(queueMatcher.group(1)),
                        new QueueSnapshot(
                                queueMatcher.group(2),
                                queueMatcher.group(3)));
                continue;
            }

            Matcher stepMatcher = stepPattern.matcher(line.trim());
            if (stepMatcher.matches()) {
                String action = stepMatcher.group(6);
                currentRun.steps.add(new SimulationStep(
                        Integer.parseInt(stepMatcher.group(1)),
                        Integer.parseInt(stepMatcher.group(2)),
                        stepMatcher.group(3),
                        stepMatcher.group(4) == null
                                ? 0
                                : Integer.parseInt(stepMatcher.group(4)),
                        stepMatcher.group(5) == null
                                ? -1
                                : Integer.parseInt(stepMatcher.group(5)),
                        action == null ? "RUNNING" : action));
                continue;
            }

            Matcher matcher = waitingPattern.matcher(line);
            if (matcher.find()) {
                currentRun.averageWaiting = matcher.group(1);
            }
            matcher = turnaroundPattern.matcher(line);
            if (matcher.find()) {
                currentRun.averageTurnaround = matcher.group(1);
            }
            matcher = responsePattern.matcher(line);
            if (matcher.find()) {
                currentRun.averageResponse = matcher.group(1);
            }
        }

        if (runs.isEmpty()
                || runs.stream().anyMatch(run -> run.steps.isEmpty())) {
            throw new IllegalStateException(
                    "The scheduler did not produce any timeline steps.");
        }
        return runs;
    }

    private void startPlayback(
            List<RunAnimation> runs,
            List<Process> processes) {
        animationRuns = runs;
        animationProcesses = new ArrayList<>();
        for (Process process : processes) {
            animationProcesses.add(new Process(process));
        }
        currentRunIndex = 0;
        currentStepIndex = 0;
        ganttChart.setRuns(runs);
        prepareCurrentRun();
        playPauseButton.setText("Pause");
        playPauseButton.setEnabled(true);
        stepButton.setEnabled(true);
        playbackTimer = new Timer(600, event -> advancePlayback());
        playbackTimer.start();
    }

    private void prepareCurrentRun() {
        if (currentRunIndex >= animationRuns.size()) {
            return;
        }
        RunAnimation run = animationRuns.get(currentRunIndex);
        schedulerValue.setText(run.name);
        clockValue.setText("0");
        cpuValue.setText("IDLE");
        initializeLiveTable();
        updateQueues(null, 0, null);
    }

    private void initializeLiveTable() {
        liveModel.setRowCount(0);
        for (Process process : animationProcesses) {
            liveModel.addRow(new Object[]{
                    process.name,
                    process.arrivalTime,
                    process.burstTime,
                    process.burstTime,
                    process.initialPriority,
                    "Not arrived"
            });
        }
    }

    private void advancePlayback() {
        if (currentRunIndex >= animationRuns.size()) {
            finishPlayback();
            return;
        }

        RunAnimation run = animationRuns.get(currentRunIndex);
        if (currentStepIndex >= run.steps.size()) {
            showRunMetrics(run);
            currentRunIndex++;
            currentStepIndex = 0;
            if (currentRunIndex >= animationRuns.size()) {
                finishPlayback();
                return;
            }
            prepareCurrentRun();
            run = animationRuns.get(currentRunIndex);
        }

        SimulationStep step = run.steps.get(currentStepIndex++);
        animateStep(run, step);

        if (currentStepIndex >= run.steps.size()
                && currentRunIndex == animationRuns.size() - 1) {
            showRunMetrics(run);
            finishPlayback();
        }
    }

    private void animateStep(
            RunAnimation run,
            SimulationStep step) {
        clockValue.setText(String.valueOf(step.end));
        cpuValue.setText(step.cpuDescription());
        statusLabel.setText(
                run.name + "  ·  time " + step.start + " → " + step.end);
        ganttChart.addStep(currentRunIndex, step);

        QueueSnapshot queues = run.queues.get(step.start);
        updateLiveRows(step, queues);
        updateQueues(step, step.start, queues);
    }

    private void updateLiveRows(
            SimulationStep step,
            QueueSnapshot queues) {
        for (int row = 0; row < liveModel.getRowCount(); row++) {
            String name = String.valueOf(liveModel.getValueAt(row, 0));
            int arrival = (Integer) liveModel.getValueAt(row, 1);
            int remaining = (Integer) liveModel.getValueAt(row, 3);

            if (step.start < arrival) {
                liveModel.setValueAt("Not arrived", row, 5);
                continue;
            }

            if (name.equals(step.processName)) {
                remaining = step.remaining;
                liveModel.setValueAt(remaining, row, 3);
                liveModel.setValueAt(
                        remaining == 0
                                ? "Completed"
                                : step.tier > 0
                                        ? "Running · T" + step.tier
                                        : "Running",
                        row, 5);
                continue;
            }

            if (remaining == 0) {
                liveModel.setValueAt("Completed", row, 5);
            } else if (queues != null) {
                liveModel.setValueAt(
                        queues.containsTierOne(name)
                                ? "Ready · T1"
                                : queues.containsTierTwo(name)
                                        ? "Waiting · T2"
                                        : "Ready",
                        row, 5);
            } else {
                liveModel.setValueAt("Ready", row, 5);
            }
        }
    }

    private void updateQueues(
            SimulationStep step,
            int time,
            QueueSnapshot queues) {
        if (queues != null) {
            tierOneValue.setText(displayQueue(queues.tierOne));
            tierTwoValue.setText(displayQueue(queues.tierTwo));
            readyQueueValue.setText(
                    "T1 " + displayQueue(queues.tierOne)
                            + "  ·  T2 " + displayQueue(queues.tierTwo));
            return;
        }

        List<String> ready = new ArrayList<>();
        for (int row = 0; row < liveModel.getRowCount(); row++) {
            String name = String.valueOf(liveModel.getValueAt(row, 0));
            int arrival = (Integer) liveModel.getValueAt(row, 1);
            int remaining = (Integer) liveModel.getValueAt(row, 3);
            if (arrival <= time && remaining > 0
                    && (step == null || !name.equals(step.processName))) {
                ready.add(name);
            }
        }
        String queue = ready.isEmpty()
                ? "No ready processes"
                : String.join("  ·  ", ready);
        readyQueueValue.setText(queue);
        tierOneValue.setText("—");
        tierTwoValue.setText("—");
    }

    private static String displayQueue(String queue) {
        if (queue.isEmpty() || "Empty".equals(queue)) {
            return "Empty";
        }
        Matcher matcher = Pattern.compile(
                "([A-Za-z0-9_-]+)(?:\\(Rem:[^)]*\\))?")
                .matcher(queue);
        List<String> names = new ArrayList<>();
        while (matcher.find()) {
            names.add(matcher.group(1));
        }
        return names.isEmpty() ? queue : String.join("  ·  ", names);
    }

    private void showRunMetrics(RunAnimation run) {
        waitingMetric.setText(metricOrDash(run.averageWaiting));
        turnaroundMetric.setText(metricOrDash(run.averageTurnaround));
        responseMetric.setText(metricOrDash(run.averageResponse));
    }

    private static String metricOrDash(String value) {
        return value == null ? "—" : value;
    }

    private void togglePlayback() {
        if (playbackTimer == null) {
            return;
        }
        if (playbackTimer.isRunning()) {
            playbackTimer.stop();
            playPauseButton.setText("Play");
        } else {
            playbackTimer.start();
            playPauseButton.setText("Pause");
        }
    }

    private void finishPlayback() {
        if (playbackTimer != null) {
            playbackTimer.stop();
        }
        playPauseButton.setText("Play");
        playPauseButton.setEnabled(false);
        stepButton.setEnabled(false);
        statusLabel.setText("Simulation completed successfully");
    }

    private void resetVisualization() {
        if (playbackTimer != null) {
            playbackTimer.stop();
        }
        animationRuns = Collections.emptyList();
        animationProcesses = Collections.emptyList();
        currentRunIndex = 0;
        currentStepIndex = 0;
        ganttChart.setRuns(animationRuns);
        liveModel.setRowCount(0);
        clockValue.setText("0");
        cpuValue.setText("IDLE");
        schedulerValue.setText("Waiting");
        waitingMetric.setText("—");
        turnaroundMetric.setText("—");
        responseMetric.setText("—");
        readyQueueValue.setText("No ready processes");
        tierOneValue.setText("—");
        tierTwoValue.setText("—");
        statusLabel.setText("Ready to simulate");
        playPauseButton.setText("Pause");
        playPauseButton.setEnabled(false);
        stepButton.setEnabled(false);
    }

    private List<Process> readProcesses() {

        List<Process> processes =
                new ArrayList<>();

        for (int row = 0;
                row < processModel.getRowCount();
                row++) {

            String name = String.valueOf(
                    processModel.getValueAt(row, 0)).trim();

            int arrival = parseInteger(
                    processModel.getValueAt(row, 1),
                    name + " arrival time", 0);

            int burst = parseInteger(
                    processModel.getValueAt(row, 2),
                    name + " burst time", 1);

            int priority = parseInteger(
                    processModel.getValueAt(row, 3),
                    name + " priority", 1);

            processes.add(
                    new Process(
                            name,
                            arrival,
                            burst,
                            priority));
        }

        return processes;
    }

    private static int parseInteger(
            Object value,
            String label,
            int minimum) {

        final int parsed;

        try {

            parsed = Integer.parseInt(
                    String.valueOf(value).trim());

        } catch (NumberFormatException exception) {

            throw new IllegalArgumentException(
                    label + " must be a whole number.");
        }

        if (parsed < minimum) {

            String requirement =
                    minimum == 0
                            ? "cannot be negative."
                            : "must be greater than zero.";

            throw new IllegalArgumentException(
                    label + " " + requirement);
        }

        return parsed;
    }

    private static Color processColor(String name) {
        int number = 0;
        Matcher matcher = Pattern.compile("\\d+").matcher(name);
        if (matcher.find()) {
            number = Integer.parseInt(matcher.group());
        }
        return PROCESS_COLORS[Math.max(0, number - 1)
                % PROCESS_COLORS.length];
    }

    private static final class RunAnimation {
        private final String name;
        private final List<SimulationStep> steps = new ArrayList<>();
        private final java.util.Map<Integer, QueueSnapshot> queues =
                new java.util.HashMap<>();
        private String averageWaiting;
        private String averageTurnaround;
        private String averageResponse;

        private RunAnimation(String name) {
            this.name = name;
        }
    }

    private static final class SimulationStep {
        private final int start;
        private final int end;
        private final String processName;
        private final int tier;
        private final int remaining;
        private final String action;

        private SimulationStep(
                int start,
                int end,
                String processName,
                int tier,
                int remaining,
                String action) {
            this.start = start;
            this.end = end;
            this.processName = processName;
            this.tier = tier;
            this.remaining = remaining;
            this.action = action;
        }

        private String cpuDescription() {
            if ("RUNNING".equals(action)) {
                return processName
                        + (tier > 0 ? "  ·  Tier " + tier : "  ·  Running");
            }
            return action;
        }
    }

    private static final class QueueSnapshot {
        private final String tierOne;
        private final String tierTwo;

        private QueueSnapshot(String tierOne, String tierTwo) {
            this.tierOne = tierOne;
            this.tierTwo = tierTwo;
        }

        private boolean containsTierOne(String name) {
            return containsProcess(tierOne, name);
        }

        private boolean containsTierTwo(String name) {
            return containsProcess(tierTwo, name);
        }

        private static boolean containsProcess(
                String queue, String name) {
            Pattern pattern = Pattern.compile(
                    "(?:^|[^A-Za-z0-9_-])"
                            + Pattern.quote(name)
                            + "(?:\\(|$)");
            return pattern.matcher(queue).find();
        }
    }

    private static final class GanttChart extends JPanel {
        private static final long serialVersionUID = 1L;
        private static final int LABEL_WIDTH = 72;
        private static final int TIME_SCALE = 38;
        private static final int ROW_HEIGHT = 35;
        private final List<RunAnimation> runs = new ArrayList<>();
        private final List<List<ChartSegment>> lanes = new ArrayList<>();
        private final List<Integer> laneTimes = new ArrayList<>();

        private GanttChart() {
            setBackground(new Color(250, 251, 247));
            setOpaque(true);
            setFont(new Font("SansSerif", Font.PLAIN, 10));
        }

        private void setRuns(List<RunAnimation> newRuns) {
            runs.clear();
            runs.addAll(newRuns);
            lanes.clear();
            laneTimes.clear();
            for (int i = 0; i < runs.size(); i++) {
                lanes.add(new ArrayList<>());
                laneTimes.add(0);
            }
            updateSize();
        }

        private void addStep(int lane, SimulationStep step) {
            if (lane < 0 || lane >= lanes.size()) {
                return;
            }
            List<ChartSegment> segments = lanes.get(lane);
            String process = step.processName;
            String kind = step.action;
            int tier = step.tier;
            if (!segments.isEmpty()) {
                ChartSegment previous = segments.get(segments.size() - 1);
                if (previous.end == step.start
                        && previous.kind.equals(kind)
                        && java.util.Objects.equals(previous.process, process)
                        && previous.tier == tier) {
                    previous.end = step.end;
                    laneTimes.set(lane, step.end);
                    updateSize();
                    return;
                }
            }
            segments.add(new ChartSegment(
                    step.start, step.end, process, tier, kind));
            laneTimes.set(lane, step.end);
            updateSize();
        }

        private void updateSize() {
            int maxTime = 0;
            for (int time : laneTimes) {
                maxTime = Math.max(maxTime, time);
            }
            int width = Math.max(
                    560, LABEL_WIDTH + maxTime * TIME_SCALE + 26);
            int height = Math.max(
                    126, 46 + runs.size() * ROW_HEIGHT);
            setPreferredSize(new Dimension(width, height));
            revalidate();
            repaint();
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);

            int maxTime = 1;
            for (int time : laneTimes) {
                maxTime = Math.max(maxTime, time);
            }
            int chartBottom = Math.max(
                    102, 43 + runs.size() * ROW_HEIGHT);
            g.setFont(new Font("SansSerif", Font.PLAIN, 9));
            g.setColor(MUTED);
            for (int time = 0; time <= maxTime; time++) {
                int x = LABEL_WIDTH + time * TIME_SCALE;
                g.drawString(String.valueOf(time), x - 3, 17);
                g.setColor(new Color(225, 230, 219));
                g.drawLine(x, 24, x, chartBottom);
                g.setColor(MUTED);
            }

            for (int lane = 0; lane < lanes.size(); lane++) {
                int y = 31 + lane * ROW_HEIGHT;
                String label = "ACS".equals(runs.get(lane).name)
                        ? "ACS"
                        : "SJF";
                g.setColor(DARK_OLIVE);
                g.setFont(new Font("SansSerif", Font.BOLD, 10));
                g.drawString(label, 10, y + 17);

                for (ChartSegment segment : lanes.get(lane)) {
                    int x = LABEL_WIDTH + segment.start * TIME_SCALE + 2;
                    int width = Math.max(
                            12, (segment.end - segment.start)
                                    * TIME_SCALE - 4);
                    Color color = "RUNNING".equals(segment.kind)
                            ? processColor(segment.process)
                            : "IDLE".equals(segment.kind)
                                    ? new Color(207, 213, 203)
                                    : GOLD;
                    g.setColor(color);
                    g.fillRoundRect(x, y, width, 25, 8, 8);

                    String text = "RUNNING".equals(segment.kind)
                            ? segment.process
                            : "IDLE".equals(segment.kind)
                                    ? "IDLE"
                                    : "CS";
                    if ("RUNNING".equals(segment.kind)
                            && segment.tier > 0
                            && width >= 62) {
                        text += " · T" + segment.tier;
                    }
                    g.setColor(
                            "IDLE".equals(segment.kind)
                                    ? DARK_OLIVE
                                    : Color.WHITE);
                    g.setFont(new Font("SansSerif", Font.BOLD, 9));
                    int textWidth = g.getFontMetrics().stringWidth(text);
                    if (textWidth + 8 < width) {
                        g.drawString(
                                text,
                                x + (width - textWidth) / 2,
                                y + 16);
                    }
                }
            }

            g.dispose();
        }
    }

    private static final class ChartSegment {
        private final int start;
        private int end;
        private final String process;
        private final int tier;
        private final String kind;

        private ChartSegment(
                int start, int end, String process, int tier, String kind) {
            this.start = start;
            this.end = end;
            this.process = process;
            this.tier = tier;
            this.kind = kind;
        }
    }
}
