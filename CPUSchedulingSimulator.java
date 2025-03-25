import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.*;

class Process {
    String id;
    int arrivalTime, burstTime, remainingTime, priority, waitingTime, turnaroundTime, finishTime, startTime, originalBurstTime;
    boolean started; // Tracks the first execution (for SJF Preemptive)

    // Constructor for general processes
    Process(String id, int arrivalTime, int burstTime) {
        this.id = id;
        this.arrivalTime = arrivalTime;
        this.burstTime = burstTime;
        this.originalBurstTime = burstTime;
        this.remainingTime = burstTime; // Initially, remaining time = burst time
        this.started = false;
    }

     // Constructor for priority scheduling
    Process(String id, int arrivalTime, int burstTime, int priority) {
        this(id, arrivalTime, burstTime);
        this.priority = priority;
    }
}

public class CPUSchedulingSimulator {
    private JFrame frame;
    private JTable processTable;
    private DefaultTableModel tableModel;
    private ArrayList<Process> processes;
    private JLabel avgWaitingTimeLabel, avgTurnaroundTimeLabel;
    private JPanel ganttChartPanel;
    private ArrayList<String> ganttChartLog = new ArrayList<>();
    private JComboBox<String> algorithmSelector;
    
    private final Color[] PASTEL_COLORS = {
        new Color(168, 201, 241),
        new Color(184, 168, 241),
        new Color(168, 241, 214),
        new Color(241, 168, 214),
        new Color(168, 214, 241),
        new Color(241, 168, 168),
        new Color(214, 241, 168),
        new Color(168, 241, 168),
        new Color(241, 214, 168),
        new Color(241, 225, 168)
    };

    public CPUSchedulingSimulator() {
        processes = new ArrayList<>();
        createGUI();
    }

    private void createGUI() {
        frame = new JFrame("CPU Scheduling Simulator");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout(10, 10));
        frame.getContentPane().setBackground(new Color(220, 240, 247));

        // Header
        JLabel headerLabel = new JLabel("CPU Scheduling Simulator", JLabel.CENTER);
        headerLabel.setFont(new Font("Comic Sans MS", Font.BOLD, 20));
        headerLabel.setForeground(new Color(25, 25, 25));
        headerLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        // Main content panel
        JPanel mainContentPanel = new JPanel();
        mainContentPanel.setLayout(new BoxLayout(mainContentPanel, BoxLayout.Y_AXIS));
        mainContentPanel.setBackground(new Color(220, 240, 247));
        mainContentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Create components
        JPanel inputPanel = createInputPanel();
        JPanel tablePanel = createTablePanel();
        JPanel bottomPanel = createBottomPanel();

        // Add components to main panel
        mainContentPanel.add(inputPanel);
        mainContentPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        mainContentPanel.add(tablePanel);
        mainContentPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        mainContentPanel.add(bottomPanel);

        frame.add(headerLabel, BorderLayout.NORTH);
        frame.add(mainContentPanel, BorderLayout.CENTER);

        frame.setSize(950, 800);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private JPanel createInputPanel() {
        JPanel inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setBackground(new Color(245, 250, 252));
        inputPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(25, 25, 25), 2),
                " Input Parameters "
            ),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        // Process Count
        gbc.gridx = 0; gbc.gridy = 0;
        JLabel processCountLabel = new JLabel("Number of Processes (3-10):");
        inputPanel.add(processCountLabel, gbc);

        gbc.gridx = 1;
        JTextField processCountField = new JTextField(10);
        inputPanel.add(processCountField, gbc);

        // Arrival Time
        gbc.gridx = 0; gbc.gridy = 1;
        JLabel arrivalTimeLabel = new JLabel("Arrival Time (comma-separated):");
        inputPanel.add(arrivalTimeLabel, gbc);

        gbc.gridx = 1;
        JTextField arrivalTimeField = new JTextField(10);
        inputPanel.add(arrivalTimeField, gbc);

        // Burst Time
        gbc.gridx = 0; gbc.gridy = 2;
        JLabel burstTimeLabel = new JLabel("Burst Time (comma-separated):");
        inputPanel.add(burstTimeLabel, gbc);

        gbc.gridx = 1;
        JTextField burstTimeField = new JTextField(10);
        inputPanel.add(burstTimeField, gbc);

        // Priority
        gbc.gridx = 0; gbc.gridy = 3;
        JLabel priorityLabel = new JLabel("Priority (comma-separated):");
        inputPanel.add(priorityLabel, gbc);

        gbc.gridx = 1;
        JTextField priorityField = new JTextField(10);
        inputPanel.add(priorityField, gbc);

        // Algorithm Selector
        gbc.gridx = 0; gbc.gridy = 4;
        JLabel algorithmLabel = new JLabel("Select Algorithm:");
        inputPanel.add(algorithmLabel, gbc);

        gbc.gridx = 1;
        String[] algorithms = {"Round Robin", "SJF Non-Preemptive", "Priority Non-Preemptive", "SJF Preemptive"};
        algorithmSelector = new JComboBox<>(algorithms);
        inputPanel.add(algorithmSelector, gbc);

        // Calculate Button
        gbc.gridx = 1; gbc.gridy = 7;
        // gbc.gridwidth = 2;
        JButton calculateButton = new JButton("Calculate");
        calculateButton.setBackground(new Color(220, 240, 247));
        calculateButton.setOpaque(true);
        calculateButton.setForeground(Color.BLACK);
        calculateButton.setFont(new Font("Comic Sans MS", Font.BOLD, 14));
        calculateButton.setBorder(new LineBorder(new Color(25, 25, 25), 2, true));
        inputPanel.add(calculateButton, gbc);

        // Add Calculate Button Action Listener
        calculateButton.addActionListener(e -> {
            try {
                int processCount = Integer.parseInt(processCountField.getText());

                if (processCount < 3 || processCount > 10) {
                    JOptionPane.showMessageDialog(frame, "The number of processes must be between 3 and 10.",
                            "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                String[] arrivalTimes = arrivalTimeField.getText().split(",");
                String[] burstTimes = burstTimeField.getText().split(",");
                String[] priorities = priorityField.getText().isEmpty() ? new String[0] : priorityField.getText().split(",");

                if (arrivalTimes.length != processCount || burstTimes.length != processCount) {
                    JOptionPane.showMessageDialog(frame, "Mismatch in number of processes and input values.",
                            "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (algorithmSelector.getSelectedItem().equals("Priority Non-Preemptive") && priorities.length != processCount) {
                    JOptionPane.showMessageDialog(frame, "Mismatch in number of processes and priorities.",
                            "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                processes.clear();
                tableModel.setRowCount(0);

                for (int i = 0; i < processCount; i++) {
                    int arrivalTime = Integer.parseInt(arrivalTimes[i].trim());
                    int burstTime = Integer.parseInt(burstTimes[i].trim());
                    if (priorities.length > 0) {
                        int priority = Integer.parseInt(priorities[i].trim());
                        processes.add(new Process("P" + i, arrivalTime, burstTime, priority));
                    } else {
                        processes.add(new Process("P" + i, arrivalTime, burstTime));
                    }
                }

                switch ((String) algorithmSelector.getSelectedItem()) {
                    case "Round Robin":
                        calculateRoundRobin();
                        break;
                    case "SJF Non-Preemptive":
                        calculateSJFNonPreemptive();
                        break;
                    case "Priority Non-Preemptive":
                        calculatePriorityNonPreemptive();
                        break;
                    case "SJF Preemptive":
                        calculateSJFPreemptive();
                        break;
                }

                ganttChartPanel.repaint();

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(frame, "Invalid input. Please check your values.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        return inputPanel;
    }

    private JPanel createTablePanel() {
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(new Color(245, 250, 252));
        tablePanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(25, 25, 25), 2),
            " Process Details "
        ));

        tableModel = new DefaultTableModel(new Object[]{
            "Process", "Arrival Time", "Burst Time", "Priority", "Finish Time", "Waiting Time", "Turnaround Time"
        }, 0);
        processTable = new JTable(tableModel);
        processTable.setPreferredScrollableViewportSize(new Dimension(800, 500));
        JScrollPane scrollPane = new JScrollPane(processTable);
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        return tablePanel;
    }

    private JPanel createBottomPanel() {
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(new Color(220, 240, 247));

        // Results Panel
        JPanel resultsPanel = new JPanel(new GridLayout(2, 1));
        resultsPanel.setBackground(new Color(220, 240, 247));
        avgWaitingTimeLabel = new JLabel("Average Waiting Time: 0.00");
        avgTurnaroundTimeLabel = new JLabel("Average Turnaround Time: 0.00");
        avgWaitingTimeLabel.setFont(new Font("Comic Sans MS", Font.BOLD, 14));
        avgTurnaroundTimeLabel.setFont(new Font("Comic Sans MS", Font.BOLD, 14));
        avgTurnaroundTimeLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        resultsPanel.add(avgWaitingTimeLabel);
        resultsPanel.add(avgTurnaroundTimeLabel);

        // Gantt Chart Panel
        ganttChartPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawGanttChart(g);
            }
        };
        ganttChartPanel.setPreferredSize(new Dimension(800, 400));
        ganttChartPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(25, 25, 25), 2),
            " Gantt Chart "
        ));
        ganttChartPanel.setBackground(new Color(245, 250, 252));

        bottomPanel.add(resultsPanel, BorderLayout.NORTH);
        bottomPanel.add(ganttChartPanel, BorderLayout.CENTER);

        return bottomPanel;
    }

    private void drawGanttChart(Graphics g) {
        if (ganttChartLog.isEmpty()) return;

        int startX = 50;
        int startY = 50;
        int barHeight = 30;
        int scale = 25;
        
        int currentX = startX;
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Process color mapping
        java.util.Map<String, Color> processColors = new java.util.HashMap<>();
        int colorIndex = 0;

        // Consolidate consecutive entries
        ArrayList<String> consolidatedLog = new ArrayList<>();
        String currentProcess = ganttChartLog.get(0);
        int startTime = 0;

        for (int i = 1; i <= ganttChartLog.size(); i++) {
            if (i == ganttChartLog.size() || !ganttChartLog.get(i).equals(currentProcess)) {
                consolidatedLog.add(currentProcess + ":" + startTime + "-" + i);
                if (i < ganttChartLog.size()) {
                    currentProcess = ganttChartLog.get(i);
                    startTime = i;
                }
            }
        }

        // Draw the consolidated Gantt chart
        for (String log : consolidatedLog) {
            String[] parts = log.split(":");
            String processId = parts[0];
            String[] times = parts[1].split("-");
            int start = Integer.parseInt(times[0]);
            int end = Integer.parseInt(times[1]);
            int width = (end - start) * scale;

            // Assign colors to processes
            if (!processColors.containsKey(processId) && !processId.equals("Idle")) {
                processColors.put(processId, PASTEL_COLORS[colorIndex % PASTEL_COLORS.length]);
                colorIndex++;
            }

            // Fill rectangle with process color or gray for idle
            Color fillColor = processId.equals("Idle") ? new Color(211, 211, 211) : processColors.get(processId);
            g2d.setColor(fillColor);
            g2d.fillRoundRect(currentX, startY, width, barHeight, 10, 10);

            // Draw border
            g2d.setColor(Color.BLACK);
            g2d.setStroke(new BasicStroke(2));
            g2d.drawRoundRect(currentX, startY, width, barHeight, 10, 10);

            // Draw process ID
            g2d.setFont(new Font("Comic Sans MS", Font.BOLD, 12));
            g2d.setColor(Color.BLACK);
            FontMetrics fm = g2d.getFontMetrics();
            String displayText = processId;
            int textX = currentX + (width - fm.stringWidth(displayText)) / 2;
            int textY = startY + (barHeight + fm.getAscent() - fm.getDescent()) / 2;
            g2d.drawString(displayText, textX, textY);

            // Draw time markers
            g2d.setFont(new Font("Comic Sans MS", Font.PLAIN, 10));
            g2d.drawString(String.valueOf(start), currentX, startY + barHeight + 15);

            currentX += width;
        }

        // Draw final time marker
        g2d.drawString(String.valueOf(Integer.parseInt(consolidatedLog.get(consolidatedLog.size() - 1)
            .split(":")[1].split("-")[1])), currentX, startY + barHeight + 15);
    }

    private void calculateRoundRobin() {
        ganttChartLog.clear(); //Clear the Gantt chart
        int currentTime = 0; //Initialize current time
        int quantum = 3; //Set the quantum time for RR
        double totalWaitingTime = 0, totalTurnaroundTime = 0; //Variables to calculate average waiting and turnaround time
    
        ArrayList<Process> readyQueue = new ArrayList<>(); //Queue to store process that are ready to execute
        ArrayList<Process> arrivalQueue = new ArrayList<>(processes); //Queue for processes waiting to arrive, sort by arrival time
    
        // Sort processes by on arrival time (ensure that process in order of arrival)
        arrivalQueue.sort(Comparator.comparingInt(p -> p.arrivalTime));
    
        // Continue as long as we have unprocessed processes
        while (!readyQueue.isEmpty() || !arrivalQueue.isEmpty()) {
            // Add processes that have arrived by the current time
            while (!arrivalQueue.isEmpty() && arrivalQueue.get(0).arrivalTime <= currentTime) {
                readyQueue.add(arrivalQueue.remove(0)); //Move the process from arrival queue to ready queue
            }
    
            // If there are no processes in the ready queue, take the next process
            if (!readyQueue.isEmpty()) {
                //Take the next process from the ready queu and assign it for execution
                Process currentProcess = readyQueue.remove(0);
                int executeTime = Math.min(currentProcess.remainingTime, quantum);//Execute process for the minimum of its remaining time or quantum
                ganttChartLog.add(currentProcess.id + ":" + currentTime + "-" + (currentTime + executeTime));//Add the execution of process to the Gantt Chart
                currentTime += executeTime; //update current time
                currentProcess.remainingTime -= executeTime; //subs executed time from the remaining time
    
                // If the process finishes, calculate its turnaround and waiting times
                if (currentProcess.remainingTime == 0) {
                    currentProcess.finishTime = currentTime;//Mark the finish time of process
                    currentProcess.turnaroundTime = currentProcess.finishTime - currentProcess.arrivalTime; //Calculate turnaround time
                    currentProcess.waitingTime = currentProcess.turnaroundTime - currentProcess.originalBurstTime;//Calculate waiting time
    
                    totalWaitingTime += currentProcess.waitingTime; //Add to total waiting time
                    totalTurnaroundTime += currentProcess.turnaroundTime; //Add to total turnaround time
                    //Add the process's final statistic to the table
                    tableModel.addRow(new Object[]{
                            currentProcess.id, currentProcess.arrivalTime, currentProcess.originalBurstTime, "",
                            currentProcess.finishTime, currentProcess.waitingTime, currentProcess.turnaroundTime
                    });
                } else {
                    // if the process is not finish, it gets added back into the ready queu for execution
                    while (!arrivalQueue.isEmpty() && arrivalQueue.get(0).arrivalTime <= currentTime) {
                        readyQueue.add(arrivalQueue.remove(0)); //move new process from the arrival queue to ready queue
                    }
                    readyQueue.add(currentProcess); // Add the current process back to the ready queue
                }
            } else {
                // If the ready queue is empty, and no processes are available, idle
                if (!arrivalQueue.isEmpty()) {
                    ganttChartLog.add("Idle:" + currentTime + "-" + (currentTime + 1));
                    currentTime++; //Increasy the time by 1 unit to simulate idle time
                }
            }
        }
    //After all process are finished, calculate the avg WT and TA
        avgWaitingTimeLabel.setText(String.format("Average Waiting Time: %.2f", totalWaitingTime / processes.size()));
        avgTurnaroundTimeLabel.setText(String.format("Average Turnaround Time: %.2f", totalTurnaroundTime / processes.size()));
    }    
    

    private void calculateSJFNonPreemptive() {
        ganttChartLog.clear();//Clear the Gantt chart
        //Sort the process by AT and then by the BT
        processes.sort(Comparator.comparingInt((Process p) -> p.arrivalTime).thenComparingInt(p -> p.burstTime));
    
        int currentTime = 0;
        double totalWaitingTime = 0, totalTurnaroundTime = 0;
    
        for (Process process : processes) {
            //if the current time is less than the arrival time, add idle time to Gant Chart
            if (currentTime < process.arrivalTime) {
                ganttChartLog.add("Idle:" + currentTime + "-" + process.arrivalTime);
                currentTime = process.arrivalTime;
            }
            //Calculate waiting time and turnaround time for process
            process.waitingTime = currentTime - process.arrivalTime;
            process.turnaroundTime = process.waitingTime + process.burstTime;
            process.finishTime = currentTime + process.burstTime;
            currentTime += process.burstTime;
            //Add to total waiting and turnaround times
            totalWaitingTime += process.waitingTime;
            totalTurnaroundTime += process.turnaroundTime;
            //log the process execution in the Gantt Chart
            ganttChartLog.add(process.id + ":" + (currentTime - process.burstTime) + "-" + currentTime);
            //Add the process details into the Table
            tableModel.addRow(new Object[]{
                    process.id, process.arrivalTime, process.originalBurstTime, "",
                    process.finishTime, process.waitingTime, process.turnaroundTime
            });
        }
        //Calculate and display the avg WT and TA times
        avgWaitingTimeLabel.setText(String.format("Average Waiting Time: %.2f", totalWaitingTime / processes.size()));
        avgTurnaroundTimeLabel.setText(String.format("Average Turnaround Time: %.2f", totalTurnaroundTime / processes.size()));
    }

    private void calculatePriorityNonPreemptive() {
        ganttChartLog.clear();//Clear the Gantt Chart 
        ArrayList<Process> readyQueue = new ArrayList<>(); // Queue to store processes ready to execute
    
        int currentTime = 0, completedCount = 0;
        double totalWaitingTime = 0, totalTurnaroundTime = 0;
        //while there are still processes to complete
        while (completedCount < processes.size()) {
            //Add processes that have been arrived by the current time to ready queue
            for (Process process : processes) {
                if (process.arrivalTime <= currentTime && !readyQueue.contains(process) && process.finishTime == 0) {
                    readyQueue.add(process);
                }
            }
            //If there are processes in the redy state queue, select the one with the highest priority
            if (!readyQueue.isEmpty()) {
                //Sort process by priority and then by arrival time
                readyQueue.sort(Comparator.comparingInt((Process p) -> p.priority)
                                           .thenComparingInt(p -> p.arrivalTime));
    
                Process currentProcess = readyQueue.remove(0);//Take the highest priority process
                //If the current time is less than the process's arrival time, add idle time
                if (currentTime < currentProcess.arrivalTime) {
                    ganttChartLog.add("Idle:" + currentTime + "-" + currentProcess.arrivalTime);
                    currentTime = currentProcess.arrivalTime;
                }
                //log the rocess execution in the Gantt Chart
                ganttChartLog.add(currentProcess.id + ":" + currentTime + "-" + (currentTime + currentProcess.burstTime));
                //Calculate WTand TA
                currentProcess.finishTime = currentTime + currentProcess.burstTime;
                currentProcess.turnaroundTime = currentProcess.finishTime - currentProcess.arrivalTime;
                currentProcess.waitingTime = currentProcess.turnaroundTime - currentProcess.burstTime;
    
                totalWaitingTime += currentProcess.waitingTime;
                totalTurnaroundTime += currentProcess.turnaroundTime;
                //Add process details to table
                tableModel.addRow(new Object[]{
                        currentProcess.id, currentProcess.arrivalTime, currentProcess.burstTime, currentProcess.priority,
                        currentProcess.finishTime, currentProcess.waitingTime, currentProcess.turnaroundTime
                });
    
                currentTime = currentProcess.finishTime;// Uodate current time to process's finish time
                completedCount++;//Increment completed processes count
            } else {
                //if no process is ready, add idle time
                ganttChartLog.add("Idle:" + currentTime + "-" + (currentTime + 1));
                currentTime++;
            }
        }
        // Calculate and display the average waiting and turnaround times
        avgWaitingTimeLabel.setText(String.format("Average Waiting Time: %.2f", totalWaitingTime / processes.size()));
        avgTurnaroundTimeLabel.setText(String.format("Average Turnaround Time: %.2f", totalTurnaroundTime / processes.size()));
    }

    private void calculateSJFPreemptive() {
        ganttChartLog.clear(); //Clear the Gantt Chart
        PriorityQueue<Process> readyQueue = new PriorityQueue<>(
                Comparator.comparingInt((Process p) -> p.remainingTime).thenComparingInt(p -> p.arrivalTime)); //Min heap by remaining time

        int currentTime = 0;
        double totalWaitingTime = 0, totalTurnaroundTime = 0;
        int completedCount = 0;
        Process lastProcess = null;//track the last process to handle preemption
        int lastStartTime = 0; 

        while (completedCount < processes.size()) {
            //add process that have been arrived by the current time to ready queue
            for (Process process : processes) {
                if (process.arrivalTime == currentTime && !readyQueue.contains(process) && process.remainingTime > 0) {
                    readyQueue.add(process);
                }
            }
            //if there are processes in the ready queue, execute the one with shortest remaining time
            if (!readyQueue.isEmpty()) {
                Process currentProcess = readyQueue.poll(); // select the process with the smallest remaining time
                //If the process is starting for the first time, mark its start time
                if (!currentProcess.started) {
                    currentProcess.startTime = currentTime;
                    currentProcess.started = true;
                }
                //If the previous process is not the same as current one, log its execution
                if (lastProcess != null && !lastProcess.id.equals(currentProcess.id)) {
                    ganttChartLog.add(lastProcess.id + ":" + lastStartTime + "-" + currentTime);
                    lastStartTime = currentTime;
                }

                lastProcess = currentProcess;

                currentProcess.remainingTime--;//execute the cureent process for 1 unit of time
                //If the process is finished, calculate its waiting and turanaround times
                if (currentProcess.remainingTime == 0) {
                    currentProcess.finishTime = currentTime + 1;
                    currentProcess.turnaroundTime = currentProcess.finishTime - currentProcess.arrivalTime;
                    currentProcess.waitingTime = currentProcess.turnaroundTime - currentProcess.originalBurstTime;

                    totalWaitingTime += currentProcess.waitingTime;
                    totalTurnaroundTime += currentProcess.turnaroundTime;

                    tableModel.addRow(new Object[]{
                            currentProcess.id, currentProcess.arrivalTime, currentProcess.originalBurstTime, "",
                            currentProcess.finishTime, currentProcess.waitingTime, currentProcess.turnaroundTime
                    });

                    completedCount++;
                } else {
                    readyQueue.add(currentProcess); //Re-add the process to ready queue if it has remaining time
                }
            } else {
                //If no processes are available, add idle time
                if (lastProcess != null) {
                    ganttChartLog.add(lastProcess.id + ":" + lastStartTime + "-" + currentTime);
                    lastProcess = null;
                }
                ganttChartLog.add("Idle:" + currentTime + "-" + (currentTime + 1));
            }

            currentTime++;//Increment current time
        }

        if (lastProcess != null) {
            ganttChartLog.add(lastProcess.id + ":" + lastStartTime + "-" + currentTime);
        }
        // Calculate and display the average waiting and turnaround times
        avgWaitingTimeLabel.setText(String.format("Average Waiting Time: %.2f", totalWaitingTime / processes.size()));
        avgTurnaroundTimeLabel.setText(String.format("Average Turnaround Time: %.2f", totalTurnaroundTime / processes.size()));
    }
//Run the java file
    public static void main(String[] args) {
        SwingUtilities.invokeLater(CPUSchedulingSimulator::new);
    }
}