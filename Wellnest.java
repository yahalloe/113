import javax.swing.*;
import javax.swing.border.EmptyBorder;

import com.toedter.calendar.JCalendar;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAdjusters;
import java.util.Stack;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Wellnest extends  JFrame  {

    private JPanel homePanel;
    private JPanel todayPanel;
    private JPanel statsPanel;
    private JPanel allHabitsPanel;
    private JPanel sidebarPanel;
    private JButton toggleSidebarButton;
    private JButton todayButton;
    private JButton statsButton;
    private JButton allHabitsButton;
    private JButton addButton; // Button to add tasks
    private JButton backButton; // Button to go back to the previous panel
    private Stack<JPanel> panelStack; // Stack to keep track of panels
    private JPanel currentPanel;
    private LocalDate currentDate; // Variable to store the current date
    private Map<String, Float> taskProgressDatabase;
    private Map<String, String> taskStatusDatabase;

    private int sidebarWidth = 200;
    private boolean isSidebarExpanded = true;

    // File path for the tasks database
    private static final String TASKS_FILE_PATH = "tasks.txt";
    private static final String TASK_COMPLETED_FILE_PATH = "TaskCompleted.txt";
    private static final String TASK_PROGRESS_FILE_PATH = "TaskProgress.txt";
    
    // Task database
    private Map<LocalDate, List<String>> taskDatabase;

    public Wellnest() {
        setTitle("Wellnest App");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Initialize task database
        taskDatabase = new HashMap<>();
        taskStatusDatabase = new HashMap<>();
        taskProgressDatabase = new HashMap<>();

        // Load tasks from file
        loadTasksFromFile();
        loadTaskProgressFromFile();
        loadTaskStatusFromFile();

        // Initialize panel stack
        panelStack = new Stack<>();
        

        // Get the current date
        currentDate = LocalDate.now();

        homePanel = new JPanel(new BorderLayout());
        homePanel.setBackground(Color.GRAY);

        todayPanel = createTodayPanel();
        statsPanel = createStatsPanel();
        allHabitsPanel = createAllHabitsPanel();

        sidebarPanel = createSidebarPanel();

        homePanel.add(createTopPanel(), BorderLayout.NORTH);
        homePanel.add(sidebarPanel, BorderLayout.WEST);
        homePanel.add(todayPanel, BorderLayout.CENTER);

        add(homePanel);
        setVisible(true);
        setLocationRelativeTo(null);

        panelStack.push(todayPanel); // Initially, todayPanel is the current panel
        currentPanel = todayPanel;
    }

    private JPanel createTodayPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
    
        // Get the tasks for the current date
        List<String> tasksForCurrentDate = taskDatabase.get(currentDate);
    
        // Create a panel to hold the calendar and tasks
        JPanel mainPanel = new JPanel(new BorderLayout());
    
        // Create a panel for the calendar
        JPanel calendarPanel = new JPanel(new GridLayout(2, 7)); // 2 rows for days of the week and dates
        calendarPanel.setBackground(Color.WHITE);
        calendarPanel.setPreferredSize(new Dimension(50, 90));
    
        // Add labels for the days of the week
        String[] daysOfWeek = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        for (String day : daysOfWeek) {
            JLabel dayLabel = new JLabel(day, SwingConstants.CENTER);
            calendarPanel.add(dayLabel);
        }
    
        // Get the start of the current week (Sunday)
        LocalDate startOfWeek = currentDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));
    
        // Add labels for the dates in the current week
        for (int i = 0; i < 7; i++) {
            LocalDate date = startOfWeek.plusDays(i);
            JLabel dateLabel = new JLabel(Integer.toString(date.getDayOfMonth()), SwingConstants.CENTER);
            dateLabel.setOpaque(true);
            dateLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
    
            // Highlight the current date
            if (date.equals(currentDate)) {
                dateLabel.setBackground(Color.YELLOW);
                dateLabel.setForeground(Color.RED);
            }
    
            calendarPanel.add(dateLabel);
        }
    
        // Add the calendar panel to the main panel
        mainPanel.add(calendarPanel, BorderLayout.NORTH);
    
        // Create a panel for the tasks
        JPanel taskPanel = new JPanel(new GridLayout(tasksForCurrentDate.size(), 1)); // One column for tasks
        taskPanel.setBackground(Color.WHITE);
    
        // Add task panels to the task panel
        if (tasksForCurrentDate != null && !tasksForCurrentDate.isEmpty()) {
            for (String task : tasksForCurrentDate) {
                JPanel taskItemPanel = createTaskItemPanel(currentDate.toString(), task);
                taskPanel.add(taskItemPanel);
            }
        } else {
            // If there are no tasks for the current date, display a message
            JLabel noTasksLabel = new JLabel("No tasks for today.", SwingConstants.CENTER);
            taskPanel.add(noTasksLabel);
        }
    
        // Add the task panel to the main panel
        mainPanel.add(new JScrollPane(taskPanel), BorderLayout.CENTER); // Add a scroll pane for tasks
    
        // Add the main panel to the panel
        panel.add(mainPanel, BorderLayout.CENTER);
    
        return panel;
    }
    
    private JPanel createTaskItemPanel(String date, String taskName) {
        JPanel taskPanel = new JPanel(new BorderLayout());
        
        // Create a label for the task name
        JLabel nameLabel = new JLabel(taskName, SwingConstants.CENTER);
        taskPanel.add(nameLabel, BorderLayout.NORTH);
        
        // Create a panel for the buttons and progress bar
        JPanel buttonPanel = new JPanel(new GridLayout(1, 4)); // 1 row and 4 columns
        
        // Create buttons for the task item
        JButton completedButton = new JButton("Completed");
        JButton skippedButton = new JButton("Skipped");
        JButton button1 = new JButton("1");
        
        // Create a progress bar for the task item
        JProgressBar progressBar = new JProgressBar();
        progressBar.setStringPainted(true);
        
        // Create a JTextField to store the float progress value
        JTextField progressField = new JTextField("0.0");
        progressField.setEditable(false);
        
        // Get progress value from the database
        float initialProgressValue = getTaskProgressValue(date, taskName);
        progressBar.setValue((int) initialProgressValue);
        progressBar.setString(String.format("%.1f%%", initialProgressValue));
        progressField.setText(String.valueOf(initialProgressValue));
        
        // Get total steps from the database
        int[] progressData = getTaskProgress(date, taskName);
        int totalSteps = progressData[1];
        
        // Get the current task status from the database
        String taskStatus = getTaskStatus(date, taskName);
        
        // If the initial progress is 100% or status is "Completed", disable the buttons and add completion status
        if (initialProgressValue >= 100.0f || "Completed".equals(taskStatus)) {
            completedButton.setEnabled(false);
            skippedButton.setEnabled(false);
            button1.setEnabled(false);
            JLabel statusLabel = new JLabel("Task Completed", SwingConstants.CENTER);
            taskPanel.add(statusLabel, BorderLayout.SOUTH);
        } else if ("Skipped".equals(taskStatus)) {
            completedButton.setEnabled(false);
            skippedButton.setEnabled(false);
            button1.setEnabled(false);
            JLabel statusLabel = new JLabel("Task Skipped", SwingConstants.CENTER);
            taskPanel.add(statusLabel, BorderLayout.SOUTH);
        }
        
        // Add action listeners to the buttons
        completedButton.addActionListener(e -> {
            progressBar.setValue(100); // Set progress to 100%
            progressBar.setString("100%");
            completedButton.setEnabled(false);
            skippedButton.setEnabled(false);
            button1.setEnabled(false);
            JLabel statusLabel = new JLabel("Task Completed", SwingConstants.CENTER);
            taskPanel.add(statusLabel, BorderLayout.SOUTH);
            taskPanel.revalidate();
            taskPanel.repaint();
            saveTaskStatus(date, taskName, "Completed");
            taskProgressDatabase.put(date + "|" + taskName, 100.0f);
            saveTaskProgressToFile();
        });
        
        skippedButton.addActionListener(e -> {
            JLabel statusLabel = new JLabel("Task Skipped", SwingConstants.CENTER);
            taskPanel.add(statusLabel, BorderLayout.SOUTH);
            completedButton.setEnabled(false);
            skippedButton.setEnabled(false);
            button1.setEnabled(false);
            taskPanel.revalidate();
            taskPanel.repaint();
            saveTaskStatus(date, taskName, "Skipped");
        });
        
        button1.addActionListener(e -> {
            float progressValue = Float.parseFloat(progressField.getText());
            if (progressValue < 100) {
                float increment = 100.0f / totalSteps;
        
                // Add the increment to the current value
                float newProgressValue = progressValue + increment;
        
                // Ensure newProgressValue doesn't exceed 100
                if (newProgressValue > 100.0f) {
                    newProgressValue = 100.0f;
                }
        
                // Update the progress in the taskProgress.txt file
                updateTaskProgress(date, taskName, newProgressValue);
        
                // Update the JTextField with the new progress
                progressField.setText(String.valueOf(newProgressValue));
        
                // Set the progress bar's value and update its display text with float precision
                progressBar.setValue((int) newProgressValue);
                progressBar.setString(String.format("%.1f%%", newProgressValue)); // Update progress text with one decimal place
        
                // Check if the progress is now complete
                if (newProgressValue >= 100.0f) {
                    JLabel statusLabel = new JLabel("Task Completed", SwingConstants.CENTER);
                    taskPanel.add(statusLabel, BorderLayout.SOUTH);
                    completedButton.setEnabled(false);
                    skippedButton.setEnabled(false);
                    button1.setEnabled(false);
                    // Repaint the task panel
                    taskPanel.revalidate();
                    taskPanel.repaint();
                    saveTaskStatus(date, taskName, "Completed");
                    // Save task progress status
                    taskProgressDatabase.put(date + "|" + taskName, newProgressValue);
                    saveTaskProgressToFile();
                } else {
                    // Save task progress status
                    taskProgressDatabase.put(date + "|" + taskName, newProgressValue);
                    saveTaskProgressToFile();
                }
            }
        });
        
        // Add components to the button panel
        buttonPanel.add(completedButton);
        buttonPanel.add(skippedButton);
        buttonPanel.add(button1);
        buttonPanel.add(progressBar);
        
        // Add the button panel to the task panel
        taskPanel.add(buttonPanel, BorderLayout.CENTER);
        
        return taskPanel;
    }
    
    // Method to save task status
    private void saveTaskStatus(String date, String taskName, String status) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(TASK_COMPLETED_FILE_PATH, true))) {
            String entry = date + "|" + taskName + "|" + status;
            writer.write(entry);
            writer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    
    private JPanel createStatsPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(Color.WHITE);
        JLabel label = new JLabel("Stats Panel");


        panel.add(label);
        return panel;
    }

    private JPanel createAllHabitsPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(Color.WHITE);
        JLabel label = new JLabel("All Habits Panel");


        panel.add(label);
        return panel;
    }

    private JPanel createSidebarPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(Color.LIGHT_GRAY);
        panel.setPreferredSize(new Dimension(200, getHeight()));

        panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.insets = new Insets(10, 10, 50, 10);

        todayButton = new JButton("Today");
        statsButton = new JButton("Stats");
        allHabitsButton = new JButton("All Habits");

        Dimension buttonSize = new Dimension(150, 40);
        todayButton.setPreferredSize(buttonSize);
        statsButton.setPreferredSize(buttonSize);
        allHabitsButton.setPreferredSize(buttonSize);

        todayButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showTodayPanel();
            }
        });

        statsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showStatsPanel();
            }
        });

        allHabitsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showAllHabitsPanel();
            }
        });

        panel.add(todayButton, gbc);
        gbc.gridy++;
        panel.add(statsButton, gbc);
        gbc.gridy++;
        panel.add(allHabitsButton, gbc);

        return panel;
    }

    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        toggleSidebarButton = new JButton("☰"); // Unicode for the hamburger icon
        addButton = new JButton("+");
        backButton = new JButton("← "); // Back button with left arrow
        backButton.setEnabled(false); // Initially disabled as there's no previous panel

        // Action listener for toggleSidebarButton
        toggleSidebarButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                toggleSidebar();
            }
        });

        // Action listener for addButton
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openAddPanel();
            }
        });

        // Action listener for backButton
        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                goBack();
            }
        });

        panel.add(toggleSidebarButton, BorderLayout.WEST);
        panel.add(addButton, BorderLayout.EAST);
        return panel;
        }

    private void showTodayPanel() {
        switchPanel(todayPanel);
    }

    private void showStatsPanel() {
        switchPanel(statsPanel);
    }

    private void showAllHabitsPanel() {
        switchPanel(allHabitsPanel);
    }

    private void toggleSidebar() {
        int targetWidth = isSidebarExpanded ? 0 : sidebarWidth;
        Timer timer = new Timer(10, new ActionListener() {
            int currentWidth = sidebarPanel.getWidth();

            @Override
            public void actionPerformed(ActionEvent e) {
                if ((isSidebarExpanded && currentWidth <= targetWidth) || (!isSidebarExpanded && currentWidth >= targetWidth)) {
                    ((Timer) e.getSource()).stop();
                    sidebarPanel.setPreferredSize(new Dimension(targetWidth, getHeight()));
                    sidebarPanel.revalidate();
                    sidebarPanel.repaint();
                    isSidebarExpanded = !isSidebarExpanded;
                } else {
                    int step = isSidebarExpanded ? -5 : 5;
                    currentWidth += step;
                    sidebarPanel.setPreferredSize(new Dimension(currentWidth, getHeight()));
                    sidebarPanel.revalidate();
                    sidebarPanel.repaint();
                }
            }
        });
        timer.start();
    }

    private void openAddPanel() {
        setCurrentPanel(createAddPanel());
    }

    private JPanel createAddPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 1));

        // Create buttons for selecting between regular habit and one-time task
        JButton regularHabitButton = new JButton("Add Regular Habit");
        JButton oneTimeTaskButton = new JButton("Add One-Time Task");

        // Attach action listeners to the buttons
        regularHabitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openCalendarPanel();
            }
        });

        oneTimeTaskButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Handle the action when the "Add One-Time Task" button is clicked
                // Implement your logic here
                System.out.println("Add One-Time Task button clicked");
            }
        });

        // Add buttons to the panel
        panel.add(regularHabitButton);
        panel.add(oneTimeTaskButton);

        return panel;
    }

    // Method to load tasks from file
    private void loadTasksFromFile() {
        try (BufferedReader reader = new BufferedReader(new FileReader(TASKS_FILE_PATH))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|");
                LocalDate date = LocalDate.parse(parts[0]);
                String task = parts[1];
                taskDatabase.computeIfAbsent(date, k -> new ArrayList<>()).add(task);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadTaskStatusFromFile() {
        try (BufferedReader reader = new BufferedReader(new FileReader("taskcompleted.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length == 3) {
                    String key = parts[0] + "|" + parts[1];
                    String status = parts[2];
                    taskStatusDatabase.put(key, status);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Load task progress from file
    private void loadTaskProgressFromFile() {
        try (BufferedReader reader = new BufferedReader(new FileReader(TASK_PROGRESS_FILE_PATH))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|");
                String key = parts[0] + "|" + parts[1];
                float value = Float.parseFloat(parts[2]);
                taskProgressDatabase.put(key, value);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private int[] getTaskProgress(String date, String taskName) {
        String filePath = "tasks.txt"; // Adjust this if the file path is different
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length == 3) {
                    String fileDate = parts[0];
                    String fileTaskName = parts[1];
                    int totalSteps = Integer.parseInt(parts[2]);
                    if (fileDate.equals(date) && fileTaskName.equals(taskName)) {
                        return new int[]{0, totalSteps}; // Progress is initially 0, and the total steps
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new int[]{0, 1}; // Default to 0 progress and 1 step if not found
    }

    private String getTaskStatus(String date, String taskName) {
        return taskStatusDatabase.get(date + "|" + taskName);
    }
    

    // Method to save tasks to file
    private void saveTasksToFile() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(TASKS_FILE_PATH))) {
            for (Map.Entry<LocalDate, List<String>> entry : taskDatabase.entrySet()) {
                LocalDate date = entry.getKey();
                List<String> tasks = entry.getValue();
                for (String task : tasks) {
                    writer.write(date + "|" + task);
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    // Method to save task progress to file
    private void saveTaskProgressToFile() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(TASK_PROGRESS_FILE_PATH))) {
            for (Map.Entry<String, Float> entry : taskProgressDatabase.entrySet()) {
                writer.write(entry.getKey() + "|" + entry.getValue());
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Method to update task progress in the taskProgress.txt file
    private void updateTaskProgress(String date, String taskName, float newProgress) {
        String filePath = "taskProgress.txt"; // Adjust this if the file path is different
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            StringBuilder fileContent = new StringBuilder();
            boolean taskFound = false;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length == 3) {
                    String fileDate = parts[0];
                    String fileTaskName = parts[1];
                    float progress = Float.parseFloat(parts[2]);
                    if (fileDate.equals(date) && fileTaskName.equals(taskName)) {
                        fileContent.append(date).append("|").append(taskName).append("|").append(newProgress).append("\n");
                        taskFound = true;
                    } else {
                        fileContent.append(line).append("\n");
                    }
                }
            }
            // If task not found, add it to the file
            if (!taskFound) {
                fileContent.append(date).append("|").append(taskName).append("|").append(newProgress).append("\n");
            }

            // Write the updated content back to the file
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
                writer.write(fileContent.toString());
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    // Method to get task progress value from taskProgress.txt file
    private float getTaskProgressValue(String date, String taskName) {
        String filePath = "taskProgress.txt"; // Adjust this if the file path is different
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length == 3) {
                    String fileDate = parts[0];
                    String fileTaskName = parts[1];
                    if (fileDate.equals(date) && fileTaskName.equals(taskName)) {
                        return Float.parseFloat(parts[2]);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0.0f; // Return 0 if not found or error
    }

    private void openCalendarPanel() {
        // Create a calendar panel to select the date
        JPanel calendarPanel = new JPanel(new BorderLayout());
        
        // Create a JCalendar instance
        JCalendar calendar = new JCalendar();
        
        // Add a property change listener to the calendar to listen for date selection changes
        calendar.getDayChooser().addPropertyChangeListener("day", new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                // Get the selected date from the calendar
                LocalDate selectedDate = calendar.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                
                // Prompt the user to input the task details
                TaskInputDialog taskDialog = new TaskInputDialog(selectedDate);
                taskDialog.setVisible(true);
            }
        });
        
        // Add the calendar to the panel
        calendarPanel.add(calendar, BorderLayout.CENTER);
        
        // Show the calendar panel in the add panel
        setCurrentPanel(calendarPanel);
    }

    private class TaskInputDialog extends JDialog {
        private LocalDate selectedDate;
    
        private JTextField taskNameField;
        private JSpinner progressSpinner;
        private JButton addButton;
        private JButton cancelButton;
    
        public TaskInputDialog(LocalDate selectedDate) {
            this.selectedDate = selectedDate;
            setTitle("Add Task");
            setSize(300, 200);
            setResizable(false);
            setLocationRelativeTo(null);
            setModal(true);
    
            initComponents();
        }
    
        private void initComponents() {
            JPanel panel = new JPanel(new BorderLayout());
            panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
            JLabel nameLabel = new JLabel("Task Name:");
            taskNameField = new JTextField(15);
        
            JLabel progressLabel = new JLabel("Times to Complete:");
            SpinnerModel spinnerModel = new SpinnerNumberModel(1, 1, 100, 1); // Set minimum value to 0
            progressSpinner = new JSpinner(spinnerModel);
        
            JPanel inputPanel = new JPanel(new GridLayout(2, 2, 5, 5));
            inputPanel.add(nameLabel);
            inputPanel.add(taskNameField);
            inputPanel.add(progressLabel);
            inputPanel.add(progressSpinner);
        
            addButton = new JButton("Add");
            addButton.addActionListener(e -> {
                addTask(selectedDate, taskNameField.getText(), (int) progressSpinner.getValue());
                dispose();
            });
        
            cancelButton = new JButton("Cancel");
            cancelButton.addActionListener(e -> {
                dispose();
            });
        
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            buttonPanel.add(addButton);
            buttonPanel.add(cancelButton);
        
            panel.add(inputPanel, BorderLayout.CENTER);
            panel.add(buttonPanel, BorderLayout.SOUTH);
        
            add(panel);
        }
        
    }

    private void addTask(LocalDate date, String task, int progress) {
        // Add the task to the task database with its progress
        String taskWithProgress = task + "|" + progress;
        taskDatabase.computeIfAbsent(date, k -> new ArrayList<>()).add(taskWithProgress);
        
        // Save tasks to file
        saveTasksToFile();

        // Refresh the Today panel to reflect the new task
        refreshTodayPanel();
    }

    
    private void refreshTodayPanel() {
        todayPanel.removeAll(); // Remove all components from the Today panel
        todayPanel.add(createTodayPanel()); // Re-create the Today panel
        todayPanel.revalidate(); // Revalidate the panel to reflect changes
        todayPanel.repaint(); // Repaint the panel
    }

    private void goBack() {
        if (!panelStack.isEmpty()) {
            JPanel previousPanel = panelStack.pop();
            setCurrentPanel(previousPanel);
            if (panelStack.isEmpty()) {
                backButton.setEnabled(false); // Disable backButton if there's no previous panel
            }
        }
    }

    private void setCurrentPanel(JPanel panel) {
        // Remove currentPanel from homePanel
        homePanel.remove(currentPanel);
        // Add new panel to homePanel
        homePanel.add(panel, BorderLayout.CENTER);
        // Update currentPanel
        currentPanel = panel;
        // Refresh the display
        homePanel.revalidate();
        homePanel.repaint();
    }

    private void switchPanel(JPanel newPanel) {
        if (newPanel != currentPanel) {
            homePanel.remove(currentPanel);
            homePanel.add(newPanel, BorderLayout.CENTER);
            homePanel.revalidate();
            homePanel.repaint();
            panelStack.push(currentPanel);
            currentPanel = newPanel;
            backButton.setEnabled(true);
        }
    }

}
