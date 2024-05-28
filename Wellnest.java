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

/**
 * The Wellnest class represents the main application window for the Wellnest task management system.
 * It extends the JFrame class to provide a graphical user interface (GUI) for interacting with tasks
 * and tracking progress.
 * 
 * This class manages the overall layout and behavior of the application, including navigation between
 * different panels, task management functionality, and user interaction.
 * 
 * Users can use the Wellnest application to view, add, remove, and update tasks, track progress,
 * and manage their daily routines efficiently.
 * 
 * The Wellnest class serves as the entry point for the application and orchestrates the interaction
 * between various components such as task panels, calendar views, and progress tracking mechanisms.
 * 
 * @author 
 * @version 1.0.0
 */
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

    /**
     * Constructs a new instance of the Wellnest application.
     * Initializes the main application window with a title, size, and default close operation.
     * 
     * The constructor also initializes the task database, task status database, and task progress database.
     * It loads existing tasks, task progress, and task statuses from files.
     * 
     * Initializes the panel stack and sets the current date to the current system date.
     * Creates and configures the main panels for home, today's tasks, statistics, and all habits.
     * Sets up the sidebar panel with navigation options.
     * 
     * Adds components to the home panel, sets it as the content pane of the frame, and makes the frame visible.
     * 
     * The constructor sets up the initial state of the application and prepares it for user interaction.
     */
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


    /**
     * Creates and configures the panel for displaying tasks for the current date.
     * 
     * This method constructs a panel with a calendar view for the current week
     * and a list of tasks for the current date. If there are tasks for the current
     * date, it creates task item panels for each task and adds them to the task list.
     * If there are no tasks for the current date, it displays a message indicating
     * that there are no tasks.
     * 
     * @return The panel for displaying tasks for the current date.
     */
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
    
    /**
     * Creates a panel representing a task item.
     * 
     * This method constructs a panel that displays information about a task,
     * including its name, progress, and buttons for completing, skipping, and
     * updating progress. It also includes a button for removing the task.
     * 
     * @param date The date of the task.
     * @param taskName The name of the task.
     * @return The panel representing the task item.
     */
    private JPanel createTaskItemPanel(String date, String taskName) {
        JPanel taskPanel = new JPanel(new BorderLayout());
        taskPanel.setBackground(Color.WHITE);
        
        // Create a label for the task name
        JLabel nameLabel = new JLabel(taskName, SwingConstants.CENTER);
        taskPanel.add(nameLabel, BorderLayout.NORTH);
        
        // Create a panel for the buttons and progress bar
        JPanel buttonPanel = new JPanel(new GridLayout(1, 4)); // 1 row and 4 columns
        
        // Create buttons for the task item
        JButton completedButton = new JButton("Completed");
        JButton skippedButton = new JButton("Skipped");
        JButton button1 = new JButton("1");
        JButton removeButton = new JButton("Remove Task");
        
        completedButton.setBackground(new Color(51,122,183));
        completedButton.setForeground(Color.WHITE);
        completedButton.setFocusPainted(false);
        completedButton.setFont(new Font("Arial", Font.BOLD, 14));
        
        skippedButton.setBackground(new Color(51,122,183));
        skippedButton.setForeground(Color.WHITE);
        skippedButton.setFocusPainted(false);
        skippedButton.setFont(new Font("Arial", Font.BOLD, 14));

        button1.setBackground(new Color(51,122,183));
        button1.setForeground(Color.WHITE);
        button1.setFocusPainted(false);
        button1.setFont(new Font("Arial", Font.BOLD, 14));

        removeButton.setBackground(new Color(255,105,97));
        removeButton.setForeground(Color.WHITE);
        removeButton.setFocusPainted(false);
        removeButton.setFont(new Font("Arial", Font.BOLD, 14));
        
        
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
                }
                
                // Save task progress status only if it's not yet completed
                if (newProgressValue < 100.0f) {
                    // Update the progress in the taskProgress.txt file
                    updateTaskProgress(date, taskName, newProgressValue);
                    // Save task progress status
                    taskProgressDatabase.put(date + "|" + taskName, newProgressValue);
                    saveTaskProgressToFile();
                }
            }
        });
        
        removeButton.addActionListener(e -> {
            // Call the removeTask method with date, taskName, and taskPanel
            removeTask(LocalDate.parse(date), taskName, taskPanel);
        });

        // Add components to the button panel
        buttonPanel.add(completedButton);
        buttonPanel.add(skippedButton);
        buttonPanel.add(button1);
        buttonPanel.add(progressBar);
        taskPanel.add(removeButton, BorderLayout.SOUTH); // Add the button to the bottom of the task panel
        
        // Add the button panel to the task panel
        taskPanel.add(buttonPanel, BorderLayout.CENTER);
        
        return taskPanel;
    }
    
    /**
     * Creates the panel for displaying statistics, such as the current streak count.
     *
     * @return The JPanel containing the statistics panel components.
     */
    private JPanel createStatsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        
        // Calculate streak count
        int streakCount = calculateStreakCount();
        
        // Create streak label
        JLabel streakLabel = new JLabel("Current Streak: " + streakCount);
        streakLabel.setHorizontalAlignment(SwingConstants.CENTER);
        streakLabel.setFont(new Font("Arial", Font.BOLD, 16));
        streakLabel.setForeground(Color.PINK);
        streakLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Add padding
    
        // Create task count label
        JLabel taskCountLabel = new JLabel("Tasks Completed: " + countCompletedTasks());
        taskCountLabel.setHorizontalAlignment(SwingConstants.CENTER);
        taskCountLabel.setFont(new Font("Arial", Font.BOLD, 16));
        taskCountLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Add padding
    
        // Add streak label to the panel's NORTH position
        panel.add(streakLabel, BorderLayout.NORTH);
        
        // Add task count label to the panel's CENTER position
        panel.add(taskCountLabel, BorderLayout.CENTER);
        
        return panel;
    }

    private JPanel createAllHabitsPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(Color.WHITE);
        JLabel label = new JLabel("All Habits Panel");
        
        
        panel.add(label);
        return panel;
    }
    
    /**
     * Creates the sidebar panel containing buttons for navigating to different sections of the application.
     * The todayButton navigates to the Today section.
     * The statsButton navigates to the Stats section.
     * The allHabitsButton navigates to the All Habits section.
     * 
     * @return The JPanel containing the sidebar panel components.
     */
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
        
        
        todayButton.setBackground(new Color(0, 120, 215));
        todayButton.setForeground(Color.WHITE);
        todayButton.setFocusPainted(false);
        todayButton.setFont(new Font("Arial", Font.BOLD, 14));
        todayButton.setBorder(BorderFactory.createEmptyBorder(10, 25, 10, 25));
        
        statsButton.setBackground(new Color(0, 120, 215));
        statsButton.setForeground(Color.WHITE);
        statsButton.setFocusPainted(false);
        statsButton.setFont(new Font("Arial", Font.BOLD, 14));
        statsButton.setBorder(BorderFactory.createEmptyBorder(10, 25, 10, 25));
        
        allHabitsButton.setBackground(new Color(0, 120, 215));
        allHabitsButton.setForeground(Color.WHITE);
        allHabitsButton.setFocusPainted(false);
        allHabitsButton.setFont(new Font("Arial", Font.BOLD, 14));
        allHabitsButton.setBorder(BorderFactory.createEmptyBorder(10, 25, 10, 25));
        
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
    
    /**
     * Creates the top panel of the application interface containing buttons for toggling the sidebar, adding tasks, and navigating back.
     * The toggleSidebarButton toggles the visibility of the sidebar panel.
     * The addButton opens the add panel to add new tasks.
     * The backButton allows navigating back to the previous panel if available.
     * 
     * @return The JPanel containing the top panel components.
     */
    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        toggleSidebarButton = new JButton("☰"); // Unicode for the hamburger icon
        addButton = new JButton("+");
        backButton = new JButton("← "); // Back button with left arrow
        backButton.setEnabled(false); // Initially disabled as there's no previous panel
        
        toggleSidebarButton.setBackground(new Color(103, 146, 103));
        toggleSidebarButton.setForeground(Color.WHITE);
        toggleSidebarButton.setFocusPainted(false);
        toggleSidebarButton.setFont(new Font("Arial", Font.BOLD, 14));
        toggleSidebarButton.setBorder(BorderFactory.createEmptyBorder(10, 25, 10, 25));
        
        addButton.setBackground(new Color(103, 146, 103));
        addButton.setForeground(Color.WHITE);
        addButton.setFocusPainted(false);
        addButton.setFont(new Font("Arial", Font.BOLD, 14));
        addButton.setBorder(BorderFactory.createEmptyBorder(10, 25, 10, 25));
        
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
                openCalendarPanel();
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
        // Get the stats panel
        JPanel statsPanel = createStatsPanel();
        
        // Update the task count label
        JLabel taskCountLabel = (JLabel) statsPanel.getComponent(1); // Assuming taskCountLabel is the second component
        updateTaskCountLabel(taskCountLabel);
        
        // Set the stats panel as the current panel
        setCurrentPanel(statsPanel);
    }
    
    private void showAllHabitsPanel() {
        switchPanel(allHabitsPanel);
    }
    
    /**
     * Toggles the visibility of the sidebar panel by expanding or collapsing it with a smooth animation.
     * If the sidebar is currently expanded, it collapses it to a width of 0. If it's collapsed, it expands it to its original width.
     * The animation is achieved using a Timer object to gradually adjust the width of the sidebar panel.
     */
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

    /**
     * Saves the status of a task to a file.
     *
     * This method appends a new entry to the task completion file, consisting of the task's
     * date, name, and status, separated by "|" (pipe) characters.
     *
     * @param date The date of the task in the format of a string.
     * @param taskName The name of the task.
     * @param status The status of the task (e.g., "Completed", "Skipped").
     */
    private void saveTaskStatus(String date, String taskName, String status) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(TASK_COMPLETED_FILE_PATH, true))) {
            String entry = date + "|" + taskName + "|" + status;
            writer.write(entry);
            writer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Loads tasks from a file into the task database.
     *
     * This method reads each line from the tasks file, splits it into parts using "|" (pipe) as the delimiter,
     * parses the date from the first part, and retrieves the task name from the second part. It then adds the
     * task to the task database under the corresponding date.
     *
     * @throws IOException If an I/O error occurs while reading the file.
     */
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


    /**
     * Loads task statuses from a file into the task status database.
     *
     * This method reads each line from the task completed file, splits it into parts using "|" (pipe) as the delimiter,
     * and retrieves the date and task name from the first two parts. It then combines the date and task name to form the key,
     * and extracts the status from the third part. If the line contains all three parts, it adds the key-value pair
     * (date and task name as key, status as value) to the task status database.
     *
     * @throws IOException If an I/O error occurs while reading the file.
     */
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

    /**
     * Method to load task progress from file.
     * used in the constructor.
     */
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

    /**
     * Method to save tasks to task.txt file (db).
     * used in the addTask and removeTask method.
     * 
     * It saves the tasks to a file by iterating through the taskDatabase map
     * where the key is the date and the value is a list of tasks.
     */
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

    /**
     * Saves the task progress data to the TASK_PROGRESS_FILE_PATH file.
     * Writes each entry in the taskProgressDatabase to the file with the format: "date|taskName|progressValue".
     * If an IOException occurs during the file writing process, it prints the stack trace.
     */
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

    /**
     * Retrieves the progress data of a task for a specific date from the tasks.txt file.
     * 
     * @param date The date of the task in the format "yyyy-MM-dd".
     * @param taskName The name of the task.
     * @return An array containing the progress data: [currentProgress, totalSteps], or [0, 1] if not found.
    */
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
    
    /**
     * Retrieves the status of a task for a specific date from the taskStatusDatabase.
     * 
     * @param date The date of the task in the format "yyyy-MM-dd".
     * @param taskName The name of the task.
     * @return The status of the task, or null if not found.
     */
    private String getTaskStatus(String date, String taskName) {
        return taskStatusDatabase.get(date + "|" + taskName);
    }

    /**
     * Updates the progress of a task for a specific date in the taskProgress.txt file.
     * 
     * @param date The date of the task in the format "yyyy-MM-dd".
     * @param taskName The name of the task.
     * @param newProgress The new progress value to be updated.
     */    
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
    
    /**
     * Retrieves the progress value of a task for a specific date from the taskProgress.txt file.
     * 
     * @param date The date of the task in the format "yyyy-MM-dd".
     * @param taskName The name of the task.
     * @return The progress value of the task as a float, or 0.0 if not found or an error occurs.
     */
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
    
    // private void openAddPanel() {
    //     setCurrentPanel(createAddPanel());
    // }
    
    /**
     * Opens a calendar panel to select a date and add a new task.
     * 
     * This method creates a calendar panel using the JCalendar component,
     * allowing the user to select a date. When a date is selected, it prompts
     * the user to input the details of a new task using a TaskInputDialog.
     * The dialog is displayed, and upon confirmation, the new task is added
     * to the application.
     */
    private void openCalendarPanel() {
        // Create a calendar panel to select the date
        JPanel calendarPanel = new JPanel(new BorderLayout());
        
        // Create a label to prompt the user
        JLabel promptLabel = new JLabel("Select a date to enter the task:");
        promptLabel.setFont(new Font("Arial", Font.BOLD, 20));
        promptLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
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
        
        // Add the prompt label and the calendar to the panel
        calendarPanel.add(promptLabel, BorderLayout.NORTH);
        calendarPanel.add(calendar, BorderLayout.CENTER);
        
        // Show the calendar panel in the add panel
        setCurrentPanel(calendarPanel);
    }
    
    /**
     * Calculates the streak count of consecutive task completions.
     * 
     * This method calculates the number of consecutive days on which tasks have
     * been completed. It iterates backward from the current date, checking for
     * tasks completed on each consecutive day until it encounters a day with no
     * completed tasks.
     * 
     * @return The streak count of consecutive task completions.
     */
    private int calculateStreakCount() {
        int streakCount = 0;
        LocalDate today = LocalDate.now();
        LocalDate previousDate = today.minusDays(1);
        
        // Check for consecutive completion of tasks
        while (taskDatabase.containsKey(today) && taskDatabase.containsKey(previousDate)) {
            streakCount++;
            today = today.minusDays(1);
            previousDate = previousDate.minusDays(1);
        }
    
        return streakCount;
    }
    
    private int countCompletedTasks() {
        int completedTasks = 0;
        String filePath = "taskCompleted.txt"; // Adjust this if the file path is different
        
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // Assuming each line represents a completed task
                completedTasks++;
            }
        } catch (IOException e) {
            e.printStackTrace();
            // Handle any IOExceptions here
        }
        
        return completedTasks;
    }
    private void updateTaskCountLabel(JLabel taskCountLabel) {
        // Get the count of completed tasks and update the label text
        int completedTasks = countCompletedTasks();
        taskCountLabel.setText("Tasks Completed: " + completedTasks);
        }

    /**
     * Method to remove a task from the panel and the database
     * used in the removeButton action listener from the createTaskItemPanel method.
     * @param date of the tast
     * @param taskName
     * @param taskPanel
     */
    private void removeTask(LocalDate date, String taskName, JPanel taskPanel) {
        // Remove the task from the panel
        currentPanel.remove(taskPanel); // Remove task panel from the current panel
        currentPanel.revalidate(); // Revalidate the panel
        currentPanel.repaint(); // Repaint the panel
    
        // Remove the task from the database
        List<String> tasksForDate = taskDatabase.get(date);
        if (tasksForDate != null) {
            tasksForDate.remove(taskName); // Remove the task from the list
            if (tasksForDate.isEmpty()) {
                taskDatabase.remove(date); // Remove the date entry if there are no tasks left for that date
                if (currentPanel.getComponentCount() == 0) {
                    // If the current panel is empty, don't remove it
                    refreshTodayPanel();
                }
            }
            saveTasksToFile(); // Save the updated task database to file
    
            // Refresh the Today panel
            refreshTodayPanel();
        }
    }

    /**
     * Method to add a task to the task database and refresh the Today panel.
     * used in the addButton action listener from the TaskInputDialog class.
     * 
     * @param date
     * @param task
     * @param progress
     */
    private void addTask(LocalDate date, String task, int progress) {
        // Check if the task name is blank
        if (task.trim().isEmpty()) {
            // Task name is blank, do not proceed
            return;
        }

        // Add the task to the task database with its progress
        String taskWithProgress = task + "|" + progress;
        taskDatabase.computeIfAbsent(date, k -> new ArrayList<>()).add(taskWithProgress);
        
        // Save tasks to file
        saveTasksToFile();
        
        // Refresh the Today panel to reflect the new task
        refreshTodayPanel();
    }
    
    /**
     * Refreshes the Today panel by removing all existing components and re-creating it.
     * 
     * This method clears all components from the Today panel and then re-creates it
     * using the {@code createTodayPanel()} method. It is useful for updating the panel
     * after changes have been made to the tasks or their status.
     */
    private void refreshTodayPanel() {
        todayPanel.removeAll(); // Remove all components from the Today panel
        todayPanel.add(createTodayPanel()); // Re-create the Today panel
        todayPanel.revalidate(); // Revalidate the panel to reflect changes
        todayPanel.repaint(); // Repaint the panel
    }
      
    /**
     * Sets the current panel to the specified panel.
     * 
     * This method replaces the current panel displayed in the homePanel with the
     * specified panel. It removes the currentPanel, adds the new panel to homePanel,
     * updates the currentPanel reference, and refreshes the display to reflect the change.
     * 
     * @param panel The panel to set as the current panel.
     */
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
    
    /**
     * Switches the current panel to the specified panel.
     * 
     * This method replaces the current panel displayed in the homePanel with the
     * specified panel if it's different from the current one. It removes the currentPanel,
     * adds the new panel to homePanel, updates the currentPanel reference, enables the
     * backButton, and refreshes the display to reflect the change.
     * 
     * @param newPanel The panel to switch to.
     */
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
    
    /**
     * A dialog window for adding a new task.
     * 
     * This class provides a dialog window with input fields for specifying
     * the task name and the number of times to complete the task. It allows
     * the user to input these details and add a new task to the application.
     * 
     * @author: Earl
     */
    private class TaskInputDialog extends JDialog {
        private LocalDate selectedDate;
    
        private JTextField taskNameField;
        private JSpinner progressSpinner;
        private JButton addButton;
        private JButton cancelButton;
    
        /**
         * Constructs a new TaskInputDialog with the specified selected date.
         * 
         * @param selectedDate The selected date for adding the task.
         */
        public TaskInputDialog(LocalDate selectedDate) {
            this.selectedDate = selectedDate;
            setTitle("Add Task");
            setSize(300, 200);
            setResizable(false);
            setLocationRelativeTo(null);
            setModal(true);
    
            initComponents();
        }
    
        /**
         * Initializes the components of the dialog window.
         */
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
                if (validateInput()) {
                    addTask(selectedDate, taskNameField.getText(), (int) progressSpinner.getValue());
                    dispose();
                }
            });
    
            cancelButton = new JButton("Cancel");
            cancelButton.addActionListener(e -> {
                dispose();
            });
    
            addButton.setBackground(new Color(0, 120, 215));
            addButton.setForeground(Color.WHITE);
            addButton.setFocusPainted(false);
            addButton.setFont(new Font("Arial", Font.BOLD, 14));
            addButton.setBorder(BorderFactory.createEmptyBorder(10, 25, 10, 25));
    
            cancelButton.setBackground(new Color(0, 120, 215));
            cancelButton.setForeground(Color.WHITE);
            cancelButton.setFocusPainted(false);
            cancelButton.setFont(new Font("Arial", Font.BOLD, 14));
            cancelButton.setBorder(BorderFactory.createEmptyBorder(10, 25, 10, 25));
    
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            buttonPanel.add(addButton);
            buttonPanel.add(cancelButton);
    
            panel.add(inputPanel, BorderLayout.CENTER);
            panel.add(buttonPanel, BorderLayout.SOUTH);
    
            add(panel);
        }
    
        /**
         * TODO: show the error message dialog if the input is invalid.
         * But the error handling is working fine.
         * 
         * @return
         */
        private boolean validateInput() {
            String input = progressSpinner.getValue().toString();
            if (!input.matches("\\d+")) {
                // If the input contains non-numeric characters, show an error message dialog
                JOptionPane.showMessageDialog(this, "Please enter a valid number for 'Times to Complete'.", "Input Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
            
            int progress = Integer.parseInt(input);
            if (progress < 1) {
                // Show an error message dialog if progress is less than 1
                JOptionPane.showMessageDialog(this, "'Times to Complete' must be greater than zero.", "Input Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
            
            // If validation succeeds, return true
            return true;
        }
    }
}