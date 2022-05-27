import java.util.ArrayList;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class GUIApp extends Application {
	Scheduler procSched = new Scheduler();

// GUI Resources
	String[] colors = {"#B5E4F5", "#CDEFB3", "#FBF0AF", "#FACF90", "#FDB1B1", "#F9BDE9", "#D8B3F7"};
	
	ImageView addIcon 			= new ImageView("file:Icons/Add.png");
	ImageView removeIcon 		= new ImageView("file:Icons/Remove.png");
	ImageView plusIcon 			= new ImageView("file:Icons/Add.png");
	ImageView calculateIcon 	= new ImageView("file:Icons/Calculate.png");
	ImageView turnAroundIcon	= new ImageView("file:Icons/Turnaround.png");
	ImageView waitIcon 			= new ImageView("file:Icons/Wait.png");
	ImageView errorIcon 		= new ImageView("file:Icons/Error.png");
	ImageView warningIcon 		= new ImageView("file:Icons/Warning.png");
	Image doneIcon 				= new Image("file:Icons/Done.png");
	Image interruptedIcon 		= new Image("file:Icons/Interrupted.png");
	Image idleIcon 				= new Image("file:Icons/Idle.png");

// GUI setup utilities
	private void setUpIcons(int size, ImageView... icons) {
		for (ImageView iv : icons) {
			iv.setFitHeight(size);
			iv.setPreserveRatio(true);
			iv.setSmooth(true);
			iv.setCache(true);
		}
	}
	//Prepare the icons (ImageViews) for later use.

	private String sizeStyle(int width, int height) {
		return String.format(
			"-fx-min-width: %d; -fx-max-width: %d; -fx-min-height: %d; -fx-max-height: %d; ", 
			width, width, height, height
		);
	}
	//String containing the style paramters for a strict size.

// Custom GUI Elements
	public class ProcessPane extends VBox {
		int order; // № of the process in the list of processes
		Label processLabel;
		TextField idField, arrivalField, cpuTimeField, priorityField;

		ProcessPane(int _order) {
			order = _order;
			processLabel = new Label("Process " + order);
			processLabel.setStyle(sizeStyle(130, 22) + "-fx-alignment: center; -fx-font-size: 14; -fx-background-radius: 5; -fx-background-color: " + colors[(order - 1) % 7]);

			Label idLabel = new Label("Process ID");
			idField = new TextField("P" + order);
			Label arrivalLabel = new Label("Time of Arrival");
			arrivalField = new TextField();
			Label cpuTimeLabel = new Label("Estimated CPU time");
			cpuTimeField = new TextField();
			Label priorityLabel = new Label("Priority");
			priorityField = new TextField();

			getChildren().addAll(
				processLabel,
				idLabel, idField,
				arrivalLabel, arrivalField,
				cpuTimeLabel, cpuTimeField,
				priorityLabel, priorityField
			);
			
			idLabel.setStyle("-fx-padding: 5 0 0 0");
			setStyle(sizeStyle(150, 220) + "-fx-spacing: 2; -fx-padding: 5 10 10 10");
		}
	}
	//A configuration panel for a process.

	public class SchedulePane extends BorderPane {
		SchedulePane(Scheduler.Work work, boolean first) {
			String idText;
			ImageView idIcon = new ImageView();
			String toolTipText;
			String color = "white";

			if (work.job == null) {
				idIcon.setImage(idleIcon);
				idText = (work.to - work.from < 3) ?  "" : "Nothing";
				toolTipText = "Processor is idle.";
			} else {
				Boolean done = work.job.finished == work.to;
				idText = work.job.id;
				toolTipText = work.job.id  + " selected for execution.\n";

				if (done) {
					idIcon.setImage(doneIcon);
					toolTipText += "Finished at " + work.to + "\nTurnaround time: " + work.job.turnAroundTime() + "\nWait time: " + work.job.waitTime();
				} else {
					idIcon.setImage(interruptedIcon);
					toolTipText += "Interrupted at " + work.to;
				}
				
				color = colors[(work.job.order - 1) % 7];
			}

			setUpIcons(20, idIcon);

			Label idLabel = new Label(idText);
			idLabel.setGraphic(idIcon);
			idLabel.setStyle(sizeStyle((work.to - work.from) * 50, 26) +  "-fx-alignment: center; -fx-font-size: 16; -fx-border-color: whitesmoke; -fx-background-color: " + color);
			
			Tooltip tp = new Tooltip(toolTipText);
			tp.setFont(new Font(13));
			idLabel.setTooltip(tp);

			setTop(idLabel);
			if(first)
				setLeft(new Label(Integer.toString(work.from)));
			setRight(new Label(Integer.toString(work.to)));
		}
	}
	//A panel which contains the start, end, and the job done in a timeframe of the schedule.

// GUI layout hierarchy
	VBox mainLayout = new VBox();
		ScrollPane processScrollPane = new ScrollPane();
			HBox processBox = new HBox();
				ArrayList<ProcessPane> processPanes = new ArrayList<>();
				VBox buttonsPane = new VBox();
					Button removeButton = new Button();
					Button addButton = new Button();
		BorderPane centerPane = new BorderPane();
			Label algorithmLabel = new Label();
			HBox algorithmBox = new HBox();
				ChoiceBox<String> mainChoice = new ChoiceBox<>();
				CheckBox preemptiveCheck = new CheckBox();
				Label plusLabel = new Label();
				ChoiceBox<String> secondChoice = new ChoiceBox<>();
				Label quantumLabel = new Label();
				ChoiceBox<String> thirdChoice = new ChoiceBox<>();
				TextField quantumField = new TextField();
			Button scheduleButton = new Button();
		ScrollPane scheduleScrollPane = new ScrollPane();
			HBox scheduleBox = new HBox();
				// schedulePanes
		HBox performancePane = new HBox();
			Label turnAroundTimeLabel = new Label();
			Label waitTimeLabel = new Label();
		Label errorLabel = new Label();

// Utilities
	ObservableList<String> algorithmsExcept(String... elements) {
		ObservableList<String> algorithms = FXCollections.observableArrayList("None", "FCFS", "SJF", "Priority");

		for (String elem : elements)
			if (algorithms.contains(elem))
				algorithms.remove(elem);

		return algorithms;
	}
	//List of possible algorithms, excluding what is passed in the parameters. For showing possible secondary criteria.

	private byte algorithmNumber(ChoiceBox<String> algorithmChoice) {
		switch (algorithmChoice.getValue()) {
			case "FCFS":
				return Scheduler.FCFS;
			case "SJF":
				return Scheduler.SJF;
			case "Priority":
				return Scheduler.PRIORITY;
			default:
				return Scheduler.NONE;
		}
	}
	//The integer value corresponding to the chosen algorithm.

// GUI management
	private void addElement(Pane parentElement, Node childElement) {
		if (!parentElement.getChildren().contains(childElement))
			parentElement.getChildren().add(childElement);
	}
	//Add the child GUI element, if parent doesn't have it.

	private void removeElement(Pane parentElement, Object childElement) {
		if (parentElement.getChildren().contains(childElement))
			parentElement.getChildren().remove(childElement);
	}
	//Remove the child GUI element, if parent has it.

	private void updateprocessPanes() {
		processBox.getChildren().clear();
		for (ProcessPane pc : processPanes)
			processBox.getChildren().addAll(pc, new Separator(Orientation.VERTICAL));

		buttonsPane.getChildren().clear();
		if (processPanes.size() > 1)
			addElement(buttonsPane, removeButton);
		addElement(buttonsPane, addButton);

		addElement(processBox, buttonsPane);

		processScrollPane.applyCss();
		processScrollPane.layout();
		processScrollPane.setHvalue(1);
	}
	//Update the processes panel. For when a process is added or removed from the list.

// GUI actions
	private void removeButtonAction() {
		processPanes.remove(processPanes.size() - 1);
		updateprocessPanes();
	}
	//Remove a process from the list and update the processes panel.

	private void addButtonAction() {
		processPanes.add(new ProcessPane(processPanes.size() + 1));
		updateprocessPanes();
	}
	//Add a process to the list and update the processes panel.

	private void mainChoiceAction() {
		algorithmBox.getChildren().clear();
		addElement(algorithmBox, mainChoice);
		preemptiveCheck.setSelected(false);

		if (mainChoice.getValue() == "Round Robin") {
			addElement(algorithmBox, quantumLabel);
			addElement(algorithmBox, quantumField);
			quantumField.setText("0");
		} else {
			if (mainChoice.getValue() != "FCFS")
				algorithmBox.getChildren().add(preemptiveCheck);

			addElement(algorithmBox, plusLabel);
			addElement(algorithmBox, secondChoice);
			secondChoice.getItems().setAll(algorithmsExcept(mainChoice.getValue()));
		}
		secondChoice.setValue("None");
	}
	//Update the available configuration options, depending on the choice of algorithm.

	private void secondChoiceAction() {
		if (secondChoice.getValue() == "None")
			removeElement(algorithmBox, thirdChoice);
		else {
			addElement(algorithmBox, thirdChoice);
			thirdChoice.setItems(algorithmsExcept(mainChoice.getValue(), secondChoice.getValue()));
		}
		
		thirdChoice.setValue("None");
	}
	//Show/hide the available options for a third selection criterion.

	private void scheduleButtonAction() {
		if (!verifyInput())
			return;

		// Reading queue and passing it to the process scheduler
		ArrayList<Scheduler.Job> processQueue = new ArrayList<>();
		for (ProcessPane pc : processPanes) {
			String _id = pc.idField.getText();
			int _timeOfArrival = Integer.parseInt(pc.arrivalField.getText());
			int _estimatedCPUTime = Integer.parseInt(pc.cpuTimeField.getText());
			int _priority = Integer.parseInt(pc.priorityField.getText());

			processQueue.add(procSched.new Job(_id, _timeOfArrival, _estimatedCPUTime, _priority, pc.order));
		}
		procSched.configureQueue(processQueue);

		// Reading algorithm and passing it to the process scheduler
		if (mainChoice.getValue() == "Round Robin")
			procSched.configureAlgorithmRR(Integer.parseInt(quantumField.getText()));
		else {
			Boolean _preemptive = preemptiveCheck.isSelected();
			byte[] _criteria = { algorithmNumber(mainChoice), algorithmNumber(secondChoice), algorithmNumber(thirdChoice) };
			procSched.configureAlgorithm(_preemptive, _criteria);
		}

		// Tell the process scheduler to caluclate the schedule and get it
		ArrayList<Scheduler.Work> works;
		works = procSched.schedule();

		// Output the schedule and performance criteria
		scheduleBox.getChildren().clear();

		boolean first = true;
		for (Scheduler.Work work : works) {
			scheduleBox.getChildren().add(new SchedulePane(work, first));
			first = false;
		}
		
		turnAroundTimeLabel.setText(" Average turnaround time: " + String.format("%.2f", procSched.averageTurnAroundTime()));
		waitTimeLabel.setText(" Average wait time: " + String.format("%.2f", procSched.averageWaitTime()));
	}
	//If the input (processes and algorithm) is valid, calculate the process schedule by using Scheduler class, and output it.

// Input verification
	private boolean isInt(String s) {
		try {
			Integer.parseInt(s);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}
	//Whether the input string can be parsed as an integer

	private boolean outputError(boolean value, String message) {
		if (value) {
			errorLabel.setGraphic(warningIcon);
			errorLabel.setText("Warning: " + message);
			errorLabel.setTextFill(Color.web("#DB7905"));
		} else {
			errorLabel.setGraphic(errorIcon);
			errorLabel.setText("ERROR: " + message);
			errorLabel.setTextFill(Color.RED);
		}
		
		return value;
	}
	//Signal input warning or error to the user, and return the value passed.

	private boolean verifyInput() {
		errorLabel.setText("");
		errorLabel.setGraphic(null);

		int lastArrivalTime = 0;
		int newArrivalTime;
		boolean first = true;

		for (ProcessPane pc : processPanes) {
			String errorReference = " (" + pc.processLabel.getText() + ")";

			// Verify process ID
			if (pc.idField.getText().isEmpty())
				return outputError(false, "Process ID must be filled." + errorReference);

			// Verify arrival time
			if (!isInt(pc.arrivalField.getText()))
				return outputError(false, "Time of arrival must be an integer." + errorReference);

			newArrivalTime = Integer.parseInt(pc.arrivalField.getText());
			if (!first && newArrivalTime < lastArrivalTime)
				return outputError(false, "Time of arrival must be later than previous process." + errorReference);
			if (newArrivalTime < 0)
				return outputError(false, "Time of arrival must be greater than 0." + errorReference);

			first = false;
			lastArrivalTime = newArrivalTime;

			// Verify CPU time
			if (!isInt(pc.cpuTimeField.getText()))
				return outputError(false, "Estimated CPU time must be an integer." + errorReference);

			int cpuTime = Integer.parseInt(pc.cpuTimeField.getText());
			if (cpuTime < 1)
				return outputError(false, "Estimated CPU time must be greater than 0." + errorReference);

			// Verify priority
			if (!isInt(pc.priorityField.getText()))
				return outputError(false, "Priority must be an integer." + errorReference);

			int priority = Integer.parseInt(pc.priorityField.getText());
			if (priority < 0) 
				outputError(true, "Negative priority." + errorReference);
		}

		// Verify quantum
		if (mainChoice.getValue() == "Round Robin") {
			if (!isInt(quantumField.getText()))
				return outputError(false, "Round robin quantum must be an integer.");

			int rrQuantum = Integer.parseInt(quantumField.getText());
			if (rrQuantum < 1)
				return outputError(false, "Round robin quantum must be greater than 0.");
		}

		return true;
	}
	//Verify whether the input values are valid, and signal a warning or error to the user, if anything is wrong.

	// Main
	public static void main(String[] args) {
		launch(args);
	}
	//Standard main method of a javafx program.

	@Override
	public void start(Stage primaryStage) throws Exception {
		// Setting up icons
		setUpIcons(16, errorIcon, warningIcon, calculateIcon, turnAroundIcon, waitIcon);
		setUpIcons(30, removeIcon, addIcon);
		setUpIcons(20, plusIcon);

		// Setting up GUI elements
		removeButton.setGraphic(removeIcon);
		removeButton.setStyle(sizeStyle(40, 40) + "-fx-background-radius: 30");
		removeButton.setTooltip(new Tooltip("Remove process"));
		removeButton.setOnAction(e -> removeButtonAction());
		
		addButton.setGraphic(addIcon);
		addButton.setStyle(sizeStyle(40, 40) + "-fx-background-radius: 30");
		addButton.setTooltip(new Tooltip("Add process"));
		addButton.setOnAction(e -> addButtonAction());

		buttonsPane.getChildren().addAll(removeButton, addButton);
		buttonsPane.setStyle(sizeStyle(150, 220) + "-fx-spacing: 10; -fx-alignment: center");

		processBox.setStyle("-fx-spacing: 10; -fx-padding: 10; -fx-min-height: 240");

		processScrollPane.setContent(processBox);
		processScrollPane.setStyle("-fx-pannable: true; -fx-min-height: 260");

		algorithmLabel.setText("Algorithm configuration:  ");
		algorithmLabel.setStyle(sizeStyle(180, 24) + "-fx-font-size: 14; -fx-alignment: center");

		mainChoice.getItems().addAll("FCFS", "SJF", "Priority", "Round Robin");
		mainChoice.setValue("FCFS");
		mainChoice.setTooltip(new Tooltip("Scheduling Algorithm"));
		mainChoice.setOnAction(e -> mainChoiceAction());

		quantumLabel.setText("Quantum:");
		quantumLabel.setStyle("-fx-min-height:24; -fx-font-size: 12; -fx-alignment: center-right");

		quantumField.setPrefWidth(40);

		preemptiveCheck.setText("Preemptive");
		preemptiveCheck.setStyle("-fx-min-height: 25; -fx-alignment: center");

		plusLabel.setGraphic(plusIcon);
		plusLabel.setMaxSize(24, 24);

		secondChoice.setTooltip(new Tooltip("Secondary selection criterion"));
		secondChoice.setOnAction(e -> secondChoiceAction());

		thirdChoice.setTooltip(new Tooltip("Tertiary selection criterion"));

		algorithmBox.setStyle("-fx-spacing: 10");

		scheduleButton.setGraphic(calculateIcon);
		scheduleButton.setText("Calculate Schedule");
		scheduleButton.setTooltip(new Tooltip("Visualize configured algorithm"));
		scheduleButton.setOnAction(e -> scheduleButtonAction());

		centerPane.setLeft(algorithmLabel);
		centerPane.setCenter(algorithmBox);
		centerPane.setRight(scheduleButton);
		centerPane.setStyle("-fx-padding: 10");

		scheduleBox.setStyle("-fx-spacing: 0; -fx-padding: 20");

		scheduleScrollPane.setContent(scheduleBox);
		scheduleScrollPane.setStyle("-fx-min-height: 85; -fx-max-height: 85; -fx-vbar-policy: never; -fx-pannable: true");
		scheduleScrollPane.setVmax(0);
		
		turnAroundTimeLabel.setGraphic(turnAroundIcon);
		turnAroundTimeLabel.setText(" Average turnaround time");
		turnAroundTimeLabel.setStyle("-fx-font-size: 14");

		waitTimeLabel.setGraphic(waitIcon);
		waitTimeLabel.setText(" Average wait time");
		waitTimeLabel.setStyle("-fx-font-size: 14");

		performancePane.getChildren().addAll(turnAroundTimeLabel, waitTimeLabel);
		performancePane.setStyle("-fx-spacing: 100; -fx-padding: 10; -fx-alignment: center");

		errorLabel.setStyle("-fx-padding: 5 10 5 10");

		addButtonAction(); // Add the first process
		mainChoiceAction(); // Set up the default algorithm options
		secondChoiceAction(); // Set up the default algorithm options

		mainLayout.getChildren().addAll( processScrollPane, centerPane, scheduleScrollPane, performancePane, new Separator(Orientation.HORIZONTAL), errorLabel);
		//mainLayout.setStyle("-fx-background-color: white");

		// Configuring application window
		Scene mainScene = new Scene(mainLayout);
		primaryStage.setScene(mainScene);
		primaryStage.setMinHeight(500);
		primaryStage.setMaxHeight(500);
		primaryStage.setMinWidth(900);
		primaryStage.setTitle("Scheduling Algorithm Visualizer");
		primaryStage.getIcons().add(new Image("file:Icons/icon.png"));
		primaryStage.show();
	}
	//Set up the GUI elements, nest them according to the hierarchy, and configure the application window. 
}
