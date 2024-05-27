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

    private int sidebarWidth = 200;
    private boolean isSidebarExpanded = true;

    // File path for the tasks database
    private static final String TASKS_FILE_PATH = "tasks.txt";

    // Task database
    private Map<LocalDate, List<String>> taskDatabase;

    public Wellnest() {
        setTitle("Wellnest App");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Initialize task database
        taskDatabase = new HashMap<>();

        // Load tasks from file
        loadTasksFromFile();

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
                JPanel taskItemPanel = createTaskItemPanel(task);
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
    
    private JPanel createTaskItemPanel(String taskName) {
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
        progressBar.setValue(0); // Set initial value
        progressBar.setString("0%"); // Set initial text

        // Create a label to indicate completion status
        JLabel completionLabel = new JLabel("", SwingConstants.CENTER); 
    
        // Add action listeners to the buttons
        completedButton.addActionListener(e -> {
            // Set progress bar to 100% when "Completed" button is clicked
            progressBar.setValue(100);
            progressBar.setString("100%");
            // Disable other buttons
            completedButton.setEnabled(false);
            skippedButton.setEnabled(false);
            button1.setEnabled(false);
            // Update completion label
            completionLabel.setText("Completed");
        });

        skippedButton.addActionListener(e -> {
            // Update completion label when "Skipped" button is clicked
            completionLabel.setText("Skipped");
            // Disable other buttons
            completedButton.setEnabled(false);
            skippedButton.setEnabled(false);
            button1.setEnabled(false);
        });

        button1.addActionListener(e -> {
            // Implement action when "1" button is clicked
            int currentValue = progressBar.getValue();
            if (currentValue < 100) {
                progressBar.setValue(currentValue + 10); // Increment progress by 10%
                progressBar.setString((currentValue + 10) + "%"); // Update progress text
                if (currentValue + 10 >= 100) {
                    // If progress reaches 100%, update completion label
                    completionLabel.setText("Completed");
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

        // Add the completion label below the button panel
        taskPanel.add(completionLabel, BorderLayout.SOUTH);
    
        return taskPanel;
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
                
                // Prompt the user to input the task name
                String taskName = JOptionPane.showInputDialog(null, "Enter the task name:");
                
                // Check if the user entered a task name
                if (taskName != null && !taskName.isEmpty()) {
                    // Add the task to the database
                    addTask(selectedDate, taskName);
                }
            }
        });
        
        // Add the calendar to the panel
        calendarPanel.add(calendar, BorderLayout.CENTER);
        
        // Show the calendar panel in the add panel
        setCurrentPanel(calendarPanel);
    }

    private void addTask(LocalDate date, String task) {
        // Add the task to the task database
        taskDatabase.computeIfAbsent(date, k -> new ArrayList<>()).add(task);
        
        // Save tasks to file
        saveTasksToFile();
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
