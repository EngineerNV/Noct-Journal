package framework.info.gui;

import javax.swing.*;
import javax.swing.event.*;

import framework.SchedulePlanner;
import framework.info.Block;
import framework.info.BlockType;
import framework.info.SleepAlgorithm;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * This class is the basis for user interaction with the program. Here
 * blocks are added to a visual grid so users can see the schedule
 * they create. Many other user interface features are also here.
 * 
 * @author Victor Zamarian
 * @author Lonny Raspberry
 */

public class GraphicSchedule {
	private static boolean skipTutorial = false;
	static SchedulePlanner schedule = new SchedulePlanner();
	JFrame window;
		JMenuItem about;
		JMenuItem options;
		JMenuItem reset;
		
	GridPanel gridPanel;
	GridMouseListener mListen = new GridMouseListener();
	
	JFrame startScreen;
	
	//sleepPreference screen attributes
	JFrame sleepPreferences;
		static final long EIGHT_HOURS = 28800000L;
		static final Color LIGHT_BLUE = new Color(0, 140, 255); //color of sleep blocks
	
	//tutorial screen attributes
	JFrame tutorialScreen;
		JPanel tutorialPanel;
		JButton createEventButton;
			JPanel createEventPanel;
		JButton deleteEventButton;
			JPanel deleteEventPanel;
		JButton editEventButton;
			JPanel editEventPanel;
		JButton lockEventButton;
			JPanel lockEventPanel;
		JButton sleepButtonInfoButton;
			JPanel sleepButtonPanel;
		JButton tutorialDoneButton;
		boolean allTutorialButtonsPressed = true;
		int tutorialCounter = 0;
	
	//blockEditMenu attributes
	JFrame blockEditMenu;
		//used when clicking on empty space on grid
		static final GraphicBlock DEFAULT_BLOCK = new GraphicBlock(new Block(new Date(),
				0, BlockType.EVENT, true, Color.RED, ""), -1);
		//used when creating blocks
		static final int[] calendarDays = {GregorianCalendar.MONDAY, GregorianCalendar.TUESDAY,
				GregorianCalendar.WEDNESDAY, GregorianCalendar.THURSDAY, GregorianCalendar.FRIDAY,
				GregorianCalendar.SATURDAY, GregorianCalendar.SUNDAY};
	
	//frames and panels for stats
	JFrame statsScreen;
		JFrame graphScreen;
		JPanel graphPanel;
		JButton graphButton;
		
	JFrame aboutScreen;
	
	//options screen attributes
	JFrame optionsScreen;
		JButton sleepPreferenceButton;
		JButton tutorialButton;
		JButton resetButton;
		
	//buttons on side of main window
	JButton sleepButton;
	JButton wakeButton;
	JButton statButton;
	JButton aboutButton;
	JButton optionsButton;
	
	boolean firstTime = !schedule.load(); //set to false if load is true
	
	public void run(){
		if (firstTime && !skipTutorial){
			doStartscreen();
			editSleepPreferences();
			allTutorialButtonsPressed = false;
			showTutorial();
		}
	}
	
	//draw entire grid with days, buttons and times
	public void initialize(){
		window = new JFrame("Noc-Journal");
		window.setBounds(75, 80, 1105, 720);
		window.setResizable(false);
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		JMenuBar menu = new JMenuBar();
		JMenu file = new JMenu("File");
		about = new JMenuItem("About", 'a');
		options = new JMenuItem("Options", 'o');
		reset = new JMenuItem("Reset", 'r');
		
		file.add(about);
		file.add(options);
		file.add(reset);
		menu.add(file);
		window.setJMenuBar(menu);
		
		window.addWindowListener(new WindowListener(){
			@Override
			public void windowActivated(WindowEvent e){
			}
			
			@Override
			public void windowClosed(WindowEvent e){
			}
			
			@Override
			public void windowClosing(WindowEvent e){
				//save schedule if first time
				if (firstTime)
					schedule.save();

				schedule.applyAlgorithm();
				schedule.exitProgram();
			}
			
			@Override
			public void windowDeactivated(WindowEvent e){
			}
			
			@Override
			public void windowDeiconified(WindowEvent e){
			}
			
			@Override
			public void windowIconified(WindowEvent e){
			}
			
			@Override
			public void windowOpened(WindowEvent e){
				schedule.load();
				drawSchedule();
			}
		});
		
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(null);
		
		gridPanel = new GridPanel(); //holds the grid with the blocks
		gridPanel.setLayout(null);
		gridPanel.setPreferredSize(new Dimension(930, 1211));
		
		JScrollPane scrollPane = new JScrollPane(gridPanel, 
				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setBounds(10, 10, 930, 636); //makes gridPanel scrollable
		scrollPane.setBorder(null);
		scrollPane.setWheelScrollingEnabled(true);
		
		JPanel buttonPanel = new JPanel(); //holds all the buttons
		buttonPanel.setBounds(950, 30, 135, 600);
		buttonPanel.setLayout(null);
		
		scrollPane.getViewport().addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e){
				scrollPane.repaint(); //this is required
			}
		});
		
		GridButtonListener bListen = new GridButtonListener();
		Font font = new Font(Font.SANS_SERIF, Font.PLAIN, 20);
		
		sleepButton = new JButton("Sleep");
		sleepButton.setBounds(5, 55, 125, 65);
		sleepButton.setFont(font);
		sleepButton.addActionListener(bListen);
		
		wakeButton = new JButton("Wake");
		wakeButton.setBounds(5, 160, 125, 65);
		wakeButton.setFont(font);
		wakeButton.addActionListener(bListen);
		wakeButton.setEnabled(false); //wake is disabled by default
		
		statButton = new JButton("Stats");
		statButton.setBounds(5, 265, 125, 65);
		statButton.setFont(font);
		statButton.addActionListener(bListen);
		
		aboutButton = new JButton("About");
		aboutButton.setBounds(5, 370, 125, 65);
		aboutButton.setFont(font);
		aboutButton.addActionListener(bListen);
		
		optionsButton = new JButton("Options");
		optionsButton.setBounds(5, 475, 125, 65);
		optionsButton.setFont(font);
		optionsButton.addActionListener(bListen);
		
		options.addActionListener(bListen);
		about.addActionListener(bListen);
		
		gridPanel.addMouseListener(mListen);
		
		buttonPanel.add(sleepButton);
		buttonPanel.add(wakeButton);
		buttonPanel.add(statButton);
		buttonPanel.add(aboutButton);
		buttonPanel.add(optionsButton);
		
		mainPanel.add(scrollPane);
		mainPanel.add(buttonPanel);
		
		SleepAlgorithm.init();
		
		window.getContentPane().add(mainPanel);
		window.setVisible(true);
	}
	
	public void drawSchedule(){ //draws schedule's blocks, if any
		gridPanel.removeAllBlocks();
		
		ArrayList<Block> blocks = schedule.getSchedule().getAllBlocks();
		GregorianCalendar cal = new GregorianCalendar();
		
		for (int i = 0; i < blocks.size(); i++){
			cal.setTime(blocks.get(i).getDate());
			GraphicBlock gb = new GraphicBlock(blocks.get(i), getDay(cal.get(GregorianCalendar.DAY_OF_WEEK)));
			gridPanel.addBlock(gb);
			gb.addMouseListener(mListen);
		}
		
		gridPanel.drawBlocks();
	}
	
	public int getDay(int calendarDay){ //return SleepAlgorithm day constants based on Calendar day constants
		switch(calendarDay){
			case GregorianCalendar.MONDAY: return SleepAlgorithm.MONDAY;
			case GregorianCalendar.TUESDAY: return SleepAlgorithm.TUESDAY;
			case GregorianCalendar.WEDNESDAY: return SleepAlgorithm.WEDNESDAY;
			case GregorianCalendar.THURSDAY: return SleepAlgorithm.THURSDAY;
			case GregorianCalendar.FRIDAY: return SleepAlgorithm.FRIDAY;
			case GregorianCalendar.SATURDAY: return SleepAlgorithm.SATURDAY;
			case GregorianCalendar.SUNDAY: return SleepAlgorithm.SUNDAY;
			default: return -1;
		}
	}
	
	public void drawBlock(GraphicBlock block){ //gets called by drawBlockEditMenu()
		gridPanel.addBlock(block);
	}
	
	public void removeBlock(GraphicBlock block){
		GridPanel.removeBlock(block);
	}
	
	public void doStartscreen(){ //only gets called if there is no save file
		window.setEnabled(false);
		
		startScreen = new JFrame("Welcome");
		startScreen.setBounds(425, 355, 375, 225);
		startScreen.setResizable(false);
		startScreen.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		
		StartPanel main = new StartPanel();
		main.setLayout(null);
		
		startScreen.getContentPane().add(main);
		startScreen.setVisible(true);
		
		//display start screen for 3.5 seconds, then close it
		try {
			Thread.sleep(3500);
		}catch (InterruptedException ie){
			;
		}
		
		startScreen.dispose();
	}
	
	public static SchedulePlanner getSchedule(){
		return schedule;
	}
	
	public void editSleepPreferences(){ //called on first use of program or from options
		window.toFront();
		
		sleepPreferences = new JFrame("Wake Up Times");
		sleepPreferences.setBounds(225, 190, 680, 500);
		sleepPreferences.setResizable(false);
		sleepPreferences.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		sleepPreferences.setAlwaysOnTop(true);
		
		WakeTimePanel main = new WakeTimePanel();
		main.setLayout(null);
		
		String[] hours = {"4", "5", "6", "7", "8", "9", "10", "11"}; //choice of wake time hours
		String[] minutes = {"00", "15", "30", "45"}; //choice of wake time minutes
		
		Font numberFont = new Font(Font.SANS_SERIF, Font.PLAIN, 20);
		
		//Time for Monday
		JPanel monday = new JPanel();
		monday.setBounds(30, 90, 185, 80);
		monday.setBorder(BorderFactory.createTitledBorder("  Monday  "));
		monday.setLayout(null);
		monday.setOpaque(false);
		
		JComboBox<String> mondayBoxHour = new JComboBox<String>(hours);
		mondayBoxHour.setBounds(10, 20, 75, 45);
		mondayBoxHour.setFont(numberFont);
		mondayBoxHour.setSelectedItem("6");
		monday.add(mondayBoxHour);
		
		JComboBox<String> mondayBoxMinute = new JComboBox<String>(minutes);
		mondayBoxMinute.setBounds(100, 20, 75, 45);
		mondayBoxMinute.setFont(numberFont);
		monday.add(mondayBoxMinute);
		
		//Time for Tuesday
		JPanel tuesday = new JPanel();
		tuesday.setBounds(30, 175, 185, 80);
		tuesday.setBorder(BorderFactory.createTitledBorder("  Tuesday  "));
		tuesday.setLayout(null);
		tuesday.setOpaque(false);
		
		JComboBox<String> tuesdayBoxHour = new JComboBox<String>(hours);
		tuesdayBoxHour.setBounds(10, 20, 75, 45);
		tuesdayBoxHour.setFont(numberFont);
		tuesdayBoxHour.setSelectedItem("6");
		tuesday.add(tuesdayBoxHour);
		
		JComboBox<String> tuesdayBoxMinute = new JComboBox<String>(minutes);
		tuesdayBoxMinute.setBounds(100, 20, 75, 45);
		tuesdayBoxMinute.setFont(numberFont);
		tuesday.add(tuesdayBoxMinute);
		
		//Time for Wednesday
		JPanel wednesday = new JPanel();
		wednesday.setBounds(30, 260, 185, 80);
		wednesday.setBorder(BorderFactory.createTitledBorder("  Wednesday  "));
		wednesday.setLayout(null);
		wednesday.setOpaque(false);
		
		JComboBox<String> wednesdayBoxHour = new JComboBox<String>(hours);
		wednesdayBoxHour.setBounds(10, 20, 75, 45);
		wednesdayBoxHour.setFont(numberFont);
		wednesdayBoxHour.setSelectedItem("6");
		wednesday.add(wednesdayBoxHour);
		
		JComboBox<String> wednesdayBoxMinute = new JComboBox<String>(minutes);
		wednesdayBoxMinute.setBounds(100, 20, 75, 45);
		wednesdayBoxMinute.setFont(numberFont);
		wednesday.add(wednesdayBoxMinute);
		
		//Time for Thursday
		JPanel thursday = new JPanel();
		thursday.setBounds(30, 345, 185, 80);
		thursday.setBorder(BorderFactory.createTitledBorder("  Thursday  "));
		thursday.setLayout(null);
		thursday.setOpaque(false);
		
		JComboBox<String> thursdayBoxHour = new JComboBox<String>(hours);
		thursdayBoxHour.setBounds(10, 20, 75, 45);
		thursdayBoxHour.setFont(numberFont);
		thursdayBoxHour.setSelectedItem("6");
		thursday.add(thursdayBoxHour);
		
		JComboBox<String> thursdayBoxMinute = new JComboBox<String>(minutes);
		thursdayBoxMinute.setBounds(100, 20, 75, 45);
		thursdayBoxMinute.setFont(numberFont);
		thursday.add(thursdayBoxMinute);
		
		//Time for Friday
		JPanel friday = new JPanel();
		friday.setBounds(265, 90, 185, 80);
		friday.setBorder(BorderFactory.createTitledBorder("  Friday  "));
		friday.setLayout(null);
		friday.setOpaque(false);
		
		JComboBox<String> fridayBoxHour = new JComboBox<String>(hours);
		fridayBoxHour.setBounds(10, 20, 75, 45);
		fridayBoxHour.setFont(numberFont);
		fridayBoxHour.setSelectedItem("6");
		friday.add(fridayBoxHour);
		
		JComboBox<String> fridayBoxMinute = new JComboBox<String>(minutes);
		fridayBoxMinute.setBounds(100, 20, 75, 45);
		fridayBoxMinute.setFont(numberFont);
		friday.add(fridayBoxMinute);
		
		//Time for Saturday
		JPanel saturday = new JPanel();
		saturday.setBounds(265, 175, 185, 80);
		saturday.setBorder(BorderFactory.createTitledBorder("  Saturday  "));
		saturday.setLayout(null);
		saturday.setOpaque(false);
		
		JComboBox<String> saturdayBoxHour = new JComboBox<String>(hours);
		saturdayBoxHour.setBounds(10, 20, 75, 45);
		saturdayBoxHour.setFont(numberFont);
		saturdayBoxHour.setSelectedItem("6");
		saturday.add(saturdayBoxHour);
		
		JComboBox<String> saturdayBoxMinute = new JComboBox<String>(minutes);
		saturdayBoxMinute.setBounds(100, 20, 75, 45);
		saturdayBoxMinute.setFont(numberFont);
		saturday.add(saturdayBoxMinute);
		
		//Time for Sunday
		JPanel sunday = new JPanel();
		sunday.setBounds(265, 260, 185, 80);
		sunday.setBorder(BorderFactory.createTitledBorder("  Sunday  "));
		sunday.setLayout(null);
		sunday.setOpaque(false);
		
		JComboBox<String> sundayBoxHour = new JComboBox<String>(hours);
		sundayBoxHour.setBounds(10, 20, 75, 45);
		sundayBoxHour.setFont(numberFont);
		sundayBoxHour.setSelectedItem("6");
		sunday.add(sundayBoxHour);
		
		JComboBox<String> sundayBoxMinute = new JComboBox<String>(minutes);
		sundayBoxMinute.setBounds(100, 20, 75, 45);
		sundayBoxMinute.setFont(numberFont);
		sunday.add(sundayBoxMinute);
		
		//radio buttons for allowing naps
		JRadioButton yesNap = new JRadioButton("Yes");
		yesNap.setBounds(500, 200, 75, 25);
		yesNap.setFont(numberFont);
		
		JRadioButton noNap = new JRadioButton("No");
		noNap.setBounds(500, 250, 75, 25);
		noNap.setFont(numberFont);
		
		JButton done = new JButton("Done");
		done.setBounds(350, 375, 175, 50);
		done.setFont(numberFont);
		done.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e){
				if (e.getSource() == done){
					//delete all sleep blocks first
					gridPanel.removeAllSleepBlocks();
					schedule.removeEventsOfType(BlockType.SLEEP);
					
					SleepAlgorithm.setNapPreference(yesNap.isSelected());
					
					GregorianCalendar calendar = schedule.getCalendar();
					calendar.set(GregorianCalendar.SECOND, 0);
					
					//set Monday time
					calendar.set(GregorianCalendar.HOUR_OF_DAY,
							Integer.parseInt((String) mondayBoxHour.getSelectedItem()));
					calendar.set(GregorianCalendar.MINUTE,
							Integer.parseInt((String) mondayBoxMinute.getSelectedItem()));
					calendar.set(GregorianCalendar.DAY_OF_WEEK, GregorianCalendar.MONDAY);
					
					Date mondaySleep = calendar.getTime();
					SleepAlgorithm.setTime(mondaySleep, SleepAlgorithm.MONDAY);
					mondaySleep.setTime(mondaySleep.getTime() - EIGHT_HOURS);
					
					Block mondayBlock = new Block(mondaySleep, 
							EIGHT_HOURS, BlockType.SLEEP, false, LIGHT_BLUE, "Sleep");
					addBlock(mondayBlock); //will automatically create 2 blocks
					
					//set Tuesday time
					calendar.set(GregorianCalendar.HOUR_OF_DAY,
							Integer.parseInt((String) tuesdayBoxHour.getSelectedItem()));
					calendar.set(GregorianCalendar.MINUTE,
							Integer.parseInt((String) tuesdayBoxMinute.getSelectedItem()));
					calendar.set(GregorianCalendar.DAY_OF_WEEK, GregorianCalendar.TUESDAY);
					
					Date tuesdaySleep = calendar.getTime();
					SleepAlgorithm.setTime(tuesdaySleep, SleepAlgorithm.TUESDAY);
					tuesdaySleep.setTime(tuesdaySleep.getTime() - EIGHT_HOURS);
					
					Block tuesdayBlock = new Block(tuesdaySleep, 
							EIGHT_HOURS, BlockType.SLEEP, false, LIGHT_BLUE, "Sleep");
					addBlock(tuesdayBlock);
					
					//set Wednesday time
					calendar.set(GregorianCalendar.HOUR_OF_DAY,
							Integer.parseInt((String) wednesdayBoxHour.getSelectedItem()));
					calendar.set(GregorianCalendar.MINUTE,
							Integer.parseInt((String) wednesdayBoxMinute.getSelectedItem()));
					calendar.set(GregorianCalendar.DAY_OF_WEEK, GregorianCalendar.WEDNESDAY);
					
					Date wednesdaySleep = calendar.getTime();
					SleepAlgorithm.setTime(wednesdaySleep, SleepAlgorithm.WEDNESDAY);
					wednesdaySleep.setTime(wednesdaySleep.getTime() - EIGHT_HOURS);
					
					Block wednesdayBlock = new Block(wednesdaySleep, 
							EIGHT_HOURS, BlockType.SLEEP, false, LIGHT_BLUE, "Sleep");
					addBlock(wednesdayBlock);
					
					//set Thursday time
					calendar.set(GregorianCalendar.HOUR_OF_DAY,
							Integer.parseInt((String) thursdayBoxHour.getSelectedItem()));
					calendar.set(GregorianCalendar.MINUTE,
							Integer.parseInt((String) thursdayBoxMinute.getSelectedItem()));
					calendar.set(GregorianCalendar.DAY_OF_WEEK, GregorianCalendar.THURSDAY);
					
					Date thursdaySleep = calendar.getTime();
					SleepAlgorithm.setTime(thursdaySleep, SleepAlgorithm.THURSDAY);
					thursdaySleep.setTime(thursdaySleep.getTime() - EIGHT_HOURS);
					
					Block thursdayBlock = new Block(thursdaySleep, 
							EIGHT_HOURS, BlockType.SLEEP, false, LIGHT_BLUE, "Sleep");
					addBlock(thursdayBlock);
					
					//set Friday time
					calendar.set(GregorianCalendar.HOUR_OF_DAY,
							Integer.parseInt((String) fridayBoxHour.getSelectedItem()));
					calendar.set(GregorianCalendar.MINUTE,
							Integer.parseInt((String) fridayBoxMinute.getSelectedItem()));
					calendar.set(GregorianCalendar.DAY_OF_WEEK, GregorianCalendar.FRIDAY);
					
					Date fridaySleep = calendar.getTime();
					SleepAlgorithm.setTime(fridaySleep, SleepAlgorithm.FRIDAY);
					fridaySleep.setTime(fridaySleep.getTime() - EIGHT_HOURS);
					
					Block fridayBlock = new Block(fridaySleep, 
							EIGHT_HOURS, BlockType.SLEEP, false, LIGHT_BLUE, "Sleep");
					addBlock(fridayBlock);
					
					//set Saturday time
					calendar.set(GregorianCalendar.HOUR_OF_DAY,
							Integer.parseInt((String) saturdayBoxHour.getSelectedItem()));
					calendar.set(GregorianCalendar.MINUTE,
							Integer.parseInt((String) saturdayBoxMinute.getSelectedItem()));
					calendar.set(GregorianCalendar.DAY_OF_WEEK, GregorianCalendar.SATURDAY);
					
					Date saturdaySleep = calendar.getTime();
					SleepAlgorithm.setTime(saturdaySleep, SleepAlgorithm.SATURDAY);
					saturdaySleep.setTime(saturdaySleep.getTime() - EIGHT_HOURS);
					
					Block saturdayBlock = new Block(saturdaySleep, 
							EIGHT_HOURS, BlockType.SLEEP, false, LIGHT_BLUE, "Sleep");
					addBlock(saturdayBlock);
					
					//set Sunday time
					calendar.set(GregorianCalendar.HOUR_OF_DAY,
							Integer.parseInt((String) sundayBoxHour.getSelectedItem()));
					calendar.set(GregorianCalendar.MINUTE,
							Integer.parseInt((String) sundayBoxMinute.getSelectedItem()));
					calendar.set(GregorianCalendar.DAY_OF_WEEK, GregorianCalendar.SUNDAY);
					
					Date sundaySleep = calendar.getTime();
					SleepAlgorithm.setTime(sundaySleep, SleepAlgorithm.SUNDAY);
					sundaySleep.setTime(sundaySleep.getTime() - EIGHT_HOURS);
					
					Block sundayBlock = new Block(sundaySleep, 
							EIGHT_HOURS, BlockType.SLEEP, false, LIGHT_BLUE, "Sleep");
					addBlock(sundayBlock);
					
					schedule.savePreference();
					schedule.applyAlgorithm();
					sleepPreferences.dispose();
					drawSchedule();
				}
			}
		});
				
		ButtonGroup radioGroup = new ButtonGroup(); //when one radio button is selected, the other is not
		radioGroup.add(yesNap);
		radioGroup.add(noNap);
		radioGroup.setSelected(yesNap.getModel(), true); //yes is selected by default
		
		main.add(monday);
		main.add(tuesday);
		main.add(wednesday);
		main.add(thursday);
		main.add(friday);
		main.add(saturday);
		main.add(sunday);
		main.add(done);
		main.add(yesNap);
		main.add(noNap);
		
		sleepPreferences.getContentPane().add(main);
		sleepPreferences.setVisible(true);
	}
	
	public void showTutorial(){ //called on first use or from options menu
		tutorialScreen = new JFrame("How to's:");
		tutorialScreen.setBounds(360, 285, 400, 375);
		tutorialScreen.setResizable(false);
		tutorialScreen.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		
		tutorialPanel = new JPanel(); //holds all the buttons
		tutorialPanel.setBounds(0, 0, 400, 400);
		GridLayout tutorial = new GridLayout(3, 2, 5, 5); //arrange buttons in a (3, 2) grid
		tutorialPanel.setLayout(tutorial);
		
		Font font = new Font(Font.SANS_SERIF, Font.PLAIN, 20);
		
		TutorialButtonListener tListen = new TutorialButtonListener();
		
		//Create Event Tutorial
		createEventButton = new JButton("Create an Event");
		createEventButton.setFont(font);
		createEventButton.addActionListener(tListen);
		
		createEventPanel = new JPanel();
		createEventPanel.setLayout(null);
		createEventPanel.setBounds(0, 0, 400, 400);
		createEventPanel.setFont(font);
			
		JLabel howToCreateEvent = new JLabel("To create an event simply double click on ");
		howToCreateEvent.setBounds(0, 20, 400, 50); // x, y, width, height
		howToCreateEvent.setFont(font);
		howToCreateEvent.setHorizontalAlignment(SwingConstants.CENTER);
		createEventPanel.add(howToCreateEvent);
		
		JLabel howToCreateEvent2 = new JLabel("the grid and fill out the information");
		howToCreateEvent2.setBounds(0, 40, 400, 50);
		howToCreateEvent2.setFont(font);
		howToCreateEvent2.setHorizontalAlignment(SwingConstants.CENTER);
		createEventPanel.add(howToCreateEvent2);
		
		JButton createDoneButton = new JButton("Back to Tutorial");
		createDoneButton.setBounds(50, 250, 300, 70);
		createDoneButton.setFont(font);
		createDoneButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e){
				tutorialPanel.setVisible(true);
				createEventPanel.setVisible(false);
				editEventPanel.setVisible(false);
				deleteEventPanel.setVisible(false);
				lockEventPanel.setVisible(false);
				sleepButtonPanel.setVisible(false);
				tutorialCounter++;
			}
		});
		createEventPanel.add(createDoneButton);
		
		//Create Event Tutorial
		deleteEventButton = new JButton("Delete an Event");
		deleteEventButton.setFont(font);
		deleteEventButton.addActionListener(tListen);
		
		deleteEventPanel = new JPanel();
		deleteEventPanel.setLayout(null);
		deleteEventPanel.setBounds(0, 0, 400, 400);
		deleteEventPanel.setFont(font);
			
		JLabel howToDeleteEvent = new JLabel("To delete an event simply click ");
		howToDeleteEvent.setBounds(0, 20, 400, 50);
		howToDeleteEvent.setFont(font);
		howToDeleteEvent.setHorizontalAlignment(SwingConstants.CENTER);
		deleteEventPanel.add(howToDeleteEvent);
		
		JLabel howToDeleteEvent2 = new JLabel("the x located on the event you wish to delete.");
		howToDeleteEvent2.setBounds(0, 40, 400, 50);
		howToDeleteEvent2.setFont(font);
		howToDeleteEvent2.setHorizontalAlignment(SwingConstants.CENTER);
		deleteEventPanel.add(howToDeleteEvent2);
		
		JButton deleteDoneButton = new JButton("Back to Tutorial");
		deleteDoneButton.setBounds(50, 250, 300, 70);
		deleteDoneButton.setFont(font);
		deleteDoneButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e){
				tutorialPanel.setVisible(true);
				createEventPanel.setVisible(false);
				editEventPanel.setVisible(false);
				deleteEventPanel.setVisible(false);
				lockEventPanel.setVisible(false);
				sleepButtonPanel.setVisible(false);
				tutorialCounter++;
			}
		});
		deleteEventPanel.add(deleteDoneButton);
	
		//Edit Event Tutorial
		editEventButton = new JButton("Edit an Event");
		editEventButton.setFont(font);
		editEventButton.addActionListener(tListen);
		
		editEventPanel = new JPanel();
		editEventPanel.setLayout(null);
		editEventPanel.setBounds(0, 0, 400, 400);
		editEventPanel.setFont(font);
			
		JLabel howToEditEvent = new JLabel("To edit an event simply double click ");
		howToEditEvent.setBounds(0, 20, 400, 50);
		howToEditEvent.setFont(font);
		howToEditEvent.setHorizontalAlignment(SwingConstants.CENTER);
		editEventPanel.add(howToEditEvent);
		
		JLabel howToEditEvent2 = new JLabel("on the event you wish to edit.");
		howToEditEvent2.setBounds(0, 40, 400, 50);
		howToEditEvent2.setFont(font);
		howToEditEvent2.setHorizontalAlignment(SwingConstants.CENTER);
		editEventPanel.add(howToEditEvent2);
		
		JButton editDoneButton = new JButton("Back to Tutorial");
		editDoneButton.setBounds(50, 250, 300, 70);
		editDoneButton.setFont(font);
		editDoneButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e){
				tutorialPanel.setVisible(true);
				createEventPanel.setVisible(false);
				editEventPanel.setVisible(false);
				deleteEventPanel.setVisible(false);
				lockEventPanel.setVisible(false);
				sleepButtonPanel.setVisible(false);
				tutorialCounter++;
			}
		});
		editEventPanel.add(editDoneButton);
	
		//Lock Event tutorial
		lockEventButton = new JButton("Lock an Event");
		lockEventButton.setFont(font);
		lockEventButton.addActionListener(tListen);
		
		lockEventPanel = new JPanel();
		lockEventPanel.setLayout(null);
		lockEventPanel.setBounds(0, 0, 400, 400);
		lockEventPanel.setFont(font);
		
		JLabel howToLockEvent1 = new JLabel("The lock button allows you to ");
		howToLockEvent1.setBounds(0, 20, 400, 50);
		howToLockEvent1.setFont(font);
		howToLockEvent1.setHorizontalAlignment(SwingConstants.CENTER);
		lockEventPanel.add(howToLockEvent1);
		
		JLabel howToLockEvent2 = new JLabel("carry events into the next week.");
		howToLockEvent2.setBounds(0, 40, 400, 50);
		howToLockEvent2.setFont(font);
		howToLockEvent2.setHorizontalAlignment(SwingConstants.CENTER);
		lockEventPanel.add(howToLockEvent2);
		
		JLabel howToLockEvent3 = new JLabel("To lock on event click the 'L' button ");
		howToLockEvent3.setBounds(0, 60, 400, 50);
		howToLockEvent3.setFont(font);
		howToLockEvent3.setHorizontalAlignment(SwingConstants.CENTER);
		lockEventPanel.add(howToLockEvent3);
		
		JLabel howToLockEvent4 = new JLabel("in the edit block menu. ");
		howToLockEvent4.setBounds(0, 80, 400, 50);
		howToLockEvent4.setFont(font);
		howToLockEvent4.setHorizontalAlignment(SwingConstants.CENTER);
		lockEventPanel.add(howToLockEvent4);
		
		JButton lockDoneButton = new JButton("Back to Tutorial");
		lockDoneButton.setBounds(50, 250, 300, 70);
		lockDoneButton.setFont(font);
		lockDoneButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e){
				tutorialPanel.setVisible(true);
				createEventPanel.setVisible(false);
				editEventPanel.setVisible(false);
				deleteEventPanel.setVisible(false);
				lockEventPanel.setVisible(false);
				sleepButtonPanel.setVisible(false);
				tutorialCounter++;
			}
		});
		lockEventPanel.add(lockDoneButton);
		
		//Sleep Button Info Tutorial
		sleepButtonInfoButton = new JButton("The Sleep Button");
		sleepButtonInfoButton.setFont(font);
		sleepButtonInfoButton.addActionListener(tListen);
		
		sleepButtonPanel = new JPanel();
		sleepButtonPanel.setLayout(null);
		sleepButtonPanel.setBounds(0, 0, 400, 400);
		sleepButtonPanel.setFont(font);
		
		JLabel sleepButton1 = new JLabel("The sleep button allows us to ");
		sleepButton1.setBounds(0, 20, 400, 50);
		sleepButton1.setFont(font);
		sleepButton1.setHorizontalAlignment(SwingConstants.CENTER);
		sleepButtonPanel.add(sleepButton1);
		
		JLabel sleepButton2 = new JLabel("track your sleeping statistic.");
		sleepButton2.setBounds(0, 40, 400, 50);
		sleepButton2.setFont(font);
		sleepButton2.setHorizontalAlignment(SwingConstants.CENTER);
		sleepButtonPanel.add(sleepButton2);
		
		JLabel sleepButton3 = new JLabel("Click the sleep button before you go to sleep.");
		sleepButton3.setBounds(0, 70, 400, 50);
		sleepButton3.setFont(font);
		sleepButton3.setHorizontalAlignment(SwingConstants.CENTER);
		sleepButtonPanel.add(sleepButton3);
		
		JLabel sleepButton4 = new JLabel("Click the wake button when you wake up.");
		sleepButton4.setBounds(0, 90, 400, 50);
		sleepButton4.setFont(font);
		sleepButton4.setHorizontalAlignment(SwingConstants.CENTER);
		sleepButtonPanel.add(sleepButton4);
		
		JLabel sleepButton5 = new JLabel("We'll handle to the rest.");
		sleepButton5.setBounds(0, 110, 400, 50);
		sleepButton5.setFont(font);
		sleepButton5.setHorizontalAlignment(SwingConstants.CENTER);
		sleepButtonPanel.add(sleepButton5);
		
		JButton sleepDoneButton = new JButton("Back to Tutorial");
		sleepDoneButton.setBounds(50, 250, 300, 70);
		sleepDoneButton.setFont(font);
		sleepDoneButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e){
				tutorialPanel.setVisible(true);
				createEventPanel.setVisible(false);
				editEventPanel.setVisible(false);
				deleteEventPanel.setVisible(false);
				lockEventPanel.setVisible(false);
				sleepButtonPanel.setVisible(false);
				tutorialCounter++;
			}
		});
		sleepButtonPanel.add(sleepDoneButton);
		
		tutorialDoneButton = new JButton ("Done");
		tutorialDoneButton.setFont(font);
		tutorialDoneButton.addActionListener(tListen);
		
		//adding buttons
		tutorialPanel.add(createEventButton);
		tutorialPanel.add(deleteEventButton);
		tutorialPanel.add(editEventButton);
		tutorialPanel.add(lockEventButton);
		tutorialPanel.add(sleepButtonInfoButton);
		tutorialPanel.add(tutorialDoneButton);
		
		tutorialScreen.getContentPane().add(tutorialPanel);
		tutorialScreen.setVisible(true);
		tutorialScreen.getContentPane().add(createEventPanel);
		createEventPanel.setVisible(false);
		tutorialScreen.getContentPane().add(deleteEventPanel);
		deleteEventPanel.setVisible(false);
		tutorialScreen.getContentPane().add(editEventPanel);
		editEventPanel.setVisible(false);
		tutorialScreen.getContentPane().add(lockEventPanel);
		lockEventPanel.setVisible(false);
		tutorialScreen.getContentPane().add(sleepButtonPanel);
		sleepButtonPanel.setVisible(false);
	}
	
	public void drawBlockEditMenu(GraphicBlock block){ //called when double clicking grid or existing block
		blockEditMenu = new JFrame("Edit Block");
		blockEditMenu.setBounds(225, 200, 700, 475);
		blockEditMenu.setResizable(false);
		blockEditMenu.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		blockEditMenu.setAlwaysOnTop(true);
		
		JPanel main = new JPanel();
		main.setLayout(null);
		main.setBackground(new Color(245, 245, 245));
		
		Font font = new Font(Font.SANS_SERIF, Font.PLAIN, 18);
		String[] hours = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12"};
		String[] minutes = {"00", "15", "30", "45"};
		String[] ampm = {"AM", "PM"};
		
		JLabel edit = new JLabel("Edit Event");
		edit.setBounds(0, 3, blockEditMenu.getWidth(), 25);
		edit.setHorizontalAlignment(SwingConstants.CENTER);
		edit.setFont(font);
		
		//Edit name of the block
		JLabel blockName = new JLabel("Block Name: ");
		blockName.setBounds(20, 55, 110, 25);
		blockName.setFont(font);
		
		JTextField editBlockName = new JTextField(block.getName());
		editBlockName.setBounds(135, 55, 175, 28);
		editBlockName.setFont(font);
		
		//Edit the start time of the block
		JLabel startTime = new JLabel("Start Time: ");
		startTime.setBounds(32, 110, 100, 25);
		startTime.setFont(font);
		
		JComboBox<String> startTimeHour = new JComboBox<String>(hours);
		startTimeHour.setBounds(135, 107, 60, 30);
		startTimeHour.setFont(font);
		
		//put in the current start time of the existing block, if any
		if (block != DEFAULT_BLOCK){
			if (block.getHour() != 0)
				startTimeHour.setSelectedItem(block.getHour().toString());
			else
				startTimeHour.setSelectedItem("12");
		}else
			startTimeHour.setSelectedIndex(11);
		
		JLabel colon1 = new JLabel(":");
		colon1.setBounds(200, 110, 10, 25);
		colon1.setFont(font);
		
		JComboBox<String> startTimeMinute = new JComboBox<String>(minutes);
		startTimeMinute.setBounds(210, 107, 60, 30);
		startTimeMinute.setFont(font);
		
		if (block != DEFAULT_BLOCK)
			startTimeMinute.setSelectedItem(block.getMinute().toString());
		else
			startTimeMinute.setSelectedIndex(0);
		
		JComboBox<String> startTimeAMPM = new JComboBox<String>(ampm);
		startTimeAMPM.setBounds(272, 107, 55, 30);
		startTimeAMPM.setFont(font);
		startTimeAMPM.setSelectedItem(block.getAMPM());
		
		//Edit the end time of block
		JLabel endTime = new JLabel("End Time: ");
		endTime.setBounds(37, 165, 100, 25);
		endTime.setFont(font);
		
		JComboBox<String> endTimeHour = new JComboBox<String>(hours);
		endTimeHour.setBounds(135, 162, 60, 30);
		endTimeHour.setFont(font);
		
		//put in the current end time of the existing block, if any
		if (block != DEFAULT_BLOCK){
			if (block.getEndHour() != 0)
				endTimeHour.setSelectedItem(block.getEndHour().toString());
			else
				endTimeHour.setSelectedItem("12");
		}else
			endTimeHour.setSelectedIndex(11);
		
		JLabel colon2 = new JLabel(":");
		colon2.setBounds(200, 165, 10, 25);
		colon2.setFont(font);
		
		JComboBox<String> endTimeMinute = new JComboBox<String>(minutes);
		endTimeMinute.setBounds(210, 162, 60, 30);
		endTimeMinute.setFont(font);
		
		if (block != DEFAULT_BLOCK)
			endTimeMinute.setSelectedItem(block.getEndMinute().toString());
		else
			endTimeMinute.setSelectedIndex(0);
		
		JComboBox<String> endTimeAMPM = new JComboBox<String>(ampm);
		endTimeAMPM.setBounds(272, 162, 55, 30);
		endTimeAMPM.setFont(font);
		endTimeAMPM.setSelectedItem(block.getEndAMPM());
		
		//set the color of the block
		JLabel color = new JLabel("Color");
		color.setBounds(50, 230, 50, 25);
		color.setFont(font);
		
		JButton colorChooser = new JButton();
		colorChooser.setBounds(130, 225, 100, 35);
		colorChooser.setBackground(block.getBackgroundColor());
		colorChooser.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e){
				blockEditMenu.setAlwaysOnTop(false);
				colorChooser.setBackground(JColorChooser.showDialog(null, 
						"Custom Color Chooser", colorChooser.getBackground()));
				blockEditMenu.setAlwaysOnTop(true);
			}
		});
		
		//set the type of block
		JLabel type = new JLabel("Type");
		type.setBounds(40, 315, 50, 25);
		type.setFont(font);
		
		JToggleButton classType = new JToggleButton("Class");
		classType.setBounds(100, 310, 100, 35);
		classType.setFont(font);
		classType.setSelected(block.getBlock().getType() == BlockType.CLASS);
		
		JToggleButton workType = new JToggleButton("Work");
		workType.setBounds(215, 310, 100, 35);
		workType.setFont(font);
		workType.setSelected(block.getBlock().getType() == BlockType.WORK);
		
		JToggleButton eventType = new JToggleButton("Event");
		eventType.setBounds(330, 310, 100, 35);
		eventType.setFont(font);
		eventType.setSelected(block.getBlock().getType() == BlockType.EVENT);
		
		JToggleButton sleepType = new JToggleButton("Sleep");
		sleepType.setBounds(445, 310, 100, 35);
		sleepType.setFont(font);
		sleepType.setSelected(block.getBlock().getType() == BlockType.SLEEP);
		
		JToggleButton napType = new JToggleButton("Nap");
		napType.setBounds(560, 310, 100, 35);
		napType.setFont(font);
		napType.setSelected(block.getBlock().getType() == BlockType.NAP);
		
		//only select one block type
		ButtonGroup typeGroup = new ButtonGroup();
		typeGroup.add(classType);
		typeGroup.add(workType);
		typeGroup.add(eventType);
		typeGroup.add(sleepType);
		typeGroup.add(napType);
		
		//set days for block, can select multiple
		JLabel days = new JLabel("Days");
		days.setBounds(490, 55, 75, 25);
		days.setFont(font);
		
		JToggleButton monday = new JToggleButton("M");
		monday.setBounds(375, 90, 60, 25);
		monday.setFont(font);
		
		JToggleButton tuesday = new JToggleButton("T");
		tuesday.setBounds(445, 90, 60, 25);
		tuesday.setFont(font);
		
		JToggleButton wednesday = new JToggleButton("W");
		wednesday.setBounds(515, 90, 60, 25);
		wednesday.setFont(font);
		
		JToggleButton thursday = new JToggleButton("Th");
		thursday.setBounds(585, 90, 60, 25);
		thursday.setFont(font);
		
		JToggleButton friday = new JToggleButton("F");
		friday.setBounds(410, 125, 60, 25);
		friday.setFont(font);
		
		JToggleButton saturday = new JToggleButton("Sa");
		saturday.setBounds(480, 125, 60, 25);
		saturday.setFont(font);
		
		JToggleButton sunday = new JToggleButton("Su");
		sunday.setBounds(550, 125, 60, 25);
		sunday.setFont(font);
		
		//set day of existing block, if any
		if (block.getEventDay() == SleepAlgorithm.MONDAY)
			monday.setSelected(true);
		else if (block.getEventDay() == SleepAlgorithm.TUESDAY)
			tuesday.setSelected(true);
		else if (block.getEventDay() == SleepAlgorithm.WEDNESDAY)
			wednesday.setSelected(true);
		else if (block.getEventDay() == SleepAlgorithm.THURSDAY)
			thursday.setSelected(true);
		else if (block.getEventDay() == SleepAlgorithm.FRIDAY)
			friday.setSelected(true);
		else if (block.getEventDay() == SleepAlgorithm.SATURDAY)
			saturday.setSelected(true);
		else if (block.getEventDay() == SleepAlgorithm.SUNDAY)
			sunday.setSelected(true);
		
		//lock or unlock block
		JLabel lock = new JLabel("Lock/Unlock");
		lock.setBounds(465, 165, 100, 25);
		lock.setFont(font);
		
		JToggleButton lockButton = new JToggleButton("L");
		lockButton.setBounds(420, 195, 85, 85);
		lockButton.setFont(font);
		lockButton.setSelected(block.getBlock().isReocurring());
		
		JToggleButton unlockButton = new JToggleButton("U");
		unlockButton.setBounds(520, 195, 85, 85);
		unlockButton.setFont(font);
		unlockButton.setSelected(!(block.getBlock().isReocurring()));
		
		ButtonGroup lockGroup = new ButtonGroup();
		lockGroup.add(lockButton);
		lockGroup.add(unlockButton);
		
		//on done click, create new graphical block and add block to schedule
		JButton done = new JButton("Done");
		done.setBounds(275, 370, 150, 50);
		done.setFont(font);
		done.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e){
				if (block != DEFAULT_BLOCK){ //remove existing block to create new one in its place
					removeBlock(block);
					schedule.removeEvent(block.getBlock().getDate());
					
					gridPanel.drawBlocks();
					gridPanel.update(gridPanel.getGraphics());
				}
				
				GregorianCalendar calendar = schedule.getCalendar();
				calendar.set(GregorianCalendar.SECOND, 0);
				
				String startAMPM = (String) startTimeAMPM.getSelectedItem();
				String endAMPM = (String) endTimeAMPM.getSelectedItem();
				
				if (startAMPM.equals("PM")){
					calendar.set(GregorianCalendar.HOUR_OF_DAY,
							(Integer.parseInt((String) startTimeHour.getSelectedItem())%12)+12);
					calendar.set(GregorianCalendar.MINUTE,
							Integer.parseInt((String) startTimeMinute.getSelectedItem()));
				}else{
					calendar.set(GregorianCalendar.HOUR_OF_DAY,
							Integer.parseInt((String) startTimeHour.getSelectedItem())%12);
					calendar.set(GregorianCalendar.MINUTE,
							Integer.parseInt((String) startTimeMinute.getSelectedItem()));
				}
				
				//set the start time of the block
				Date startTime = calendar.getTime();
				
				if (endAMPM.equals("PM")){
					calendar.set(GregorianCalendar.HOUR_OF_DAY,
							(Integer.parseInt((String) endTimeHour.getSelectedItem())%12)+12);
					calendar.set(GregorianCalendar.MINUTE,
							Integer.parseInt((String) endTimeMinute.getSelectedItem()));
				}else{
					calendar.set(GregorianCalendar.HOUR_OF_DAY,
							Integer.parseInt((String) endTimeHour.getSelectedItem())%12);
					calendar.set(GregorianCalendar.MINUTE,
							Integer.parseInt((String) endTimeMinute.getSelectedItem()));
				}
				
				//set the end time of the block
				Date endTime = calendar.getTime();
				
				BlockType type = null; //should never actually stay null
				if (classType.isSelected())
					type = BlockType.CLASS;
				else if (workType.isSelected())
					type = BlockType.WORK;
				else if (eventType.isSelected())
					type = BlockType.EVENT;
				else if (sleepType.isSelected())
					type = BlockType.SLEEP;
				else if (napType.isSelected())
					type = BlockType.NAP;
				
				boolean[] days = {monday.isSelected(), tuesday.isSelected(), wednesday.isSelected(),
						thursday.isSelected(), friday.isSelected(), saturday.isSelected(),
						sunday.isSelected()};

				if (endTime.getTime() - startTime.getTime() > 0){ //don't add blocks with length <= 0
					for (int i = 0; i < days.length; i++){
						if (days[i] == true){ //add a block for this day
							calendar.setTime(startTime);
							calendar.set(GregorianCalendar.DAY_OF_WEEK, calendarDays[i]);
							startTime = calendar.getTime();
							
							calendar.setTime(endTime);
							calendar.set(GregorianCalendar.DAY_OF_WEEK, calendarDays[i]);
							endTime = calendar.getTime();
							
							Block b = new Block(startTime, (endTime.getTime()-startTime.getTime()),
									type, lockButton.isSelected(), colorChooser.getBackground(), 
									editBlockName.getText());
							
							if (addBlock(b)){ //create graphic block if block was added successfully
								GraphicBlock gb = new GraphicBlock(b, i);
								
								drawBlock(gb);
								gb.addMouseListener(mListen);
							}else{
								blockEditMenu.setAlwaysOnTop(false);
								Toolkit.getDefaultToolkit().beep();
								JOptionPane.showMessageDialog(null, "You cannot add overlapping blocks", "ERROR", JOptionPane.ERROR_MESSAGE);
								blockEditMenu.setAlwaysOnTop(true);
							}
						}
					}
				}
				
				schedule.saveBlocks();
				schedule.applyAlgorithm();
				drawSchedule();
				blockEditMenu.dispose();
			}
		});
		
		main.add(edit);
		
		main.add(blockName);
		main.add(editBlockName);
		
		main.add(startTime);
		main.add(startTimeHour);
		main.add(colon1);
		main.add(startTimeMinute);
		main.add(startTimeAMPM);
		
		main.add(endTime);
		main.add(endTimeHour);
		main.add(colon2);
		main.add(endTimeMinute);
		main.add(endTimeAMPM);
		
		main.add(color);
		main.add(colorChooser);
		
		main.add(type);
		main.add(classType);
		main.add(workType);
		main.add(eventType);
		main.add(sleepType);
		main.add(napType);
		
		main.add(days);
		main.add(monday);
		main.add(tuesday);
		main.add(wednesday);
		main.add(thursday);
		main.add(friday);
		main.add(saturday);
		main.add(sunday);
		
		main.add(lock);
		main.add(lockButton);
		main.add(unlockButton);
		
		main.add(done);
		
		blockEditMenu.getContentPane().add(main);
		blockEditMenu.setVisible(true);
	}
	
	public boolean addBlock(Block block){ //adds block to Schedule
		return schedule.addEvent(block);
	}
	
	public void swapSleepButton(){
		sleepButton.setEnabled(!sleepButton.isEnabled());
		wakeButton.setEnabled(!wakeButton.isEnabled());
		
		schedule.save(); //for now just save here
		
		schedule.applyAlgorithm();
	}
	
	public void drawStats(){ //display some statistics from SleepAlgorithm
		statsScreen = new JFrame("Stats");
		statsScreen.setBounds(325, 255, 420, 325);
		statsScreen.setResizable(false);
		statsScreen.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		
		JPanel statsPanel = new JPanel();
		statsPanel.setBounds(0, 0, 400, 400);
		statsPanel.setLayout(null);
		
		Font font = new Font(Font.SANS_SERIF, Font.PLAIN, 20);
		
		//passed in Andre+Nicks SleepAlgorithm that keeps track of stats variables
		long slept = SleepAlgorithm.getStat(SleepAlgorithm.HOURS_SLEPT)/3600000L;
		long needed = SleepAlgorithm.getStat(SleepAlgorithm.SLEEP_NEEDED)/3600000L;
		
		JLabel numHoursSlept = new JLabel("Number of hours slept this week: " + slept);
		numHoursSlept.setBounds(30, 20, 365, 50);
		numHoursSlept.setFont(font);
		
		JLabel numHoursNeeded = new JLabel("Number of hours needed this week: " + needed);
		numHoursNeeded.setBounds(30, 60, 380, 50);
		numHoursNeeded.setFont(font);
		
//		JLabel numHoursMissed = new JLabel("Number of hours missed this week: " + missed);
//		numHoursMissed.setBounds(30, 100, 375, 50);
//		numHoursMissed.setFont(font);
		
		graphButton = new JButton("Graph");
		graphButton.setBounds(95, 190, 200, 65);
		graphButton.setFont(font);
		graphButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e){
				drawGraph();
			}
		});
		
		statsPanel.add(numHoursSlept);
		statsPanel.add(numHoursNeeded);
//		statsPanel.add(numHoursMissed);
		statsPanel.add(graphButton);
		statsScreen.getContentPane().add(statsPanel);
		statsScreen.setVisible(true);
	}
	
	//function that was supposed to draw the stats graph
	public void drawGraph(){
		graphScreen = new JFrame("Graph");
		graphScreen.setBounds(350, 255, 700, 500);
		graphScreen.setResizable(false);
		graphScreen.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		
		GraphPanel graphPanel = new GraphPanel(); //holds graph
		graphPanel.setBounds(0, 0, 700, 500);
		graphPanel.setLayout(null);
		
		//drawing graph
		Font font1 = new Font(Font.SANS_SERIF, Font.PLAIN, 30);
		graphPanel.setFont(font1);
		
//		graphPanel.drawString("Monday", 48, 40);
//		
//		graphPanel.drawString("Tuesday", 170, 40);
//		
//		graphPanel.drawString("Wednesday", 293, 40);
//		
//		graphPanel.drawString("Thursday", 420, 40);
//		
//		graphPanel.drawString("Friday", 554, 40);
//		
//		graphPanel.drawString("Saturday", 672, 40);
//		
//		graphPanel.drawString("Sunday", 800, 40);
	
		graphScreen.getContentPane().add(graphPanel);
		graphScreen.setVisible(true);
		
	}
	
	public void showAboutScreen(){ //called when About button is pressed
		aboutScreen = new JFrame("About");
		aboutScreen.setBounds(325, 255, 500, 375);
		aboutScreen.setResizable(false);
		aboutScreen.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		
		AboutPanel about = new AboutPanel();
		about.setBounds(0, 0, 500, 375);
		about.setLayout(null);
		
		JButton ok = new JButton("OK");
		ok.setBounds(195, 270, 100, 50);
		ok.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e){
				aboutScreen.dispose(); //closes the screen
			}
		});
		
		about.add(ok);
		
		aboutScreen.getContentPane().add(about);
		aboutScreen.setVisible(true);
	}

	public void drawOptions(){ //called when Options button is pressed
		optionsScreen = new JFrame("Options");
		optionsScreen.setBounds(350, 255, 400, 375);
		optionsScreen.setResizable(false);
		optionsScreen.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		
		JPanel optionsPanel = new JPanel(); //holds all the buttons
		optionsPanel.setBounds(0, 0, 400, 400);
		optionsPanel.setLayout(null);
		
		Font font = new Font(Font.SANS_SERIF, Font.PLAIN, 20);
		OptionButtonListener oListen = new OptionButtonListener();
		
		sleepPreferenceButton = new JButton("Edit Sleep Preferences");
		sleepPreferenceButton.setBounds(65, 30, 265, 65);
		sleepPreferenceButton.setFont(font);
		sleepPreferenceButton.addActionListener(oListen);
		
		tutorialButton = new JButton("View tutorial");
		tutorialButton.setBounds(80, 135, 220, 65);
		tutorialButton.setFont(font);
		tutorialButton.addActionListener(oListen);
		
		resetButton = new JButton("Reset");
		resetButton.setBounds(125, 240, 125, 65);
		resetButton.setFont(font);
		resetButton.addActionListener(oListen);
		
		reset.addActionListener(oListen);
		
		//adding buttons
		optionsPanel.add(sleepPreferenceButton);
		optionsPanel.add(tutorialButton);
		optionsPanel.add(resetButton);
		
		optionsScreen.getContentPane().add(optionsPanel);
		optionsScreen.setVisible(true);
	}
	
	public void resetSchedule(){ //called when Reset button is pressed
		int option = JOptionPane.showConfirmDialog(null,
				"Are you sure you want to erase the calendar?", "Reset Schedule",
				JOptionPane.YES_NO_OPTION);
		if (option == JOptionPane.YES_OPTION){
			schedule.clear();
			gridPanel.removeAllBlocks();
			schedule.saveBlocks();
			optionsScreen.dispose();
		}
	}
	
	//add a MouseListener for GridPanel
	private class GridMouseListener implements MouseListener {
		@Override
		public void mouseClicked(MouseEvent e){
			if (e.getClickCount() >= 2){ //2 or more mouse clicks
				if (e.getSource() instanceof GraphicBlock)
					drawBlockEditMenu((GraphicBlock) e.getSource());
				else
					drawBlockEditMenu(DEFAULT_BLOCK);
			}
		}
		
		//others methods will not be used
		@Override
		public void mouseEntered(MouseEvent e){
		}

		@Override
		public void mouseExited(MouseEvent e){
		}

		@Override
		public void mousePressed(MouseEvent e){
		}

		@Override
		public void mouseReleased(MouseEvent e){
		}
	}
	
	//listen to button presses on grid's buttons
	private class GridButtonListener implements ActionListener {
		@SuppressWarnings("deprecation")
		@Override
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == sleepButton){ //sleep button is pressed
				swapSleepButton();
				schedule.goToSleep();
				SleepAlgorithm.setProgramExitTimestamp(new Date());
			}else if (e.getSource() == wakeButton){ //wake button is pressed
				swapSleepButton();
				schedule.wakeUp();
			}else if (e.getSource() == optionsButton || e.getSource() == options){ //options button is pressed
				if (optionsScreen == null) //only have one instance of the screen
					drawOptions();
				else
					optionsScreen.toFront();
				optionsScreen.show();
			}else if (e.getSource() == statButton){ //stats button is pressed
				if (statsScreen == null)
					drawStats();
				else
					statsScreen.toFront();
				statsScreen.show();
			}else if (e.getSource() == aboutButton || e.getSource() == about){ //about button is pressed
				if (aboutScreen == null)
					showAboutScreen();
				else
					aboutScreen.toFront();
				aboutScreen.show();
			}
		}
	}
	
	//listen to button presses in options screen
	private class OptionButtonListener implements ActionListener {
		@SuppressWarnings("deprecation")
		@Override
		public void actionPerformed(ActionEvent e){
			if (e.getSource() == sleepPreferenceButton){
				if (sleepPreferences == null)
					editSleepPreferences();
				else
					sleepPreferences.toFront();
				sleepPreferences.show();
			}else if (e.getSource() == tutorialButton){
				if (tutorialScreen == null)
					showTutorial();
				else
					tutorialScreen.toFront();
				tutorialScreen.show();
			}else if (e.getSource() == resetButton || e.getSource() == reset){
				resetSchedule();
			}
		}
	}
	
	//listen to button presses in tutorial screen
	private class TutorialButtonListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e){
			if (e.getSource() == createEventButton){
				tutorialPanel.setVisible(false);
				deleteEventPanel.setVisible(false);
				editEventPanel.setVisible(false);
				lockEventPanel.setVisible(false);
				sleepButtonPanel.setVisible(false);
				createEventPanel.setVisible(true);
			}else if (e.getSource() == deleteEventButton){
				tutorialPanel.setVisible(false);
				deleteEventPanel.setVisible(true);
				editEventPanel.setVisible(false);
				lockEventPanel.setVisible(false);
				sleepButtonPanel.setVisible(false);
				createEventPanel.setVisible(false);
			}else if (e.getSource() == editEventButton){
				tutorialPanel.setVisible(false);
				deleteEventPanel.setVisible(false);
				editEventPanel.setVisible(true);
				lockEventPanel.setVisible(false);
				sleepButtonPanel.setVisible(false);
				createEventPanel.setVisible(false);
			}else if (e.getSource() == lockEventButton){
				tutorialPanel.setVisible(false);
				deleteEventPanel.setVisible(false);
				editEventPanel.setVisible(false);
				lockEventPanel.setVisible(true);
				sleepButtonPanel.setVisible(false);
				createEventPanel.setVisible(false);
			}else if (e.getSource() == sleepButtonInfoButton){
				tutorialPanel.setVisible(false);
				deleteEventPanel.setVisible(false);
				editEventPanel.setVisible(false);
				lockEventPanel.setVisible(false);
				sleepButtonPanel.setVisible(true);
				createEventPanel.setVisible(false);
			}else if (e.getSource() == tutorialDoneButton){	//counter to make sure they view tutorial
				if(tutorialCounter >= 5 || allTutorialButtonsPressed){
					tutorialScreen.dispose();
					window.setEnabled(true);
					window.toFront();
					if (optionsScreen != null)
						optionsScreen.toFront();
				}
			}
		}
	}
	
	//start of program
	public static void main(String[] args){
		GraphicSchedule x = new GraphicSchedule();
		x.initialize();
		if(args.length > 0 && args[0].equals("-d")){ //just in case you want to skip the tutorial
			skipTutorial = true;
		}
		x.run();
	}
}

/* PLANTUML CODE
 * 
 * @startuml
 * !define GraphicScheduleUML
 * 
 * class GraphicSchedule{
-skipTutorial : boolean {static}
-schedule : SchedulePlanner {static}
-window : JFrame
-about : JMenuItem
-options : JMenuItem
-reset : JMenuItem
-gridPanel : GridPanel
-mListen : GridMouseListener
-startScreen : JFrame
-sleepPreferences : JFrame
-EIGHT_HOURS : long {static}
-LIGHT_BLUE : Color {static}
-tutorialScreen : JFrame
-tutorialPanel : JPanel
-createEventButton : JButton
-createEventPanel : JPanel
-deleteEventButton : JButton
-deleteEventPanel : JPanel
-editEventButton : JButton
-editEventPanel : JPanel
-lockEventButton : JButton
-lockEventPanel : JPanel
-sleepButtonInfoButton : JButton
-sleepButtonPanel : JPanel
-tutorialDoneButton : JPanel
-allTutorialButtonsPressed : boolean
-tutorialCounter : int
-blockEditMenu : JFrame
-DEFAULT_BLOCK : GraphicBlock {static}
-CALENDAR_DAYS : int[] {static}
-statsScreen : JFrame
-graphScreen : JFrame
-graphPanel : JPanel
-graphButton : JButton
-aboutScreen : JFrame
-optionsScreen : JFrame
-sleepPreferenceButton : JButton
-tutorialButton : JButton
-resetButton : JButton
-sleepButton : JButton
-wakeButton : JButton
-statButton : JButton
-aboutButton : JButton
-optionsButton : JButton
-firstTime : boolean

+run() : void
+initialize() : void
+drawSchedule() : void
+getDay(int) : int
+drawBlock(GraphicBlock) : void
+removeBlock(GraphicBlock) : void
+doStartScreen() : void
+getSchedule() : SchedulePlanner {static}
+editSleepPreferences() : void
+showTutorial() : void
+drawBlockEditMenu(GraphicBlock) : void
+addBlock(Block) : boolean
+swapSleepButton() : void
+drawStats() : void
+drawGraph() : void
+showAboutScreen() : void
+drawOptions() : void
+resetSchedule() : void
 * }
 * 
 * @enduml
 * 
 */
